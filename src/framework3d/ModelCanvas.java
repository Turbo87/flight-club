/*
  @(#)ModelCanvas.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Copyright 2001-2003 Dan Burton <danb@dircon.co.uk>
*/
package flightclub.framework3d;

import java.awt.*;
import java.awt.event.*;
/**
  This class is responsible for displaying a 3d model on the
  screen. Dragging on the canvas rotates the camera.

  @see ModelViewer
  @see CameraMan 
*/
public class ModelCanvas extends Canvas {
    protected ModelViewer modelViewer;
    protected Color backColor = Color.white;
    protected Graphics graphicsBuffer;
    private Image imgBuffer;
    private boolean dragging = false;
    private int width, height;
    private int x0 = 0,y0 = 0;
    private int dx = 0,dy = 0;
    private final float rotationStep;
    private boolean painted = true; //set to false whilst painting the model
    private String[] textMessage;

    /** The amount mouse must be dragged in order to trigger any camera movement. */
    protected final static int DRAG_MIN = 20;
    protected final static int TEXT_LINES = 3;

    public ModelCanvas(ModelViewer modelViewer) {
	this.modelViewer = modelViewer;

	//4 seconds to rotate 90 degrees (at 25hz) - slooow !
	rotationStep = (float) Math.PI/(25 * 8);

	//event handlers
	this.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e){
		    x0 = e.getX();
		    y0 = e.getY();
		    dragging = true;
		}
		public void mouseReleased(MouseEvent e){
		    dx = 0;
		    dy = 0;
		    dragging = false;
		}
	    });

	this.addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e){
		    dx = e.getX() - x0;
		    dy = e.getY() - y0;
		}
	    });

	this.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e){
		    ModelCanvas.this.modelViewer.eventManager.handleEvent(e);
		}
		public void keyReleased(KeyEvent e){
		    ModelCanvas.this.modelViewer.eventManager.handleEvent(e);
		}
	    });
    }
	
    public void init() {
	width = getSize().width;
	height = getSize().height;
		
	imgBuffer = modelViewer.createImage(width,height);
	graphicsBuffer = imgBuffer.getGraphics();

	textMessage = new String[TEXT_LINES];
    }
	
    void tick() {
	/*
	  Change camera angle if dragging mouse and
	  moused has moved more than the minimum amount
	*/
	float dtheta = 0, dz = 0;
	float zStep;

	if (!dragging) return;
	
	/*
	  Q. How much do we change camera height by ?
	  A. Depends how far camera is from the focus.
	  Take 3 seconds to move up or down by the same
	  distance that the camera is from the focus.
	*/
	zStep = modelViewer.cameraMan.getDistance()/(25 * 3);

	if (dx > DRAG_MIN) dtheta = rotationStep;
	if (dx < - DRAG_MIN) dtheta = -rotationStep;

	if (dy > DRAG_MIN) dz = -zStep; 
	if (dy < -DRAG_MIN) dz = zStep; 
	    
	if (dtheta!=0||dz!=0) {
	    modelViewer.cameraMan.rotate(dtheta,dz);
	}
    }
	
    public void paint(Graphics g) {
	if (imgBuffer == null) return;
	g.drawImage(imgBuffer,0,0,this);
    }

    public void update(Graphics g) { paint(g); }

    /**
       Paints the model to the image buffer. We have three
       steps. First transform the objects. Then sort them. Finally
       draw them.  
    */
    protected void paintModel() {

	//clear buffer
	graphicsBuffer.setColor(backColor);
	graphicsBuffer.fillRect(0,0,width,height);
	
	//transform to screen co-ords
	for (int i = 0;i < modelViewer.obj3dManager.size();i++) {
	    Obj3d o = modelViewer.obj3dManager.obj(i);
	    o.transform();
	}
	
	//sort
	modelViewer.obj3dManager.sortObjects();

	//draw
	for (int i = 0;i < modelViewer.obj3dManager.size();i++) {
	    Obj3d o = modelViewer.obj3dManager.obj(i);
	    o.draw(graphicsBuffer);
	}

	//any text ?
	paintText();
    }

    private Font font = new Font("SansSerif", Font.PLAIN, 10);

    /** Display some text at the bottom of the screen. */
    private void paintText() {
	graphicsBuffer.setFont(font);
	graphicsBuffer.setColor(new Color(0x333333));
	
	for (int i = 0; i < textMessage.length; i++) {
	    if (textMessage[i] != null && textMessage[i].length() > 0) {
		graphicsBuffer.drawString(textMessage[i], 15, height - 15 * (i + 1));
	    }
        }
    }

    /**
       Returns the size of this canvas. Only relevant when running as
       an application. For an applet the size is given by the web
       page.
    */
    public Dimension getPreferredSize() {
	return new Dimension(700, 370);
    }

    /** Displays some text at the bottom of the screen. */
    public void setText(String s, int line) {
	textMessage[line] = s;
    }
}

