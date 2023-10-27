package io.autoswim.swim.network.scalecube;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.autoswim.AutoSwimException;
import io.autoswim.swim.messages.SwimMessage;
import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.ClusterMessageHandler;
import io.scalecube.cluster.Member;
import io.scalecube.cluster.membership.MembershipEvent;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.net.Address;
import io.scalecube.transport.netty.tcp.TcpTransportFactory;

public class ScaleCubeSwimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(ScaleCubeSwimNetwork.class);

	private final ScaleCubeSwimNetworkConfig swimNetworkConfig;
	private final BlockingQueue<SwimMessage> receivedMessages = new LinkedBlockingQueue<>();
	private Cluster cluster;

	public ScaleCubeSwimNetwork(ScaleCubeSwimNetworkConfig swimNetworkConfig) {
		this.swimNetworkConfig = swimNetworkConfig;
	}

	public void start() {
		String memberAlias = swimNetworkConfig.getMemberAlias();
		List<Address> seedNodes = swimNetworkConfig.getSeedNodes();
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

	public void stop() {
		cluster.shutdown();
	}
	
	public Set<Member> getMembers() {
		return new HashSet<>(cluster.members());
	}

	public SwimMessage receiveMessage() {
		try {
			return receivedMessages.take();
		} catch (InterruptedException e) {
			throw new AutoSwimException("Got interrupted while waiting for the next SwimMessage", e);
		}
	}

	public void sendMessage(SwimMessage swimMessage) {
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
