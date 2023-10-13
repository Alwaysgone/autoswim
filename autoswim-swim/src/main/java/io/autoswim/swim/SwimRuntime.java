package io.autoswim.swim;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.messages.HeartbeatMessage;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.swim.messages.SwimMessageHandler;
import io.autoswim.swim.network.SwimNetwork;
import io.autoswim.types.Endpoint;

public class SwimRuntime {
	private static final Logger LOG = LoggerFactory.getLogger(SwimRuntime.class);

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); 
	private final Queue<SwimMessage> messagesToSend = new ConcurrentLinkedQueue<>();
	private final List<SwimMessageHandler> messageHandlers = new ArrayList<>();
	private final Endpoint ownEndpoint;
	private final MessageIdGenerator messageIdGenerator;
	private final SwimNetwork swimNetwork;
	private final Cache<String, String> seenMessages = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(10L))
			.build();
	private final Cache<Endpoint, Instant> aliveNodes = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofSeconds(30L))
			.build();
	private boolean running = false;
	private Thread receiveThread;
	private ScheduledFuture<?> sendSchedule;

	public SwimRuntime(Set<Endpoint> seedNodes,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator,
			SwimNetwork swimNetwork) {
		// just add the seed nodes to the alive nodes so that the Startup message can be sent to them
		// if seed nodes are down they will be removed from the member list anyway
		seedNodes.forEach(sn -> aliveNodes.put(sn, Instant.now()));
		this.swimNetwork = swimNetwork;
		this.messageIdGenerator = messageIdGenerator;
		this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
	}

	public void start() {
		swimNetwork.start();
		running = true;
		receiveThread = new Thread(() -> receive());
		receiveThread.start();
		messagesToSend.add(StartupMessage.builder()
				.withId(messageIdGenerator.generateId())
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
			aliveNodes.put(swimMessage.getSender(), Instant.now());
			String messageId = swimMessage.getId();
			if(seenMessages.getIfPresent(messageId) == null) {
				seenMessages.put(messageId, messageId);
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
				messagesToSend.add(swimMessage);
			} else {
				LOG.info("Ignoring already seen message with id {} of type {}", messageId, swimMessage.getClass().getSimpleName());
			}
		}
	}

	public void scheduleMessage(SwimMessage message) {
		messagesToSend.add(message);
	}
	
	private void sendMessages() {
		if(messagesToSend.isEmpty()) {
			LOG.debug("Sending heartbeat message ...");
			String id = messageIdGenerator.generateId();
			seenMessages.put(id, id);
			swimNetwork.sendMessage(HeartbeatMessage.builder()
					.withId(id)
					.withCreatedAt(Instant.now())
					.withSender(ownEndpoint)
					.build(), aliveNodes.asMap().keySet());
		} else {
			while(!messagesToSend.isEmpty()) {
				// adding messages to send to already seen messages
				// since they contain data that has already been processed
				SwimMessage messageToSend = messagesToSend.poll();
				String id = messageToSend.getId();
				seenMessages.put(id, id);
				swimNetwork.sendMessage(messageToSend, aliveNodes.asMap().keySet());
			}
		}
	}
	
	public Set<Endpoint> getAliveNodes() {
		return aliveNodes.asMap().keySet();
	}
}
