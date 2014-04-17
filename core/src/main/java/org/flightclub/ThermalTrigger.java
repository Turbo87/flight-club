/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.util.Vector;

public class ThermalTrigger implements Clock.Observer {
    final XCGame app;
    Clock clock;
    final int x;
    final int y;
    float t;

    /** 0, 1 or 2 - stop clouds overlapping */
    int nextCloud;

    final int cycleLength;
    final int cloudDuration;
    final int cloudStrength;
    int dummyClick = 0;
    final Vector<Cloud> clouds;

    /** how many thermals per cycle (1 or 2) */
    final int bubbles;

    static final float SPREAD = (float) 1.2; // 0.5
    static final int CYCLE_LENGTH = 20; // 30

    /** life span of cloud in seconds */
    static final int CLOUD_DURATION = 10;
    static final int MAX_WAIT = 7;

    public ThermalTrigger(XCGame theApp, int inX, int inY) {
        this(theApp, inX, inY, 1, 1, 1);
    }

    public ThermalTrigger(XCGame theApp, int inX, int inY, int inCloudStrenth) {
        this(theApp, inX, inY, inCloudStrenth, 1, 1);
    }

    public ThermalTrigger(XCGame theApp, int inX, int inY, int inCloudStrenth, float inCycleLength, float inCloudDuration) {
        app = theApp;
        app.clock.addObserver(this);
        x = inX;
        y = inY;

        cloudStrength = inCloudStrenth;
        cycleLength = (int) (inCycleLength * CYCLE_LENGTH);
        cloudDuration = (int) (inCloudDuration * CLOUD_DURATION);

        t = (int) Tools3d.rnd(0, cycleLength - 1);
        nextCloud = (int) Tools3d.rnd(0, 2);
        clouds = new Vector<>();

        //more cloud on center tiles
        if (x < Landscape.TILE_WIDTH / 2
                && x > -Landscape.TILE_WIDTH / 2) {
            bubbles = 1;//was 2, trying bruce's hexagon
        } else {
            bubbles = 1;
        }

        if (t < cycleLength - MAX_WAIT) {
            makeCloud();
        }

        Landscape.crossHair(x, y);
    }

    @Override
    public void tick(float delta) {

        if (t == 0) makeCloud();
        //if (bubbles > 1) if (t == 7) makeCloud();
        //if (bubbles > 2) if (t == 14) makeCloud();

        t += delta * app.timeMultiplier / 2.0f;
        if (t > cycleLength) t = 0;

        //System.out.println("avg secs: " + c.getAvgSleep());
    }

    void makeCloud() {
        float dx = (float) Tools3d.rnd(-SPREAD, SPREAD);
        float dy;

        switch (nextCloud) {
            case 0:
                dy = SPREAD;
                break;
            case 1:
                dy = 0;
                break;
            case 2:
                dy = -SPREAD;
                break;
            default:
                dy = 0;
        }

        nextCloud++;
        if (nextCloud == 3) nextCloud = 0;

        Cloud cloud = new Cloud(app, x + dx, y + dy, cloudDuration, cloudStrength);
        clouds.addElement(cloud);
        cloud.trigger = this;
    }

    void destroyMe() {
        app.clock.removeObserver(this);

        // hurry up clouds
        for (int i = 0; i < clouds.size(); i++) {
            Cloud cloud = clouds.elementAt(i);
            if (cloud.age < cloud.t_nose + cloud.t_mature) {
                if (cloud.age > cloud.t_nose)
                    cloud.t_mature = (int) cloud.age - cloud.t_nose;
                else
                    cloud.t_mature = 0;
            }
        }

    }

    void destroyMe(boolean really) {
        destroyMe();
        if (!really) return;

        //24/10 - kill those clouds, but beware that bug (see comment below)
        //hence kill them gradually
        //nb this is wanted for thermalling across tile boundary anyway
        for (int i = 0; i < clouds.size(); i++) {
            Cloud cloud = clouds.elementAt(i);
            //cloud.destroyMe();
            //found bug - do not modify contents of a vector while looping thru it!
            cloud.age = cloud.t_nose + cloud.t_mature + cloud.t_tail;
        }
    }
}
