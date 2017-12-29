package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;

import shared.Constants;
import shared.Helpers;
import shared.Message;
import shared.Node;

public class Client extends Node {

	private DatagramSocket socket;
	private ActionHandler handler = null;
	private InputStreamReader in = new InputStreamReader(System.in);
	private BufferedReader keyboard = new BufferedReader(in);

	public Client(String ident, int port) {
		super(ident);
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void run() throws IOException {
		// set the context of the action handler to this client.
		// this is a pretty bogus implementation due to tight coupling
		// but this can always be refactored later.
		handler = new ActionHandler(this);
		handler.discoverServers();

		while (this.getRunning()) {
			Message m = promptUser();
			handler.handle(m);
		}
	}

	public DatagramSocket getSocket() {
		return this.socket;
	}

	private boolean validateAction(String action) {
		switch (action) {
		case Constants.TYPE_SEND:
		case Constants.TYPE_ACK:
		case Constants.TYPE_GET:
			return true;
		default:
			System.out.println("Invalid action.");
			return false;
		}
	}

	public Map.Entry<InetAddress, String> serverPrompt(Map<InetAddress, String> servers) {
		System.out.println("Server List:");

		int i = 1;
		// iterate through our list of pulled servers

		// this is the most god awful piece of crap code i've ever written.
		ArrayList<Map.Entry<InetAddress, String>> addresses = new ArrayList<Map.Entry<InetAddress, String>>();
		for (Map.Entry<InetAddress, String> entry : servers.entrySet()) {
			System.out.println(String.valueOf(i) + ": " + entry.getValue() + " -- " + entry.getKey());
			addresses.add(entry);
			i++;
		}

		System.out.println("Which server would you like to connect to?");

		String selection = "";
		try {
			selection = keyboard.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int s = Integer.parseInt(selection) - 1;
		return addresses.get(s);
	}

	private Message promptUser() throws IOException {
		// prompt the user.
		System.out.println("Enter 'Send', 'Get' or 'Ack' : ");

		// Read the input line from keyboard
		String action = keyboard.readLine().toUpperCase();

		if (validateAction(action)) {
			if (action.equals(Constants.TYPE_SEND)) {
				System.out.println("Recipient: ");
				String recipient = keyboard.readLine();

				System.out.println("Content: ");
				String content = keyboard.readLine();

				return new Message(action, getIdent(), recipient,
						Helpers.setPreamble(Constants.CLIENT_PREAMBLE, content).getBytes());
			}
			if (action.equals(Constants.TYPE_GET)) {
				return new Message(action, getIdent(), getIdent(),
						Helpers.setPreamble(Constants.CLIENT_PREAMBLE, "").getBytes());
			}
		} else {
			// recursive call due to invalid action.
			promptUser();
		}

		return null;
	}
}
