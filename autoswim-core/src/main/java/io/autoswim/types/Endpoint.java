package io.autoswim.types;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = Endpoint.Builder.class)
public class Endpoint implements Serializable {
	private static final long serialVersionUID = 8557220143141942323L;
	private final String hostname;
	private final int port;

	private Endpoint(Builder builder) {
		this.hostname = builder.hostname;
		this.port = builder.port;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public String toHostAndPortString() {
		return String.format("%s:%s", hostname, port);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hostname, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Endpoint other = (Endpoint) obj;
		return Objects.equals(hostname, other.hostname) && port == other.port;
	}

	@Override
	public String toString() {
		return "Endpoint [hostname=" + hostname + ", port=" + port + "]";
	}
	
	public static Endpoint of(String endpoint) {
		String[] splittedEndpoint = endpoint.split(":");
		return Endpoint.builder()
				.withHostname(splittedEndpoint[0])
				.withPort(Integer.parseInt(splittedEndpoint[1]))
				.build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(Endpoint endpoint) {
		return new Builder(endpoint);
	}

	public static final class Builder {
		private String hostname;
		private int port;

		private Builder() {
		}

		private Builder(Endpoint endpoint) {
			this.hostname = endpoint.hostname;
			this.port = endpoint.port;
		}

		public Builder withHostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		public Builder withPort(int port) {
			this.port = port;
			return this;
		}

		public Endpoint build() {
			return new Endpoint(this);
		}
	}
}
