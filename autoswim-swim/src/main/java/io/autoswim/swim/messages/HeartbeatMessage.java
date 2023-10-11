package io.autoswim.swim.messages;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = HeartbeatMessage.Builder.class)
public class HeartbeatMessage extends SwimMessage {

	private HeartbeatMessage(Builder builder) {
		super(builder.createdAt, builder.sender);
	}
	
	@Override
	public void handle(SwimMessageHandler handler) {
		handler.handle(this);	
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(HeartbeatMessage startupMessage) {
		return new Builder(startupMessage);
	}

	public static final class Builder {
		private Instant createdAt;
		private Endpoint sender;

		private Builder() {
		}

		private Builder(HeartbeatMessage startupMessage) {
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

		public HeartbeatMessage build() {
			return new HeartbeatMessage(this);
		}
	}
}
