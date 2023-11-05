package io.autoswim.runtime;

import java.time.Instant;
import java.util.function.UnaryOperator;

import org.automerge.Document;

import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.IncrementalSyncMessage;
import io.autoswim.types.Endpoint;

public class AutoswimController {
	private final AutoswimRuntime runtime;
	private final AutoswimStateHandler stateHandler;
	private final MessageIdGenerator idGen;
	private final Endpoint ownEndpoint;

	public AutoswimController(AutoswimRuntime runtime,
			AutoswimStateHandler stateHandler,
			MessageIdGenerator idGen,
			OwnEndpointProvider ownEndpointProvider) {
				this.runtime = runtime;
				this.stateHandler = stateHandler;
				this.idGen = idGen;
				this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
	}
	
	public Document getCurrentState() {
		return stateHandler.getCurrentState();
	}
	
	public void updateState(UnaryOperator<Document> update) {
		byte[] changeSet = stateHandler.updateState(update);
		runtime.scheduleMessage(IncrementalSyncMessage.builder()
				.withChangeSet(changeSet)
				.withCreatedAt(Instant.now())
				.withId(idGen.generateId())
				.withSender(ownEndpoint)
				.build());
	}
}
