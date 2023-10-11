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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.autoswim.AutoSwimException;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

public class UdpSwimNetwork implements SwimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(UdpSwimNetwork.class);

	private final SwimNetworkConfig swimNetworkConfig;
	private final DatagramSocket swimSocket;
	private final ObjectMapper objectMapper;
	private final Random random;
	private boolean running = false;
	private Thread receiveThread;
	
	public UdpSwimNetwork(SwimNetworkConfig swimNetworkConfig, ObjectMapper objectMapper) {
		this.swimNetworkConfig = swimNetworkConfig;
		this.objectMapper = objectMapper;
		this.random = new Random();
		int swimPort = swimNetworkConfig.getSwimPort();
		try {
			swimSocket = new DatagramSocket(InetSocketAddress.createUnresolved("0.0.0.0", swimPort));
		} catch (SocketException e) {
			throw new AutoSwimException(String.format("Could not set up udp socket at 0.0.0.0:%s", swimPort), e);
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
		// TODO Auto-generated method stub

	}
	
	private void receive() {
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while(running) {
			try {
				swimSocket.receive(packet);
			} catch (IOException e) {
				LOG.error("An error occurred while receiving UDP packets", e);
			}
			//TODO setup socket and determine how messages are delimited might need to set up a protocol
			// 1. deserialize message
			// 2. add sender to member list or update its heartbeat status
			// 3. add the deserialized message to an ordered queue so receiveMessage() can return it
		}
	}

	@Override
	public SwimMessage receiveMessage() {
		
		// TODO Auto-generated method stub
		return null;
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
		while(pickedNodes.size() < numberOfNodes || remainingNodes.isEmpty()) {
			pickedNodes.add(remainingNodes.remove(random.nextInt(remainingNodes.size())));
		}
		return pickedNodes;
	}
}
