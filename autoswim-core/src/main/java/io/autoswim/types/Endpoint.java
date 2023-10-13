package io.autoswim.types;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.AutoSwimException;

@JsonDeserialize(builder = Endpoint.Builder.class)
public class Endpoint {
	private final InetAddress address;
	private final String hostname;
	private final int port;

	private Endpoint(Builder builder) {
		if(builder.address == null) {
			try {
				this.address = InetAddress.getByName(builder.hostname);
			} catch (UnknownHostException e) {
				throw new AutoSwimException(String.format("Could not resolve hostname \"%s\"", builder.hostname), e);
			}
		} else {			
			this.address = builder.address;
		}
		this.hostname = builder.hostname;
		this.port = builder.port;
	}
	
	public InetAddress getAddress() {
		return address;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		return Objects.hash(address, hostname, port);
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
		return Objects.equals(address, other.address) && Objects.equals(hostname, other.hostname) && port == other.port;
	}

	@Override
	public String toString() {
		return "Endpoint [address=" + address + ", hostname=" + hostname + ", port=" + port + "]";
	}
	

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(Endpoint endpoint) {
		return new Builder(endpoint);
	}

	public static final class Builder {
		private InetAddress address;
		private String hostname;
		private int port;

		private Builder() {
		}

		private Builder(Endpoint endpoint) {
			this.address = endpoint.address;
			this.hostname = endpoint.hostname;
			this.port = endpoint.port;
		}

		public Builder withAddress(InetAddress address) {
			this.address = address;
			return this;
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
