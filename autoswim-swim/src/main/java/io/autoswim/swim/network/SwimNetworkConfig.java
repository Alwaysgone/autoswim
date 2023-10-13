package io.autoswim.swim.network;

import java.util.Objects;

public class SwimNetworkConfig {
	private final String bindHost;
	private final int swimPort;
	private final int maxTransmissionsPerMessage;

	private SwimNetworkConfig(Builder builder) {
		this.bindHost = builder.bindHost;
		this.swimPort = builder.swimPort;
		this.maxTransmissionsPerMessage = builder.maxTransmissionsPerMessage;
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
	
	@Override
	public int hashCode() {
		return Objects.hash(maxTransmissionsPerMessage, swimPort);
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
		return maxTransmissionsPerMessage == other.maxTransmissionsPerMessage && swimPort == other.swimPort;
	}

	@Override
	public String toString() {
		return "SwimNetworkConfig [swimPort=" + swimPort + ", maxTransmissionsPerMessage=" + maxTransmissionsPerMessage
				+ "]";
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

		private Builder() {
		}

		private Builder(SwimNetworkConfig swimNetworkConfig) {
			this.bindHost = swimNetworkConfig.bindHost;
			this.swimPort = swimNetworkConfig.swimPort;
			this.maxTransmissionsPerMessage = swimNetworkConfig.maxTransmissionsPerMessage;
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

		public SwimNetworkConfig build() {
			return new SwimNetworkConfig(this);
		}
	}
}
