package shared;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ListenerThread extends ThreadBase {

	private DatagramSocket socket;
	private Receiver receiver;
	private MessageHandler handler;

	/**
	 * Default constructor.
	 * 
	 * @param socket,
	 *            socket to listen on
	 * @param handler,
	 *            message handler for received messages.
	 */
	public ListenerThread(DatagramSocket socket, MessageHandler handler) {
		super(Constants.LISTENER_THREAD);
		this.socket = socket;
		this.handler = handler;
	}

	@Override
	public void start() {
		setContext(this);
		super.start();
	}

	@Override
	public void run() {
		// notify
		System.out.println("Listening on : " + socket.getLocalAddress() + ":" + socket.getLocalPort());
		while (true) {
			try {
				// set up our receiver here.
				receiver = new Receiver(socket);
				DatagramPacket packet = receiver.Receive();

				// ignore all packets coming from our own socket / address.
				if (!Helpers.getInterfaceAddresses().contains(packet.getAddress())) {
					
					// set some output.
					System.out.println("Received data from : " + packet.getAddress() + ":" + packet.getPort());
					System.out.println("Data length: " + packet.getLength());
					
					// once the message is received
					// handle it!
					handler.handle(socket, packet);
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}
}
