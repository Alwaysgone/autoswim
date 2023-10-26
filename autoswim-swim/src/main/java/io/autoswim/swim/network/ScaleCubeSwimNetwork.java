package io.autoswim.swim.network;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoSwimException;
import io.autoswim.OwnEndpointProvider;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import io.scalecube.transport.netty.tcp.TcpTransportFactory;

public class ScaleCubeSwimNetwork implements SwimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(ScaleCubeSwimNetwork.class);
	
	private final SwimNetworkConfig swimNetworkConfig;
	private final OwnEndpointProvider ownEndpointProvider;
	private final BlockingQueue<SwimMessage> receivedMessages = new LinkedBlockingQueue<>();
	//	private Transport transport;
	private Cluster cluster;

	public ScaleCubeSwimNetwork(SwimNetworkConfig swimNetworkConfig, OwnEndpointProvider ownEndpointProvider) {
		this.swimNetworkConfig = swimNetworkConfig;
		this.ownEndpointProvider = ownEndpointProvider;
	}

	@Override
	public void start() {
		Endpoint ownEndpoint = ownEndpointProvider.getOwnEndpoint();
		String memberAlias = String.format("%s:%s", ownEndpoint.getHostname(), ownEndpoint.getPort());
		List<Address> seedNodes = swimNetworkConfig.getSeedNodes().stream()
				.map(sn -> (String.format("%s:%s", sn.getHostname(), sn.getPort())))
				.map(Address::from)
				.collect(Collectors.toList());
		ClusterConfig clusterConfig = new ClusterConfig()
				.memberAlias(memberAlias)
				.transport(opts -> opts.port(swimNetworkConfig.getSwimPort()))
				;
		if(!seedNodes.isEmpty()) {
			clusterConfig.membership(opts -> opts.seedMembers(seedNodes));
		}
		cluster = new ClusterImpl(clusterConfig)
				.transportFactory(TcpTransportFactory::new)
				.handler(cluster -> new SwimClusterMessageHandler(receivedMessages))
				.startAwait();
		LOG.info("Started up with these members: {}", cluster.members());
		//		transport = Transport.bindAwait(TransportConfig.defaultConfig()
		//				.messageCodec(new JacksonMessageCodec())
		//				.port(swimNetworkConfig.getSwimPort()));
		//		transport.start();
	}

	@Override
	public void stop() {
		cluster.shutdown();
	}

	@Override
	public SwimMessage receiveMessage() {
		try {
			return receivedMessages.take();
		} catch (InterruptedException e) {
			throw new AutoSwimException("Got interrupted while waiting for the next SwimMessage", e);
		}
	}

	@Override
	public void sendMessage(SwimMessage swimMessage, Set<Endpoint> aliveNodes) {
		cluster.spreadGossip(Message.fromData(swimMessage))
		.doOnError(t -> LOG.error("Sending {} failed", swimMessage.getClass().getSimpleName(), t))
		.subscribe();
	}
	
	private static class SwimClusterMessageHandler implements ClusterMessageHandler {
		
		private final BlockingQueue<SwimMessage> receivedMessages;

		private SwimClusterMessageHandler(BlockingQueue<SwimMessage> receivedMessages) {
			this.receivedMessages = receivedMessages;
		}
		
		@Override
		public void onMessage(Message message) {
			SwimMessage swimMessage = message.data();
			receivedMessages.add(swimMessage);
		}
		
		@Override
		public void onGossip(Message gossip) {
			onMessage(gossip);
		}
		
		@Override
		public void onMembershipEvent(MembershipEvent event) {
			LOG.info("{}", event);
		}
	}
}
