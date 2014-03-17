/*
 * @(#)Tools3d.java (part of 'Flight Club')
 * 
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 *	
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Copyright 2001-2002 Dan Burton <danb@dircon.co.uk>
 */
package flightclub.framework3d;

import java.util.*;
import java.awt.*;
import java.io.*;
/**
   This class implements static methods for 3d geometry.
*/
public class Tools3d {

    // the following four properties are all for quickSin and quickCos
    private static float dAngle;
    private static float sines[] = null;
    private static final int NUM_ANGLES = 20; 

    // initialiser
    {
	System.out.println("Tools3d initialiser !");
    }

    /**
       Returns a list of points for a circle lying in the XZ plane.
    */
    public static float[][] circleXZ(int npoints, float radius, float[] center) {
	float dtheta = (float) Math.PI * 2/npoints;
	float[][] circle = new float[npoints][3];
		
	for (int i = 0;i<npoints;i++) {
	    float theta = dtheta * i;
	    float x = (float) Math.sin(theta) * radius;
	    float z = (float) Math.cos(theta) * radius;
		
	    circle[i][0] = x;
	    circle[i][1] = 0;
	    circle[i][2] = z;
	    Tools3d.add(center, circle[i], circle[i]);
	}
	return circle;
    }

    /**
       Returns a list of points for a circle lying in the XZ plane.
    */
    public static float[][] circleXY(int npoints, float radius, float[] center) {
	float dtheta = (float) Math.PI * 2/npoints;
	float[][] circle = new float[npoints][3];
		
	for (int i = 0;i<npoints;i++) {
	    float theta = dtheta * i;
	    float x = (float) Math.sin(theta) * radius;
	    float y = (float) Math.cos(theta) * radius;
		
	    circle[i][0] = x;
	    circle[i][1] = y;
	    circle[i][2] = 0;
	    Tools3d.add(center, circle[i], circle[i]);
	}
	return circle;
    }
	
    /** Returns the identity matrix. */
    public static final float[][] identity() {
	float[][] m = {{1,0,0},{0,1,0},{0,0,1}};
	return m ;
    }
	
    public static final float[][] zero() {
	float[][] m = {{0,0,0},{0,0,0},{0,0,0}};
	return m ;
    }

    public static final float dot(float[] a, float[] b) {
	return (float) a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
	
    /**
	Creates a rotation matix that will rotate the given point so
	that it lies on the x axis. We do the transformation in two
	steps. First, rotate about z axis ( so the point lies in the plane
	y = 0). Second, rotate about y axis ( so the point lies in the
	plane z = 0).  
    */
    public static final float[][] rotateX(float[] v) {
  	float r,r_;
	float[][] m1 = identity();
	float[][] m2 = identity();
	float[][] m3 = identity();
	float[][] m4 = identity();
	float[][] m5 = identity();

	r_ = (float) Math.sqrt(v[1] * v[1] + v[0] * v[0]);
	if (r_ != 0) {
	    m1[0][0] = v[0]/r_;	
	    m1[0][1] = v[1]/r_;
	    m1[1][0] = - m1[0][1];
	    m1[1][1] = m1[0][0];
	}
		
	r = length(v);
	if (r_ != 0) {
	    m2[0][0] = r_/r;
	    m2[0][2] = v[2]/r;
	    m2[2][0] = -m2[0][2];
	    m2[2][2] = m2[0][0];
	}

	m3 = applyTo(m2,m1);
	return m3;
    }

    /**
	Creates a rotation matix that will rotate by x radians about
	the z axis.  
    */
    public static final float[][] rotateAboutZ(float x) {

	float[][] m = identity();

	m[0][0] = (float) Math.cos(x);	
	m[0][1] = (float) Math.sin(x);
	m[1][0] = - m[0][1];
	m[1][1] = m[0][0];
	return m;
    }
	
    /** Sets a_ to matrix multiplication of m times a. */
    public static final void applyTo(float[][] m,float[] a,float[] a_) {
	float[] a__ = {0, 0, 0};
		
	/**
	   Bug !! if a and a_ refer to the same array we always get zero !
	   Soln: introduce a__
	*/
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		a__[i] +=  m[i][j] * a[j];
	    }
	}
	a_[0] = a__[0];
	a_[1] = a__[1];
	a_[2] = a__[2];
    }

    public static final float[][] applyTo(float[][] m1, float[][] m2)
    {
	float[][] m3 = zero();
		
	for (int i = 0; i < 3; i++)
	    {
		for (int j = 0; j < 3; j++)
		    {
			for (int k = 0; k < 3; k++)
			    {
				m3[i][j] = m3[i][j] + m1[i][k] * m2[k][j];
			    }
		    }
	    }
	return m3;
    }

    /**
       Projects (x, y, z) to (x_, y_, z_). We are doing a perspective
       projection with the eye located a distance d along the x axis
       looking towards the origin. Return false if (x, y, z) falls
       outside field of view, true otherwise.  
    */
    public static final boolean projectYZ(float[] a, float[] a_,float d)
    {
	float tanMax = 25;
		
	a_[0] = a[0];
	if (a[0] >= d) return false; //point behind camera
		
	float tan;
	float d_ = (d - a[0]); //distance of point in front of camera
		
	tan = a[1]/d_;
	if (tan * tan > tanMax) {
	    return false;
	} else { 
	    a_[1] = tan;
	}

	tan = a[2]/d_;
	if (tan * tan > tanMax) {
	    return false;
	} else { 
	    a_[2] = tan;
	}

	return true;
    }

    /** Adds a and b to give the result c. */
    public static final void add(float[] a, float[] b, float[] c) {
	c[0] = a[0] + b[0];
	c[1] = a[1] + b[1];
	c[2] = a[2] + b[2];
    }
       
    /**
       Sets c equal to a minus b. Thus c becomes the vector that takes you
       from b to a.  
    */
    public static final void subtract(float[] a,float[] b, float[] c) {
	c[0] = a[0] - b[0];
	c[1] = a[1] - b[1];
	c[2] = a[2] - b[2];
    }

    /** Adds x * a and y * b to give the result c. */
    public static final void linearSum(float x, float[] a, float y, float[] b, float[] c) {
	c[0] = x * a[0] + y * b[0];
	c[1] = x * a[1] + y * b[1];
	c[2] = x * a[2] + y * b[2];
    }
	
    public static final float length(float[] v) {
	return  (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
	
    public static final void scaleBy(float[] v, float scale)
    {
	v[0] *= scale;
	v[1] *= scale;
	v[2] *= scale;
    }
	
    /**
       Makes c equal the cross product of a and b. The result has
       magnitude |a|.|b|.sin(theta) and its direction is perpendicular
       to the plane defined by a and b. If, as we look at the plane,
       we turn clockwise to go from a to b then c is pointing away
       from us.
       
       <pre>

        b <--------   c points 'into' the page
	      theta \
                     \
                      \
		       >a

       </pre>
    */
    public static final void cross(float[] a,float[] b,float[] c) {
	c[0] = a[1] * b[2] - a[2] * b[1];
	c[1] = -a[0] * b[2] + a[2] * b[0];
	c[2] = a[0] * b[1] - a[1] * b[0];
    }

    public static final double rnd(double lower, double upper) {
	return (double) (Math.random() * (upper - lower) + lower);
    }

    public static final float rnd(float lower, float upper) {
	return (float) (Math.random() * (upper - lower) + lower);
    }
	
    public static final void makeUnit(float[] v)
    {
	float scale = 1/Tools3d.length(v);
	v[0] *= scale;
	v[1] *= scale;
	v[2] *= scale;
    }
	
    public static final void scaleToLength(float[] v, float length)
    {
	float scale = length/Tools3d.length(v);
	v[0] *= scale;
	v[1] *= scale;
	v[2] *= scale;
    }

    /**
       Rounds a float to n decimal places.
    */
    public static final float round(float x) {
	return ((float) Math.round(x * 1000)) / 1000;
    }

    /**
       New idea - represent a vector as a float[].
    */
    public static final String toString(float[] p) {
	return "(" + round(p[0]) + ", " + round(p[1]) + ", " + round(p[2]) + ")";
    }

    public static final String toString(float[][] m) {
	StringBuffer s = new StringBuffer();
	for (int i = 0; i < m.length ; i++) {
	    float[] n = m[i];
	    for (int j = 0; j < n.length; j ++) {
		s.append("\t [" + i + "][" + j + "]: " + round(m[i][j]));
	    }
	    s.append("\n");
	}
	return s.toString();
    }

    /**
       SPEED - round theta by splitting PI/4 into N steps. Then look
       up a pre computed value from array.
    */
    public static final float quickSin(float x) {
	if (sines == null) {
	    initSines();
	}
	int i =  Math.round(x/dAngle);
	if (i >= 0) {
	    return sines[mapSine(i)];
	} else {
	    return -sines[mapSine(-i)];
	}
    }

    public static final float quickCos(float theta) {
	return quickSin((float) Math.PI/4 - theta);
    }

    private static void initSines() {
	dAngle = (float) (Math.PI/4)/NUM_ANGLES; //split a right angle into N small steps;
	sines = new float[NUM_ANGLES + 1];
	for (int i = 0; i <= NUM_ANGLES; i++) {
	    sines[i] = (float) Math.sin(i * dAngle);
	}
    }

    private static int mapSine(int i) {

	// use eq sin(x) = sin(i - 2PI) to get i between -PI and PI
	// note this routine only gets called with i > 0
	while (i > NUM_ANGLES * 2) {
	    i -= NUM_ANGLES * 4;
	}

	if (i >= 0) {
	    if (i < NUM_ANGLES) {
		return i;
	    } else  {
		return NUM_ANGLES * 2 - i;
	    }
	} else {
	    if (i > -NUM_ANGLES) {
		return i;
	    } else {
		return -NUM_ANGLES * 2 - i;
	    }
	}

    }

    /**
       Prints out the tokens in a stream. Useful for debugging. This
       class has become the home of static utility methods !
    */
    public static void debugTokens(StreamTokenizer st) throws IOException {
	System.out.println("The tokens...\n");
	while(st.nextToken() != StreamTokenizer.TT_EOF) {
	    // TODO:
	    switch (st.ttype) {
	    case StreamTokenizer.TT_WORD: 
		System.out.println("Word: "  + st.sval);
		break;
	    case StreamTokenizer.TT_NUMBER:
		System.out.println("Num: " + st.nval);
		break;
	    case StreamTokenizer.TT_EOL:
		System.out.println("EOL");
		break;
	    case ':':
		System.out.println(":");
		break;
	    case ',':
		System.out.println(",");
		break;
	    default:
		System.out.println("Ttype, sval: " + st.ttype + ", " + st.sval);
		break;
	    }
	}
    }

    /** Converts a vector to a string for debugging. */
    public static String asString(float[] p) {
	return "(" + Tools3d.round(p[0]) + ", " 
	    + Tools3d.round(p[1]) + ", " 
	    + Tools3d.round(p[2]) + ")"; 
    }

    /**
       VM 1.1 compliant methods for parsing numbers.
    */
    public final static float parseFloat(String s) {
	return Float.valueOf(s).floatValue();
    }

    public final static int parseInt(String s) {
	return Integer.valueOf(s).intValue();
    }

}
