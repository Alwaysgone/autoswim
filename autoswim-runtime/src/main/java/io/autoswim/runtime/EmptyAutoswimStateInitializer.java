package io.autoswim.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.automerge.Document;

import com.google.common.io.ByteStreams;

import io.autoswim.AutoswimException;

public class EmptyAutoswimStateInitializer implements AutoswimStateInitializer {

	private Path resolveInitialStateFile(Path root) {
		return root.resolve("initial-state.dat");
	}

	@Override
	public Document getInitialState() {
		try {
			Path root = Paths.get(EmptyAutoswimStateInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			byte[] initialState;
			
			if(root.endsWith("classes\\java\\main")) {
				// test case
				Path dat = Paths.get("src/main/resources/initial-state.dat").toAbsolutePath();
				initialState = Files.readAllBytes(dat);
			} else if(Files.isDirectory(root)) {
				try(InputStream is = Files.newInputStream(resolveInitialStateFile(root))) {
					initialState = ByteStreams.toByteArray(is);
				}
			} else {
				URI jarUri = new URI("jar", root.toUri().toString(), null);
				try(FileSystem jarFs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
					Path jarRoot = jarFs.getPath("/");
					Path doc = jarRoot.resolve("initial-state.dat");
					initialState = Files.readAllBytes(doc);						
				}
			}
			return Document.load(initialState);
		} catch (URISyntaxException | IOException e) {
			throw new AutoswimException("Could not load initial state", e);
		}
	}
}
