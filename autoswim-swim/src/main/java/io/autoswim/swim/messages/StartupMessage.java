package io.autoswim.swim.messages;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = StartupMessage.Builder.class)
public class StartupMessage extends SwimMessage {

	private StartupMessage(Builder builder) {
		super(builder.createdAt, builder.sender);
	}
	
	@Override
	public void handle(SwimMessageHandler handler) {
		handler.handle(this);	
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(StartupMessage startupMessage) {
		return new Builder(startupMessage);
	}

	public static final class Builder {
		private Instant createdAt;
		private Endpoint sender;

		private Builder() {
		}

		private Builder(StartupMessage startupMessage) {
			this.createdAt = startupMessage.getCreatedAt();
			this.sender = startupMessage.getSender();
		}

		public Builder withCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder withSender(Endpoint sender) {
			this.sender = sender;
			return this;
		}

		public StartupMessage build() {
			return new StartupMessage(this);
		}
	}
}
