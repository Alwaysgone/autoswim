package io.autoswim.swim.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.autoswim.AutoSwimException;
import io.autoswim.swim.messages.SwimMessage;
import io.autoswim.types.Endpoint;

public class FragmentedUdpSwimNetwork implements SwimNetwork {
	private static final Logger LOG = LoggerFactory.getLogger(FragmentedUdpSwimNetwork.class);

	private final SwimNetworkConfig swimNetworkConfig;
	private final DatagramSocket swimSocket;
	private final ObjectMapper objectMapper;
	private final Random random;
	private final BlockingQueue<SwimMessage> receivedMessages = new LinkedBlockingQueue<>();
	private boolean running = false;
	private Thread receiveThread;
	private Map<SocketAddress, Queue<byte[]>> receiveQueues = new ConcurrentHashMap<>();

	public FragmentedUdpSwimNetwork(SwimNetworkConfig swimNetworkConfig, ObjectMapper objectMapper) {
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
		byte[] buffer = new byte[swimNetworkConfig.getBufferSize()];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while(running) {
			try {
				//TODO implement fragmented packets so that arbitrary data can be sent and received
				swimSocket.receive(packet);
				SocketAddress senderAddress = packet.getSocketAddress();
				ByteBuffer dataBuffer = ByteBuffer.wrap(packet.getData());
				byte type = dataBuffer.get();
				int length = dataBuffer.getInt();
				byte[] data = new byte[length];
				dataBuffer.get(5, data);
				Queue<byte[]> receiveQueue = receiveQueues.computeIfAbsent(senderAddress, k -> new LinkedBlockingQueue<>());
				receiveQueue.add(data);
				if(type == (byte)'e') {
					
				}
				SwimMessage swimMessage = objectMapper.readerFor(SwimMessage.class)
						.readValue(packet.getData());
				LOG.debug("Received message: {}", swimMessage);
				receivedMessages.add(swimMessage);
			} catch (IOException e) {
				LOG.error("An error occurred while receiving UDP packets", e);
			}
		}
	}
	
//	private byte[] buildMessage(SocketAddress sender) {
//		
//	}

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
		List<byte[]> messageFragments = fragmentMessage(swimMessage);
		nodesToGossipTo.forEach(gn -> {
			try {
				LOG.debug("Sending {} to {} ...", swimMessageClassName, gn);
				for(byte[] fragment : messageFragments) {
					swimSocket.send(new DatagramPacket(fragment, fragment.length, gn.getAddress(), gn.getPort()));
				}
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

	private List<byte[]> fragmentMessage(SwimMessage swimMessage) {
		int bufferSize = swimNetworkConfig.getBufferSize();
		byte[] serializedMessage;
		try {
			serializedMessage = objectMapper.writeValueAsBytes(swimMessage);
		} catch (JsonProcessingException e) {
			throw new AutoSwimException(String.format("Could not serialize %s", swimMessage.getClass().getSimpleName()), e);
		}
		// Using type length value protocol and subtracting 5 header bytes
		// type is 1 byte
		// length is 4 bytes allowing messages up to roughly 2GB
		int payloadSize = bufferSize - 5;
		int numberOfPackets = (serializedMessage.length / payloadSize) + 1;
		LOG.debug("Fragmenting message of size {} into {} parts", serializedMessage.length, numberOfPackets);
		List<byte[]> messageFragments = new ArrayList<>(numberOfPackets);

		for(int i = 0; i < numberOfPackets; i++) {
			ByteBuffer messageFragment;
			if(i == numberOfPackets - 1) {
				int remainingMessageSize = serializedMessage.length - ((i+1) * payloadSize);
				messageFragment = ByteBuffer.allocate(remainingMessageSize + 5)
						.put((byte)'e')
						.putInt(remainingMessageSize)
						.put(serializedMessage, i * payloadSize, remainingMessageSize);
			} else {
				messageFragment = ByteBuffer.allocate(bufferSize)
						.put(swimMessage.getMessageType())
						.putInt(payloadSize)
						.put(serializedMessage, i * payloadSize, payloadSize);
			}
			messageFragments.add(messageFragment.array());
		}
		return messageFragments;
	}
}
