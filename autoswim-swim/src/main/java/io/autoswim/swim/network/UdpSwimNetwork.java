package io.autoswim.swim.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.autoswim.AutoSwimException;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

public class UdpSwimNetwork implements SwimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(UdpSwimNetwork.class);
	private static final int BUFFER_SIZE = 100 * 1024;
	
	private final SwimNetworkConfig swimNetworkConfig;
	private final DatagramSocket swimSocket;
	private final ObjectMapper objectMapper;
	private final Random random;
	private final BlockingQueue<SwimMessage> receivedMessages = new LinkedBlockingQueue<>();
	private boolean running = false;
	private Thread receiveThread;
	
	public UdpSwimNetwork(SwimNetworkConfig swimNetworkConfig, ObjectMapper objectMapper) {
		this.swimNetworkConfig = swimNetworkConfig;
		this.objectMapper = objectMapper;
		this.random = new Random();
		String bindHost = swimNetworkConfig.getBindHost();
		int swimPort = swimNetworkConfig.getSwimPort();
		try {
			swimSocket = new DatagramSocket(new InetSocketAddress(bindHost, swimPort));
		} catch (SocketException e) {
			throw new AutoSwimException(String.format("Could not set up udp socket at %s:%s", bindHost, swimPort), e);
		}
	}
	
	@Override
	public void start() {
		running = true;
		receiveThread = new Thread(() -> receive());
		receiveThread
		.start();
	}

	@Override
	public void stop() {
		running = false;
		receiveThread.interrupt();
	}
	
	private void receive() {
		byte[] buffer = new byte[BUFFER_SIZE];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while(running) {
			try {
				//TODO implement fragmented packets so that arbitrary data can be sent and received
				swimSocket.receive(packet);
				LOG.info("Received message");
				receivedMessages.add(objectMapper.readerFor(SwimMessage.class)
				.readValue(packet.getData()));
			} catch (IOException e) {
				LOG.error("An error occurred while receiving UDP packets", e);
			}
		}
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
		if(aliveNodes.isEmpty()) {
			LOG.info("No alive nodes present not broadcasting {}", swimMessage.getClass().getSimpleName());			
		}
		List<Endpoint> nodesToGossipTo = pickNodesToGossipTo(aliveNodes, swimNetworkConfig.getMaxTransmissionsPerMessage());
		String swimMessageClassName = swimMessage.getClass().getSimpleName();
		byte[] serializedMessage;
		try {
			serializedMessage = objectMapper.writeValueAsBytes(swimMessage);
		} catch (JsonProcessingException e) {
			throw new AutoSwimException(String.format("Could not serialize %s", swimMessage.getClass().getSimpleName()), e);
		}
		nodesToGossipTo.forEach(gn -> {
			try {
				LOG.debug("Sending {} to {} ...", swimMessageClassName, gn);
				swimSocket.send(new DatagramPacket(serializedMessage, serializedMessage.length, gn.getAddress(), gn.getPort()));
			} catch (IOException e) {
				LOG.error("Could not send {} to {}", swimMessageClassName, gn, e);
			}
		});
	}
	
	private List<Endpoint> pickNodesToGossipTo(Set<Endpoint> aliveNodes, int numberOfNodes) {
		List<Endpoint> remainingNodes = new ArrayList<>(aliveNodes);
		List<Endpoint> pickedNodes = new ArrayList<>();
		while(pickedNodes.size() < numberOfNodes && !remainingNodes.isEmpty()) {
			int remainingNodesSize = remainingNodes.size();
			pickedNodes.add(remainingNodes.remove(random.nextInt(remainingNodesSize)));
		}
		return pickedNodes;
	}
}
