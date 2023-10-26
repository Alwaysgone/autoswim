package io.autoswim.swim.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

class ScaleCubeSwimNetworkTest {

	@Test
	void testSendAndReceiveMessage() {
		Endpoint endpoint1 = Endpoint.of("192.168.0.143:9033");
		ScaleCubeSwimNetwork network1 = new ScaleCubeSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(9033)
				.build(), new OwnEndpointProvider(endpoint1));
		ScaleCubeSwimNetwork network2 = new ScaleCubeSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(9034)
				.withSeedNodes(List.of(endpoint1))
				.build(), new OwnEndpointProvider(Endpoint.of("192.168.0.143:9034")));
		network1.start();
		network2.start();
		StartupMessage swimMessage = StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withId("id-1")
				.withSender(endpoint1)
				.build();
		network1.sendMessage(swimMessage, null);
		SwimMessage receivedMessage = network2.receiveMessage();
		assertEquals(swimMessage, receivedMessage);
	}
}
