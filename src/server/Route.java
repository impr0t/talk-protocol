package server;

import java.io.Serializable;
import java.net.InetAddress;

public class Route implements Serializable{

	// Constants
	private static final long	serialVersionUID	= -497720710290152459L;
	
	public String clientName;
	public String serverName;
	public InetAddress serverAddress;
	public int cost;
	
	public Route(String clientName, String serverName, InetAddress serverAddress, int cost){
		this.clientName = clientName;
		this.serverName = serverName;
		this.serverAddress = serverAddress;
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "Route [clientName=" + clientName + ", serverName=" + serverName + ", serverAddress=" + serverAddress
				+ ", cost=" + cost + "]";
	}
	
}
