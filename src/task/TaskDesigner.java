package flightclub.task;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.applet.*;
import java.io.*;

interface TaskEnv {
    public InputStream openFile(String name);
}

/**
   The gui
*/
class TaskDesigner extends Panel {
    TaskCanvas taskCanvas;
    Choice actionChoice;
    Choice typeChoice;
    TaskEnv taskEnv;
    String taskID = "t001";
    float wind_x, wind_y;
    ParameterCanvas strengthCanvas;
    ParameterCanvas lengthCanvas;
    ParameterCanvas durationCanvas;
    Panel panel, p;

    public TaskDesigner() {
    }

    public void init(TaskEnv taskEnv) {
	this.taskEnv = taskEnv;
	setLayout(new BorderLayout());
	this.add("Center", taskCanvas = new TaskCanvas(this));
	addControls();
	addSliders();

	if (taskEnv instanceof Applet) {
	    Applet a = (Applet) taskEnv;
	    this.setSize(a.getSize().width, a.getSize().height);
	}

	doLayout();
	panel.doLayout();
	p.doLayout();
	
	taskCanvas.init();
	strengthCanvas.init();
	lengthCanvas.init();
	durationCanvas.init();

	// open task
	try {
	    openTask();
	} catch (IOException f) {
	    System.out.println(f);
	}

    }

    void addControls() {
	Button b;
	p = new Panel();

	b = new Button("New");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    newTask();
		}
	    });
	p.add(b);

	b = new Button("Open");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			openTask();
		    } catch (IOException f) {
			System.out.println(f);
		    }
		}
	    });
	p.add(b);

	b = new Button("Save");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			saveTask();
		    } catch (IOException f) {
			System.out.println(f);
		    }
		}
	    });
	p.add(b);

	b = new Button("Debug");
	b.addActionListener( new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    debugMe();
		}
	    });
	p.add(b);

	actionChoice = new Choice();
	actionChoice.addItem("Construct");
	actionChoice.addItem("Delete");
	actionChoice.addItem("Simulate");
	p.add(actionChoice);

	typeChoice = new Choice();
	typeChoice.addItem("Triggers");
	typeChoice.addItem("Turn Points");
	typeChoice.addItem("Roads");
	typeChoice.addItem("Hills");
	p.add(typeChoice);

	this.add("North", p);
    }

    void addSliders() {
	panel = new Panel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints c =  new GridBagConstraints();
	c.fill= GridBagConstraints.BOTH;
	c.insets=new Insets(1,1,1,1);
	GridBagLayout layout =(GridBagLayout) panel.getLayout();

	Label l1=new Label("S",Label.CENTER);
	l1.setForeground(Color.black);
	l1.setBackground(Color.white);
	c.gridx=0;c.gridy=1;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(l1,c);
	panel.add(l1);
		
	Label l2=new Label("L",Label.CENTER);
	l2.setForeground(Color.black);
	l2.setBackground(Color.white);
	c.gridx=1;c.gridy=1;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(l2,c);
	panel.add(l2);
		
	Label l3=new Label("D",Label.CENTER);
	l3.setForeground(Color.black);
	l3.setBackground(Color.white);
	c.gridx=2;c.gridy=1;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(l3,c);
	panel.add(l3);
		
	strengthCanvas= new ParameterCanvas(0, 5, 2, this);
	c.gridx=0;c.gridy=0;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(strengthCanvas,c);
	panel.add(strengthCanvas);
		
	lengthCanvas= new ParameterCanvas(10, 120, 30, this);
	c.gridx=1;c.gridy=0;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(lengthCanvas,c);
	panel.add(lengthCanvas);
		
	durationCanvas= new ParameterCanvas(0, 1, 0.5f, this);
	c.gridx=2;c.gridy=0;c.gridwidth=1;c.gridheight=1;
	c.weightx=1.00;c.weighty=1.0;
	layout.setConstraints(durationCanvas,c);
	panel.add(durationCanvas);

	this.add("South", panel);
    }

    void newTask() {
	taskCanvas.clear();
    }

    void openTask() throws IOException {
	taskCanvas.clear();
	parseFile();
	taskCanvas.repaint();
    }

    /**
       Writes a text file of data representing the task.
    */
    void saveTask() throws IOException {
	File f = new File(taskID + ".task");
	FileWriter out = null;

	try {
	    out = new FileWriter(f);
	    String s = this.toString(); 
	    out.write(s, 0, s.length());
	} finally {
	    if (out != null) {
		try { out.close(); } catch (IOException e) {;}
	    }
	}
    }

    void debugMe() {
	System.out.println("Debug...");
	Vector ps = taskCanvas.points;
	for (int i = 0; i < ps.size(); i++) {
	    Point p = (Point) ps.elementAt(i);
	    p.asString();
	}
    }

    /**
       Creates a string of data representing the task.
    */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	Vector ps;

	// header - task id name and date
	Date today =  new Date();
	sb.append("# Task: " + taskID + "\n# " + today.toString() + "\n");

	// wind
	sb.append("WIND: " + wind_x + " " + wind_y + "\n");

	// turn points
	ps = taskCanvas.turnPoints;
	sb.append("NUM_TURNS: " + ps.size() + "\n");
	for (int i = 0; i < ps.size(); i++) {
	    Point p = (Point) ps.elementAt(i);
	    sb.append(p.toString() + "\n");
	}

	// triggers
	ps = taskCanvas.triggers;
	sb.append("NUM_TRIGGERS: " + ps.size() + "\n");
	for (int i = 0; i < ps.size(); i++) {
	    Point p = (Point) ps.elementAt(i);
	    sb.append(p.toString() + "\n");
	}

	// roads - TODO
	sb.append("NUM_ROADS: " + 0 + "\n");
	return sb.toString();
    }

    private void parseFile() throws IOException {
	InputStream is = taskEnv.openFile(taskID + ".task");
	StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
	st.eolIsSignificant(true);
	st.commentChar('#');
	st.wordChars(':', ':');
	st.wordChars('_', '_'); //TODO
	int n;

	// gobble EOL's due to comments
	while (st.nextToken() == StreamTokenizer.TT_EOL) {;}

	// turn radius
	if (! "WIND:".equals(st.sval))
	    throw new FileFormatException("Unable to read wind: " + st.sval);
	st.nextToken();
	wind_x = (float) st.nval;
	st.nextToken();
	wind_y = (float) st.nval;
	st.nextToken();

	// turn points
	st.nextToken();
	if (! "NUM_TURNS:".equals(st.sval))
	    throw new FileFormatException("Unable to read number of turn points: " + st.sval);
	st.nextToken();
	n = (int) st.nval;
	st.nextToken();	

	for (int i = 0; i < n; i++) {
	    Point p = new TurnPoint(st);
	    taskCanvas.turnPoints.addElement(p);
	    taskCanvas.points.addElement(p);
	}

	// triggers
	st.nextToken();
	if (! "NUM_TRIGGERS:".equals(st.sval))
	    throw new FileFormatException("Unable to read number of triggers: " + st.sval);
	st.nextToken();
	n = (int) st.nval;
	st.nextToken();	

	for (int i = 0; i < n; i++) {
	    Point p = new TriggerPoint(st);
	    taskCanvas.triggers.addElement(p);
	    taskCanvas.points.addElement(p);
	}

	// roads

	// should be at end of file
	is.close();
    }
}

/**
   A canvas for drawing and mouse clicking on.
*/
class TaskCanvas extends Canvas {
    Vector triggers;
    Vector turnPoints;
    Vector points; //union of the above lists
    protected Graphics graphicsBuffer;
    private Image imgBuffer;
    private int width, height;
    TaskDesigner taskDesigner;
    protected Color backColor = Color.white;
    Point selectPoint = null; // the current point
    Point clickPoint = null; // the current point
    int xOff, yOff;

    public TaskCanvas(TaskDesigner taskDesigner) {
	this.taskDesigner = taskDesigner;
	triggers = new Vector();
	turnPoints = new Vector();
	points = new Vector();

	//event handlers
	this.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e){
		    zmousePressed(e.getX(), e.getY());
		}
		public void mouseReleased(MouseEvent e){
		    zmouseUp(e.getX(), e.getY());
		}
	    });

	this.addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e){
		    zmouseDrag(e.getX(), e.getY());
		}
		public void mouseMoved(MouseEvent e){
		    selectTest(e.getX(), e.getY(), false);
		}
	    });

    }

    void clear() {
	triggers = new Vector();
	turnPoints = new Vector();
	points = new Vector();
	clickPoint = selectPoint = null;
	repaint();
    }

    /**
       Adds a point when user clicks.
    */
    public void zmousePressed(int x, int y) {
	selectTest(x, y, true);

	// add
	if (selectPoint == null) {
	    if (taskDesigner.actionChoice.getSelectedItem().equals("Construct")) {
		Point p = null;
		if (taskDesigner.typeChoice.getSelectedItem().equals("Triggers")) {
		    p = new TriggerPoint(x, y);
		    triggers.add(p);
		    points.add(p);
		}
		if (taskDesigner.typeChoice.getSelectedItem().equals("Turn Points")) {
		    p = new TurnPoint(x, y);
		    turnPoints.add(p);
		    points.add(p);
		}
		selectPoint = clickPoint = p;
		setSliders();
	    }
	}

	if (taskDesigner.actionChoice.getSelectedItem().equals("Delete")) {
	    if (selectPoint != null) {
		if (selectPoint instanceof TurnPoint) {
		    turnPoints.removeElement(selectPoint);
		    points.removeElement(selectPoint);
		} else if (selectPoint instanceof Point) {
		    triggers.removeElement(selectPoint);
		    points.removeElement(selectPoint);
		}
		selectPoint = null;
		clickPoint = null;
	    }
	}

	repaint(); //until on a thread
    }

    public void init() {
	width = getSize().width;
	height = getSize().height;
	imgBuffer = taskDesigner.createImage(width,height);
	graphicsBuffer = imgBuffer.getGraphics();
	Point.defineMap(width, height, 0, 300, 0, 300);
	repaint();
    }

    public void paint(Graphics g) {
	if (imgBuffer == null) return;
	paintTask(); // todo - on a thread ?
	g.drawImage(imgBuffer,0,0,this);
    }

    public void update(Graphics g) { paint(g); }

    void paintTask() {
	//clear buffer
	graphicsBuffer.setColor(backColor);
	graphicsBuffer.fillRect(0,0,width,height);

	//draw triggers
	graphicsBuffer.setColor(new Color(180, 180, 180));

	for (int i = 0;i < triggers.size();i++) {
	    Point p = (Point) triggers.elementAt(i);
	    p.draw(graphicsBuffer);
	}

	//draw turn points
	graphicsBuffer.setColor(new Color(250, 200, 180));

	for (int i = 0;i < turnPoints.size();i++) {
	    Point p = (Point) turnPoints.elementAt(i);
	    p.draw(graphicsBuffer);
	    if (i < turnPoints.size() - 1) {
		Point p2 = (Point) turnPoints.elementAt(i + 1);
		graphicsBuffer.drawLine(p.xx, p.yy, p2.xx, p2.yy);
	    }
	}

	if (selectPoint != null) {
	    selectPoint.highLight(graphicsBuffer);
	}

	if (clickPoint != null) {
	    clickPoint.highLight(graphicsBuffer);
	}
    }

    void selectTest(int x, int y, boolean click) {
	Vector ps = points;
	float d, minD;
	int selectIndex;
	int selectTest = Point.size * 2;
	boolean redraw = false;

	minD = Float.POSITIVE_INFINITY;
	selectIndex = -1;

	for (int i=0; i < ps.size(); i++) {
	    if ((Math.abs(x - ((Point) ps.elementAt(i)).xx)) < selectTest
		&& (Math.abs(y - ((Point) ps.elementAt(i)).yy)) < selectTest) {
		d = ((Point) ps.elementAt(i)).distanceTo(x, y);
		if (d <= minD) {
		    minD = d;
		    selectIndex=i;
		}
	    }
	}

	if (selectIndex != -1) {
	    if ((Point) ps.elementAt(selectIndex)!=selectPoint) {
		redraw = true;
		selectPoint = (Point) ps.elementAt(selectIndex);
	    }
	    xOff = x-selectPoint.xx;
	    yOff = y-selectPoint.yy;
	    if (click) {
		clickPoint = selectPoint;
		setSliders();
	    }
	} else {
	    if (selectPoint != null) {
		selectPoint = null;
		redraw = true;
	    }
	}

	if (redraw) {
	    repaint();
	}
    }

    public void zmouseDrag(int x, int y) {
	if (selectPoint != null) {
	    selectPoint.moveTo(x - xOff, y - yOff);
	    repaint();
	}
    }
	
    public void zmouseUp(int x, int y) {	
	if (selectPoint != null) {
	    selectPoint.moveTo(x - xOff, y - yOff);
	    repaint();
	}
	xOff = 0;
	yOff = 0;
    }

    /**
       Sets the param canvas values for the current trigger.
    */
    void setSliders() {
	System.out.println("Set sliders...");
	TriggerPoint t;
	try {
	    t = (TriggerPoint) clickPoint;
	} catch (Exception e) { return; }

	taskDesigner.strengthCanvas.setValue(t.thermalStrength);
	taskDesigner.lengthCanvas.setValue(t.cycleLength);
	taskDesigner.durationCanvas.setValue(t.duration);
    }

    /**
       Gets the param canvas values and copies them to the current
       trigger.
    */
    void getSliders() {
	System.out.println("Get sliders...");
	TriggerPoint t;
	try {
	    t = (TriggerPoint) clickPoint;
	} catch (Exception e) { return; }

	t.thermalStrength = taskDesigner.strengthCanvas.getValue();
	t.cycleLength = taskDesigner.lengthCanvas.getValue();
	t.duration = taskDesigner.durationCanvas.getValue();
    }

}
