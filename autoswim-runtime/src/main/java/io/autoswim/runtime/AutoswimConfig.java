package io.autoswim.runtime;

import java.nio.file.Path;

import io.autoswim.swim.network.AutoswimNetworkConfig;

public class AutoswimConfig {
	private final boolean registerDefaultMessageHandler;
	private final Path autoswimWorkingDir;
	private final AutoswimNetworkConfig networkConfig;

	private AutoswimConfig(Builder builder) {
		this.registerDefaultMessageHandler = builder.registerDefaultMessageHandler;
		this.autoswimWorkingDir = builder.autoswimWorkingDir;
		this.networkConfig = builder.networkConfig;
	}

	/**
	 * If set to false the default Autoswim message handler is not registered, meaning
	 * all updates need to be handled by a user provided message handler.
	 * This is useful if there is either a bug in the default implementation or
	 * the user needs to apply additional logic during sync operations.
	 * @return true if the default message handler should be registered; otherwise false
	 */
	public boolean isRegisterDefaultMessageHandler() {
		return registerDefaultMessageHandler;
	}
	
	public Path getAutoswimWorkingDir() {
		return autoswimWorkingDir;
	}
	
	public AutoswimNetworkConfig getNetworkConfig() {
		return networkConfig;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(AutoswimConfig autoswimConfig) {
		return new Builder(autoswimConfig);
	}

	public static final class Builder {
		private boolean registerDefaultMessageHandler = true;
		private Path autoswimWorkingDir;
		private AutoswimNetworkConfig networkConfig;

		private Builder() {
		}

		private Builder(AutoswimConfig autoswimConfig) {
			this.registerDefaultMessageHandler = autoswimConfig.registerDefaultMessageHandler;
			this.autoswimWorkingDir = autoswimConfig.autoswimWorkingDir;
			this.networkConfig = autoswimConfig.networkConfig;
		}

		public Builder withRegisterDefaultMessageHandler(boolean registerDefaultMessageHandler) {
			this.registerDefaultMessageHandler = registerDefaultMessageHandler;
			return this;
		}

		public Builder withAutoswimWorkingDir(Path autoswimWorkingDir) {
			this.autoswimWorkingDir = autoswimWorkingDir;
			return this;
		}

		public Builder withNetworkConfig(AutoswimNetworkConfig networkConfig) {
			this.networkConfig = networkConfig;
			return this;
		}

		public AutoswimConfig build() {
			return new AutoswimConfig(this);
		}
	}
}
