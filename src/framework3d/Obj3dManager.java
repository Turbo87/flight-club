/*
  @(#)Obj3dManager.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.awt.*;
import java.util.*;

/**
   This class manages a list of 3d objects - the model. 
*/
public class Obj3dManager
{
    protected ModelViewer modelViewer;
    private Vector list;
	
    Obj3dManager(ModelViewer modelViewer) {
	this.modelViewer = modelViewer;
	list = new Vector();
    }
	
    public void addObj(Obj3d o) { list.addElement(o); }
    public void removeObj(Obj3d o) { list.removeElement(o); }
    public void removeAll() { list.removeAllElements(); }
    public Obj3d obj(int i) { return (Obj3d) list.elementAt(i); }
    public int size() { return list.size(); }
	
    /**
       Sorts so that the furthest away obj is first in the list. We
       use the depth bounding box x_min -> x_max and take its mid
       point to be THE depth of the object. This is ok for local
       objects.  ***TODO*** advanced zorder to handle terrain, roads
       etc. will be implemented in subclass.  
    */
    public void sortObjects() {
	if (list.size() >= 2) {
	    for (int i = 0;i < list.size() - 1;i++) {
		for (int j = i + 1;j < list.size();j++) {
		    sortPair(i,j);
		}
	    }
	}
    }
	
    void sortPair(int i,int j) {

	Obj3d o1 = (Obj3d) list.elementAt(i);
	Obj3d o2 = (Obj3d) list.elementAt(j);

	if ((o1.getDepthMin() + o1.getDepthMax())/2 <= (o2.getDepthMin() + o2.getDepthMax())/2) {
	    //second point closer than first
	    return;
	} else {
	    list.setElementAt(o2,i);
	    list.setElementAt(o1,j);
	}
    }
}
