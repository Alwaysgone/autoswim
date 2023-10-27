package io.autoswim.swim.scalecube;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.messages.SwimMessageHandler;
import io.autoswim.swim.network.scalecube.ScaleCubeSwimNetwork;
import io.autoswim.types.Endpoint;
import io.scalecube.cluster.Member;

public class ScaleCubeSwimRuntime {
	private static final Logger LOG = LoggerFactory.getLogger(ScaleCubeSwimRuntime.class);

	private final List<SwimMessageHandler> messageHandlers = new ArrayList<>();
	private final Endpoint ownEndpoint;
	private final MessageIdGenerator messageIdGenerator;
	private final ScaleCubeSwimNetwork swimNetwork;
	private boolean running = false;
	private Thread receiveThread;

	public ScaleCubeSwimRuntime(ScaleCubeSwimNetwork swimNetwork,
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

	public void registerMessageHandler(SwimMessageHandler messageHandler) {
		messageHandlers.add(messageHandler);
	}

	private void receive() {
		while(running) {
			SwimMessage swimMessage = swimNetwork.receiveMessage();
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

	public void scheduleMessage(SwimMessage message) {
		swimNetwork.sendMessage(message);
	}

	public Set<Member> getMembers() {
		return swimNetwork.getMembers();
	}
}
