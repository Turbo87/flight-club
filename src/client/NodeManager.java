/*
 * @(#)NodeManager.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.client;

import flightclub.framework3d.*;
/**
   Managers a list of nodes for a task. A task is 'covered' using N nodes
   (cf mobile phone transmitters).
*/
class NodeManager {
    XCModelViewer xcModelViewer;
    Task task; // a quick ref to the task (needed before the top down ref works !)
    Node[] nodes;
    int currentNode=-1; // index of the current node
    float nodeSpacing = Cloud.CLOUDBASE * 12; 
    float nodeRadius = nodeSpacing * 0.6f; // was 0.75
    boolean allLoad = false;

    static final int AWAKE_NODES = 2; // how many nodes are awake at the same time ?

    public NodeManager(XCModelViewer xcModelViewer, Task task) {
	this.xcModelViewer = xcModelViewer;
	this.task = task;
	carveUpTask();
    }

    /**
       Breaks the task down into nodes. We start from the first turn
       point and create a node every distance d along the course until
       we reach the last turn point.

       If a node falls 'near' the next turn point we increase its
       radius and locate it at the turn point.
    */
    private void carveUpTask() {
	float dTotal = task.getTotalDistance();
	int numNodes = (int) Math.floor(dTotal/nodeSpacing) + 1;
	if (dTotal % nodeSpacing != 0) {
	    numNodes++;
	}
	nodes = new Node[numNodes];

	float[] p = new float[3]; 
	TurnPoint tp = task.turnPointManager.turnPoints[0];
	int iTP = 0; // index of turn point
	int next = 0;
	float d = 0; // distance from previous turn point

	// loop around the course creating the nodes
	while (next < numNodes) {
	    tp.getPointOnLine(d, p);
	    nodes[next] = new Node(xcModelViewer, p[0], p[1], nodeRadius, next);
	    nodes[next].turnPoint = tp;
	    next++;
	    d += nodeSpacing;
	    //if (d >= tp.distanceFromStart + tp.distanceToNext && tp.nextTP != null) {
	    if (d >= tp.distanceToNext && tp.nextTP != null) {
		// next turn point 
		d = d - tp.distanceToNext; // was d = 0
		tp = tp.nextTP;
	    }
	}

	/* 
	   Register the triggers with nodes that contain them. Beware
	   ! This nested loop is a potential performance bottleneck.
	*/
	for (int i = 0; i < task.triggers.length; i++) {
	    Trigger trigger = task.triggers[i];
	    for (int j = 0; j < nodes.length; j++) {
		Node node = nodes[j];
		if (node.contains(trigger.x, trigger.y)) {
		    node.addTrigger(trigger);
		}
	    }
	}
    }

    /**
       Loads this node (and the next one or two along the task route). 

       We do the sleeping before the waking so that a trigger that
       occurs on both a sleeping node and a waking node ends up awake.
    */
    void loadNodes(int pNode, float t) {
	if (pNode == currentNode || allLoad) return;
	System.out.println("pNode: " + pNode);

	for (int i = 0; i < nodes.length; i++) {
	    if (i < pNode || i >= pNode + AWAKE_NODES) {
		nodes[i].sleep();
	    } else {
		// wake up - see below loop
	    }
	}

	for (int i = 0; i < nodes.length; i++) {
	    if (i < pNode || i >= pNode + AWAKE_NODES) {
		// sleep - see above loop
	    } else {
		nodes[i].wakeUp(t);
	    }
	}

	currentNode = pNode;
	xcModelViewer.xcModel.task.turnPointManager.renderMe();
	xcModelViewer.xcModel.task.roadManager.renderMe();
    }

    /**
       Load *all* the nodes. Use this for a vector map of the entire task. 

       TODO: do not include clouds in this view (too slow ?).
    */
    void loadAllNodes(boolean flag) {
	float t = xcModelViewer.clock.getTime();
	if (flag) {
	    for (int i = 0; i < nodes.length; i++) {
		nodes[i].wakeUp(t);
	    }
	    xcModelViewer.xcModel.task.turnPointManager.renderMe();
	    xcModelViewer.xcModel.task.roadManager.renderMe();
	    allLoad = true;
	} else {
	    allLoad = false;
	    // toggle current node to force reloading
	    int tmp = currentNode;
	    currentNode = -1;
	    loadNodes(tmp, t);
	}
    }

    /**
       Returns a list of the loaded nodes.
    */
    Node[] loadedNodes() {
	Node[] loadedNodes = new Node[this.countLoadedNodes()];
	int n = 0;
	for (int i = 0; i < nodes.length; i++) {
	    if (nodes[i].mode == Node.AWAKE){ 
		loadedNodes[n++] = nodes[i];
	    }
	}
	return loadedNodes;
    }

    //  does what it says on the tin.
    private int countLoadedNodes() {
	int n = 0;
	for (int i = 0; i< nodes.length; i++) {
	    if (nodes[i].mode == Node.AWAKE){ 
		n++;
	    }
	}
	return n;
    }

    /**
       Returns the index of the node that p is nearest to.
    */
    int nearestNodeIndex(float[] p) {
	float ddMin = 0;
	int j = 0;
	float dx, dy;

	for (int i = 0; i < nodes.length; i++) {
	    Node node = nodes[i];
	    dx = p[0] - node.x;
	    dy = p[1] - node.y;
	    float dd =  dx * dx +  dy * dy;
	    if (dd < ddMin || i == 0) {
		ddMin = dd;
		j = i;
	    }
	}
	return j;
    }

    Node nearestNode(float[] p) {
	return nodes[nearestNodeIndex(p)];
    }

    /**
       Returns the current node
    */
    Node currentNode() { return nodes[currentNode]; }
    Node nextNode() { return nodes[currentNode + 1]; }

    /** Prints debug info. */
    void asString() {
	for (int i = 0; i < nodes.length; i++) {
	    nodes[i].asString();
	}
    }

    /**
       Returns true if p falls within a *loaded* node.

       @see TurnPoint.renderMe(boolean)
    */
    boolean contains(float x, float y) {
	Node[] xs = this.loadedNodes();
	for (int i = 0; i < xs.length; i++) {
	    if (xs[i].contains(x, y)) {
		return true;
	    }
	}
	return false;
    }
}
