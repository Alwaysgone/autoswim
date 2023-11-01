package io.autoswim.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.automerge.Document;
import org.automerge.ObjectId;
import org.automerge.Transaction;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.UuidMessageIdGenerator;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.messages.FullSyncMessage;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.messages.RequestSyncMessage;
import io.autoswim.messages.StartupMessage;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.swim.network.AutoswimNetworkConfig;
import io.autoswim.swim.scalecube.network.ScaleCubeAutoswimNetwork;
import io.autoswim.test.SwimTestUtil;
import io.autoswim.types.Endpoint;

class AutoswimRuntimeTest {

	@TempDir
	File autoswimTempDir;
	
	@Disabled
	@Test
	void createInitialState() throws FileNotFoundException, IOException {
		Document initialDoc = new Document("autoswim".getBytes(StandardCharsets.UTF_8));
		try(FileOutputStream outStream = new FileOutputStream("C:\\Develop\\autoswim\\autoswim\\autoswim-runtime\\src\\main\\resources\\initial-state.dat")) {
			outStream.write(initialDoc.save());
		}
	}
	
	@Timeout(10)
	@Test
	void testStartupMessage() {
		Endpoint endpoint1 = Endpoint.of("localhost:" + SwimTestUtil.getFreePort());
		Endpoint endpoint2 = Endpoint.of("localhost:" + SwimTestUtil.getFreePort());
		AutoswimNetwork network1 = new ScaleCubeAutoswimNetwork(AutoswimNetworkConfig.builder()
				.withMemberAlias(endpoint1.toHostAndPortString())
				.withSwimPort(endpoint1.getPort())
				.build());
		AutoswimNetwork network2 = new ScaleCubeAutoswimNetwork(AutoswimNetworkConfig.builder()
				.withMemberAlias(endpoint2.toHostAndPortString())
				.withSwimPort(endpoint2.getPort())
				.withSeedNodes(List.of(endpoint1))
				.build());
		MessageIdGenerator messageIdGenerator = new UuidMessageIdGenerator();
		
		EmptyAutoswimStateInitializer stateInitializer1 = new EmptyAutoswimStateInitializer();
		
		Path autoswimPath1 = autoswimTempDir.toPath().resolve("autoswim1");
		autoswimPath1.toFile().mkdirs();
		OwnEndpointProvider ownEndpointProvider1 = new OwnEndpointProvider(endpoint1);
		AutoswimStateHandler stateHandler1 = new AutoswimStateHandler(autoswimPath1, stateInitializer1);
		ExecutorAutoswimRequestSyncScheduler requestSyncScheduler1 = new ExecutorAutoswimRequestSyncScheduler(network1, stateHandler1, ownEndpointProvider1, messageIdGenerator);
		AutoswimRuntime swimRuntime1 = new AutoswimRuntime(AutoswimConfig.builder().build(),
				network1,
				stateHandler1,
				ownEndpointProvider1,
				messageIdGenerator,
				requestSyncScheduler1);
		TestAutoswimMessageHandler testAutoswimMessageHandler1 = new TestAutoswimMessageHandler();
		swimRuntime1.registerMessageHandler(testAutoswimMessageHandler1);
		
		Path autoswimPath2 = autoswimTempDir.toPath().resolve("autoswim2");
		autoswimPath2.toFile().mkdirs();
		OwnEndpointProvider ownEndpointProvider2 = new OwnEndpointProvider(endpoint2);
		TestAutoswimStateInitializer stateInitializer2 = new TestAutoswimStateInitializer(stateInitializer1.getInitialState());
		AutoswimStateHandler stateHandler2 = new AutoswimStateHandler(autoswimPath2, stateInitializer2);
		ExecutorAutoswimRequestSyncScheduler requestSyncScheduler2 = new ExecutorAutoswimRequestSyncScheduler(network2, stateHandler2, ownEndpointProvider2, messageIdGenerator);
		AutoswimRuntime swimRuntime2 = new AutoswimRuntime(AutoswimConfig.builder().build(),
				network2,
				stateHandler2,
				ownEndpointProvider2,
				messageIdGenerator,
				requestSyncScheduler2);
		TestAutoswimMessageHandler testAutoswimMessageHandler2 = new TestAutoswimMessageHandler();
		swimRuntime2.registerMessageHandler(testAutoswimMessageHandler2);
		
		swimRuntime1.start();
		swimRuntime2.start();
		
		Awaitility.await().atMost(Duration.ofSeconds(5L)).until(() -> testAutoswimMessageHandler1.incrementalSyncMessages.size() > 0);
		Awaitility.await().atMost(Duration.ofSeconds(5L)).until(() -> testAutoswimMessageHandler1.requestSyncMessages.size() > 0);
		Awaitility.await().atMost(Duration.ofSeconds(5L)).until(() -> testAutoswimMessageHandler2.requestSyncMessages.size() > 0);
		
		assertThat(testAutoswimMessageHandler1.startupMessages.size(), is(1));
		assertThat(testAutoswimMessageHandler1.requestSyncMessages.size(), is(2));
		assertThat(testAutoswimMessageHandler1.incrementalSyncMessages.size(), is(2));
		IncrementalSyncMessage incrementalSyncMessage1 = testAutoswimMessageHandler1.incrementalSyncMessages.get(0);
		assertThat(incrementalSyncMessage1.getSender(), is(endpoint2));
		byte[] expectedChanges = stateInitializer2.getInitialState().encodeChangesSince(stateInitializer1.getInitialState().getHeads());
		assertThat(incrementalSyncMessage1.getChangeSet(), is(expectedChanges));
		
		assertThat(testAutoswimMessageHandler2.startupMessages.size(), is(1));
		assertThat(testAutoswimMessageHandler2.requestSyncMessages.size(), is(2));
		assertThat(testAutoswimMessageHandler2.incrementalSyncMessages.size(), is(0));
	}
	
	private static class TestAutoswimStateInitializer implements AutoswimStateInitializer {
		
		private final Document initialState;

		public TestAutoswimStateInitializer(Document initialState) {
			this.initialState = initialState;
			Transaction transaction = initialState.startTransaction();
			transaction.set(ObjectId.ROOT, "name", "dio");
			transaction.commit();
		}
		
		@Override
		public Document getInitialState() {			
			return initialState;
		}
	}
	
//	private static class TestAutoswimStateInitializer implements AutoswimStateInitializer {
//		
//		private final OwnEndpointProvider ownEndpointProvider;
//
//		public TestAutoswimStateInitializer(OwnEndpointProvider ownEndpointProvider) {
//			this.ownEndpointProvider = ownEndpointProvider;
//		}
//		
//		@Override
//		public Document getInitialState() {
//			Endpoint ownEndpoint = ownEndpointProvider.getOwnEndpoint();
//			String actorId = ownEndpoint.getHostname() + ownEndpoint.getPort();
//			Document document = new Document(actorId.getBytes(StandardCharsets.UTF_8));
//			Transaction transaction = document.startTransaction();
//			transaction.set(ObjectId.ROOT, "name", "dio");
//			transaction.commit();
//			return document;
//		}
//	}
	
	private static class TestAutoswimMessageHandler implements AutoswimMessageHandler {

		private List<StartupMessage> startupMessages = new ArrayList<>();
		private List<FullSyncMessage> fullSyncMessages = new ArrayList<>();
		private List<IncrementalSyncMessage> incrementalSyncMessages = new ArrayList<>();
		private List<RequestSyncMessage> requestSyncMessages = new ArrayList<>();
		
		@Override
		public void handle(StartupMessage startupMessage) {
			startupMessages.add(startupMessage);	
		}

		@Override
		public void handle(FullSyncMessage fullSyncMessage) {
			fullSyncMessages.add(fullSyncMessage);
		}

		@Override
		public void handle(IncrementalSyncMessage incrementalSyncMessage) {
			incrementalSyncMessages.add(incrementalSyncMessage);
		}

		@Override
		public void handle(RequestSyncMessage requestSyncMessage) {
			requestSyncMessages.add(requestSyncMessage);
		}
	}
}
