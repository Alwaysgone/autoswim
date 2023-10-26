package io.autoswim.swim.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.autoswim.swim.SwimTestUtil;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.serialization.ObjectMapperProvider;
import io.autoswim.types.Endpoint;

class FragmentedUdpSwimNetworkTest {

	@Timeout(5)
	@Test
	void testSingleMessageSending() {
		int swimPort1 = SwimTestUtil.getFreePort();
		int swimPort2 = SwimTestUtil.getFreePort();
		FragmentedUdpSwimNetwork udpSwimNetwork1 = new FragmentedUdpSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(swimPort1)
				.build(), ObjectMapperProvider.OBJECT_MAPPER);
		FragmentedUdpSwimNetwork udpSwimNetwork2 = new FragmentedUdpSwimNetwork(SwimNetworkConfig.builder()
				.withSwimPort(swimPort2)
				.build(), ObjectMapperProvider.OBJECT_MAPPER);
		udpSwimNetwork1.start();
		udpSwimNetwork2.start();
		Endpoint network1 = Endpoint.builder()
				.withHostname("localhost")
				.withPort(swimPort1)
				.build();
		Endpoint network2 = Endpoint.builder()
				.withHostname("localhost")
				.withPort(swimPort2)
				.build();
		Instant now = Instant.now();
		udpSwimNetwork1.sendMessage(StartupMessage.builder()
				.withCreatedAt(now)
				.withId("id1")
				.withSender(network1)
				.build(), Set.of(network2));
		SwimMessage swimMessage = udpSwimNetwork2.receiveMessage();
		assertNotNull(swimMessage);
		assertEquals("id1", swimMessage.getId());
		assertEquals(now, swimMessage.getCreatedAt());
		assertTrue(swimMessage instanceof StartupMessage);
		udpSwimNetwork2.stop();
		udpSwimNetwork1.stop();
	}

}
