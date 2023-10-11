package io.autoswim.swim;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoSwimException;
import io.autoswim.swim.messages.FullSyncMessage;
import io.autoswim.swim.messages.HeartbeatMessage;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.messages.SwimMessageHandler;
import io.autoswim.swim.network.SwimNetwork;
import io.autoswim.types.Endpoint;

public class SwimRuntime implements SwimMessageHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SwimRuntime.class);

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); 
	private final Queue<SwimMessage> messagesToSend = new ConcurrentLinkedQueue<>();
	private final List<SwimMessageHandler> messageHandlers = new ArrayList<>();
	private final Endpoint ownEndpoint;
	private final SwimNetwork swimNetwork;
	private final Set<Endpoint> seedNodes;
	private final Set<Endpoint> aliveNodes = Collections.synchronizedSet(new HashSet<>());
	private boolean running = false;
	private Thread receiveThread;
	private ScheduledFuture<?> sendSchedule;

	public SwimRuntime(Set<Endpoint> seedNodes, int swimPort, SwimNetwork swimNetwork) {
		this.seedNodes = seedNodes;
		aliveNodes.addAll(seedNodes);
		this.swimNetwork = swimNetwork;
		try {
			this.ownEndpoint = Endpoint.builder()
					.withHostname(InetAddress.getLocalHost().getHostName())
					.withPort(swimPort)
					.build();
		} catch (UnknownHostException e) {
			throw new AutoSwimException("Could not get own hostname", e);
		}
	}

	public void start() {
		swimNetwork.start();
		running = true;
		receiveThread = new Thread(() -> receive());
		receiveThread.start();
		messagesToSend.add(StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withSender(ownEndpoint)
				.build());
		sendSchedule = scheduler.scheduleAtFixedRate(() -> sendMessages(), 1L, 1L, TimeUnit.SECONDS);
	}

	public void stop() {
		running = false;
		sendSchedule.cancel(true);
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
		messagesToSend.add(message);
	}
	
	private void sendMessages() {
		if(messagesToSend.isEmpty()) {
			swimNetwork.sendMessage(HeartbeatMessage.builder()
					.withCreatedAt(Instant.now())
					.withSender(ownEndpoint)
					.build(), aliveNodes);
		} else {
			while(!messagesToSend.isEmpty()) {
				swimNetwork.sendMessage(messagesToSend.poll(), aliveNodes);
			}
		}
	}

	@Override
	public void handle(StartupMessage startupMessage) {
		//TODO remove nodes after a timeout from alive nodes
		// a cache that handles this is probably the simplest way
		// check if message was already seen and send it to other nodes if not
		aliveNodes.add(startupMessage.getSender());
	}

	@Override
	public void handle(FullSyncMessage fullSyncMessage) {
		//TODO remove nodes after a timeout from alive nodes
		// a cache that handles this is probably the simplest way
		// check if message was already seen and send it to other nodes if not
		aliveNodes.add(fullSyncMessage.getSender());
	}

	@Override
	public void handle(HeartbeatMessage heartbeatMessage) {
		//TODO remove nodes after a timeout from alive nodes
		// a cache that handles this is probably the simplest way
		// check if message was already seen and send it to other nodes if not
		aliveNodes.add(heartbeatMessage.getSender());
	}
}
