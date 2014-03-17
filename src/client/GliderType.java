/*
  GliderType.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Dan Burton , Nov 2001 
*/
package flightclub.client;

import flightclub.framework3d.*;
import java.io.*;
import java.util.Date;
/**
   This class implements the reading and writing of data files for
   different types of glider. A glider's data file determines its
   flying characteristics and its 3d representation.

   Currently we have three types of glider. The relevant data files -
   hangglider.txt, paraglider.txt, sailplane.txt - may be found in the
   folder ~/xc/class/. The file format is as follows:

   <pre>

   # hangglider
   # by foo, 18 Aug 2002
   TURN: 0.3
   POLAR: 20 -2, 40 -6, 60 -10, 
   OBJ3D:
   2
   t "3399ff" 0 0 0, 1 0 0, 1 -0.2 0, 0 -0.2 0,
   t "3399ff" 0 0 0, -1 0 0, -1 -0.2 0, 0 -0.2 0,

   </pre>
   
   Note the polar has a trailing comma after the last pair of speed
   and sink values. The text after OBJ3D: is the 3d representation of
   the glider. This portion of the file will be parsed by Obj3d.  
*/
public class GliderType {
    protected ModelViewer modelViewer;
    protected String typeName;
    protected int typeID;
    protected Obj3dDir obj;
    protected float[][] polar;
    protected float turnRadius;

    static final int POLAR_LEN = 2; // just two points on polar curve

    /** 
	Creates an instance of <code>typeName</code> or throws an IO
	error. Opens the relevant data file, parses it and sets my
	properties accordingly. This is the constructor used by the
	client.  
    */
    public GliderType(ModelViewer modelViewer, String typeName, int typeID) throws IOException {
	this.modelViewer = modelViewer;
	this.typeName = typeName;
	this.typeID = typeID;
	parseFile();
    }

    /** 
	Creates an empty instance of glider type ready for you to
	specify its properties. Use this constructor if you are
	running glider type stand alone in order to *write* a def
	file.

	@see flightclub.data.Paraglider
	@see flightclub.data.Hangglider
	@see flightclub.data.Sailplane 
    */
    public GliderType(String typeName) {
	this.typeName = typeName;
    }

    private void parseFile() throws IOException {
	InputStream is = modelViewer.modelEnv.openFile(typeName + ".txt");
	StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(is)));
	st.eolIsSignificant(true);
	st.commentChar('#');
	st.wordChars(':', ':');

	// Tools3d.debugTokens(st);

	// gobble EOL's due to comments
	while (st.nextToken() == StreamTokenizer.TT_EOL) {;}

	// turn radius
	if (! "TURN:".equals(st.sval))
	    throw new FileFormatException("Unable to read turn radius: " + st.sval);
	st.nextToken();
	turnRadius = (float) st.nval;

	// gobble new line
	st.nextToken();	

	// polar
	st.nextToken();
	if (! "POLAR:".equals(st.sval)) {
	    throw new FileFormatException("Unable to read polar: " + st.sval);
	}

	polar = new float[POLAR_LEN][2];
	int index = 0;
	float speed, sink;

	//  loop throw pairs of speed, sink data
	while(st.nextToken() != StreamTokenizer.TT_EOL) {
	    if (index < POLAR_LEN) {
		polar[index][0] =  (float) st.nval;
		st.nextToken();
		polar[index][1] = (float) st.nval;
		if (st.nextToken() != ',') {
		    throw new FileFormatException("Error in polar data ! The required format is: \n\tPOLAR: <speed> <sink> , <speed> <sink> ,");
		}
	    }
	    index++;
	}

	// obj3d
	st.nextToken();
	if (! "OBJ3D:".equals(st.sval)) {
	    throw new FileFormatException("Unable to read OBJ3D: " + st.sval);
	}

	// gobble new line
	st.nextToken();	
	
	// parse the 3d model
	obj = new Obj3dDir(st, modelViewer, false);

	// gobble any trailing EOL's
	while (st.nextToken() == StreamTokenizer.TT_EOL) {;}

	// should be at end of file
	is.close();
	if (st.ttype != StreamTokenizer.TT_EOF)
	    throw new FileFormatException(st.toString());
    }

    /**
       Outputs the data for this type of glider to a text file.
    */
    public void writeFile() throws IOException {
	File f = new File(typeName + ".txt");
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

    public String toString() {
	StringBuffer sb = new StringBuffer();

	// header - type name and date
	Date today =  new Date();
	sb.append("# " + typeName + "\n# " + today.toString() + "\n");

	// turn
	sb.append("TURN: " + Tools3d.round(turnRadius) + "\n");

	// polar
	sb.append("POLAR: ");
	for (int i = 0; i < POLAR_LEN; i ++) {
	    sb.append(Tools3d.round(polar[i][0]) + " "
		  + Tools3d.round(polar[i][1])
		  + " , ");
	}
	sb.append("\n");

	// obj3d
	sb.append("OBJ3D: \n");
	sb.append(obj.toString());
	return new String(sb);
    }
}
