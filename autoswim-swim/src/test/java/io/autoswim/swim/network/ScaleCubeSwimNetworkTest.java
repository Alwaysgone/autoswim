package io.autoswim.swim.network;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

class ScaleCubeSwimNetworkTest {

	@Test
	void testSendMessage() {
		ScaleCubeSwimNetwork network1 = new ScaleCubeSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(9033)
				.withSeedNodes(List.of(Endpoint.of("192.168.0.143:9034")))
				.build(), new OwnEndpointProvider(Endpoint.of("192.168.0.143:9033")));
		ScaleCubeSwimNetwork network2 = new ScaleCubeSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(9034)
				.withSeedNodes(List.of(Endpoint.of("192.168.0.143:9033")))
				.build(), new OwnEndpointProvider(Endpoint.of("192.168.0.143:9034")));
		network1.start();
		network2.start();
		network1.sendMessage(StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withId("id-1")
				.build(), null);
		SwimMessage swimMessage = network2.receiveMessage();
		System.out.println(swimMessage);
	}
}
