/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class FlyingBody extends FlyingDot {
    private Object3dWithShadow body0;
    private Object3dWithShadow body1;
    protected float bodyHeight;

    public FlyingBody(ModelViewer theApp, float speed, float inTurnRadius) {
        super(theApp, speed, inTurnRadius);
    }

    public FlyingBody(ModelViewer theApp, float speed, float inTurnRadius, boolean isUser) {
        super(theApp, speed, inTurnRadius, isUser);
    }

    public void init(Object3dWithShadow inBody, Vector3d inP) {
        body0 = inBody;    //the base object should not be registed
        body1 = new Object3dWithShadow(app);
        Object3dWithShadow.clone(body0, body1);

        super.init(inP);
        rotateBody();
        translateBody();
    }

    @Override
    public void tick(Clock c) {
        //update position and velocity
        super.tick(c);

        //flap wings etc
        //body0.timeStep();

        rotateBody();
        translateBody();
        body1.updateShadow();
    }

    public void rotateBody() {
        for (int i = 0; i < body0.points.size(); i++) {
            Vector3d p1 = body1.points.elementAt(i);
            Vector3d p0 = body0.points.elementAt(i);

            Vector3d xp1 = new Vector3d();
            Vector3d yp1 = new Vector3d();
            Vector3d zp1 = new Vector3d();

            xp1.set(axisX);
            yp1.set(axisY);
            zp1.set(axisZ);

            xp1.scaleBy(p0.x);
            yp1.scaleBy(p0.y);
            zp1.scaleBy(p0.z);

            p1.x = xp1.x + yp1.x + zp1.x;
            p1.y = xp1.y + yp1.y + zp1.y;
            p1.z = xp1.z + yp1.z + zp1.z;
        }
    }

    public void translateBody() {
        for (Vector3d point : body1.points)
            point.add(p);
    }

    float getBodyHeight() {
        return bodyHeight;
    }

}
