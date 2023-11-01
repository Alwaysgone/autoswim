package io.autoswim.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.automerge.Document;

import com.google.common.io.ByteStreams;

import io.autoswim.AutoswimException;

public class EmptyAutoswimStateInitializer implements AutoswimStateInitializer {

	@Override
	public Document getInitialState() {
		try(InputStream is = this.getClass().getClassLoader().getSystemResourceAsStream("initial-state.dat")) {
			byte[] initialState = ByteStreams.toByteArray(is);			
			return Document.load(initialState);
		} catch (IOException e) {
			throw new AutoswimException("Could not load initial state", e);
		}
	}
}
