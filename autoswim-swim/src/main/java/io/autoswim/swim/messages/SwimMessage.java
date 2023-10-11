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
		  @Type(value = FullSyncMessage.class, name = "truck") 
		})
public abstract class SwimMessage {
	private final Instant createdAt;
	private final Endpoint sender;
	
	protected SwimMessage(Instant createdAt, Endpoint sender) {
		this.createdAt = createdAt;
		this.sender = sender;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Endpoint getSender() {
		return sender;
	}
	
	public abstract void handle(SwimMessageHandler handler);
}
