package io.autoswim.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoSwimException;
import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.SwimRuntime;
import io.autoswim.swim.messages.FullSyncMessage;
import io.autoswim.types.Endpoint;

public class AutoswimStateHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimStateHandler.class);
	private static final String AUTOSWIM_STATE_FILE_NAME = "autoswim.dat";
	
	private final Path autoswimStatePath;
	private final AtomicReference<Document> currentState = new AtomicReference<>();
	private final SwimRuntime swimRuntime;
	private final OwnEndpointProvider ownEndpointProvider;
	private final MessageIdGenerator messageIdGenerator;
	
	public AutoswimStateHandler(Path autoswimWorkingDir,
			SwimRuntime swimRuntime,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator) {
		this.ownEndpointProvider = ownEndpointProvider;
		this.messageIdGenerator = messageIdGenerator;
		this.autoswimStatePath = autoswimWorkingDir.resolve(AUTOSWIM_STATE_FILE_NAME);
		this.swimRuntime = swimRuntime;
		if(autoswimStatePath.toFile().exists()) {
			try {
				Files.readAllBytes(autoswimStatePath);
			} catch (IOException e) {
				throw new AutoSwimException(String.format("Could not read Autoswim state at %s", autoswimStatePath), e);
			}
		} else {
			Endpoint ownEndpoint = ownEndpointProvider.getOwnEndpoint();
			String actorId = ownEndpoint.getHostname() + ownEndpoint.getPort();
			Document newDoc = new Document(actorId.getBytes(StandardCharsets.UTF_8));
			//TODO maybe init standard maps or others fields
			currentState.set(newDoc);
			storeState();
		}
	}
	
	private void storeState() {
		try {
			Files.write(autoswimStatePath,
					currentState.get().save(),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOG.error("Could not store state at {}", e);
		}
	}
	
	public void updateState(UnaryOperator<Document> update) {
		Document newDocument = currentState.updateAndGet(update);
		//TODO only send delta
		swimRuntime.scheduleMessage(FullSyncMessage.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withState(newDocument.save())
				.withSender(ownEndpointProvider.getOwnEndpoint())
				.build());
		storeState();
	}
	
	public Document getCurrentState() {
		return currentState.get();
	}
	
	public byte[] getSerializedState() {
		return currentState.get().save();
	}
}
