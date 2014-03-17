package flightclub.task;

import java.applet.*;
import java.io.*;

public class TaskApplet extends Applet implements TaskEnv {
    public void init() {
	TaskDesigner td;
	this.add(td = new TaskDesigner());
	td.init(this);
    }

    public InputStream openFile(String name) { return null;}
}
