package io.autoswim.messages;

import java.time.Instant;
import java.util.Arrays;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = IncrementalSyncMessage.Builder.class)
public class IncrementalSyncMessage extends AutoswimMessage {
	private static final long serialVersionUID = -3419944944409542504L;
	private final byte[] changeSet;

	private IncrementalSyncMessage(Builder builder) {
		super(builder.id, builder.createdAt, builder.sender);
		this.changeSet = builder.changeSet;
	}

	public byte[] getChangeSet() {
		return changeSet;
	}
	
	@Override
	public void handle(AutoswimMessageHandler handler) {
		handler.handle(this);	
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(changeSet);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncrementalSyncMessage other = (IncrementalSyncMessage) obj;
		return Arrays.equals(changeSet, other.changeSet);
	}

	@Override
	public String toString() {
		return "IncrementalSyncMessage [changeSet=" + Arrays.toString(changeSet) + "]";
	}
	

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(IncrementalSyncMessage incrementalSyncMessage) {
		return new Builder(incrementalSyncMessage);
	}

	public static final class Builder {
		private String id;
		private Instant createdAt;
		private Endpoint sender;
		private byte[] changeSet;

		private Builder() {
		}

		private Builder(IncrementalSyncMessage incrementalSyncMessage) {
			this.id = incrementalSyncMessage.getId();
			this.createdAt = incrementalSyncMessage.getCreatedAt();
			this.sender = incrementalSyncMessage.getSender();
			this.changeSet = incrementalSyncMessage.changeSet;
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

		public Builder withChangeSet(byte[] changeSet) {
			this.changeSet = changeSet;
			return this;
		}

		public IncrementalSyncMessage build() {
			return new IncrementalSyncMessage(this);
		}
	}
}
