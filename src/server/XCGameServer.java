import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
/**
   This class implements the game server. When a client does something
   her state is relayed to all the other clients. When clients
   join/leave the game all the clients are notified. A heartbeat is
   sent to all the clients every N seconds. Clients are told the model
   time when they join the game.
*/
public class XCGameServer {
    private static final int SERVER_PORT = 7777;
    private static final int MAX_CLIENTS = 15;
    private static ServerSocket listenSocket = null;
    private static boolean keepRunning = true;
    private  static XCHandler[] XCClient;
    private  static Thread[] XCClientThreads;
    private static int lastXCClient;
    static XCClock clock = null;
    private static PrintWriter log = null;

    // static final String BASE_DIR = "/home/dan/xc/class/"; // for log file
    static final String BASE_DIR = "/usr/local/jserv/logs/"; 

    public static void main(String[] args) {
	XCGameServer server = new XCGameServer();
	server.serveClients();
    }

    public XCGameServer() {
	lastXCClient = 0;
	XCClient = new XCHandler[MAX_CLIENTS];
	XCClientThreads = new Thread[MAX_CLIENTS];
	openLog();

	try {
	    listenSocket = new ServerSocket(SERVER_PORT, MAX_CLIENTS);
	    log("XC game server started: " + new Date());
	    log("On port: " + listenSocket.getLocalPort());
	} catch(IOException excpt) {
	    log("Unable to listen on port " + SERVER_PORT + ": " + excpt);
	    System.exit(1);
	}

	// a clock which sends a heartbeat to the clients
	clock = new XCClock(0);
	clock.start();
    }

    public void serveClients() {
	Socket clientSocket = null;
	try {
	    while(keepRunning) {
		clientSocket = listenSocket.accept();
		int i = ConnectXCClient(clientSocket);
		log("New user "+ i +" connected");
	    }
	} catch(IOException except) {
	    log("Failed I/O: "+ except);
	} finally {
	    try {
		listenSocket.close();
		clock.stop();
	    } catch (Exception e) {}
	    closeLog();
	    System.out.println("Goodbye from XC server !");
	    System.exit(0);
	}
    }

    public static void disconnectXCClient(int myID) {
	if (XCClient[myID]!=null) {
            XCClient[myID] = null;
            XCClientThreads[myID] = null;
	}
    }

    public static int ConnectXCClient(Socket clientSocket) {
	for (int index = 0;index <MAX_CLIENTS;index++) {
	    if (XCClient[index] == null) {
		XCClient[index] = new XCHandler(clientSocket,index);
		XCClientThreads[index] = new Thread(XCClient[index]);
		XCClientThreads[index].start();
		return index;
	    }
	}
	return -1;
    }

    public static void sendToAll(int from, String Message) {
	for (int index = 0;index <MAX_CLIENTS;index++) {
	    if (XCClient[index] != null && index != from) {
		XCClient[index].send(from+"> "+ Message) ;
	    }
	}
    }

    public static void sendTime(float time) {
	String msg = "Time: " + time;
	for (int index = 0;index <MAX_CLIENTS;index++) {
	    if (XCClient[index] != null) {
		XCClient[index].send(msg) ;
	    }
	}
    }

    public static void disconnectAllXCClient() {
	for (int index = 0;index <MAX_CLIENTS;index++) {
	    disconnectXCClient(index);
	}
    }

    public static void sendWelcomeMessage(int from) {
	for (int index = 0;index <MAX_CLIENTS;index++) {
	    if (XCClient[index] != null && index != from) {
		XCClient[from].send("+" + index + "> Connected: " + XCClient[index].gliderType);

		if (XCClient[index].lastSended.length() > 0) {
		    XCClient[from].send(index+"> "+XCClient[index].lastSended);
		}
	    }
	}
    }

    protected static void stop() {
	try {
	    listenSocket.close();
	} catch (Exception e) {}
    }

    public static boolean getKeepRunning() { return keepRunning; }

    /**
       Writes to log file and prints to console (whilst developing ?).
    */
    static void log(String msg) {
	System.out.println(msg); 
	if (log != null) {
	    log.println(new Date() + "-> " + msg);
	    log.flush();
	}
    }

    static void openLog() {
	String file = BASE_DIR + "xc_log.txt";
	try {
	    log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
	} catch (Exception e) {
	    System.out.println("Unable to open log: " + e);
	    System.out.println("Server *will* continue without any logging.");
	}
    }

    static void closeLog() {
	log("XC game server stopped: " + new Date());
	if (log != null) {
	    log.close();
	}
    }
}

class XCHandler implements Runnable {
    private Socket mySocket = null;
    private PrintWriter clientSend = null;
    private BufferedReader clientReceive = null;
    private int myID = -1;
    public String lastSended = "";
    public int gliderType = 1; // default to 1 until user takes off

    public XCHandler (Socket newSocket,int lastXCClient) {
	mySocket = newSocket;
	myID = lastXCClient;
    }

    public void send(String Message) {
	clientSend.println(Message);
	clientSend.flush();
    }

    public void run() {
	String nextLine;
	try {
	    clientSend = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
	    clientReceive = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

	    clientSend.println("+Hello: "+myID);
	    clientSend.flush();

	    clientSend.println("+Time: "+ XCGameServer.clock.getModelTime());
	    clientSend.flush();

	    XCGameServer.sendWelcomeMessage(myID);
	    XCGameServer.sendToAll(myID, "Connected: " + gliderType);

	    while((nextLine = clientReceive.readLine()) != null) {
		nextLine = nextLine.toUpperCase();

		if (!XCGameServer.getKeepRunning()) {
		    break;
		} else if (nextLine.indexOf("QUIT") == 0) {
		    break;
		} else if (nextLine.indexOf("TEST" ) == 0) {
		    clientSend.println("+OK ");
		    clientSend.flush();
		} else if (nextLine.indexOf("KILLALL") == 0) {
		    XCGameServer.disconnectAllXCClient();
		} else if (nextLine.indexOf("LAUNCH") == 0) {
		    String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
		    gliderType = parseInt(tmp);
		    XCGameServer.sendToAll(myID, nextLine);
		} else if (nextLine.indexOf("WARM_FRONT") == 0) { // secret code to shut down the server
		    XCGameServer.log("User " + myID + ": warm_front => closing server !");
		    XCGameServer.disconnectAllXCClient();
		    XCGameServer.stop();
		} else {
		    XCGameServer.sendToAll(myID, nextLine);
		}
		lastSended = nextLine;
	    }
	    clientSend.println("+BYE");
	    clientSend.flush();

	    XCGameServer.sendToAll(myID,"UnConnected");
	    XCGameServer.disconnectXCClient(myID);
	    XCGameServer.log("User " + myID + " disconnected");
	} catch(IOException excpt) {
	    if (excpt.getMessage().indexOf("Connection reset by peer:")==0) {
		XCGameServer.sendToAll(myID,"UnConnected");
		XCGameServer.disconnectXCClient(myID);
		XCGameServer.log("User Disconnected");
	    } else {
		XCGameServer.log("Failed I/O: " + excpt);
	    }
	} finally {
	    try {
		if (clientSend !=null) clientSend.close();
		if (clientReceive !=null) clientReceive.close();
		if (mySocket !=null) mySocket.close();
	    } catch(IOException excpt) {
		XCGameServer.log("Failed I/O: " + excpt);
	    }
	}
    }

    public final static int parseInt(String s) {
	return Integer.valueOf(s).intValue();
    }

}

/**
   A clock which defines model time.
*/
class XCClock implements Runnable {
    Thread ticker = null;
    float modelTime;
    float t0;
    long startTick, sleepTime;

    static final int TICK_LEN = 30; // how many seconds between heartbeats to clients
    static final int DAY_LEN = 60 * 60 * 24; // how many seconds in the loop
    static final int LOG_BEAT = 60 * 60; // every hour

    public XCClock(float modelTime) { 
	this.t0 = modelTime;
    }

    public void start(){
	if (ticker == null) {
	    ticker = new Thread(this);
	    ticker.setPriority(Thread.MIN_PRIORITY);
	}
	startTick = System.currentTimeMillis();
	ticker.start();
    }
	
    public void stop(){
	if (ticker != null) {
	    //ticker.destroy(); no such method ? anything here
	    ticker = null;
	}
    }

    private float lastLog = 0;
    /**
       Called when a glider connects to server. We round to 1/10th
       second.
    */
    public float getModelTime() {
	long currentTick = System.currentTimeMillis();
	float t = (currentTick - startTick)/1000f + t0;
	t = t % DAY_LEN;
	t = Math.round(t * 10)/10;

	// log
	if (t - lastLog > LOG_BEAT|| t < lastLog) {
	    XCGameServer.log("t = " + t);
	    lastLog = t;
	}
	return t;
    }

    public void run(){
	while (ticker != null) {
	    long currentTick = System.currentTimeMillis();

	    XCGameServer.sendTime(getModelTime());
			
	    long now = System.currentTimeMillis();
	    long timeLeft = TICK_LEN * 1000 + currentTick - now;

	    // idle for a bit
	    if (timeLeft > 0) {
		try {Thread.sleep(timeLeft);}
		catch (InterruptedException e) {}
	    }
	}
	ticker = null;
    }
}
