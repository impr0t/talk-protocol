package shared;

public class ThreadBase implements Runnable {

	// base thread object to run.
	private Thread thread;
	
	// the thread base object to fire.
	private ThreadBase context;
	
	// the name of the thread for logging or tracking.
	protected String threadName;
	
	// default constructor.
	public ThreadBase(String threadName) {
		this.threadName = threadName;
	}
	
	// set the thread context.
	protected void setContext(ThreadBase context) {
		this.context = context;
	}
	
	public void start() {
		if (context == null)
			try {
				throw new Exception("Context was not set. failing.");
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		
		if (thread == null) {
			System.out.println("Starting " + this.threadName);
			thread = new Thread(context, threadName);
			thread.start();
		}
	}
	
	@Override
	public void run() {
		// intentionally left blank.
	}
}
