/*
	Tail.java (part of 'Flight Club')
	
	This code is covered by the GNU General Public License
	detailed at http://www.gnu.org/copyleft/gpl.html
	
	Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
	Dan Burton , Nov 2001 
*/

import java.awt.*;
import java.util.*;
/*
	a tail of length n may be attached to a flying dot
*/

class Tail extends Object3d
{
	int tailLength;		
	Color c;
	private Vector3d[] tail;
	int wireEvery = 4;	//default add a wire for every 5 points
	int updateEvery = 1;
	int moveCount = 0;
	
	public Tail(ModelViewer theApp, int inTailLength, Color inC)
	{
		//only register top level objects with the manager 
		//a tail has a parent who IS registered with the manager
		// and is responsible for drawing and ticking its tail
		//...er no
		super(theApp, true);
		tailLength = inTailLength;
		c = inC;
	}

	public Tail(ModelViewer theApp, int inTailLength, Color inC, int layer)
	{
		/*
			as aboove but add to a specific layer
			eg zero for long jet tails, roads...
		*/
		super(theApp, true, layer);
		tailLength = inTailLength;
		c = inC;
	}

	public void init(Vector3d p)
	{
		tail = new Vector3d[tailLength];
	
		for (int i = 0;i < tailLength;i++)
		{
			tail[i] = new Vector3d(p.x,p.y - (float) i/1000,p.z);
		}
	
		Vector tailWire = new Vector();
		int j = 0;
		for (int i = wireEvery;i < tailLength;i++)
		{
			if (j < 2) tailWire.addElement(tail[i]);
			j ++;
			
			if (j == wireEvery + 1)
			{
				super.addWire(tailWire,c, false);		
				tailWire = new Vector();
				tailWire.addElement(tail[i]);
				j = 1;
			}
		}
	}
	
	public void moveTo(Vector3d newP)
	{
		moveCount ++;

		if (moveCount == updateEvery) {
			moveCount = 0;
			//newP is the current position
			for (int i = 0;i < tailLength - 1;i++)
			{
				int j = tailLength - 1 - i;
				Tools3d.clone(tail[j - 1], tail[j]);
			}
			Tools3d.clone(newP,tail[0]);
		}
	}
	
	public void reset(Vector3d newP)
	{
		/*
			move entire tail to newP (e.g. after glider 
			has landed and we move it to a new position
			to resume play
		*/
		for (int i = 0;i < tailLength - 1;i++) {
			Tools3d.clone(newP, tail[i]);
		}
	}

}