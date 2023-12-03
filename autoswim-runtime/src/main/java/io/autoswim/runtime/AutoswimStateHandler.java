package io.autoswim.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;

import org.automerge.ChangeHash;
import org.automerge.Document;
import org.automerge.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoswimException;

public class AutoswimStateHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AutoswimStateHandler.class);
	private static final String AUTOSWIM_STATE_FILE_NAME = "autoswim.dat";

	private final Path autoswimStatePath;
	private final Document document;

	public AutoswimStateHandler(Path autoswimWorkingDir,
			AutoswimStateInitializer stateIntializer) {
		this.autoswimStatePath = autoswimWorkingDir.resolve(AUTOSWIM_STATE_FILE_NAME);
		if(autoswimStatePath.toFile().exists()) {
			try {
				byte[] stateBytes = Files.readAllBytes(autoswimStatePath);
				Document storedDoc = Document.load(stateBytes);
				document = storedDoc;
			} catch (IOException e) {
				throw new AutoswimException(String.format("Could not read Autoswim state at %s", autoswimStatePath), e);
			}
		} else {
			document = stateIntializer.getInitialState();
			storeState(document);
		}
	}

	private void storeState(Document state) {
		try {
			Files.write(autoswimStatePath,
					state.save(),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOG.error("Could not store state at {}", autoswimStatePath, e);
		}
	}

	public byte[] updateState(Consumer<Transaction> update) {
		synchronized(document) {
			ChangeHash[] currentHeads = document.getHeads();
			try(Transaction tx = document.startTransaction()) {
				update.accept(tx);
				tx.commit();
			}
			byte[] updateChanges = document.encodeChangesSince(currentHeads);
			storeState(document);
			return updateChanges;
		}
	}

	public void applyUpdate(byte[] encodedChangeSet) {
		synchronized(document) {
			document.applyEncodedChanges(encodedChangeSet);
			storeState(document);
		}
	}

	public void mergeDocument(Document other) {
		synchronized(document) {
			document.merge(other);
			storeState(document);
		}
	}

	public Document getCurrentState() {
		return document;
	}

	public byte[] getSerializedState() {
		synchronized(document) {
			return document.save();
		}
	}

	public byte[][] getCurrentHeads() {
		synchronized(document) {
			ChangeHash[] heads = document.getHeads();
			return Arrays.stream(heads)
					.map(ChangeHash::getBytes)
					.toArray(i -> new byte[i][]);
		}
	}

	public byte[] getChangesSince(byte[][] heads) {
		synchronized(document) {
			ChangeHash[] changeHashes = Arrays.stream(heads)
					.map(this::toChangeHash)
					.toArray(i -> new ChangeHash[i]);
			return document.encodeChangesSince(changeHashes);
		}
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
