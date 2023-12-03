package io.autoswim.cdi;

import io.autoswim.AutoswimRequestSyncScheduler;
import io.autoswim.MessageIdGenerator;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.UuidMessageIdGenerator;
import io.autoswim.runtime.AutoswimConfig;
import io.autoswim.runtime.AutoswimController;
import io.autoswim.runtime.AutoswimRuntime;
import io.autoswim.runtime.AutoswimStateHandler;
import io.autoswim.runtime.AutoswimStateInitializer;
import io.autoswim.runtime.EmptyAutoswimStateInitializer;
import io.autoswim.runtime.ExecutorAutoswimRequestSyncScheduler;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.swim.scalecube.network.ScaleCubeAutoswimNetwork;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AutoswimProducer {
	
	@ApplicationScoped
	@Produces
	public AutoswimRuntime produceAutoswimRuntime(AutoswimConfig config,
			AutoswimNetwork network,
			AutoswimStateHandler stateHandler,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator,
			AutoswimRequestSyncScheduler requestSyncScheduler) {
		return new AutoswimRuntime(config, network, stateHandler, ownEndpointProvider, messageIdGenerator, requestSyncScheduler);
	}
	
	@ApplicationScoped
	@Produces
	public AutoswimNetwork produceAutoswimNetwork(AutoswimConfig config) {
		return new ScaleCubeAutoswimNetwork(config.getNetworkConfig());
	}
	
	@ApplicationScoped
	@Produces
	public AutoswimStateHandler produceAutoswimStateHandler(AutoswimConfig config, AutoswimStateInitializer stateInitializer) {
		return new AutoswimStateHandler(config.getAutoswimWorkingDir(), stateInitializer);
	}
	
	@ApplicationScoped
	@Produces
	public AutoswimStateInitializer produceAutoswimStateInitializer() {
		return new EmptyAutoswimStateInitializer();
	}
	
	@ApplicationScoped
	@Produces
	public MessageIdGenerator produceMessageIdGenerator() {
		return new UuidMessageIdGenerator();
	}
	
	@ApplicationScoped
	@Produces
	public AutoswimRequestSyncScheduler produceAutoswimRequestSyncScheduler(AutoswimNetwork swimNetwork,
			AutoswimStateHandler stateHandler,
			OwnEndpointProvider ownEndpointProvider,
			MessageIdGenerator messageIdGenerator) {
		return new ExecutorAutoswimRequestSyncScheduler(swimNetwork, stateHandler, ownEndpointProvider, messageIdGenerator);
	}
	
	@ApplicationScoped
	@Produces
	public AutoswimController produceAutoswimController(AutoswimRuntime runtime,
			AutoswimStateHandler stateHandler,
			MessageIdGenerator messageIdGenerator,
			OwnEndpointProvider ownEndpointProvider) {
		return new AutoswimController(runtime, stateHandler, messageIdGenerator, ownEndpointProvider);
	}
}
