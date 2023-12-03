package io.autoswim.quarkus;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoswimRequestSyncScheduler;
import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.RequestSyncMessage;
import io.autoswim.runtime.AutoswimStateHandler;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.types.Endpoint;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

@Alternative
@Priority(1)
@ApplicationScoped
public class QuarkusAutoswimRequestSyncScheduler implements AutoswimRequestSyncScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(QuarkusAutoswimRequestSyncScheduler.class);

	private final AutoswimNetwork swimNetwork;
	private final AutoswimStateHandler stateHandler;
	private final Endpoint ownEndpoint;
	private final MessageIdGenerator messageIdGenerator;

	@Inject
	public QuarkusAutoswimRequestSyncScheduler(AutoswimNetwork swimNetwork,
			AutoswimStateHandler stateHandler,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator) {
		this.swimNetwork = swimNetwork;
		this.stateHandler = stateHandler;
		this.ownEndpoint = ownEndpointProvider.getOwnEndpoint();
		this.messageIdGenerator = messageIdGenerator;
	}

	@Override
	public void start() {
		// not necessary
	}

	@Scheduled(cron = "{autoswim.requestSync.cron}")
	void sendRequestSyncMessage() {
		if(swimNetwork.isStarted()) {
			LOG.info("Sending scheduled RequestSyncMessage ...");
			swimNetwork.sendMessage(RequestSyncMessage.builder()
					.withCreatedAt(Instant.now())
					.withId(messageIdGenerator.generateId())
					.withSender(ownEndpoint)
					.withHeads(stateHandler.getCurrentHeads())
					.build());
		}
	}

	@Override
	public void stop() {
		// not necessary
	}
}
