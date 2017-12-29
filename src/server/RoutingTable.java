package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;


public class RoutingTable {

	ArrayList<Route> routes;
	public RoutingTable(){
		routes = new ArrayList<Route>();
	}
	
	public void addClient(String clientName, String serverName, InetAddress serverAddress, int cost){
		Route route = new Route(clientName, serverName, serverAddress, cost);
		routes.add(route);
	}
	
	/**
	 * Merges/Updates the routing tables
	 * @param byteArray
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean mergeList(byte[] byteArray) throws ClassNotFoundException, IOException{
		boolean updated = false;
		
		ArrayList<Route> routesToMerge = (ArrayList<Route>) deserialize(byteArray);
		for(int i = 0; i < routesToMerge.size(); i++){
			//foundIndex = matched index of the route in routes arrayList of this object 
			int foundIndex = this.contains(routesToMerge.get(i));
			
			// if this object has the route being examined from routesToMerge
			if(foundIndex > -1){
				// if the cost of the matched route is greater than the cost of new route + 1(route to get to server)
				if(routes.get(foundIndex).cost > routesToMerge.get(i).cost + 1){
					//update the cost, address and serverName to the new guy.
					routes.get(foundIndex).cost = routesToMerge.get(i).cost + 1;
					routes.get(foundIndex).serverAddress = routesToMerge.get(i).serverAddress;
					routes.get(foundIndex).serverName = routesToMerge.get(i).serverName;
					updated = true;
				}
			}
			//Else add a new one
			else{
				this.addClient(routesToMerge.get(i).clientName, routesToMerge.get(i).serverName, routesToMerge.get(i).serverAddress, ++routesToMerge.get(i).cost);
				updated = true;
			}
		}
		
		return updated;
	}

	/**
	 * Returns index if found the clientName else returns -1 for not found
	 * @param route
	 * @return
	 */
	private int contains(Route route) {
		int found = -1;
		
		for(int i = 0 ; i < routes.size(); i++){
			if(routes.get(i).clientName.equals(route.clientName))
				found = i;			
		}
		return found;
	}
	
	public Route getRoute(String client) {
		for (Route r : routes) {
			if (r.clientName.equals(client)) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Converts this.routes to byte[]
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException{
		return serialize(this.routes);
	}
	
    public byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }

	@Override
	public String toString() {
		String result = "";
		for(Route r : routes) {
			result += r.toString() + System.lineSeparator();
		}
		return result;
	}	
}
