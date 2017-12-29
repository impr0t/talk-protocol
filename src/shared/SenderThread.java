package shared;

import java.net.DatagramSocket;

public class SenderThread extends ThreadBase {

	private DatagramSocket socket;

	public SenderThread(DatagramSocket socket) {
		super(Constants.SENDER_THREAD);
		this.socket = socket;
	}

	@Override
	public void start() {
		setContext(this);
		super.run();
	}

	@Override
	public void run() {
		while (true) {
			// notify
			System.out.println("Waiting to send.....");
		}
	}
}
