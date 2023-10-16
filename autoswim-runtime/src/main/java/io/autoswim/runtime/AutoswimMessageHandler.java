package io.autoswim.runtime;

import java.time.Instant;

import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.SwimRuntime;
import io.autoswim.swim.messages.FullSyncMessage;
import io.autoswim.swim.messages.HeartbeatMessage;
import io.autoswim.swim.messages.StartupMessage;
import io.autoswim.swim.messages.SwimMessageHandler;

public class AutoswimMessageHandler implements SwimMessageHandler {	
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimMessageHandler.class);
	private final AutoswimStateHandler stateHandler;
	private final OwnEndpointProvider ownEndpointProvider;
	private final MessageIdGenerator messageIdGenerator;
	private final SwimRuntime swimRuntime;

	public AutoswimMessageHandler(AutoswimStateHandler stateHandler
			, OwnEndpointProvider ownEndpointProvider
			, MessageIdGenerator messageIdGenerator
			, SwimRuntime swimRuntime) {
		this.stateHandler = stateHandler;
		this.ownEndpointProvider = ownEndpointProvider;
		this.messageIdGenerator = messageIdGenerator;
		this.swimRuntime = swimRuntime;
		swimRuntime.registerMessageHandler(this);
	}
	
	@Override
	public void handle(StartupMessage startupMessage) {
		LOG.info("Sending full state update because of StartupMessage with id {} ...", startupMessage.getId());
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
		LOG.info("Merging FullSyncMessage with id {} into local state ...", fullSyncMessage.getId());
		stateHandler.updateState(d -> {
			d.merge(otherDoc);
			return d;
		}, false);
	}

	@Override
	public void handle(HeartbeatMessage heartbeatMessage) {
		// nothing to do
	}
}
