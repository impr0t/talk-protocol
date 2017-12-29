package shared;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

public class Helpers {
	public static void clearScreen() throws IOException {
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			Runtime.getRuntime().exec("cls");
		} else {
			Runtime.getRuntime().exec("clear");
		}
	}

	public static HashSet<InetAddress> getBroadcastAddresses() {

		// storage
		HashSet<InetAddress> broadCastAddresses = new HashSet<InetAddress>();
		Enumeration<NetworkInterface> interfaces;

		try {
			// get the available interfaces.
			interfaces = NetworkInterface.getNetworkInterfaces();

			// iterate through all of our interfaces
			// if they aren't loop back, and they're up
			// we'll try to grab a broadcast address from them.
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();

				if (iface == null)
					continue;

				if (!iface.isLoopback() && iface.isUp()) {
					Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress address = it.next();
						if (address == null)
							continue;
						InetAddress broadcast = address.getBroadcast();
						if (broadcast != null) {
							broadCastAddresses.add(broadcast);
						}
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println("Cannot get network interfaces.");
			ex.printStackTrace();
		}
		return broadCastAddresses;
	}

	public static HashSet<InetAddress> getInterfaceAddresses() {
		// storage
		HashSet<InetAddress> interfaceAddresses = new HashSet<InetAddress>();
		Enumeration<NetworkInterface> interfaces;

		try {
			// get the available interfaces.
			interfaces = NetworkInterface.getNetworkInterfaces();

			// iterate through all of our interfaces
			// if they aren't loop back, and they're up
			// we'll try to grab a broadcast address from them.
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();

				if (iface == null)
					continue;

				if (!iface.isLoopback() && iface.isUp()) {
					Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
					while (it.hasNext()) {
						InterfaceAddress address = it.next();
						if (address == null)
							continue;
						interfaceAddresses.add(address.getAddress());
					}
				}
			}
		} catch (SocketException ex) {
			System.err.println("Cannot get network interfaces.");
			ex.printStackTrace();
		}
		return interfaceAddresses;
	}

	public static InetAddress myIP() {
		InetAddress result = null;
		for (InetAddress address : getInterfaceAddresses()) {
			result = address;
		}
		return result;
	}

	public static String setPreamble(String preamble, String message) {
		return preamble + message;
	}
	
	public static byte[] setBytePreamble(String preamble, byte[] content) {
		byte[] p = preamble.getBytes();
		byte[] result = new byte[p.length + content.length];
		System.arraycopy(p, 0, result, 0, p.length);
		System.arraycopy(content, 0, result, p.length, content.length);
		return result;
	}

	public static String getPreamble(byte[] payload) {
		byte[] preamble = new byte[1];
		preamble[0] = payload[0];
		return new String(preamble);
	}

	public static String getMessageWithoutPreamble(byte[] payload) {
		byte[] message = new byte[payload.length];
		System.arraycopy(payload, 1, message, 0, payload.length - 1);
		return new String(message).trim();
	}
	
	public static byte[] getPayloadWithoutPreamble(byte[] payload) {
		byte[] result = new byte[payload.length];
		System.arraycopy(payload, 1, result, 0, payload.length -1);
		return result;
	}
}
