package io.autoswim.swim.messages;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = HeartbeatMessage.Builder.class)
public class HeartbeatMessage extends SwimMessage {
	private static final long serialVersionUID = 7643489110483726833L;

	private HeartbeatMessage(Builder builder) {
		super(builder.id, builder.createdAt, builder.sender);
	}

	@Override
	public void handle(SwimMessageHandler handler) {
		handler.handle(this);	
	}
	
	@Override
	public byte getMessageType() {
		return (byte)'h';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(HeartbeatMessage heartbeatMessage) {
		return new Builder(heartbeatMessage);
	}

	public static final class Builder {
		private String id;
		private Instant createdAt;
		private Endpoint sender;

		private Builder() {
		}

		private Builder(HeartbeatMessage heartbeatMessage) {
			this.id = heartbeatMessage.getId();
			this.createdAt = heartbeatMessage.getCreatedAt();
			this.sender = heartbeatMessage.getSender();
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
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
