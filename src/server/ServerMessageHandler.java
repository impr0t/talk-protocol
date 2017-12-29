package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;

import shared.Constants;
import shared.Helpers;
import shared.Message;
import shared.MessageHandler;

public class ServerMessageHandler implements MessageHandler {
	private Server context;

	public ServerMessageHandler(Server context) {
		this.context = context;
	}

	@Override
	public void handle(DatagramSocket socket, DatagramPacket packet) {
		Message received = unpack(packet);
		System.out.println(
				"The message received was: " + received.getType() + " " + new String(received.getPayload()).trim());

		switch (received.getType()) {
		case Constants.TYPE_ACK:
			handleAck(received, socket, packet);
			break;
		case Constants.TYPE_SEND:
			handleSend(received, socket, packet);
			break;
		case Constants.TYPE_GET:
			handleGet(received, socket, packet);
			break;
		}
	}

	@Override
	public Message unpack(DatagramPacket packet) {
		return Message.fromBytes(packet.getData());
	}

	/**
	 * Responds to the node that sent the message.
	 * 
	 * @param message,
	 *            the message.
	 * @param socket,
	 *            the socket.
	 * @param packet,
	 *            the packet.
	 */
	private void respond(Message message, DatagramSocket socket, DatagramPacket packet) {
		byte[] respBuf = null;
		try {
			respBuf = Message.toBytes(message);
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Could not pack message");
			e1.printStackTrace();
		}

		//System.out.println("Sending: " + message.getType() + " " + new String(message.getPayload()));
		System.out.println("Sending: " + message.getType());
		DatagramPacket rp = new DatagramPacket(respBuf, respBuf.length, packet.getAddress(), packet.getPort());

		try {
			socket.send(rp);
		} catch (IOException e) {
			System.err.println("Could not send packet.");
			e.printStackTrace();
		}
	}

	/**
	 * This handles a general ACK packet coming into the server
	 * 
	 * @param message,
	 *            the message.
	 * @param socket,
	 *            the socket.
	 * @param packet,
	 *            the packet.
	 */
	private void handleAck(Message message, DatagramSocket socket, DatagramPacket packet) {
		String strippedPayload = Helpers.getMessageWithoutPreamble(message.getPayload());
		String actualPayload = new String(message.getPayload()).trim();

		if (strippedPayload.equals(Constants.DISCOVERY)) {
			handleDiscovery(message, socket, packet);
		} else if (strippedPayload.equals(Constants.REGISTER)) {
			handleRegistration(message, socket, packet);
		} else if (actualPayload.length() == Constants.MAX_SEQ_LENGTH) {
			// we're just going to nuke a message here based off an ack
			// reception.
			ArrayList<Message> pull = context.messages.get(message.getSource());
			if (pull.isEmpty())
				return;
			else {
				pull.removeIf(m -> m.getSequence().equals(actualPayload));
			}
		} else {
			System.out.println(message.getPayload().length);
			System.out.println("Received Ack.");
		}
	}

	/**
	 * This handles a get request coming into the server.
	 * 
	 * @param message,
	 *            the message sent to the server
	 * @param socket,
	 *            the socket which it came in on.
	 * @param packet,
	 *            the packet which contained the message.
	 */
	private void handleGet(Message message, DatagramSocket socket, DatagramPacket packet) {
		String preamble = Helpers.getPreamble(message.getPayload());

		if (preamble.equals(Constants.CLIENT_PREAMBLE)) {
			// this function is going to send all messages back to the client.
			if (context.messages.containsKey(message.getDestination())) {
				ArrayList<Message> stored = context.messages.get(message.getDestination());
				for (Message m : stored) {
					respond(m, socket, packet);
				}
			}
		} else if (preamble.equals(Constants.SERVER_PREAMBLE)) {
			String idToCompare = Helpers.getMessageWithoutPreamble(message.getPayload());
			int id = Integer.parseInt(idToCompare);

			// we're only replying if our id is the max node -1 or our id is 0.
			// 0 indicates the root node, and -1 indicates prior.
			if (id - 1 == context.id || context.id == 0) {
				
				System.out.println("Sending routing table to: " + message.getSource());
				try {
					Message response = new Message(Constants.TYPE_SEND, context.getIdent(), message.getSource(),
							Helpers.setBytePreamble(Constants.SERVER_PREAMBLE, context.routingTable.toByteArray()));
					respond(response, socket, packet);
				} catch (IOException e) {
					System.err.println("Could not generate message to send...");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This handles a send request coming into the server
	 * 
	 * @param message,
	 *            the message that was sent.
	 * @param socket,
	 *            the socket which it came in on
	 * @param packet,
	 *            the packet which contained the message.
	 */
	private void handleSend(Message message, DatagramSocket socket, DatagramPacket packet) {
		String preamble = Helpers.getPreamble(message.getPayload());

		if (preamble.equals(Constants.CLIENT_PREAMBLE)) {
			// get the route for the message.
			Route route = context.routingTable.getRoute(message.getDestination());
			if (route == null) {
				System.out.println("No route found, dropping message");
				return;
			}

			// if the route points here, sit on it.
			if (route.serverAddress.equals(Helpers.myIP())) {
				// this function is going to store a message from a client.
				if (context.messages.containsKey(message.getDestination())) {
					ArrayList<Message> stored = context.messages.get(message.getDestination());
					stored.add(message);
					context.messages.put(message.getDestination(), stored);
				} else {
					ArrayList<Message> toPut = new ArrayList<Message>();
					toPut.add(message);
					context.messages.put(message.getDestination(), toPut);
				}
				int messages = context.messages.get(message.getDestination()).size();
				System.out.println(
						"This server has " + String.valueOf(messages) + " message(s) for " + message.getDestination());

				Message response = new Message(Constants.TYPE_ACK, context.getIdent(), message.getSource(),
						new byte[0]);
				respond(response, socket, packet);
			} else {
				System.out.println("this server does not have client: " + message.getDestination());
				System.out.println("forwarding the message");

				byte[] mb = null;
				try {
					mb = Message.toBytes(message);
				} catch (UnsupportedEncodingException e) {
					System.err.println("Could not convert message to bytes");
					e.printStackTrace();
				}
				DatagramPacket p = new DatagramPacket(mb, mb.length, route.serverAddress, packet.getPort());

				try {
					socket.send(p);
				} catch (IOException e) {
					System.err.println("Could not send packet");
					e.printStackTrace();
				}
			}
		} else if (preamble.equals(Constants.SERVER_PREAMBLE)) {
			System.out.println("We're being sent something by another server: " + message.getSource());
			byte[] rt = Helpers.getPayloadWithoutPreamble(message.getPayload());
			try {

				System.out.println("Updating the routing table.");

				System.out.println("Routing table was: ");
				System.out.println(context.routingTable.toString());

				context.routingTable.mergeList(rt);

				System.out.println("Routing table is now: ");
				System.out.println(context.routingTable.toString());

				if (!message.getSource().equals(context.getIdent())) {
					Message response = new Message(Constants.TYPE_SEND, message.getSource(), message.getSource(),
							Helpers.setBytePreamble(Constants.SERVER_PREAMBLE, context.routingTable.toByteArray()));

					respond(response, socket, packet);
				}
			} catch (ClassNotFoundException e) {
				System.err.println("There was a problem deserializing the routing table.");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("There was a problem writing the routing table.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This handle client registration with the server.
	 * 
	 * @param message,
	 *            the message for registration.
	 * @param socket,
	 *            the socket it came in on.
	 * @param packet,
	 *            the packet which contained the payload.
	 */
	private void handleRegistration(Message message, DatagramSocket socket, DatagramPacket packet) {
		String preamble = Helpers.getPreamble(message.getPayload());

		// check the preamble.
		if (preamble.equals(Constants.CLIENT_PREAMBLE)) {
			handleClientRegistration(message, socket, packet);
		} else if (preamble.equals(Constants.SERVER_PREAMBLE)) {
			handleServerRegistration(message, socket, packet);
		}
	}

	private void handleClientRegistration(Message message, DatagramSocket socket, DatagramPacket packet) {
		context.clients.put(packet.getAddress(), message.getSource());
		context.routingTable.addClient(message.getSource(), context.getIdent(), Helpers.myIP(), 0);
		Message response = new Message(Constants.TYPE_ACK, context.getIdent(), "",
				Helpers.setPreamble(Constants.SERVER_PREAMBLE, Constants.REGISTER).getBytes());
		respond(response, socket, packet);
		updateTopology();
	}

	private void handleServerRegistration(Message message, DatagramSocket socket, DatagramPacket packet) {
		context.id += 1;
	}

	/**
	 * This handles a discovery ping to the server.
	 * 
	 * @param message,
	 *            the message sent.
	 * @param socket,
	 *            the socket it came in on.
	 * @param packet,
	 *            the packet containing the message.
	 */
	private void handleDiscovery(Message message, DatagramSocket socket, DatagramPacket packet) {
		String preamble = Helpers.getPreamble(message.getPayload());

		if (preamble.equals(Constants.CLIENT_PREAMBLE)) {
			Message response = new Message(Constants.TYPE_ACK, context.getIdent(), "",
					Helpers.setPreamble(Constants.SERVER_PREAMBLE, Constants.DISCOVERY).getBytes());
			respond(response, socket, packet);
		} else if (preamble.equals(Constants.SERVER_PREAMBLE)) {
			Message response = new Message(Constants.TYPE_ACK, context.getIdent(), "",
					Helpers.setPreamble(Constants.SERVER_PREAMBLE, Constants.REGISTER).getBytes());
			respond(response, socket, packet);
		}
	}

	private void sendMessage(InetAddress address, Message message) {
		byte[] mBytes = null;
		try {
			mBytes = Message.toBytes(message);
			DatagramPacket packet = new DatagramPacket(mBytes, mBytes.length, address, context.socket.getLocalPort());
			context.socket.send(packet);
		} catch (IOException e) {
			System.err.println("Could not send packet.");
			e.printStackTrace();
		}
	}

	/**
	 * Starts the network topology update.
	 */
	public void updateTopology() {
		HashSet<InetAddress> broadcastAddresses = Helpers.getBroadcastAddresses();
		for (InetAddress a : broadcastAddresses) {
			Message m = new Message(Constants.TYPE_ACK, context.getIdent(), "",
					Helpers.setPreamble(Constants.SERVER_PREAMBLE, Constants.DISCOVERY).getBytes());

			sendMessage(a, m);

			// sleep and wait.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Error in thread sleep");
				e.printStackTrace();
			}

			// god this is such a hack job.
			System.out.println("I am server " + String.valueOf(context.id) + " on the network");

			// now that we know our place in the network, we try to find our -1
			// and +1. We do this by sending a get to the network with a server
			// preamble.
			// any server listening will see the context id and use it as an
			// indicator of whether
			// or not to send their routing table.
			Message get = new Message(Constants.TYPE_GET, context.getIdent(), "",
					Helpers.setPreamble(Constants.SERVER_PREAMBLE, String.valueOf(context.id)).getBytes());

			sendMessage(a, get);
		}
	}
}
