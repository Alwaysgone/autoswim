package io.autoswim.swim.network;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.autoswim.types.Endpoint;

public class AutoswimNetworkConfig {
	private final String memberAlias;
	private final List<Endpoint> seedNodes;
	private final int swimPort;

	private AutoswimNetworkConfig(Builder builder) {
		this.memberAlias = builder.memberAlias;
		this.seedNodes = builder.seedNodes;
		this.swimPort = builder.swimPort;
	}
	
	public String getMemberAlias() {
		return memberAlias;
	}
	
	public int getSwimPort() {
		return swimPort;
	}
	
	public List<Endpoint> getSeedNodes() {
		return seedNodes;
	}	

	@Override
	public int hashCode() {
		return Objects.hash(memberAlias, seedNodes, swimPort);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AutoswimNetworkConfig other = (AutoswimNetworkConfig) obj;
		return Objects.equals(memberAlias, other.memberAlias) && Objects.equals(seedNodes, other.seedNodes)
				&& swimPort == other.swimPort;
	}

	@Override
	public String toString() {
		return "SwimNetworkConfig [memberAlias=" + memberAlias + ", seedNodes=" + seedNodes + ", swimPort=" + swimPort
				+ "]";
	}
	

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(AutoswimNetworkConfig swimNetworkConfig) {
		return new Builder(swimNetworkConfig);
	}

	public static final class Builder {
		private String memberAlias;
		private List<Endpoint> seedNodes = Collections.emptyList();
		private int swimPort;

		private Builder() {
		}

		private Builder(AutoswimNetworkConfig swimNetworkConfig) {
			this.memberAlias = swimNetworkConfig.memberAlias;
			this.seedNodes = swimNetworkConfig.seedNodes;
			this.swimPort = swimNetworkConfig.swimPort;
		}

		public Builder withMemberAlias(String memberAlias) {
			this.memberAlias = memberAlias;
			return this;
		}

		public Builder withSeedNodes(List<Endpoint> seedNodes) {
			this.seedNodes = seedNodes;
			return this;
		}

		public Builder withSwimPort(int swimPort) {
			this.swimPort = swimPort;
			return this;
		}

		public AutoswimNetworkConfig build() {
			return new AutoswimNetworkConfig(this);
		}
	}
}
