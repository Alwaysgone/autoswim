package io.autoswim.swim.network.scalecube;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;
import io.scalecube.net.Address;

class ScaleCubeSwimNetworkTest {

	@Timeout(5)
	@Test
	void testSendAndReceiveMessage() {
		ScaleCubeSwimNetwork network1 = new ScaleCubeSwimNetwork(ScaleCubeSwimNetworkConfig.builder()
				.withSwimPort(9033)
				.build());
		ScaleCubeSwimNetwork network2 = new ScaleCubeSwimNetwork(ScaleCubeSwimNetworkConfig.builder()
				.withSwimPort(9034)
				.withSeedNodes(List.of(Address.from("192.168.0.143:9033")))
				.build());
		network1.start();
		network2.start();
		StartupMessage swimMessage = StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withId("id-1")
				.withSender(Endpoint.of("192.168.0.143:9033"))
				.build();
		network1.sendMessage(swimMessage);
		SwimMessage receivedMessage = network2.receiveMessage();
		assertEquals(swimMessage, receivedMessage);
	}

}
