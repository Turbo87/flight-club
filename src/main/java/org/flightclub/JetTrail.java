/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.Color;

/**
 * a jet in the upper atmosphere - leaves a long trail
 */
public class JetTrail extends FlyingDot {
    static final float SPEED = 5;
    static final float ALTITUDE = 6;
    static final float TURN_RADIUS = 16;
    FlyingDot buzzThis;
    static final float RANGE = 40;

    public JetTrail(ModelViewer app, float x, float y) {
        //set flag so camera will follow my cuts when in mode 1
        //(see glider.gotoNextLiftSource)
        super(app, SPEED, TURN_RADIUS);
        super.init(new Vector3d(x, y, ALTITUDE));
        v.y = SPEED;
    }

    void makeFlyX() {
        //override the default of flying down y axix
        v.x = -SPEED;
        v.y = 0;
    }

    @Override
    protected void createTail() {
        int tailLength = 80;
        tail = new Tail(app, tailLength, new Color(200, 200, 200), 0);    //add to layer zero
        tail.wireEvery = 1;
        tail.updateEvery = 3;
        tail.init(p);
    }

    void checkBounds() {
        if (buzzThis != null) {
            Vector3d t = new Vector3d(buzzThis.p.x, buzzThis.p.y + TURN_RADIUS, 0);
            if (p.x > buzzThis.p.x + RANGE
                    || p.x < buzzThis.p.x - RANGE) {
                moveManager.setTargetPoint(t);
            }
            if (p.y > buzzThis.p.y + RANGE
                    || p.y < buzzThis.p.y - RANGE) {
                moveManager.setTargetPoint(t);
            }
        }
    }

    @Override
    public void tick(Clock c) {
        super.tick(c);
        checkBounds();
    }
}

