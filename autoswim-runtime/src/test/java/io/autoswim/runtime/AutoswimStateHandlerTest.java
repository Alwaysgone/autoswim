package io.autoswim.runtime;

import java.nio.charset.StandardCharsets;

import org.automerge.Document;
import org.junit.jupiter.api.Test;

import io.autoswim.OwnEndpointProvider;
import io.autoswim.types.Endpoint;

class AutoswimStateHandlerTest {

	@Test
	void test() {
//		fail("Not yet implemented");
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
			return new Document(actorId.getBytes(StandardCharsets.UTF_8));
		}
	}
}
