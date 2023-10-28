package io.autoswim.swim.network;

import java.util.Set;

import io.autoswim.messages.AutoswimMessage;
import io.autoswim.types.Endpoint;

public interface AutoswimNetwork {
	void start();
	
	void stop();
	
	AutoswimMessage receiveMessage();
	
	void sendMessage(AutoswimMessage swimMessage);
	
	Set<Endpoint> getMembers();
}
