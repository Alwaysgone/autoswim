package io.autoswim.runtime;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoswimRequestSyncScheduler;
import io.autoswim.Cancellable;
import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.AutoswimMessage;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.messages.FullSyncMessage;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.messages.RequestSyncMessage;
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
	private final AutoswimRequestSyncScheduler requestSyncScheduler;
	private final List<AutoswimMessageHandler> messageHandlers = new ArrayList<>();

	private boolean running = false;
	private Thread receiveThread;


	public AutoswimRuntime(
			AutoswimConfig config,
			AutoswimNetwork swimNetwork,
			AutoswimStateHandler stateHandler,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator,
			AutoswimRequestSyncScheduler requestSyncScheduler
			) {
		this.config = config;
		this.swimNetwork = swimNetwork;
		this.stateHandler = stateHandler;
		this.messageIdGenerator = messageIdGenerator;
		this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
		this.requestSyncScheduler = requestSyncScheduler;
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
		requestSyncScheduler.start();
		// the startup message is simply an informative message and does not serve a specific purpose
		// other than indicating a startup of a node
		scheduleMessage(StartupMessage.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpoint)
				.build());
		// also sending the current local state so that other nodes can get possible offline changes
		scheduleMessage(RequestSyncMessage.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpoint)
				.withHeads(stateHandler.getCurrentHeads())
				.build());
	}

	public void stop() {
		running = false;
		requestSyncScheduler.stop();
		if(receiveThread != null) {
			receiveThread.interrupt();
		}
		swimNetwork.stop();
	}

	public Cancellable registerMessageHandler(AutoswimMessageHandler messageHandler) {
		messageHandlers.add(messageHandler);
		return () -> messageHandlers.remove(messageHandler);
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
		LOG.info("{}: Sending RequestSyncMessage because of StartupMessage from {} with id {} ...", ownEndpoint.toHostAndPortString(), startupMessage.getSender(), startupMessage.getId());
		scheduleMessage(RequestSyncMessage.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpoint)
				.withHeads(stateHandler.getCurrentHeads())
				.build());
	}

	@Override
	public void handle(FullSyncMessage fullSyncMessage) {
		Document otherDoc = Document.load(fullSyncMessage.getState());
		LOG.info("Merging FullSyncMessage from {} with id {} into local state ...", fullSyncMessage.getSender(), fullSyncMessage.getId());
		stateHandler.mergeDocument(otherDoc);
	}

	@Override
	public void handle(IncrementalSyncMessage incrementalSyncMessage) {
		LOG.info("{}: Applying incremental change set from {}", ownEndpoint.toHostAndPortString(), incrementalSyncMessage.getSender());
		stateHandler.applyUpdate(incrementalSyncMessage.getChangeSet());
	}

	@Override
	public void handle(RequestSyncMessage requestSyncMessage) {
		LOG.info("{}: Received sync request from {}", ownEndpoint.toHostAndPortString(), requestSyncMessage.getSender());
		byte[] changesSince = stateHandler.getChangesSince(requestSyncMessage.getHeads());
		if(changesSince.length > 0) {
			LOG.info("Detected changes, sending incremental changes ...");
			scheduleMessage(IncrementalSyncMessage.builder()
					.withCreatedAt(Instant.now())
					.withId(messageIdGenerator.generateId())
					.withChangeSet(changesSince)
					.withSender(ownEndpoint)
					.build());
		} else {
			LOG.info("No changes found, not sending incremental message");			
		}
	}
}
