package io.autoswim;

import java.util.UUID;

public class UuidMessageIdGenerator implements MessageIdGenerator {

	@Override
	public String generateId() {
		return UUID.randomUUID().toString();
	}
}
