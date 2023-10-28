package io.autoswim.swim.scalecube.network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoSwimException;
import io.autoswim.messages.AutoswimMessage;
import io.autoswim.swim.network.AutoswimNetwork;
import io.autoswim.swim.network.AutoswimNetworkConfig;
import io.autoswim.types.Endpoint;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import io.scalecube.transport.netty.tcp.TcpTransportFactory;

public class ScaleCubeAutoswimNetwork implements AutoswimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(ScaleCubeAutoswimNetwork.class);

	private final AutoswimNetworkConfig swimNetworkConfig;
	private final BlockingQueue<AutoswimMessage> receivedMessages = new LinkedBlockingQueue<>();
	private Cluster cluster;

	public ScaleCubeAutoswimNetwork(AutoswimNetworkConfig swimNetworkConfig) {
		this.swimNetworkConfig = swimNetworkConfig;
	}

	@Override
	public void start() {
		String memberAlias = swimNetworkConfig.getMemberAlias();
		List<Address> seedNodes = swimNetworkConfig.getSeedNodes().stream()
				.map(this::asString)
				.map(Address::from)
				.collect(Collectors.toList());
		ClusterConfig clusterConfig = new ClusterConfig()
				.memberAlias(memberAlias)
				.transport(opts -> opts.port(swimNetworkConfig.getSwimPort()))
				;
		if(!seedNodes.isEmpty()) {
			LOG.info("Using seed nodes: {}", seedNodes);
			clusterConfig = clusterConfig.membership(opts -> opts.seedMembers(seedNodes));
		}
		cluster = new ClusterImpl(clusterConfig)
				.transportFactory(TcpTransportFactory::new)
				.handler(cluster -> new SwimClusterMessageHandler(receivedMessages))
				.startAwait();
		LOG.info("Started up on {} with these members: {}", cluster.address(), cluster.members());
	}
	
	private String asString(Endpoint endpoint) {
		return String.format("%s:%s", endpoint.getHostname(), endpoint.getPort());
	}
	
	private String asString(Address address) {
		return String.format("%s:%s", address.host(), address.port());
	}
	
	@Override
	public void stop() {
		cluster.shutdown();
	}
	
	@Override
	public Set<Endpoint> getMembers() {
		return new HashSet<>(cluster.members().stream()
				.map(m -> m.address())
				.map(this::asString)
				.map(Endpoint::of)
				.collect(Collectors.toList()));
	}

	@Override
	public AutoswimMessage receiveMessage() {
		try {
			return receivedMessages.take();
		} catch (InterruptedException e) {
			throw new AutoSwimException("Got interrupted while waiting for the next SwimMessage", e);
		}
	}

	@Override
	public void sendMessage(AutoswimMessage swimMessage) {
		cluster.spreadGossip(Message.fromData(swimMessage))
		.doOnError(t -> LOG.error("Sending {} failed", swimMessage.getClass().getSimpleName(), t))
		.subscribe();
	}

	private static class SwimClusterMessageHandler implements ClusterMessageHandler {

		private final BlockingQueue<AutoswimMessage> receivedMessages;

		private SwimClusterMessageHandler(BlockingQueue<AutoswimMessage> receivedMessages) {
			this.receivedMessages = receivedMessages;
		}

		@Override
		public void onMessage(Message message) {
			AutoswimMessage swimMessage = message.data();
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
