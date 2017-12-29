package shared;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface MessageHandler {
	void handle(DatagramSocket socket, DatagramPacket packet);
	Message unpack(DatagramPacket packet);
}
