package io.autoswim.runtime;

public class AutoswimConfig {
	private final boolean registerDefaultMessageHandler;

	private AutoswimConfig(Builder builder) {
		this.registerDefaultMessageHandler = builder.registerDefaultMessageHandler;
	}
	
	public boolean isRegisterDefaultMessageHandler() {
		return registerDefaultMessageHandler;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(AutoswimConfig autoswimConfig) {
		return new Builder(autoswimConfig);
	}

	public static final class Builder {
		private boolean registerDefaultMessageHandler = true;

		private Builder() {
		}

		private Builder(AutoswimConfig autoswimConfig) {
			this.registerDefaultMessageHandler = autoswimConfig.registerDefaultMessageHandler;
		}

		/**
		 * If set to false the default Autoswim message handler is not registered, meaning
		 * all updates need to be handled by a user provided message handler.
		 * This is useful if there is either a bug in the default implementation or
		 * the user needs to apply additional logic during sync operations.
		 * @param registerDefaultMessageHandler whether to enable registering the default message handler or not
		 * @return the builder instance
		 */
		public Builder withRegisterDefaultMessageHandler(boolean registerDefaultMessageHandler) {
			this.registerDefaultMessageHandler = registerDefaultMessageHandler;
			return this;
		}

		public AutoswimConfig build() {
			return new AutoswimConfig(this);
		}
	}
}
