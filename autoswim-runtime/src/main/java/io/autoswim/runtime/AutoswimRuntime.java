package io.autoswim.runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.AutoswimMessage;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.messages.FullSyncMessage;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.messages.StartupMessage;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.types.Endpoint;

public class AutoswimRuntime implements AutoswimMessageHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimRuntime.class);

	private final AutoswimConfig config;
	private final AutoswimNetwork swimNetwork;
	private final AutoswimStateHandler stateHandler;
	private final MessageIdGenerator messageIdGenerator;
	private final Endpoint ownEndpoint;
	private final List<AutoswimMessageHandler> messageHandlers = new ArrayList<>();

	private boolean running = false;
	private Thread receiveThread;

	public AutoswimRuntime(
			AutoswimConfig config,
			AutoswimNetwork swimNetwork,
			AutoswimStateHandler stateHandler,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator
			) {
		this.config = config;
		this.swimNetwork = swimNetwork;
		this.stateHandler = stateHandler;
		this.messageIdGenerator = messageIdGenerator;
		this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
	}

	public void start() {
		if(config.isRegisterDefaultMessageHandler()) {
			LOG.info("Registering default message handler ...");
			registerMessageHandler(this);
		} else {
			LOG.warn("Default message handler is not registered, for updates to be processed a user provided handler needs to be registered.");
		}
		swimNetwork.start();
		running = true;
		receiveThread = new Thread(() -> receive());
		receiveThread.start();
		// the startup message tells all the other nodes to send their full state so that remote changes
		// can be merged into the local state
		scheduleMessage(StartupMessage.builder()
				.withId(messageIdGenerator.generateId())
				.withCreatedAt(Instant.now())
				.withSender(ownEndpoint)
				.build());
		// also sending the current local state so that other nodes can get possible offline changes
		scheduleMessage(FullSyncMessage.builder()
				.withId(messageIdGenerator.generateId())
				.withCreatedAt(Instant.now())
				.withSender(ownEndpoint)
				.withState(stateHandler.getSerializedState())
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

	@Override
	public void handle(StartupMessage startupMessage) {
		LOG.info("Sending full state update because of StartupMessage from {} with id {} ...", startupMessage.getSender(), startupMessage.getId());
		scheduleMessage(FullSyncMessage
				.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpoint)
				.withState(stateHandler.getSerializedState())
				.build());
	}

	@Override
	public void handle(FullSyncMessage fullSyncMessage) {
		Document otherDoc = Document.load(fullSyncMessage.getState());
		LOG.info("Merging FullSyncMessage from {} with id {} into local state ...", fullSyncMessage.getSender(), fullSyncMessage.getId());
		stateHandler.updateState(d -> {
			d.merge(otherDoc);
			return d;
		});
	}

	@Override
	public void handle(IncrementalSyncMessage incrementalSyncMessage) {
		LOG.info("Applying incremental change set from {}", incrementalSyncMessage.getSender());
		stateHandler.updateState(d -> {
			d.applyEncodedChanges(incrementalSyncMessage.getChangeSet());
			return d;
		});
	}
}
