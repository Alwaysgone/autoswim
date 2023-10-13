package io.autoswim.swim.messages;

import java.time.Instant;
import java.util.Arrays;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = FullSyncMessage.Builder.class)
public class FullSyncMessage extends SwimMessage {
	private final byte[] state;

	private FullSyncMessage(Builder builder) {
		super(builder.id, builder.createdAt, builder.sender);
		this.state = builder.state;
	}

	public byte[] getState() {
		return state;
	}
	
	@Override
	public void handle(SwimMessageHandler handler) {
		handler.handle(this);	
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(state);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FullSyncMessage other = (FullSyncMessage) obj;
		return Arrays.equals(state, other.state);
	}

	@Override
	public String toString() {
		return "FullSync [state=" + Arrays.toString(state) + "]";
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(FullSyncMessage fullSyncMessage) {
		return new Builder(fullSyncMessage);
	}

	public static final class Builder {
		private String id;
		private Instant createdAt;
		private Endpoint sender;
		private byte[] state;

		private Builder() {
		}

		private Builder(FullSyncMessage fullSyncMessage) {
			this.id = fullSyncMessage.getId();
			this.createdAt = fullSyncMessage.getCreatedAt();
			this.sender = fullSyncMessage.getSender();
			this.state = fullSyncMessage.state;
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

		public Builder withState(byte[] state) {
			this.state = state;
			return this;
		}

		public FullSyncMessage build() {
			return new FullSyncMessage(this);
		}
	}


}
