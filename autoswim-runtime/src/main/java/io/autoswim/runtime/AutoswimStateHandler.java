package io.autoswim.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.automerge.ChangeHash;
import org.automerge.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoswimException;

public class AutoswimStateHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimStateHandler.class);
	private static final String AUTOSWIM_STATE_FILE_NAME = "autoswim.dat";
	
	private final Path autoswimStatePath;
	private final AtomicReference<Document> currentState = new AtomicReference<>();
	
	public AutoswimStateHandler(Path autoswimWorkingDir,
			AutoswimStateInitializer stateIntializer) {
		this.autoswimStatePath = autoswimWorkingDir.resolve(AUTOSWIM_STATE_FILE_NAME);
		if(autoswimStatePath.toFile().exists()) {
			try {
				byte[] stateBytes = Files.readAllBytes(autoswimStatePath);
				Document storedDoc = Document.load(stateBytes);
				currentState.set(storedDoc);
			} catch (IOException e) {
				throw new AutoswimException(String.format("Could not read Autoswim state at %s", autoswimStatePath), e);
			}
		} else {
			Document intialDoc = stateIntializer.getInitialState();
			currentState.set(intialDoc);
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
	
	public byte[] updateState(UnaryOperator<Document> update) {
		ChangeHash[] currentHeads = currentState.get().getHeads();
		Document newDocument = currentState.updateAndGet(update);
		byte[] updateChanges = newDocument.encodeChangesSince(currentHeads);
		storeState();
		return updateChanges;
	}
	
	public Document getCurrentState() {
		return currentState.get();
	}
	
	public byte[] getSerializedState() {
		return currentState.get().save();
	}
	
	public byte[][] getCurrentHeads() {
		ChangeHash[] heads = currentState.get().getHeads();
		return Arrays.stream(heads)
		.map(h -> h.getBytes())
		.toArray(i -> new byte[i][]);
	}
	
	public byte[] getChangesSince(byte[][] heads) {
		ChangeHash[] changeHashes = Arrays.stream(heads)
		.map(this::toChangeHash)
		.toArray(i -> new ChangeHash[i]);
		return currentState.get().encodeChangesSince(changeHashes);
	}
	
	private ChangeHash toChangeHash(byte[] head) {
		// this is necessary since ChangeHash does not implement Serializable and has no publicly available
		// way to be constructed
		@SuppressWarnings("unchecked")
		Constructor<ChangeHash> constructor = (Constructor<ChangeHash>)ChangeHash.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		try {
			return constructor.newInstance(head);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new AutoswimException("Could not create ChangeHash instance", e);
		}
	}
}
