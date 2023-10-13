package io.autoswim;

import io.autoswim.types.Endpoint;

public class OwnEndpointProvider {
	
	private final Endpoint ownEndpoint;

	public OwnEndpointProvider(Endpoint ownEndpoint) {
		this.ownEndpoint = ownEndpoint;
	}
	
	public Endpoint getOwnEndpoint() {
		return ownEndpoint;
	}
}
