package io.autoswim.swim.network;

import java.util.Set;

import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

public interface SwimNetwork {
	void start();
	
	void stop();
	
	SwimMessage receiveMessage();
	
	void sendMessage(SwimMessage swimMessage, Set<Endpoint> aliveNodes);
}
