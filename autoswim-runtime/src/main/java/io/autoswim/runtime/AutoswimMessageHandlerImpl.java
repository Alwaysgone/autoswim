package io.autoswim.runtime;

import java.time.Instant;

import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.AutoswimMessageHandler;
import io.autoswim.messages.FullSyncMessage;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.messages.StartupMessage;

public class AutoswimMessageHandlerImpl implements AutoswimMessageHandler{	
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimMessageHandlerImpl.class);
	private final AutoswimStateHandler stateHandler;
	private final OwnEndpointProvider ownEndpointProvider;
	private final MessageIdGenerator messageIdGenerator;
	private final AutoswimRuntime swimRuntime;

	public AutoswimMessageHandlerImpl(AutoswimStateHandler stateHandler
			, OwnEndpointProvider ownEndpointProvider
			, MessageIdGenerator messageIdGenerator
			, AutoswimRuntime swimRuntime) {
		this.stateHandler = stateHandler;
		this.ownEndpointProvider = ownEndpointProvider;
		this.messageIdGenerator = messageIdGenerator;
		this.swimRuntime = swimRuntime;
		swimRuntime.registerMessageHandler(this);
	}
	
	@Override
	public void handle(StartupMessage startupMessage) {
		LOG.info("Sending full state update because of StartupMessage from {} with id {} ...", startupMessage.getSender(), startupMessage.getId());
		swimRuntime.scheduleMessage(FullSyncMessage
				.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpointProvider.getOwnEndpoint())
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
		}, false);
	}

	@Override
	public void handle(IncrementalSyncMessage incrementalSyncMessage) {
		LOG.info("Applying incremental change set from {}", incrementalSyncMessage.getSender());
		stateHandler.updateState(d -> {
			d.applyEncodedChanges(incrementalSyncMessage.getChangeSet());
			return d;
		}, false);
	}
}
