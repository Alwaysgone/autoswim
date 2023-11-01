package io.autoswim.runtime;

import org.automerge.Document;

/**
 * <p>Used to initialize the Automerge document when non is present. This interface is useful for testing
 * if different nodes should start with a different state.
 * </p>
 */
public interface AutoswimStateInitializer {
	Document getInitialState();
}
