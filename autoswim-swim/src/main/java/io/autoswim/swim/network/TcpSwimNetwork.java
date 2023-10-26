//package io.autoswim.swim.network;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import io.autoswim.AutoSwimException;
//import io.autoswim.swim.messages.SwimMessage;
//import io.autoswim.types.Endpoint;
//
//public class TcpSwimNetwork implements SwimNetwork {
//	private static final Logger LOG = LoggerFactory.getLogger(TcpSwimNetwork.class);
//	
//	private final SwimNetworkConfig swimNetworkConfig;
//	private final ObjectMapper objectMapper;
//	private final Random random;
//	private final ServerSocket swimSocket;
//	private final BlockingQueue<SwimMessage> receivedMessages = new LinkedBlockingQueue<>();
//	private boolean running = false;
//	private Thread acceptThread;
//	private Map<InetAddress, Socket> socketMap = new ConcurrentHashMap<>();
//	
//	public TcpSwimNetwork(SwimNetworkConfig swimNetworkConfig, ObjectMapper objectMapper) {
//		this.swimNetworkConfig = swimNetworkConfig;
//		this.objectMapper = objectMapper;
//		this.random = new Random();
//		String bindHost = swimNetworkConfig.getBindHost();
//		int swimPort = swimNetworkConfig.getSwimPort();
//		try {
//			swimSocket = new ServerSocket();
//			swimSocket.setReuseAddress(true);
//			swimSocket.bind(new InetSocketAddress(bindHost, swimPort));
//		} catch (IOException e) {
//			throw new AutoSwimException(String.format("Could not set up tcp socket at %s:%s", bindHost, swimPort), e);
//		}
//	}
//	
//	@Override
//	public void start() {
//		running = true;
//		acceptThread = new Thread(() -> acceptClients());
//		acceptThread
//		.start();
//	}
//
//	@Override
//	public void stop() {
//		running = false;
//		acceptThread.interrupt();
//	}
//	
//	private void acceptClients() {
//		byte[] buffer = new byte[swimNetworkConfig.getBufferSize()];
//		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//		while(running) {
//			Socket swimClient = swimSocket.accept();
//			socketMap.put(swimClient.getInetAddress(), swimClient);
//			try {
//				//TODO implement fragmented packets so that arbitrary data can be sent and received
//				swimSocket.receive(packet);
//				SwimMessage swimMessage = objectMapper.readerFor(SwimMessage.class)
//				.readValue(packet.getData());
//				LOG.debug("Received message: {}", swimMessage);
//				receivedMessages.add(swimMessage);
//			} catch (IOException e) {
//				LOG.error("An error occurred while receiving UDP packets", e);
//			}
//		}
//	}
//	
//	private void receive(Socket socket) {
//		while(true) {
//			
//		}
//	}
//
//	@Override
//	public SwimMessage receiveMessage() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void sendMessage(SwimMessage swimMessage, Set<Endpoint> aliveNodes) {
//		// TODO Auto-generated method stub
//
//	}
//
//}
