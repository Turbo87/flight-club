/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.*;

/*
  a spine running // to x axis or y axis (orientation 0 or 1)
  has a circuit for ridge soaring
	
  build surface from maths functions...
  - taylor approx's of sin wave (cubic polynomial)
*/

class Hill implements CameraSubject {
    final float x0;
    final float y0; //spine start point
    int orientation = 0;
    final int spineLength;
    final float phase;
    final float h0;
    final int face;
    final ModelViewer app;
    final Object3d object3d;
    final Color color;
    final float tileWidth;//make smaller to increase curvy resolution
    int[] zorderedTiles;
    final boolean inForeGround;
    float maxH = 0;

    static final int OR_X = 0;
    static final int OR_Y = 1;
    static final int FACE_SPIKEY = 0;    //width 1 - spiky
    static final int FACE_CURVY = 1;    //width 2 - curvy

    public Hill(ModelViewer theApp, int inX, int inY, int inOr, int inSpineLength, float inPhase, float inH0, int inFace) {
        app = theApp;
        x0 = inX;
        y0 = inY;
        orientation = inOr;
        spineLength = inSpineLength;
        phase = inPhase;
        h0 = inH0;
        face = inFace;
        object3d = new Object3d(app);
        color = new Color(255, 255, 255);

        //higher resolution/sample rate if
        //on central tile
        if (x0 < Landscape.TILE_WIDTH / 2
                && x0 > -Landscape.TILE_WIDTH / 2) {
            tileWidth = (float) 0.5;
        } else {
            tileWidth = 1;
        }

        tileHill();

        inForeGround = (x0 < Landscape.TILE_WIDTH / 2 && x0 > -Landscape.TILE_WIDTH / 2);
    }

    public Hill(ModelViewer theApp, int inX, int inY) {
        //default hill (curvy spine, curvy face)
        this(theApp, inX, inY, OR_X, 2, 1, (float) 0.5, FACE_CURVY);
    }

    void tileHill() {
    /*
	  run over hill adding tiles
	*/
        int numSlices = (int) ((spineLength + 2) / tileWidth);
        int numTiles;

        float frontFace;
        if (face == FACE_SPIKEY) frontFace = 1;
        else frontFace = 2;
        numTiles = numSlices * (1 + (int) (frontFace / tileWidth));
        zorderedTiles = new int[numTiles];

        for (float i = 0; i < spineLength + 2; i += tileWidth) {
            for (float j = -1 + tileWidth; j <= frontFace; j += tileWidth) {
                addTile(i, j);
            }
        }

        if (orientation == OR_Y) object3d.reverse();
    }

    void addTile(float i, float j) {
	/*
	  add a tile at x0 +i, y0 + j if OR_X else swap i and j
	*/
        float x1, x2, y1, y2;
        Vector3d[] corners = new Vector3d[4];

        if (orientation == OR_X) {
            x1 = x0 + i;
            x2 = x1 + tileWidth;
            y1 = y0 - j; //back face has j=-1 and y > y0
            y2 = y1 + tileWidth;
        } else {
            y1 = y0 + i;
            y2 = y1 + tileWidth;
            x1 = x0 + j - tileWidth; //back face has j=-1 and x < x0
            x2 = x1 + tileWidth;
        }

        //TODO make getZ transform to i/j coords
        if (orientation == OR_X) {
            corners[0] = new Vector3d(x1, y1, getZ(i, j));
            corners[1] = new Vector3d(x1, y2, getZ(i, j - tileWidth));
            corners[2] = new Vector3d(x2, y2, getZ(i + tileWidth, j - tileWidth));
            corners[3] = new Vector3d(x2, y1, getZ(i + tileWidth, j));
        } else {
            corners[0] = new Vector3d(x1, y1, getZ(i, j - tileWidth));
            corners[1] = new Vector3d(x1, y2, getZ(i + tileWidth, j - tileWidth));
            corners[2] = new Vector3d(x2, y2, getZ(i + tileWidth, j));
            corners[3] = new Vector3d(x2, y1, getZ(i, j));
        }

        object3d.addTile(corners, color, true, true);
        //object3d.addTile(corners, color, false, false);
    }

    Circuit getCircuit() {
	/*
	  soaring circuit - treat x0,y0 as origin
	*/
        Circuit circuit = new Circuit(this, 2);

        float frontFace;
        if (face == FACE_CURVY) frontFace = 2;
        else frontFace = 1;

        if (orientation == OR_X) {
            circuit.add(new Vector3d(1, -frontFace, 0));
            circuit.add(new Vector3d(1 + spineLength, -frontFace, 0));
        } else {
            circuit.add(new Vector3d(frontFace, 1, 0));
            circuit.add(new Vector3d(frontFace, 1 + spineLength, 0));
            //override the default fall line
            circuit.fallLine = new Vector3d(-1, 0, 0);
        }
        return circuit;
    }

    public Vector3d getEye() {
        if (orientation == OR_X) {
            return new Vector3d(x0 + 2 + spineLength, y0 - 2 - spineLength, (float) 0.8);
        } else {
            return new Vector3d(x0 + 2 + spineLength, y0, (float) 0.8);
        }
    }

    public Vector3d getFocus() {
        if (orientation == OR_X) {
            return new Vector3d(x0 + (2 + spineLength) / 2, y0, h0 / 2);
        } else {
            return new Vector3d(x0, y0 + (2 + spineLength) / 2, h0 / 2);
        }
    }

    boolean contains(float inX, float inY) {

        float frontFace;
        if (face == FACE_CURVY) frontFace = 2;
        else frontFace = 1;

        if (orientation == OR_X) {
            return (inY >= y0 - frontFace && inY <= y0 + 1
                    && inX > x0 && inX < x0 + 2 + spineLength);
        } else {
            return (inX <= x0 + frontFace && inX >= x0 - 1
                    && inY > y0 && inY < y0 + 2 + spineLength);
        }
    }

    private float getZ(float i, float j) {
	/*
	  return h at point i along spine and j away from spine
			
	  slice perp to spine gives f1...
	  f1 = 1+j, j < 0 backface
	  f1 = (1-j) * (1-j), j > 0 and spiky
	  f1 = sin , j > 0 and curvy
	  then, scale f1 by f2, h at this point on spine
				
	*/

        float f1 = 1; //tmp
        float f2 = spineHeight(i);

        if (j < 0) {
            f1 = 1 + j;
        } else {
            if (face == FACE_CURVY) {
                f1 = sin(2 - j);
            } else {
                f1 = (1 - j) * (1 - j);
            }
        }

        float h = f1 * f2;
        if (h > maxH) maxH = h;
        return h;
    }

    public float getHeight(float x, float y) {
	/*
	  convert to local coords then
	  call getZ
	*/
        float i, j;
        if (orientation == OR_X) {
            i = x - x0;
            j = y0 - y;
        } else {
            i = y - y0;
            j = x - x0;
        }
        return getZ(i, j);
    }

    float spineHeight(float i) {
	/*
	  spine h a distance i along it
	*/
        if (i < 0 || i > spineLength + 2) {
            return 0;
        }

        if (i <= 1) {
            return i * i * h0;
        }

        if (i > 1 + spineLength) {
            float ii = spineLength + 2 - i;

            //beware - recursion would be infinite if
            //'>' changed to '>=' above
            float h1 = spineHeight(1 + spineLength);
            return ii * ii * h1;
        }

        return h0 + sin(i - 1 + phase) - sin(phase);
    }

    float sin(float x) {
	/*
	  cubic approx to a sin wave with
	  wave length 4, going between 0 
	  and 1 
			
	  24/10 try halving amplitude
	*/
        while (x >= 4) {
            x -= 4;
        }
        while (x < 0) {
            x += 4;
        }

        float xx;
        if (x <= 2) {
            xx = x / 2;
        } else {
            xx = 2 - x / 2;
        }
        return 3 * xx * xx - 2 * xx * xx * xx;
    }

    float getLift(Vector3d p) {
	/*
	  lift twice sink rate close to hill, falling to zero
	  as we get further away
	*/
        float lmax = -3 * Glider.SINK_RATE;
        float dh = (float) 0.1;
        //if (p.z > maxH + (float) 0.2) return 0;
        if (p.y < y0) {
            float z = getHeight(p.x, p.y);
            float h = p.z - z;
            //System.out.println("ground: " + z);
            if (h < dh) {
                return lmax;
            } else if (h < (float) 1 + dh) {
                float f = (2 + dh - h) / 2;
                return (f * f * f) * lmax;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void destroyMe() {
        object3d.destroyMe();
    }

}
