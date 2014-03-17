/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public final class Vector3d {
    public float x;
    public float y;
    public float z;

    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3d(float inX, float inY, float inZ) {
        x = inX;
        y = inY;
        z = inZ;
    }

    public Vector3d(double inX, double inY, double inZ) {
        x = (float) inX;
        y = (float) inY;
        z = (float) inZ;
    }

    Vector3d cloneMe() {
        return new Vector3d(x, y, z);
    }
}
