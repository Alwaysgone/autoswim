package io.autoswim.messages;

import java.time.Instant;
import java.util.Arrays;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.autoswim.types.Endpoint;

@JsonDeserialize(builder = RequestSyncMessage.Builder.class)
public class RequestSyncMessage extends AutoswimMessage {
	private static final long serialVersionUID = -3419944944409542504L;
	private final byte[][] heads;

	private RequestSyncMessage(Builder builder) {
		super(builder.id, builder.createdAt, builder.sender);
		this.heads = builder.heads;
	}

	public byte[][] getHeads() {
		return heads;
	}
	
	@Override
	public void handle(AutoswimMessageHandler handler) {
		handler.handle(this);	
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.deepHashCode(heads);
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
		RequestSyncMessage other = (RequestSyncMessage) obj;
		return Arrays.deepEquals(heads, other.heads);
	}

	@Override
	public String toString() {
		return "RequestSyncMessage [heads=" + Arrays.toString(heads) + "]";
	}

	
	public static Builder builder() {
		return new Builder();
	}

	public static Builder builder(RequestSyncMessage requestSyncMessage) {
		return new Builder(requestSyncMessage);
	}

	public static final class Builder {
		private String id;
		private Instant createdAt;
		private Endpoint sender;
		private byte[][] heads;

		private Builder() {
		}

		private Builder(RequestSyncMessage requestSyncMessage) {
			this.id = requestSyncMessage.getId();
			this.createdAt = requestSyncMessage.getCreatedAt();
			this.sender = requestSyncMessage.getSender();
			this.heads = requestSyncMessage.heads;
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

		public Builder withHeads(byte[][] heads) {
			this.heads = heads;
			return this;
		}

		public RequestSyncMessage build() {
			return new RequestSyncMessage(this);
		}
	}
}
