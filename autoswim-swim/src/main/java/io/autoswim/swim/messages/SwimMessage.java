package io.autoswim.swim.messages;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.autoswim.types.Endpoint;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
		  use = JsonTypeInfo.Id.NAME, 
		  include = JsonTypeInfo.As.PROPERTY, 
		  property = "type")
		@JsonSubTypes({ 
		  @Type(value = StartupMessage.class, name = "startup"), 
		  @Type(value = FullSyncMessage.class, name = "full-sync"),
		  @Type(value = HeartbeatMessage.class, name = "heartbeat")
		})
public abstract class SwimMessage implements Serializable {
	private static final long serialVersionUID = 8150048954595274240L;
	private final String id;
	private final Instant createdAt;
	private final Endpoint sender;
	
	protected SwimMessage(String id, Instant createdAt, Endpoint sender) {
		this.id = id;
		this.createdAt = createdAt;
		this.sender = sender;
	}
	
	public String getId() {
		return id;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Endpoint getSender() {
		return sender;
	}
	
	public abstract void handle(SwimMessageHandler handler);
	
	public abstract byte getMessageType();

	@Override
	public int hashCode() {
		return Objects.hash(createdAt, id, sender);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwimMessage other = (SwimMessage) obj;
		return Objects.equals(createdAt, other.createdAt) && Objects.equals(id, other.id)
				&& Objects.equals(sender, other.sender);
	}
}
