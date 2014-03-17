package flightclub.task;

import java.awt.*;

public class ParameterCanvas extends Canvas {
    Image offscreenImage;
    Graphics offscreenGraphics;
    TaskDesigner taskDesigner;
    int height,width;
    float min,max,value,v;
    boolean dragging=false;
    int mY;
 	
    double yDragOffset;
 	
    public ParameterCanvas(float min, float max, float start, TaskDesigner taskDesigner) {
	this.taskDesigner = taskDesigner;
	this.min = min;
	this.max = max;
	value = start;
    }
	
    public void init() {
	this.setBackground(Color.white);
	Dimension d = size();
	offscreenImage = createImage(d.width,d.height);
	offscreenGraphics = offscreenImage.getGraphics();
		
	height=d.height;
	width=d.width;
		
	updateImage();
    }	
	
    public Dimension minimumSize() 
    {
	return new Dimension(6,100);
    }
	
    public Dimension preferredSize() 
    {
	return minimumSize() ;
    }
    
    public void paint(Graphics g)
    {	
	g.drawImage(offscreenImage,0,0,this);
    }
	
    public void update(Graphics g) 
    {
	paint(g);
    }
    
    public void updateImage()
    {
	offscreenGraphics.clearRect(0,0,width,height);
		
	int y=getY();
	final int R = 2;
	offscreenGraphics.setColor(new Color(170, 170, 170));
	offscreenGraphics.drawLine(width/2,y,width/2,height);
	offscreenGraphics.fillOval(width/2 - R, y - R, R * 2, R * 2);
	offscreenGraphics.drawLine(0,y,width,y);
    }
	
    public int getY()
    {
	//return (int) (height-(Math.sqrt((value-min)/(max-min)))*height);
	return (int) (height - (
			      (value-min)/(max-min)
			      ) * height);
    }
	
    public float getValue()
    {
	return value;
    }
	
    public void setValue(float inV)
    {
	value=inV;
	updateImage();
	repaint();
    }
	
    public boolean mouseDown(Event e, int x, int y)
    {
	dragging=true;
	mY=height-getY();
	yDragOffset=y-getY();
	return true;
    }
    
    public boolean mouseDrag(Event e, int x, int y)
    {
	if (dragging)
	    {
		mY=height-(int)(y-yDragOffset);
			
		if (mY<0)
		    mY=0;
		else
		    if (mY>height)
			mY=height;
					
		//value=(float) (Math.pow(mY/(float)height,2)*(max-min))+min;
		value = ((float) mY/height) * (max - min) + min;

		if (value<min)
		    value=min;
		else
		    if (value>max)
			value=max;
		updateImage();
		repaint();
		taskDesigner.taskCanvas.getSliders();
	    }
	return true;
    }
	
    public boolean mouseUp(Event e, int x, int y)
    {
	dragging=false;
	return true;
    }
}
