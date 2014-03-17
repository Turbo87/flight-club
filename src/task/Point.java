package flightclub.task;

import java.awt.*;
import java.io.*;

public class Point {
    float x, y; // model space
    int xx, yy; // screen space
    protected static int size = 2;
    static float x0, y0, dx, dy;

    public Point() {;}

    public Point(int xx, int yy) {
	this.xx = xx;
	this.yy = yy;
	map_();
    }

    public Point(float x, float y) {
	this.x = x;
	this.y = y;
	map();
    }

    /**
       Maps from screen to model space.
    */
    void map_() {
	x = x0 + dx * xx;
	y = y0 + dy * yy;
    }

    /**
       Maps from model to screen space.
    */
    void map() {
	xx = (int) ((x - x0)/dx);
	yy = (int) ((y - y0)/dy);
    }

    /**
       Defines the space mapping given the canvas size and model
       co-ords of the corners.
    */
    static void defineMap(int w, int h, float x0, float x1, float y0, float y1) {
	Point.x0 = x0;
	dx = (x1 - x0)/w;
	Point.y0 = y1;
	dy = (y0 - y1)/h;
    }

    public void draw(Graphics g) {
	g.drawLine(xx - size, yy, xx + size, yy);
	g.drawLine(xx, yy - size, xx, yy + size);
    }
	
    public void highLight(Graphics g) {
	g.drawOval(xx - size - 2, yy - size - 2, size + size + 4, size + size + 4);
    }

    public void moveTo(int inX, int inY) {
	xx = inX;
	yy = inY;
	map_();
    }

    float distanceTo(float x, float y) {
	float dx = this.x - x;
	float dy = this.y - y;
	return (float) Math.sqrt(dx * dx + dy * dy);
    }

    float distanceTo(int xx, int yy) {
	float dx = this.xx - xx;
	float dy = this.yy - yy;
	return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // debug
    void asString() {
	System.out.println("(xx,yy) = (" + xx + "," + yy + ") (x,y) = (" + x + "," + y +")" );
    }
}

class TurnPoint extends Point {
    public TurnPoint(float x, float y) { super(x, y); }
    public TurnPoint(int xx, int yy) { super(xx, yy); }
    
    public TurnPoint(StreamTokenizer st) throws IOException {
	st.nextToken();
	x = (float) st.nval;
	st.nextToken();
	y = (float) st.nval;
	st.nextToken(); // gobble end of line
	map();
    }

    public void draw(Graphics g) {
	g.drawOval(xx - size, yy - size, size + size, size + size);
    }

    public String toString() {
	return x + " " + y;
    }

}

class TriggerPoint extends Point {
    float thermalStrength = 2;
    float cycleLength = 30;
    float duration = 0.5f;

    public TriggerPoint(float x, float y) { super(x, y); }
    public TriggerPoint(int xx, int yy) { super(xx, yy); }

    public TriggerPoint(StreamTokenizer st) throws IOException {
	st.nextToken();
	x = (float) st.nval;
	st.nextToken();
	y = (float) st.nval;
	st.nextToken();
	thermalStrength = (float) st.nval;
	st.nextToken();
	cycleLength = (float) st.nval;
	st.nextToken(); // gobble end of line
	duration = (float) st.nval;
	st.nextToken(); // gobble end of line
	map();
    }

    public String toString() {
	return x + " " + y + " " + thermalStrength + " " + cycleLength + " " + duration;
    }
}
