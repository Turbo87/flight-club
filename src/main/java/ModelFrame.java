//the frame for stand alone play cf model applet
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class ModelFrame extends Frame implements ModelEnv
{
    ModelViewer app;
	
    public ModelFrame(ModelViewer theApp, String title, int w, int h)
    {
	super(title);
	app = theApp;
	add(app, "Center");
	setSize(w,h);
	show();
	app.init((ModelEnv) this);
	app.start();

	this.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    System.exit(0);
		}
	    });

	this.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e){
		    app.eventManager.handleEvent(e);
		}
		public void keyReleased(KeyEvent e){
		    app.eventManager.handleEvent(e);
		}
	    });
    }
	
    public Image getImage(String s) {
	return Toolkit.getDefaultToolkit().getImage(s);
    }
	
    public void play(String s){ 
	//can not play sound from a frame
    }

    public DataInputStream openFile(String name) {
	return null;
    }
	
    public URL getCodeBase() {
	try {
	    return new URL("");
	} catch (MalformedURLException e) {
	    System.out.println("Unable to get code base for model panel");
	    return null;
	}
    }
}
