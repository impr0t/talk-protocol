package client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import shared.Constants;
import shared.Helpers;
import shared.Message;
import shared.MessageHandler;

public class ClientMessageHandler implements MessageHandler {

	private int responses = 0;
	private ActionHandler context;

	public ClientMessageHandler(ActionHandler context) {
		this.context = context;
	}

	@Override
	public void handle(DatagramSocket socket, DatagramPacket packet) {
		Message message = unpack(packet);
		switch (message.getType()) {
		case Constants.TYPE_ACK:
			handleAck(message, socket, packet);
			break;
		case Constants.TYPE_SEND:
			handleGet(message, socket, packet);
		}
	}
 
	@Override
	public Message unpack(DatagramPacket packet) {
		return Message.fromBytes(packet.getData());
	}

	private void respond(Message message, DatagramSocket socket, DatagramPacket packet) {
		byte[] respBuf = null;
		try {
			respBuf = Message.toBytes(message);
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Could not pack message");
			e1.printStackTrace();
		}

		DatagramPacket rp = new DatagramPacket(respBuf, respBuf.length, packet.getAddress(), packet.getPort());

		try {
			socket.send(rp);
		} catch (IOException e) {
			System.err.println("Could not send packet.");
			e.printStackTrace();
		}
	}

	private void handleAck(Message message, DatagramSocket socket, DatagramPacket packet) {
		String payload = Helpers.getMessageWithoutPreamble(message.getPayload());
		if (payload.equals(Constants.DISCOVERY)) {
			handleDiscovery(message, socket, packet);
		}
	}

	private void handleGet(Message message, DatagramSocket socket, DatagramPacket packet) {
		System.out.println("Received message for: " + message.getDestination());
		if (message.getDestination().equals(context.getIdent())) {
			String strippedPayload = Helpers.getMessageWithoutPreamble(message.getPayload());
			System.out.println(new String(strippedPayload));
			Message response = new Message(Constants.TYPE_ACK, context.getIdent(), "",
					message.getSequence().getBytes());
			respond(response, socket, packet);
		}
	}

	private void handleDiscovery(Message message, DatagramSocket socket, DatagramPacket packet) {
		// since our ack might pretty general we need to analyze the pay load
		// first and do
		// some additional conditional processing.
		if (Helpers.getPreamble(message.getPayload()).equals(Constants.SERVER_PREAMBLE)) {
			context.setServer(packet.getAddress(), message.getSource());
			Message response = new Message(Constants.TYPE_ACK, context.getIdent(), "", new byte[0]);
			respond(response, socket, packet);
		}
	}
}
