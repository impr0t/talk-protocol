package shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Receiver {

	// socket to use for the receiver.
	private DatagramSocket socket;
	
	// default constructor.
	public Receiver(DatagramSocket socket) throws SocketException {
		// initialize our socket using the port provided..
		// this will be handed to the application on startup.
		this.socket = socket;
	}
	
	public DatagramPacket Receive() {
		// receiver buffer, able to accept 1024 bytes (arbitrary)
		byte[] buffer = new byte[1024];
		
		// hand off the reception to a private receive method.
		DatagramPacket packet = receive(buffer);
		
		// convert the received data to a message from the receiver.
		Message m = Message.fromBytes(packet.getData());
				
		return packet;
	}
	
	private DatagramPacket receive(byte[] buffer) {
		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			return packet;
		} catch (IOException e) {
			System.out.println("Server has failed. There was a problem receiving data.");
			e.printStackTrace();
			return null;
		}
	}
	
}
