package io.autoswim.messages;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = StartupMessage.Builder.class)
public class StartupMessage extends AutoswimMessage {
	private static final long serialVersionUID = -2638419722084700850L;

	private StartupMessage(Builder builder) {
		super(builder.id, builder.createdAt, builder.sender);
	}

	@Override
	public void handle(AutoswimMessageHandler handler) {
		handler.handle(this);	
	}
	
	@Override
	public byte getMessageType() {
		return (byte)'s';
	}

	@Override
	public String toString() {
		return "StartupMessage [id=" + getId() + ", createdAt=" + getCreatedAt() + ", sender="
				+ getSender() + "]";
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(StartupMessage startupMessage) {
		return new Builder(startupMessage);
	}

	public static final class Builder {
		private String id;
		private Instant createdAt;
		private Endpoint sender;

		private Builder() {
		}

		private Builder(StartupMessage startupMessage) {
			this.id = startupMessage.getId();
			this.createdAt = startupMessage.getCreatedAt();
			this.sender = startupMessage.getSender();
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

		public StartupMessage build() {
			return new StartupMessage(this);
		}
	}

	
}
