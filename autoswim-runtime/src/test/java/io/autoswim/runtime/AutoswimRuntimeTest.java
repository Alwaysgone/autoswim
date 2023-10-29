package io.autoswim.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.automerge.Document;
import org.automerge.ObjectId;
import org.automerge.Transaction;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.UuidMessageIdGenerator;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.messages.FullSyncMessage;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.messages.StartupMessage;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.swim.network.AutoswimNetworkConfig;
import io.autoswim.swim.scalecube.network.ScaleCubeAutoswimNetwork;
import io.autoswim.test.SwimTestUtil;
import io.autoswim.types.Endpoint;

class AutoswimRuntimeTest {

	@TempDir
	File autoswimTempDir;
	
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
		
		Path autoswimPath1 = autoswimTempDir.toPath().resolve("autoswim1");
		autoswimPath1.toFile().mkdirs();
		OwnEndpointProvider ownEndpointProvider1 = new OwnEndpointProvider(endpoint1);
		AutoswimStateHandler stateHandler1 = new AutoswimStateHandler(autoswimPath1, new TestAutoswimStateInitializer(ownEndpointProvider1));
		AutoswimRuntime swimRuntime1 = new AutoswimRuntime(AutoswimConfig.builder().build(),
				network1,
				stateHandler1,
				ownEndpointProvider1,
				messageIdGenerator);
		TestAutoswimMessageHandler testAutoswimMessageHandler1 = new TestAutoswimMessageHandler();
		swimRuntime1.registerMessageHandler(testAutoswimMessageHandler1);
		
		Path autoswimPath2 = autoswimTempDir.toPath().resolve("autoswim2");
		autoswimPath2.toFile().mkdirs();
		OwnEndpointProvider ownEndpointProvider2 = new OwnEndpointProvider(endpoint2);
		AutoswimStateHandler stateHandler2 = new AutoswimStateHandler(autoswimPath2, new TestAutoswimStateInitializer(ownEndpointProvider2));
		AutoswimRuntime swimRuntime2 = new AutoswimRuntime(AutoswimConfig.builder().build(),
				network2,
				stateHandler2,
				ownEndpointProvider2,
				messageIdGenerator);
		TestAutoswimMessageHandler testAutoswimMessageHandler2 = new TestAutoswimMessageHandler();
		swimRuntime2.registerMessageHandler(testAutoswimMessageHandler2);
		
		swimRuntime1.start();
		swimRuntime2.start();
		
		Awaitility.await().atMost(Duration.ofSeconds(5L)).until(() -> testAutoswimMessageHandler1.fullSyncMessages.size() > 0);
		Awaitility.await().atMost(Duration.ofSeconds(5L)).until(() -> testAutoswimMessageHandler2.fullSyncMessages.size() > 0);
		
		assertThat(testAutoswimMessageHandler1.startupMessages.size(), is(1));
		// since both nodes send a full sync on startup there are 2 expected messages
		assertThat(testAutoswimMessageHandler1.fullSyncMessages.size(), is(2));
		FullSyncMessage fullSyncMessage1 = testAutoswimMessageHandler1.fullSyncMessages.get(0);
		assertThat(fullSyncMessage1.getSender(), is(endpoint2));
		assertThat(testAutoswimMessageHandler2.startupMessages.size(), is(1));
		assertThat(testAutoswimMessageHandler2.fullSyncMessages.size(), is(2));
		FullSyncMessage fullSyncMessage2 = testAutoswimMessageHandler2.fullSyncMessages.get(0);
		assertThat(fullSyncMessage2.getSender(), is(endpoint1));
	}
	
	private static class TestAutoswimStateInitializer implements AutoswimStateInitializer {
		
		private final OwnEndpointProvider ownEndpointProvider;

		public TestAutoswimStateInitializer(OwnEndpointProvider ownEndpointProvider) {
			this.ownEndpointProvider = ownEndpointProvider;
		}
		
		@Override
		public Document getInitialState() {
			Endpoint ownEndpoint = ownEndpointProvider.getOwnEndpoint();
			String actorId = ownEndpoint.getHostname() + ownEndpoint.getPort();
			Document document = new Document(actorId.getBytes(StandardCharsets.UTF_8));
			Transaction transaction = document.startTransaction();
			transaction.set(ObjectId.ROOT, "name", "dio");
			transaction.commit();
			return document;
		}
	}
	
	private static class TestAutoswimMessageHandler implements AutoswimMessageHandler {

		private List<StartupMessage> startupMessages = new ArrayList<>();
		private List<FullSyncMessage> fullSyncMessages = new ArrayList<>();
		
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
			// not needed
		}
	}
}
