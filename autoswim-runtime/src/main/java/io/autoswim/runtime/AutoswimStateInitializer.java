package io.autoswim.runtime;

import org.automerge.Document;

public interface AutoswimStateInitializer {
	Document getInitialState();
}
