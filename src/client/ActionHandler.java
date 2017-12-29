package client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import shared.Constants;
import shared.Helpers;
import shared.ListenerThread;
import shared.Message;

public class ActionHandler {

	private ListenerThread listener;
	private ClientMessageHandler messageHandler;
	private Map<InetAddress, String> servers;
	private Map.Entry<InetAddress, String> myServer;
	private DatagramSocket socket;
	private Client context;

	public ActionHandler(Client context) {
		this.context = context;
		this.socket = context.getSocket();
		this.messageHandler = new ClientMessageHandler(this);
		servers = new HashMap<InetAddress, String>();
	}

	public void discoverServers() {
		// notify
		System.out.println("Starting server discovery process");

		// go through the broadcast addresses and find the servers.
		HashSet<InetAddress> broadcastAddresses = Helpers.getBroadcastAddresses();
		for (InetAddress a : broadcastAddresses) {
			Message m = new Message(Constants.TYPE_ACK, context.getIdent(), "",
					Helpers.setPreamble(Constants.CLIENT_PREAMBLE, Constants.DISCOVERY).getBytes());
			// send a discovery message. (ping)
			sendMessage(a, m);
		}

		// wait for the server list to populate.. we'll wait 1 second.
		System.out.println("Waiting for server list");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.println("Thread problems...");
			e.printStackTrace();
		}
		
		// display the server list, and allow selection.
		myServer = context.serverPrompt(servers);
		
		// once a server is selected, create a registration message.
		Message m = new Message(Constants.TYPE_ACK, context.getIdent(), "",
				Helpers.setPreamble(Constants.CLIENT_PREAMBLE, Constants.REGISTER).getBytes());
		
		// send the message.
		handle(m);
	}

	public void handle(Message m) {
		sendMessage(myServer.getKey(), m);
	}

	public String getIdent() {
		return context.getIdent();
	}

	public void setServer(InetAddress address, String name) {
		System.out.println("Found Server: " + address.toString());
		if (!servers.containsKey(address)) {
			servers.put(address, name);
		}
	}

	private void sendMessage(InetAddress address, Message m) {
		System.out.println("About to send " + m.toString());
		// start a listener thread to listen for responses from this request.
		if (listener == null) {
			listener = new ListenerThread(this.socket, messageHandler);
			listener.start();
		}

		byte[] mBytes;
		try {
			mBytes = Message.toBytes(m);
			DatagramPacket packet = new DatagramPacket(mBytes, mBytes.length, address, socket.getLocalPort());
			socket.send(packet);
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
