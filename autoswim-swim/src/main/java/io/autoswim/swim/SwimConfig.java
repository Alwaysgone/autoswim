package io.autoswim.swim;

import java.time.Duration;
import java.util.Set;

import io.autoswim.types.Endpoint;
import java.util.Collections;

public class SwimConfig {
	private final Set<Endpoint> seedNodes;
	private final Duration heartbeatInterval;
	
	private SwimConfig(Builder builder) {
		this.seedNodes = builder.seedNodes;
		this.heartbeatInterval = builder.heartbeatInterval;
	}
	public Set<Endpoint> getSeedNodes() {
		return seedNodes;
	}
	
	public Duration getHeartbeatInterval() {
		return heartbeatInterval;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(SwimConfig swimConfig) {
		return new Builder(swimConfig);
	}
	
	public static final class Builder {
		private Set<Endpoint> seedNodes = Collections.emptySet();
		private Duration heartbeatInterval = Duration.ofSeconds(30L);

		private Builder() {
		}

		private Builder(SwimConfig swimConfig) {
			this.seedNodes = swimConfig.seedNodes;
			this.heartbeatInterval = swimConfig.heartbeatInterval;
		}

		public Builder withSeedNodes(Set<Endpoint> seedNodes) {
			this.seedNodes = seedNodes;
			return this;
		}

		public Builder withHeartbeatInterval(Duration heartbeatInterval) {
			this.heartbeatInterval = heartbeatInterval;
			return this;
		}

		public SwimConfig build() {
			return new SwimConfig(this);
		}
	}
}
