package io.autoswim.swim.scalecube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.autoswim.OwnEndpointProvider;
import io.autoswim.UuidMessageIdGenerator;
import io.autoswim.swim.SwimTestUtil;
import io.autoswim.swim.messages.FullSyncMessage;
import io.autoswim.swim.messages.HeartbeatMessage;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.messages.SwimMessageHandler;
import io.autoswim.swim.network.scalecube.ScaleCubeSwimNetwork;
import io.autoswim.swim.network.scalecube.ScaleCubeSwimNetworkConfig;
import io.autoswim.types.Endpoint;
import io.scalecube.net.Address;

class ScaleCubeSwimRuntimeTest {

	@Timeout(5)
	@Test
	void testSendMessageOverSwimRuntime() {
		int swimPort1 = SwimTestUtil.getFreePort();
		int swimPort2 = SwimTestUtil.getFreePort();
		UuidMessageIdGenerator idGenerator = new UuidMessageIdGenerator();

		String address1 = "localhost:" + swimPort1;
		ScaleCubeSwimNetwork network1 = new ScaleCubeSwimNetwork(ScaleCubeSwimNetworkConfig.builder()
				.withMemberAlias(address1)
				.withSwimPort(swimPort1)
				.build());
		String address2 = "localhost:" + swimPort2;
		ScaleCubeSwimNetwork network2 = new ScaleCubeSwimNetwork(ScaleCubeSwimNetworkConfig.builder()
				.withMemberAlias(address2)
				.withSwimPort(swimPort2)
				.withSeedNodes(List.of(Address.from(address1)))
				.build());

		ScaleCubeSwimRuntime swimRuntime1 = new ScaleCubeSwimRuntime(network1, new OwnEndpointProvider(Endpoint.of(address1)), idGenerator);
		ScaleCubeSwimRuntime swimRuntime2 = new ScaleCubeSwimRuntime(network2, new OwnEndpointProvider(Endpoint.of(address2)), idGenerator);
		SwimMessageHandlerStub swimMessageHandler = new SwimMessageHandlerStub();
		swimRuntime2.registerMessageHandler(swimMessageHandler);
		swimRuntime1.start();
		swimRuntime2.start();
		
		Awaitility.await().atMost(Duration.ofSeconds(2L)).until(() -> !swimMessageHandler.getHandledMessages().isEmpty());
		List<SwimMessage> handledMessages = swimMessageHandler.getHandledMessages();
		assertEquals(1, handledMessages.size());
		SwimMessage swimMessage = handledMessages.get(0);
		assertNotNull(swimMessage.getId());
		assertNotNull(swimMessage.getCreatedAt());
		assertTrue(swimMessage instanceof StartupMessage);
		swimRuntime1.stop();
		swimRuntime2.stop();
	}
	
	private static class SwimMessageHandlerStub implements SwimMessageHandler {

		private List<SwimMessage> handledMessages = new ArrayList<>();
		
		public List<SwimMessage> getHandledMessages() {
			return handledMessages;
		}
		
		@Override
		public void handle(StartupMessage startupMessage) {
			handledMessages.add(startupMessage);
		}

		@Override
		public void handle(FullSyncMessage fullSyncMessage) {
			handledMessages.add(fullSyncMessage);
		}

		@Override
		public void handle(HeartbeatMessage heartbeatMessage) {
			handledMessages.add(heartbeatMessage);
		}
	}
}
