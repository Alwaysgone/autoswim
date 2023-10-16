package io.autoswim.swim.messages;

import java.time.Instant;

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
public abstract class SwimMessage {
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
}
