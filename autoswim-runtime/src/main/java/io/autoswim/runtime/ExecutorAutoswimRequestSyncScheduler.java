package io.autoswim.runtime;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoswimRequestSyncScheduler;
import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.messages.RequestSyncMessage;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.types.Endpoint;

public class ExecutorAutoswimRequestSyncScheduler implements AutoswimRequestSyncScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(ExecutorAutoswimRequestSyncScheduler.class);
	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
	
	private final AutoswimNetwork swimNetwork;
	private final AutoswimStateHandler stateHandler;
	private final Endpoint ownEndpoint;
	private final MessageIdGenerator messageIdGenerator;
	
	private ScheduledFuture<?> schedule;
	

	public ExecutorAutoswimRequestSyncScheduler(AutoswimNetwork swimNetwork,
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
		schedule = EXECUTOR.scheduleAtFixedRate(this::sendRequestSyncMessage, 5L, 5L, TimeUnit.MINUTES);
	}

	@Override
	public void stop() {
		if(schedule != null) {
			schedule.cancel(true);
			schedule = null;
		}

	}
	
	private void sendRequestSyncMessage() {
		LOG.info("Sending scheduled RequestSyncMessage ...");
		swimNetwork.sendMessage(RequestSyncMessage.builder()
				.withCreatedAt(Instant.now())
				.withId(messageIdGenerator.generateId())
				.withSender(ownEndpoint)
				.withHeads(stateHandler.getCurrentHeads())
				.build());
	}
}
