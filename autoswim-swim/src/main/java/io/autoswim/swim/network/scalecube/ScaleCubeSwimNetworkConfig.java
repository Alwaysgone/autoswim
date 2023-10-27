package io.autoswim.swim.network.scalecube;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.scalecube.net.Address;

public class ScaleCubeSwimNetworkConfig {
	private final String memberAlias;
	private final List<Address> seedNodes;
	private final int swimPort;

	private ScaleCubeSwimNetworkConfig(Builder builder) {
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
	
	public List<Address> getSeedNodes() {
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
		ScaleCubeSwimNetworkConfig other = (ScaleCubeSwimNetworkConfig) obj;
		return Objects.equals(memberAlias, other.memberAlias) && Objects.equals(seedNodes, other.seedNodes)
				&& swimPort == other.swimPort;
	}

	@Override
	public String toString() {
		return "ScaleCubeSwimNetworkConfig [memberAlias=" + memberAlias + ", seedNodes=" + seedNodes + ", swimPort="
				+ swimPort + "]";
	}
	

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(ScaleCubeSwimNetworkConfig scaleCubeSwimNetworkConfig) {
		return new Builder(scaleCubeSwimNetworkConfig);
	}

	public static final class Builder {
		private String memberAlias;
		private List<Address> seedNodes = Collections.emptyList();
		private int swimPort;

		private Builder() {
		}

		private Builder(ScaleCubeSwimNetworkConfig scaleCubeSwimNetworkConfig) {
			this.memberAlias = scaleCubeSwimNetworkConfig.memberAlias;
			this.seedNodes = scaleCubeSwimNetworkConfig.seedNodes;
			this.swimPort = scaleCubeSwimNetworkConfig.swimPort;
		}

		public Builder withMemberAlias(String memberAlias) {
			this.memberAlias = memberAlias;
			return this;
		}

		public Builder withSeedNodes(List<Address> seedNodes) {
			this.seedNodes = seedNodes;
			return this;
		}

		public Builder withSwimPort(int swimPort) {
			this.swimPort = swimPort;
			return this;
		}

		public ScaleCubeSwimNetworkConfig build() {
			return new ScaleCubeSwimNetworkConfig(this);
		}
	}
}
