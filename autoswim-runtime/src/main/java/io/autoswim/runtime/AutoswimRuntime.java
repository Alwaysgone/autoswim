package io.autoswim.runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.StartupMessage;
import io.autoswim.messages.AutoswimMessage;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.types.Endpoint;

public class AutoswimRuntime {
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimRuntime.class);

	private final List<AutoswimMessageHandler> messageHandlers = new ArrayList<>();
	private final Endpoint ownEndpoint;
	private final MessageIdGenerator messageIdGenerator;
	private final AutoswimNetwork swimNetwork;
	private boolean running = false;
	private Thread receiveThread;

	public AutoswimRuntime(AutoswimNetwork swimNetwork,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator
			) {
		this.swimNetwork = swimNetwork;
		this.messageIdGenerator = messageIdGenerator;
		this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
	}

	public void start() {
		swimNetwork.start();
		running = true;
		receiveThread = new Thread(() -> receive());
		receiveThread.start();
		scheduleMessage(StartupMessage.builder()
				.withId(messageIdGenerator.generateId())
				.withCreatedAt(Instant.now())
				.withSender(ownEndpoint)
				.build());
	}

	public void stop() {
		running = false;
		receiveThread.interrupt();
		swimNetwork.stop();
	}

	public void registerMessageHandler(AutoswimMessageHandler messageHandler) {
		messageHandlers.add(messageHandler);
	}

	private void receive() {
		while(running) {
			AutoswimMessage swimMessage = swimNetwork.receiveMessage();
			LOG.info("Handling: {}", swimMessage);
			messageHandlers.forEach(mh -> {
				try {
					swimMessage.handle(mh);
				} catch(Exception e) {
					LOG.error("An error occurred while {} handled {}"
							, mh.getClass().getSimpleName()
							, swimMessage.getClass().getSimpleName(), e);
				}
			});
		}
	}

	public void scheduleMessage(AutoswimMessage message) {
		swimNetwork.sendMessage(message);
	}

	public Set<Endpoint> getMembers() {
		return swimNetwork.getMembers();
	}
}
