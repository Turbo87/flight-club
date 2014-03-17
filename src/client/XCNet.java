package flightclub.client;

import java.io.*;
import java.net.*;
import flightclub.framework3d.Tools3d;
/**
   This class implements communication with the game server.
*/
public class XCNet implements Runnable {
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Socket mySocket = null;
    String host;
    int port;
    Thread thread = null; // Thread where will run connector
    XCModelViewer xcModelViewer;

    public XCNet (XCModelViewer xcModelViewer) throws IOException {
	this.xcModelViewer = xcModelViewer;
	parseHostPort(); //read server info from the environment
	mySocket = new Socket(host, port);
	// ? mySocket.setTcpNoDelay(true);
    }

    private void parseHostPort() {
	String hp = xcModelViewer.modelEnv.getHostPort();
	int i = hp.indexOf(":");
	host = hp.substring(0, i);
	port = Tools3d.parseInt(hp.substring(i + 1));
    }

    public void send(String s) {
	if (out!=null) {
            out.println(s);
            out.flush();
	}
	//System.out.println(s);//debug
    }

    public void start(){
	if (thread == null) {
	    thread = new Thread(this);
	}
	thread.start();
    }
	
    public void stop(){
	if(thread != null) {
            //try {
	    //thread.join(); hangs ?
	    //} catch (InterruptedException ignored) {}
	    thread = null;
	}
    }

    public void run() {
	String nextLine;
	try {
	    out = new PrintWriter(new OutputStreamWriter(mySocket.getOutputStream()));
	    in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

	    GliderManager gliderManager = xcModelViewer.xcModel.gliderManager;

	    while ((nextLine = in.readLine())!=null) {
		nextLine = nextLine.toUpperCase();
		//System.out.println("IN: " + nextLine); //debug

		if (nextLine.indexOf("TIME:")==0){ // what's the model time
		    String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
		    float t = Tools3d.parseFloat(tmp);
		    xcModelViewer.clock.synchTime(t);
		} 

		if (nextLine.indexOf("+")==0) { // server.sendWelcomeMessage
		    //String cmdLine = nextLine.substring(4,nextLine.length());

		    if (nextLine.indexOf("+HELLO:")==0){ // what player id am i ?
			String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
			int myID = Tools3d.parseInt(tmp);
			gliderManager.setMyID(myID);
		    } 

		    // first time we have model time
		    if (nextLine.indexOf("+TIME:")==0){ // what's the model time
			String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
			float t = Tools3d.parseFloat(tmp);
			xcModelViewer.clock.synchTime(t);
			NodeManager x = xcModelViewer.xcModel.task.nodeManager;
			x.loadNodes(0, t);
			xcModelViewer.netTimeFlag = true;
		    } 

		    if (nextLine.indexOf("CONNECTED")>0) {
			String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
			int wingType = Tools3d.parseInt(tmp);
			gliderManager.addUser(parseId(nextLine), wingType);
		    }

		    /**
		    if (cmdLine.indexOf("LAUNCHED")==0) {
			int from = parseId(nextLine);
			gliderManager.addUser(from);
			gliderManager.launchUser(from);
		    }

		    if (cmdLine.indexOf("LANDED")==0) {
			gliderManager.addUser(parseId(nextLine));
		    }
			
		    if (cmdLine.indexOf("#")==0) {
			int from = parseId(nextLine);
			gliderManager.addUser(from);
			gliderManager.launchUser(from);
			gliderManager.changeUser(from,cmdLine.substring(1,cmdLine.length()));
		    }
			
		    if (cmdLine.indexOf("UNCONNECTED")==0) {
			gliderManager.removeUser(parseId(nextLine));
		    }
		    */

		} else { // server.sendToAll
		    String cmdLine  = nextLine.substring(3,nextLine.length()); // todo: > 9 !
		    if (cmdLine.indexOf("CONNECTED")==0) {
			String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
			int wingType = Tools3d.parseInt(tmp);
			gliderManager.addUser(parseId2(nextLine), wingType);
		    }
		    
		    if (cmdLine.indexOf("UNCONNECTED")==0) {
			gliderManager.removeUser(parseId2(nextLine));
		    }

		    if (cmdLine.indexOf("LAUNCHED")==0) {
			String tmp = nextLine.substring(nextLine.indexOf(":")+2 ,nextLine.length());
			int wingType = Tools3d.parseInt(tmp);
			int id = parseId2(nextLine);
			gliderManager.changeNetType(id, wingType);
			gliderManager.launchUser(id);
		    }

		    if (cmdLine.indexOf("LANDED")==0) {
			gliderManager.landUser(parseId2(nextLine));
		    }

		    if (cmdLine.indexOf("#")==0) {
			gliderManager.changeUser(parseId2(nextLine),cmdLine.substring(1,cmdLine.length()));
		    }
		    
		}
	    }
	    out.println("QUIT");
	} catch(IOException excpt) {
	    System.out.println("Failed I/O: " + excpt);
	} finally {
	    System.out.println("XCNet - final clean up");
	    try {
		if (out!=null) out.println("QUIT");
		if (out !=null) out.close();
		if (in !=null) in.close();
		if (mySocket !=null) mySocket.close();
	    } catch(IOException excpt) {
		System.out.println("Failed I/O: " + excpt);
	    }
	}
    }

    /**
       Returns the glider id which appears before '>' in messages from
       the server. We ignore the leading '+' (which appears in in
       welcome messages).

       +10> CONNECTED
    */
    private int parseId(String msg) {
	return Tools3d.parseInt(msg.substring(1, msg.indexOf(">")));
    }

    /**
       Returns the glider id which appears before '>' in messages from
       the server.

       10> CONNECTED
    */
    private int parseId2(String msg) {
	return Tools3d.parseInt(msg.substring(0, msg.indexOf(">")));
    }

    void destroyMe() {
	if (mySocket != null) {
	    System.out.println("Closing socket !");
	    try {
		mySocket.close();
	    } catch (Exception e) {}
	}
    }
}
