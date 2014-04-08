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
        set(0, 0, 0);
    }

    public Vector3d(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3d(double x, double y, double z) {
        set((float) x, (float) y, (float) z);
    }

    public Vector3d(Vector3d other) {
        set(other);
    }

    public float length() {
        return (float) Math.hypot(Math.hypot(x, y), z);
    }

    public Vector3d set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3d set(Vector3d other) {
        set(other.x, other.y, other.z);
        return this;
    }

    public Vector3d add(Vector3d other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vector3d subtract(Vector3d other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    /** Returns a copy of this instance subtracted by the other instance. */
    public Vector3d minus(Vector3d other) {
        return new Vector3d(this).subtract(other);
    }

    public Vector3d scaleBy(float factor) {
        x *= factor;
        y *= factor;
        z *= factor;
        return this;
    }

    public Vector3d scaleToLength(float length) {
        scaleBy(length / this.length());
        return this;
    }

    public Vector3d makeUnit() {
        scaleToLength(1);
        return this;
    }

    public float dot(Vector3d other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3d cross(Vector3d other) {
        set(y * other.z - z * other.y,
            -x * other.z + z * other.x,
            x * other.y - y * other.x);
        return this;
    }

    /** Returns a copy of this instance cross multiplied with the other instance. */
    public Vector3d crossed(Vector3d other) {
        return new Vector3d(this).cross(other);
    }
}
