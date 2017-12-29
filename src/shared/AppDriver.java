package shared;

import java.io.IOException;

import server.Server;
import client.Client;

public class AppDriver {

	private Client client;
	private Server server;

	public AppDriver(String[] args) {
		try {
			String ident = "";
			if (args[0].equals("-c")) {
				ident = args[1];
				int port = Integer.parseInt(args[2]);
				client = new Client(ident, port);
				client.setRunning(true);
				client.run();
			} else if (args[0].equals("-s")) {
				ident = args[1];
				int port = Integer.parseInt(args[2]);
				server = new Server(ident, port);
				server.setRunning(true);
				server.run();
			} else {
				System.out.println("Nothing Started... Check your args!");
			}
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// server can be run with java -jar talk.jar -s <portnumber>
		// client can be run with java -jar talk.jar -c <portnumber>

		if (args.length < 2) {
			System.out.println("Usage: talk [-s|-c] <ident> <portnum>");
			System.out.println("-s : Start a talk server.");
			System.out.println("-c : Start a talk client.");
			return;
		}

		// init the application driver.
		new AppDriver(args);
	}
}
