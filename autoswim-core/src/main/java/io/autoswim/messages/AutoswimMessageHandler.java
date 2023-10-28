package io.autoswim.messages;

public interface AutoswimMessageHandler {
	public void handle(StartupMessage startupMessage);
	
	public void handle(FullSyncMessage fullSyncMessage);
}
