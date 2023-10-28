package io.autoswim.swim.scalecube.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.autoswim.messages.StartupMessage;
import io.autoswim.messages.AutoswimMessage;
import io.autoswim.swim.network.AutoswimNetworkConfig;
import io.autoswim.test.SwimTestUtil;
import io.autoswim.types.Endpoint;

class ScaleCubeSwimNetworkTest {

	@Timeout(5)
	@Test
	void testSendAndReceiveMessage() {
		Endpoint endpoint1 = Endpoint.of("localhost:" + SwimTestUtil.getFreePort());
		Endpoint endpoint2 = Endpoint.of("localhost:" + SwimTestUtil.getFreePort());
		ScaleCubeAutoswimNetwork network1 = new ScaleCubeAutoswimNetwork(AutoswimNetworkConfig.builder()
				.withMemberAlias(endpoint1.toHostAndPortString())
				.withSwimPort(endpoint1.getPort())
				.build());
		ScaleCubeAutoswimNetwork network2 = new ScaleCubeAutoswimNetwork(AutoswimNetworkConfig.builder()
				.withMemberAlias(endpoint2.toHostAndPortString())
				.withSwimPort(endpoint2.getPort())
				.withSeedNodes(List.of(endpoint1))
				.build());
		network1.start();
		network2.start();
		Set<Endpoint> members1 = network1.getMembers();
		assertThat(members1, hasItems(Matchers.<Endpoint>hasProperty("port", is(endpoint1.getPort())),
				Matchers.<Endpoint>hasProperty("port", is(endpoint2.getPort()))));
		Set<Endpoint> members2 = network1.getMembers();
		assertThat(members2, hasItems(Matchers.<Endpoint>hasProperty("port", is(endpoint1.getPort())),
				Matchers.<Endpoint>hasProperty("port", is(endpoint2.getPort()))));
		StartupMessage swimMessage = StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withId("id-1")
				.withSender(endpoint1)
				.build();
		network1.sendMessage(swimMessage);
		AutoswimMessage receivedMessage = network2.receiveMessage();
		assertEquals(swimMessage, receivedMessage);
	}

}
