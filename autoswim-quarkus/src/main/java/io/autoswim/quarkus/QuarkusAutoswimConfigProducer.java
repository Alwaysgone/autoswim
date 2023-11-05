package io.autoswim.quarkus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Collectors;

import io.autoswim.AutoswimException;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.quarkus.config.MicroprofileAutoswimConfig;
import io.autoswim.runtime.AutoswimConfig;
import io.autoswim.swim.network.AutoswimNetworkConfig;
import io.autoswim.types.Endpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class QuarkusAutoswimConfigProducer {
	
	@ApplicationScoped
	@Produces
	public AutoswimConfig getAutoswimConfig(MicroprofileAutoswimConfig config, OwnEndpointProvider ownEndpointProvider) {
		return AutoswimConfig.builder()
				.withAutoswimWorkingDir(config.getWorkingDir())
				.withRegisterDefaultMessageHandler(config.isRegisterDefaultMessageHandler())
				.withNetworkConfig(AutoswimNetworkConfig.builder()
						.withSwimPort(config.getSwimPort())
						.withMemberAlias(config.getMemberAlias().orElse(ownEndpointProvider.getOwnEndpoint().toHostAndPortString()))
						.withSeedNodes(config.getSeedNodes().stream()
								.map(Endpoint::of)
								.collect(Collectors.toList()))
						.build())
				.build();
	}
	
	@ApplicationScoped
	@Produces
	public OwnEndpointProvider produceOwnEndpointProvider(MicroprofileAutoswimConfig config) {
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			return new OwnEndpointProvider(Endpoint.builder()
					.withHostname(hostname)
					.withPort(config.getSwimPort())
					.build());
		} catch (UnknownHostException e) {
			throw new AutoswimException("Could not get hostname", e);
		}
	}
}
