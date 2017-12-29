package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import shared.Constants;
import shared.Helpers;
import shared.ListenerThread;
import shared.Message;
import shared.MessageHandler;
import shared.Node;

public class Server extends Node {
	private ListenerThread listener;
	private ServerMessageHandler handler;

	// links ip's to client names.
	public Map<InetAddress, String> clients;

	// client name and message.
	public Map<String, ArrayList<Message>> messages;
	
	public Map<InetAddress, String> neighbours;

	public DatagramSocket socket;

	// routing table.
	public RoutingTable routingTable;

	public int id = 0;

	public Server(String ident, int port) {
		super(ident);

		clients = new HashMap<InetAddress, String>();
		messages = new HashMap<String, ArrayList<Message>>();
		neighbours = new HashMap<InetAddress, String>();
		routingTable = new RoutingTable();

		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void run() {
		handler = new ServerMessageHandler(this);
		listener = new ListenerThread(socket, handler);
		listener.start();

		// fire a call to update the network topology.
		handler.updateTopology();
	}
}
