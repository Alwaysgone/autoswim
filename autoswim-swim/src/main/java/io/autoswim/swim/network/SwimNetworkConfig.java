package io.autoswim.swim.network;

import java.util.List;
import java.util.Objects;

import io.autoswim.types.Endpoint;
import java.util.Collections;

public class SwimNetworkConfig {
	private final String bindHost;
	private final int swimPort;
	private final int maxTransmissionsPerMessage;
	private final int bufferSize;
	private final List<Endpoint> seedNodes;

	private SwimNetworkConfig(Builder builder) {
		this.bindHost = builder.bindHost;
		this.swimPort = builder.swimPort;
		this.maxTransmissionsPerMessage = builder.maxTransmissionsPerMessage;
		this.bufferSize = builder.bufferSize;
		this.seedNodes = builder.seedNodes;
	}

	public String getBindHost() {
		return bindHost;
	}
	
	public int getSwimPort() {
		return swimPort;
	}
	
	public int getMaxTransmissionsPerMessage() {
		return maxTransmissionsPerMessage;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public List<Endpoint> getSeedNodes() {
		return seedNodes;
	}	

	@Override
	public int hashCode() {
		return Objects.hash(bindHost, bufferSize, maxTransmissionsPerMessage, seedNodes, swimPort);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwimNetworkConfig other = (SwimNetworkConfig) obj;
		return Objects.equals(bindHost, other.bindHost) && bufferSize == other.bufferSize
				&& maxTransmissionsPerMessage == other.maxTransmissionsPerMessage
				&& Objects.equals(seedNodes, other.seedNodes) && swimPort == other.swimPort;
	}

	@Override
	public String toString() {
		return "SwimNetworkConfig [bindHost=" + bindHost + ", swimPort=" + swimPort + ", maxTransmissionsPerMessage="
				+ maxTransmissionsPerMessage + ", bufferSize=" + bufferSize + ", seedNodes=" + seedNodes + "]";
	}
	

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(SwimNetworkConfig swimNetworkConfig) {
		return new Builder(swimNetworkConfig);
	}

	public static final class Builder {
		private String bindHost = "0.0.0.0";
		private int swimPort;
		private int maxTransmissionsPerMessage = 2;
		private int bufferSize = 100 * 1024;
		private List<Endpoint> seedNodes = Collections.emptyList();

		private Builder() {
		}

		private Builder(SwimNetworkConfig swimNetworkConfig) {
			this.bindHost = swimNetworkConfig.bindHost;
			this.swimPort = swimNetworkConfig.swimPort;
			this.maxTransmissionsPerMessage = swimNetworkConfig.maxTransmissionsPerMessage;
			this.bufferSize = swimNetworkConfig.bufferSize;
			this.seedNodes = swimNetworkConfig.seedNodes;
		}

		public Builder withBindHost(String bindHost) {
			this.bindHost = bindHost;
			return this;
		}

		public Builder withSwimPort(int swimPort) {
			this.swimPort = swimPort;
			return this;
		}

		public Builder withMaxTransmissionsPerMessage(int maxTransmissionsPerMessage) {
			this.maxTransmissionsPerMessage = maxTransmissionsPerMessage;
			return this;
		}

		public Builder withBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Builder withSeedNodes(List<Endpoint> seedNodes) {
			this.seedNodes = seedNodes;
			return this;
		}

		public SwimNetworkConfig build() {
			return new SwimNetworkConfig(this);
		}
	}
}
