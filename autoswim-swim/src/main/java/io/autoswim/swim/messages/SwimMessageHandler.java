package io.autoswim.swim.messages;

public interface SwimMessageHandler {
	public void handle(StartupMessage startupMessage);
	
	public void handle(FullSyncMessage fullSyncMessage);
	
	public void handle(HeartbeatMessage heartbeatMessage);
}
