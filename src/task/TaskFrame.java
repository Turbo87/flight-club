package flightclub.task;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TaskFrame extends Frame implements TaskEnv {
    public TaskFrame() {
	super("Flight Club - Task Designer");
	this.setSize(500, 500);
	TaskDesigner td;
	this.add(td = new TaskDesigner(), "Center");
	this.doLayout();
	this.show();
	td.init(this);

	this.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    System.exit(0);
		}
	    });

    }

    public static void main(String[] args) {
	TaskFrame x = new TaskFrame();
    }

    public InputStream openFile(String name) {
	File f;
	// FileReader fr;
	FileInputStream is = null;
	String dir = System.getProperty("user.dir");

	try {
	    f = new File(dir, name);
	    is = new FileInputStream(f);
	    return is;
	} catch (Exception e) {
	    String msg = "Error opening file. Dir: " + dir + ", name: " + name + "\n";
	    System.out.println(msg + e.toString());
	} 
	return null;
    }

}
