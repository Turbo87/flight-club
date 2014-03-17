/*
 * @(#)GliderManager.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
import java.util.*;
import java.io.*;
/**
   This class manages all the gliders (user, networked and AI). Ths
   user's glider is the first in the list.
 */
public class GliderManager implements ClockObserver {
    XCModelViewer xcModelViewer;
    GliderAI[] gliderAIs;
    GliderUser gliderUser = null;
    private int pNode_ = 0;
    private int n = 1; // count as we create gliders (user glider is always zero)

    Glider[] gliderNets; // array of connected users
    int numNet = 0; // how many connected

    static final int MAX_USERS = 15; // Max number of Connected Users
    static final int NUM_TYPES = 3; // how many glider types
    static final String[] typeNames = new String[] {"paraglider", "hang-glider", "sailplane"};

    public GliderManager(XCModelViewer xcModelViewer, int pilotType) {
	this.xcModelViewer = xcModelViewer;
	xcModelViewer.clock.addObserver(this);
	initTypes();
	createUser(pilotType);
	if (xcModelViewer.netFlag) {
	    gliderNets = new Glider[MAX_USERS];
	}
    }

    static GliderType[] types = new GliderType[3]; // array of glider types (pg, hg, sp)

    /** Loads the glider types (reads polar data etc from a text
     * file). */
    private void initTypes() {
	try {
	    types[0] = new GliderType(xcModelViewer, "paraglider", 0);
	    types[1] = new GliderType(xcModelViewer, "hangglider", 1);
	    types[2] = new GliderType(xcModelViewer, "sailplane", 2);
	} catch (IOException e) {
	    System.out.println(e);
	    System.exit(1);
	} 
    }

    int pilotType_ = -1;

    void createUser(int pilotType) {
	if (gliderUser != null) {
	    if (pilotType == pilotType_) {
		// do nothing
		return;
	    } else {
		// destroy old glider before creating another
		gliderUser.destroyMe();
	    }
	}
	gliderUser = new GliderUser(xcModelViewer, types[pilotType], 0);
	pilotType_ = pilotType;
    }

    /**
       Creates the AI gliders. Only used in single player mode.
    */
    protected void createAIs(int x, int y, int z) {
	int[] nums = new int[] {x, y, z};
	gliderAIs = new GliderAI[nums[0] + nums[1] + nums[2]];

	int next = 0;
	for (int type = 0; type < 3; type++) {
	    for (int j = 0; j < nums[type]; j++) {
		gliderAIs[next++] = new GliderAI(xcModelViewer, types[type], n++);
	    }
	}
    }

    /**
       Take off - puts gliders near start point and heading towards
       next turn point.
    */
    void launchUser() { 
	gliderUser.takeOff(); 
	pNode_ = 0; 
    }
    
    void launchAIs() {
	for (int i = 0; i < gliderAIs.length; i++) {
	    gliderAIs[i].takeOff();
	}
	pNode_ = 0; 
    }

    /**
       Sets the vertical air movement for a glider. We search the
       loaded nodes for lift sources (rather than searching the entire
       model.) 
     */
    private void setLift(Glider glider, Node[] nodes) {
	if (glider == null) return;
	float[] p = glider.p;
	LiftSource ls = null;
	for (int i = 0; i < nodes.length; i++) {
	    ls = nodes[i].myLiftSource(p);
	    if (ls != null) {
		break;
	    }
	}
	float lift = 0;

	if (ls != null) {
	    lift = ls.getLift(p);
	}

	// pass message to glider
	glider.air[2] = lift;
    }

    private int nextGaggle = 0;

    /** Returns the glider to watch in camera view #2. As this fn is
     * called we cycle round the gaggle gliders. */
    Glider gaggleGlider() {
	if (xcModelViewer.xcNet != null) {
	    int n = 0;
	    for (int i = 0; i < gliderNets.length; i++) {
		if (gliderNets[i] != null) {
		    if (n++ == nextGaggle%numNet) {
			nextGaggle++;
			return gliderNets[i];
		    }
		}
	    }
	    return null;
	} else if (gliderAIs.length > 0) {
	    return gliderAIs[nextGaggle++%gliderAIs.length];
	} else {
	    return null;
	}
    }

    /** Returns either user or demo glider. */
    Glider theGlider() {
	if (xcModelViewer.xcModel.mode == XCModel.DEMO) {
	    return gaggleGlider();
	} else {
	    return gliderUser;
	}
    }

    private float t_ = 0; // time when loadNodes
    static final float T_INTERVAL = 1.19f; // time between calls to loadNodes()

    /**
       Sets the lift for each glider. Also, every T, load the nodes
       around 'the' glider. If networked then we don't do anything
       until we know from the server what the model time is.
     */
    public void tick(float t, float dt) {
	if (xcModelViewer.xcNet != null && !xcModelViewer.netTimeFlag) {
	    return;
	}
	Node[] nodes = xcModelViewer.xcModel.task.nodeManager.loadedNodes();

	// User
	setLift(gliderUser, nodes);

	// Net gliders or AI gliders 
	if (xcModelViewer.xcNet != null) {
	    for (int i = 0; i < gliderNets.length; i++) {
		if (gliderNets[i] != null) {
		    setLift(gliderNets[i], nodes);
		}
	    }
	} else {
	    for (int i = 0; i < gliderAIs.length; i++) {
		if (gliderAIs[i] != null) {
		    this.setLift(gliderAIs[i], nodes);
		}
	    }
	}

	if ((t - t_) > T_INTERVAL) {
	    this.loadNodes(t);
	}
    }

    /**
       Loads the node(s) that *the* glider is 'nearest' to. 

       Constraint: Load nodes in task seq. Algorithm: pNode = n
       changes to pNode = n + 1 when glider is nearer to node n + 1
       than node n. We need this logic for turnpoints where several
       nodes may overlap.
    */
    private void loadNodes(float t) {
	Glider g = theGlider();
	if (g != null) {
	    NodeManager x = xcModelViewer.xcModel.task.nodeManager;
	    float[] p = g.p;
	    if (pNode_ == x.nodes.length - 1) {
		// last node is already loaded
		return;
	    }
	    Node n1 = x.nodes[pNode_];
	    Node n2 = x.nodes[pNode_ + 1];
	    float d1 = n1.distanceSqd(p[0], p[1]);
	    float d2 = n2.distanceSqd(p[0], p[1]);
	    if (d2 < d1) {
		x.loadNodes(++pNode_, t);
	    }
	    //old...
	    //int pNode = x.nearestNodeIndex(g.p);
	    //x.loadNodes(pNode, t);
	}
	t_ = t;
    }

    /**
       Net stuff from Artem.
    */

    /**
       Adds a new network user with the specified type of glider.
       @see XCGameNetConnector
    */
    void addUser(int index, int type){ 
	gliderNets[index] = new Glider(xcModelViewer, types[type], index);
	netType[index] = type;
	numNet++;
    }

    int[] netType = new int[MAX_USERS];
    /**
       Allows net users to change thier wing type.
    */
    void changeNetType(int index, int pilotType) {
	if (gliderNets[index] != null) {
	    if (pilotType == netType[index]) {
		// do nothing
		return;
	    } else {
		// destroy old glider before creating another
		gliderNets[index].destroyMe();
	    }
	}
	gliderNets[index] = new Glider(xcModelViewer, types[pilotType], index);
	netType[index] = pilotType;
    }

    void launchUser(int index) {
	gliderNets[index].takeOff();
    }

    // todo - not needed ?
    void landUser(int index) {
	gliderNets[index].hitTheSpuds();
    }

    /**
       Sets value of myID to value assigned by the server for user's glider.
    */
    void setMyID(int index){
	gliderNets[index] = null;
	gliderUser.myID = index;
	// now we can get the correct starting position
	gliderUser.takeOff(false);
    }

    void changeUser(int index, String line) { // Change position of User at index
	if (gliderNets[index].landed) {
	    gliderNets[index].takeOff();
	}
	
	StringTokenizer tokens = new StringTokenizer(line,":");
	int what = 0;
	
	while (tokens.hasMoreTokens()) {
	    String token = tokens.nextToken();
	    if (what==0) {
		gliderNets[index].p[0] = Tools3d.parseFloat(token);
	    }
	    if (what==1) {
		gliderNets[index].p[1] = Tools3d.parseFloat(token);
	    }
	    if (what==2) {
		gliderNets[index].p[2] = Tools3d.parseFloat(token);
	    }
	    if (what==3) {
		gliderNets[index].v[0] = Tools3d.parseFloat(token);
	    }
	    if (what==4) {
		gliderNets[index].v[1] = Tools3d.parseFloat(token);
	    }
	    if (what==5) {
		gliderNets[index].setPolar(Tools3d.parseInt(token));
	    }
	    if (what==6) {
		gliderNets[index].nextTurn = Tools3d.parseInt(token); 
	    }
	    what++;
	}
    }

    /**
       Deletes a connected user when user has become disconnected.
    */
    void removeUser(int index) {
	if (index >= 0 && gliderNets[index]!=null) {
	    gliderNets[index].destroyMe();
	    gliderNets[index]=null;
	    numNet--;
	}
    }


}
