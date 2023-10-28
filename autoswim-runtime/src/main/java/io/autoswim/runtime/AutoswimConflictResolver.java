package io.autoswim.runtime;

import org.automerge.Document;

public interface AutoswimConflictResolver {
	Document resolveMergeConflict(Document local, Document other);
}
