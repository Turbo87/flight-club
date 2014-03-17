/**
	Object3d.java (part of 'Flight Club')
	
	This code is covered by the GNU General Public License
	detailed at http://www.gnu.org/copyleft/gpl.html
	
	Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
	Dan Burton , Nov 2001 
*/

import java.awt.*;
import java.util.Vector;

public class Object3d
{
	protected ModelViewer app = null;
	
	float xMin,yMin,xMax,yMax;
	Vector points = new Vector();
 	Vector points_ = new Vector();
	Vector wires = new Vector();
	Vector inFOVs = new Vector(); //list of flags - is point within field of view
	boolean inFOV = false;
	int layer = 1;	//default layer 1

	Object3d(ModelViewer theApp)
	{
		app = theApp;
		registerObject3d();
	}

	Object3d(ModelViewer theApp, boolean register)
	{
		app = theApp;
		if (register) registerObject3d();
	}

	Object3d(ModelViewer theApp, boolean register, int inLayer)
	{
		app = theApp;
		layer = inLayer;
		if (register) registerObject3d();
	}

	public void destroyMe(){app.obj3dManager.removeObj(this, layer);}
	
	void registerObject3d() {app.obj3dManager.addObj(this, layer);}
	
	public void draw(Graphics g)
	{
		if (!inFOV) return;
		
		for (int i = 0;i < wires.size();i++)
		{
			PolyLine wire = (PolyLine) wires.elementAt(i);
			
			if (!wire.isBackFace())
				wire.draw(g);
		}
	}
	
	//use camera to get from 3d to 2d
	void film()
	{
		Vector3d v;
		Vector3d v_;

		inFOV = false; //set true if any points are in FOV
		
		for (int i=0;i<points.size();i++)
		{
			v = (Vector3d) points.elementAt(i);
			v_ = (Vector3d) points_.elementAt(i);
			
			//translate, rotate and project (only if visible)
			Tools3d.subtract(v,app.cameraMan.getFocus(),v_);
			Tools3d.applyTo(app.cameraMan.getMatrix(),v_,v_);
			
			boolean rc = Tools3d.projectYZ(v_,v_,app.cameraMan.getDistance());
			inFOV = inFOV || rc;
			Boolean flag = new Boolean (rc);
			inFOVs.setElementAt(flag, i);
			this.app.cameraMan.scaleToScreen(v_);
		}
		
	}
	
	public void translateBy(Vector3d v)
	{
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			Tools3d.add(q,v,q);
		}
	}

	//rotate 90
	public void flipYZ()
	{
		float  tmp;
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			tmp = q.y;
			q.y = q.z;
			q.z = tmp;
		}
	}

	//turn upside down
	public void flipZ()
	{
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			q.z = -q.z;
		}
	}

	//swap left and right
	public void flipY()
	{
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			q.y = -q.y;
		}
	}

	//swap front and back
	public void flipX()
	{
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			q.x = -q.x;
		}
	}
	
	public void scaleBy(Vector3d v)
	{
		for (int i=0;i<points.size();i++)
		{
			Vector3d q = (Vector3d) points.elementAt(i);
			q.x = q.x * v.x;
			q.y = q.y * v.y;
			q.z = q.z * v.z;
		}
	}
	
	public void scaleBy(float s)
	{
		for (int i = 0; i < points.size(); i ++)
		{
			Vector3d v = (Vector3d) points.elementAt(i);
			Tools3d.scaleBy(v,s);
		}
	}

	public void setColor(Color c)
	{
		for (int i = 0;i < wires.size();i++)
		{
			PolyLine wire = (PolyLine) wires.elementAt(i);
			wire.c = c;
		}
	}
	
	protected int addPoint(Vector3d p)
	{
		Vector3d q;
		int index = 0;
		boolean found = false;
		
		for (int i=0;i<points.size();i++)
		{
			q = (Vector3d) points.elementAt(i);
			if ((q.x == p.x) && (q.y == p.y) && (q.z == p.z))
			{ 
				index = i;
				found = true;
				break;
			}
		}
		
		if (found)
			return index;
		else
		{
			points.addElement(p);
			points_.addElement(new Vector3d());
			inFOVs.addElement(new Boolean(false));
			return points.size() - 1;
		}
	}

	public int addWire(Vector wirePoints,Color c)
	{
		//default - solid and has normal (ie only one side of surface is visible)
		return addWire(wirePoints,c,true, true);
	}

	public int addWire(Vector wirePoints,Color c,boolean isSolid)
	{
		//default has normal (ie only one side of surface is visible)
		return addWire(wirePoints,c,isSolid, true);
	}
	
	public int addWire(Vector wirePoints,Color c,boolean isSolid, boolean hasNormal)
	{
		PolyLine wire;
		if (isSolid)
		{
			Surface wireTmp = new Surface(this, wirePoints.size(),c);
			wire = (PolyLine) wireTmp;
		}
		else
			wire = new PolyLine(this, wirePoints.size(),c);
		
		int pointIndex = 0;
		
		for (int i = 0; i < wirePoints.size(); i++)
		{
			pointIndex = this.addPoint((Vector3d) wirePoints.elementAt(i));
			wire.addPoint(pointIndex);
		}
		
		if (hasNormal)
			wire.setNormal();
		
		wires.addElement(wire);
		return wires.size() - 1;
	}

	public void addTile(Vector3d[] corners,Color c,boolean isSolid, boolean isConcave) {
		/*
			special case of the above - we are passed four
			points and make two triangles to tessalate the 
			surface defined by these four points - diagonal
			may join either corners 0 and 2 or 1 and 3; this 
			makes tile either concave or convex.
		*/
		
		Vector wire1 = new Vector();
		Vector wire2 = new Vector();
		
		float h1 = corners[0].z + corners[2].z;
		float h2 = corners[1].z + corners[3].z;
		
		if (h1 < h2 && isConcave || h1 > h2 && !isConcave) {
			
			wire1.addElement(corners[0]);
			wire1.addElement(corners[1]);
			wire1.addElement(corners[2]);
			wire1.addElement(corners[0]);
			
			wire2.addElement(corners[2]);
			wire2.addElement(corners[3]);
			wire2.addElement(corners[0]);
			wire2.addElement(corners[2]);
			
		} else {

			wire1.addElement(corners[0]);
			wire1.addElement(corners[1]);
			wire1.addElement(corners[3]);
			wire1.addElement(corners[0]);
			
			wire2.addElement(corners[2]);
			wire2.addElement(corners[3]);
			wire2.addElement(corners[1]);
			wire2.addElement(corners[2]);
		
		}
		
		addWire(wire1,c,isSolid, true);
		addWire(wire2,c,isSolid, true);
	}
	
	public static void clone(Object3d from, Object3d to)
	{
		for (int i = 0;i<from.wires.size();i++)
		{
			PolyLine fromWire = (PolyLine) from.wires.elementAt(i);
			Vector toWire = new Vector();
			for (int j = 0;j<fromWire.points.length;j++)
			{
				 int k = fromWire.points[j];
				 Vector3d v = (Vector3d) from.points.elementAt(k);
				 Vector3d q = new Vector3d(v.x,v.y,v.z);
				 toWire.addElement(q);
			}
			boolean hasNorm =  (fromWire.normal!=null);
			to.addWire(toWire, fromWire.c, fromWire.isSolid, hasNorm);	
		}
	}
	
	public void project_(Vector3d f, float[][] m, float distance)
	{
		Vector3d v;
		Vector3d v_;
		
		for (int i=0;i<points.size();i++)
		{
			v = (Vector3d) points.elementAt(i);
			v_ = (Vector3d) points_.elementAt(i);
			
			//translate, rotate and project (only if visible)
			Tools3d.subtract(v,f,v_);
			Tools3d.applyTo(m,v_,v_);
			Tools3d.projectYZ(v_,v_,distance);
		}
	}

	public void timeStep()
	{
		//abstract method for flapping wings etc.
	}

/*
	public void sort(Vector3d p)
	{
		for (int i = 1;i < wires.size() - 1;i++)
		{
			for (int j = i + 1;j < wires.size();j++)
			{
				sortPair(i,j,p);
			}
		}
	}
					 
	private void sortPair(int i,int j,Vector3d p)
	{
		Vector3d p1,p1_;
		Vector3d p2,p2_;
		float d1,d2;
		
		
		PolyLine wire1 = (PolyLine) wires.elementAt(i);
		PolyLine wire2 = (PolyLine) wires.elementAt(j);
		
		if (wire1.numPoints <= 1 || wire2.numPoints <= 1) return;
		
		p1_ = new Vector3d();
		p2_ = new Vector3d();
		p1 = (Vector3d) points_.elementAt(wire1.points[0]);
		p2 = (Vector3d) points_.elementAt(wire2.points[0]);
		
		Tools3d.subtract(p1,p,p1_);
		Tools3d.subtract(p2,p,p2_);
		
		d1 = Tools3d.length(p1_);
		d2 = Tools3d.length(p2_);
		
		//we want furthest surface first
		if (p1.x <= p2.x)
			return;
		else
		{
			wires.setElementAt(wire2,i);
			wires.setElementAt(wire1,j);
		}
	}
	*/
	
	void reverse()
	{
		for (int i=0; i<wires.size()/2 - 1; i++)
		{
			int j = wires.size() - 1 - i;
			PolyLine wire1 = (PolyLine) wires.elementAt(i);
			PolyLine wire2 = (PolyLine) wires.elementAt(j);
			
			wires.setElementAt(wire2,i);
			wires.setElementAt(wire1,j);
			
		}
	}	
	
}
	
class PolyLine
{
	int numPoints;
	int[] points;
	int nextIndex = 0;
	Object3d object3d;
	Color c;	//true color
	Color c_;	//apparent color
	boolean isSolid = false;
	boolean isVisible = false;
	Vector3d normal;

	public PolyLine(Object3d o,int inNumPoints,Color inColor)
	{
		numPoints = inNumPoints;
		points = new int[numPoints];
		object3d = o;
		c = inColor;
		c_ = inColor;
	}

	public void addPoint(int point)
	{
		points[nextIndex] = point;
		nextIndex++;
	}

	boolean isBackFace()
	{
		if (normal==null) return false;
		
		setNormal();	//now ???
		
		Vector3d p = (Vector3d) object3d.points.elementAt(points[0]);
		Vector3d ray = new Vector3d();
		Tools3d.subtract(p,object3d.app.cameraMan.getEye(),ray);
		
		return (Tools3d.dot(normal,ray) >= 0);
	}
	
	void setNormal()
	{
		if (numPoints < 3) return;
		normal = new Vector3d();
		 
		Vector3d[] ps = new Vector3d[3];
		for (int i = 0;i<3;i++)
		   ps[i] = (Vector3d) object3d.points.elementAt(points[i]);
		
		Vector3d e1 = new Vector3d();
		Vector3d e2 = new Vector3d();
		Tools3d.subtract(ps[0],ps[1],e1);
		Tools3d.subtract(ps[2],ps[1],e2);
		 
		Tools3d.cross(e1,e2,normal);
		Tools3d.makeUnit(normal);
		 
		calcLight();
	}
		
	void calcLight()
	{
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		float light = object3d.app.cameraMan.surfaceLight(normal);
		r *= light;
		g *= light;
		b *= light;
		
		c_ = new Color(r,g,b);
	}
	
	public void draw(Graphics g)
	{
		Vector3d a;
		Vector3d b;

		if (numPoints <= 1) return;
		g.setColor(this.getColor());
		
		for (int i = 0; i < numPoints - 1; i++)
		{
			a = (Vector3d) object3d.points_.elementAt(points[i]);
			b = (Vector3d) object3d.points_.elementAt(points[i+1]);
			
			Boolean inFOV1 = (Boolean) object3d.inFOVs.elementAt(points[i]);
			Boolean inFOV2 = (Boolean) object3d.inFOVs.elementAt(points[i+1]);

			//System.out.println(inFOV1.booleanValue() && inFOV2.booleanValue());

			if (inFOV1.booleanValue() && inFOV2.booleanValue()) {
				g.drawLine((int) a.y, (int) a.z, (int) b.y, (int) b.z);
			}
		}
	}

	Color getColor(){
		//fogging		
		Vector3d p = (Vector3d) object3d.points_.elementAt(points[0]);
		return object3d.app.cameraMan.foggyColor(p.x,c_);		
	}
}

class Surface extends PolyLine
{
	
	int[] xs;
	int[] ys;				   

	public Surface(Object3d o,int inNumPoints,Color inC)
	{
		super(o,inNumPoints,inC);
	
		isSolid = true;
		
		xs = new int[numPoints];
		ys = new int[numPoints];
	}
	
	public void draw(Graphics g)
	{
		Vector3d a;
		Vector3d b;
		boolean ok = true;
		
		if (numPoints <= 1) return;

		g.setColor(super.getColor());
		
		for (int i = 0; i < numPoints ; i++)
		{
			a = (Vector3d) object3d.points_.elementAt(points[i]);
			Boolean inFOV = (Boolean) object3d.inFOVs.elementAt(points[i]);
			ok = ok && inFOV.booleanValue();
			xs[i] = (int) (a.y);
			ys[i] = (int) (a.z);
		}
		if (ok) g.fillPolygon(xs,ys,xs.length);
	}
	
}
