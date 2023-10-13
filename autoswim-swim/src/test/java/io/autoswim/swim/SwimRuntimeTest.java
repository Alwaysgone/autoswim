package io.autoswim.swim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.autoswim.OwnEndpointProvider;
import io.autoswim.UuidMessageIdGenerator;
import io.autoswim.swim.messages.FullSyncMessage;
import io.autoswim.swim.messages.HeartbeatMessage;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.messages.SwimMessageHandler;
import io.autoswim.swim.network.SwimNetworkConfig;
import io.autoswim.swim.network.UdpSwimNetwork;
import io.autoswim.swim.serialization.ObjectMapperProvider;
import io.autoswim.types.Endpoint;

class SwimRuntimeTest {
	
	@Timeout(5)
	@Test
	void testSendMessageOverSwimRuntime() {
		int swimPort1 = SwimTestUtil.getFreePort();
		int swimPort2 = SwimTestUtil.getFreePort();
		UuidMessageIdGenerator idGenerator = new UuidMessageIdGenerator();
		UdpSwimNetwork udpSwimNetwork1 = new UdpSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(swimPort1)
				.build(), ObjectMapperProvider.OBJECT_MAPPER);
		UdpSwimNetwork udpSwimNetwork2 = new UdpSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(swimPort2)
				.build(), ObjectMapperProvider.OBJECT_MAPPER);

		Endpoint network1 = Endpoint.builder()
				.withHostname("localhost")
				.withPort(swimPort1)
				.build();
		Endpoint network2 = Endpoint.builder()
				.withHostname("localhost")
				.withPort(swimPort2)
				.build();
		SwimRuntime swimRuntime1 = new SwimRuntime(Set.of(network2),
				new OwnEndpointProvider(network1),
				idGenerator,
				udpSwimNetwork1);
		SwimRuntime swimRuntime2 = new SwimRuntime(Set.of(network1),
				new OwnEndpointProvider(network2),
				idGenerator,
				udpSwimNetwork2);
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
