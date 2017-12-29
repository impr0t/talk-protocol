package shared;

public class Node {
	
	protected String ident;
	private boolean running = false;
	
	public Node(String ident) {
		this.ident = ident;
	}
	
	public String getIdent() {
		return this.ident;
	}
	
	public void setIdent(String ident) {
		this.ident = ident;
	}
	
	public boolean getRunning() {
		return this.running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void displayStats() {
		System.out.println("Stats:");
	}
	
	public void displayMenu() {
		System.out.println("Menu:");
	}
}
