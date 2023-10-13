package io.autoswim.swim;

import java.io.IOException;
import java.net.ServerSocket;

public class SwimTestUtil {
	public static int getFreePort() {
		try(ServerSocket socket = new ServerSocket(0)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new IllegalStateException("Could not get free port", e);
		}
	}
}
