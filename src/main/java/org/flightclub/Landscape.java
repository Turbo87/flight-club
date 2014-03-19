/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;

import java.util.Vector;

/**
 * Some hills, triggers and a road arranged onto tiles
 */
public class Landscape implements CameraSubject {
    // hills in order heading downwind
    final Vector[] hills;
    final Vector[] triggers;
    static XCGame app;
    int currentTile;
    // cycle through the different hill shapes
    int hillCount = 0;
    final boolean[] loaded;
    static final int TILE_WIDTH = 20;
    static final int MAX_TILES = 10;

    public Landscape(XCGame theApp) {
        app = theApp;
        hills = new Vector[MAX_TILES];
        triggers = new Vector[MAX_TILES];
        loaded = new boolean[MAX_TILES];

        currentTile = 0;
        loadTile(currentTile);
        road();
        goalLine();
    }

    /**
     * load a chunk of landscape mostly thermal triggers, some tiles have hills
     */
    void loadTile(int t) {
        if (loaded[t]) return;

        hills[t] = new Vector();
        triggers[t] = new Vector();

        if (t == 0) {
            loadFlatLand(t);
            //loadHill_1(t);
        } else if (t == MAX_TILES - 1) {
            loadFinalTile(t);
        } else if (t == 1) {
            loadFlatLand(t);
        } else if (t == 2) {
            loadHill_2(t);
        } else if (t == 3) {
            loadStorm(t);
        } else if (t == 4) {
            loadHill_1(t);
        } else if (t == 5) {
            loadFlatLand(t);
        } else if (t == 6) {
            loadMountain_1(t);
        } else if (t == 7) {
            loadBlueHole(t);
        } else if (t == 8) {
            loadMountain_2(t);
        } else {
            loadFlatLand(t);
        }

        loaded[t] = true;
    }

    void addFrame(int tile, int x) {
        //Vector wire;

        //int y0 = tile * TILE_WIDTH;
        //int x0 = - TILE_WIDTH/2 + x * TILE_WIDTH;

        //two cross hairs
        //crossHair(x0, y0);
        //crossHair(x0 + TILE_WIDTH, y0);
    }

    /*
     * add a cross hair (show your working + LED !)
     * - use for tile corners and triggers
     */
    static void crossHair(float x, float y) {
        float HAIR = 1;

        Object3d o = new Object3d(app, true, 0);    //layer zero !!
        Vector<Vector3d> wire;

        wire = new Vector<>();
        wire.addElement(new Vector3d(x, y - HAIR, 0));
        wire.addElement(new Vector3d(x, y + HAIR, 0));
        o.addWire(wire, new Color(230, 230, 230), false, false);

        wire = new Vector<>();
        wire.addElement(new Vector3d(x - HAIR, y, 0));
        wire.addElement(new Vector3d(x + HAIR, y, 0));
        o.addWire(wire, new Color(230, 230, 230), false, false);

    }

    /**
     * hack a long straight pink road - refine later - ha
     */
    static void road() {
        float ATOM = 2;

        Object3d o = new Object3d(app, true, 0);    //layer zero !!
        Vector<Vector3d> wire;

        for (int i = 0; i < MAX_TILES * TILE_WIDTH; i += ATOM * 1) {
            wire = new Vector<>();

            float x1 = (float) Math.sin((double) i * Math.PI / TILE_WIDTH);
            float x2 = (float) Math.sin((double) (i + ATOM) * Math.PI / TILE_WIDTH);
            wire.addElement(new Vector3d(x1 * 1, i, 0));
            wire.addElement(new Vector3d(x2 * 1, i + ATOM, 0));
            o.addWire(wire, new Color(220, 220, 220), false, false);
        }
    }

    /**
     * finish line at start of last tile
     */
    static void goalLine() {
        float y0 = TILE_WIDTH * MAX_TILES;

        Object3d o = new Object3d(app, true, 0);    //layer zero !!
        Vector<Vector3d> wire;
        float x1 = (float) -TILE_WIDTH / 8;
        float x2 = (float) TILE_WIDTH / 8;

        wire = new Vector<>();
        wire.addElement(new Vector3d(x1, y0, 0));
        wire.addElement(new Vector3d(x2, y0, 0));
        o.addWire(wire, new Color(220, 220, 100), false, false);

        wire = new Vector<>();
        wire.addElement(new Vector3d(x1, 0, 0));
        wire.addElement(new Vector3d(x2, 0, 0));
        o.addWire(wire, new Color(220, 220, 100), false, false);
    }

    /**
     * one cloud before goal line and one after
     */
    void loadFinalTile(int tile) {
        addFrame(tile, 0);
        ThermalTrigger trigger;
        int y0 = tile * TILE_WIDTH;
        int x0 = 0;

        trigger = new ThermalTrigger(app, x0, y0 + 3, 2);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0, y0 + 15, 2);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0, y0 + TILE_WIDTH + 2, 1, (float) 0.2, (float) 0.5);
        triggers[tile].addElement(trigger);
    }

    /**
     * bruce's hexagon of thermal triggers
     */
    void loadFlatLand(int tile) {
        addFrame(tile, 0);
        ThermalTrigger trigger;
        int y0 = tile * TILE_WIDTH;
        int x0 = 0;

        trigger = new ThermalTrigger(app, x0, y0 + 3, 2);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0, y0 + 15, 1, 1, (float) 0.5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 + 5, y0 + 5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 + 5, y0 + 13, 2, (float) 0.5, (float) 0.5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 - 5, y0 + 5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 - 5, y0 + 13);
        triggers[tile].addElement(trigger);

        loadBackTriggers(tile);
    }

    /*
     * load some cloud sources off track just for looks
     */
    void loadBackTriggers(int tile) {
        int y0 = tile * TILE_WIDTH;
        int x0 = (int) Tools3d.rnd(2, -2);
        ThermalTrigger trigger;
        int dx = TILE_WIDTH / 2 + 3;

        trigger = new ThermalTrigger(app, x0 - dx, y0 + 3, 2);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 + dx, y0 + 15, 2);
        triggers[tile].addElement(trigger);
    }

    void loadHill_1(int tile) {

        int y0 = tile * TILE_WIDTH;
        Hill hill;
        ThermalTrigger trigger;
        int x0 = 0;

        addFrame(tile, 0);

        hill = new Hill(app, 2, y0 + TILE_WIDTH / 4, Hill.OR_X, 2, (float) 0.3, (float) 0.5, Hill.FACE_CURVY);
        hills[tile].addElement(hill);

        //distant hills
        hill = new Hill(app, -TILE_WIDTH / 2 - 4, y0 + TILE_WIDTH * 3 / 4 + 3, Hill.OR_Y, 6, 1, (float) 1, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        hill = new Hill(app, +TILE_WIDTH / 2 + 6, y0 + TILE_WIDTH * 3 / 4, Hill.OR_Y, 3, 0, (float) 0.5, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        //triggers
        trigger = new ThermalTrigger(app, x0, y0 + 15, 1, 1, (float) 0.5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, 5, y0 + 5, 1, (float) 0.1, (float) 0.1);
        triggers[tile].addElement(trigger);

        loadBackTriggers(tile);
    }

    void loadHill_2(int tile) {

        int y0 = tile * TILE_WIDTH;
        Hill hill;
        ThermalTrigger trigger;
        int x0 = 0;

        hill = new Hill(app, -TILE_WIDTH / 4, y0 + TILE_WIDTH / 2, Hill.OR_X, 4, 2, (float) 1, Hill.FACE_CURVY);
        hills[tile].addElement(hill);

        //background
        hill = new Hill(app, TILE_WIDTH / 2 + 4, y0 + TILE_WIDTH * 3 / 4 + 3, Hill.OR_Y, 6, 1, (float) 1, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        hill = new Hill(app, -TILE_WIDTH / 2 - 6, y0 + TILE_WIDTH * 3 / 4 - 3, Hill.OR_Y, 3, 0, (float) 0.5, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        //triggers
        trigger = new ThermalTrigger(app, x0, y0 + 10, 1, (float) 0.2, (float) 0.1);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 - 4, y0 + 10, 1, (float) 0.2, (float) 0.1);
        triggers[tile].addElement(trigger);

        loadBackTriggers(tile);
    }

    /**
     * one trigger with short cycle
     */
    void loadStorm(int tile) {
        ThermalTrigger trigger;
        int y0 = tile * TILE_WIDTH;

        //trigger with half the cycle length and 3 times the cloud duration
        trigger = new ThermalTrigger(app, -4, y0 + TILE_WIDTH / 4, 2, (float) 0.3, 3);
        triggers[tile].addElement(trigger);
    }

    /*
     * 1d - no triggers - just tumble weed !
     * well, just one slow one
     */
    void loadBlueHole(int tile) {
        ThermalTrigger trigger;
        int y0 = tile * TILE_WIDTH;

        trigger = new ThermalTrigger(app, 0, y0 + TILE_WIDTH / 2, 1, 2, 1);
        triggers[tile].addElement(trigger);
    }

    void loadMountain_1(int tile) {

        int y0 = tile * TILE_WIDTH;
        Hill hill;
        ThermalTrigger trigger;
        int x0 = 0;

        hill = new Hill(app, 2, y0 + TILE_WIDTH / 2, Hill.OR_X, 3, 3, (float) 1.1, Hill.FACE_CURVY);
        hills[tile].addElement(hill);

        //distant hills
        hill = new Hill(app, -TILE_WIDTH / 2, y0, Hill.OR_Y, 6, 1, (float) 1, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        hill = new Hill(app, +TILE_WIDTH / 2 + 6, y0 + TILE_WIDTH * 3 / 4, Hill.OR_Y, 3, 0, (float) 0.5, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        //triggers
        trigger = new ThermalTrigger(app, x0, y0 + 15, 1, 1, (float) 0.5);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 + 4, y0 + 10, 1, (float) 0.1, (float) 0.1);
        triggers[tile].addElement(trigger);

        loadBackTriggers(tile);

    }

    void loadMountain_2(int tile) {

        int y0 = tile * TILE_WIDTH;
        Hill hill;
        ThermalTrigger trigger;
        int x0 = 0;

        hill = new Hill(app, -7, y0 + TILE_WIDTH / 2, Hill.OR_X, 4, 2, (float) 1.5, Hill.FACE_CURVY);
        hills[tile].addElement(hill);

        //distant hills
        hill = new Hill(app, -TILE_WIDTH / 2, y0 + TILE_WIDTH * 3 / 4, Hill.OR_Y, 4, 1, (float) 1, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        hill = new Hill(app, +TILE_WIDTH / 2 + 2, y0, Hill.OR_Y, 3, 0, (float) 0.5, Hill.FACE_SPIKEY);
        hills[tile].addElement(hill);

        //triggers
        trigger = new ThermalTrigger(app, x0 - 6, y0 + 10, 1, (float) 0.2, (float) 0.1);
        triggers[tile].addElement(trigger);

        trigger = new ThermalTrigger(app, x0 - 2, y0 + 10, 1, (float) 0.2, (float) 0.1);
        triggers[tile].addElement(trigger);
    }

    /*
     * load tile user is over
     * and the next tile downwind
     * unload the upwind tile
     */
    public void loadTilesAround(Vector3d p) {
        currentTile = getTile(p);
        if (currentTile < MAX_TILES) loadTile(currentTile);
        if (currentTile + 1 < MAX_TILES) loadTile(currentTile + 1);
        if (currentTile > 0) removeTile(currentTile - 1);
    }

    public boolean reachedGoal(Vector3d p) {
        return p.y >= MAX_TILES * TILE_WIDTH;
    }

    void removeTile(int tileNum) {
        removeTile(tileNum, false);
    }

    void removeTile(int tileNum, boolean really) {

        if (!loaded[tileNum]) return;

        for (int i = 0; i < hills[tileNum].size(); i++) {
            Hill hill = (Hill) hills[tileNum].elementAt(i);
            hill.destroyMe();
        }

        for (int i = 0; i < triggers[tileNum].size(); i++) {
            ThermalTrigger trigger = (ThermalTrigger) triggers[tileNum].elementAt(i);
            trigger.destroyMe(really);
        }

        hills[tileNum] = null;
        triggers[tileNum] = null;
        loaded[tileNum] = false;
    }

    void removeAll() {

        for (int i = 0; i < MAX_TILES; i++) {
            //pass really flag so clouds disappear aswell
            removeTile(i, true);
        }
    }

    /*
     * add a hill
     * each call adds a different hill shape
     */
    void addHill(int tileNum, int x, int y) {
        Hill hill;
        hill = new Hill(app, x, y);
        hills[tileNum].addElement(hill);
    }

    /**
     * return first hill downwind (+y) of p within glide
     */
    Hill nextHill(Vector3d p) {
        int tile = getTile(p);
        if (!loaded[tile]) return null;
        float RANGE = 8;

        for (int i = 0; i < hills[tile].size(); i++) {
            Hill hill = (Hill) hills[tile].elementAt(i);
            if (hill.y0 >= p.y &&
                    hill.y0 - p.y < RANGE * p.z && hill.inForeGround) return hill;
        }

        //try next tile
        tile++;
        if (tile > MAX_TILES - 1) return null;

        if (!loaded[tile]) return null;
        for (int i = 0; i < hills[tile].size(); i++) {
            Hill hill = (Hill) hills[tile].elementAt(i);
            if (hill.y0 >= p.y &&
                    hill.y0 - p.y < RANGE * p.z && hill.inForeGround) return hill;
        }
        return null;
    }

    /**
     * which tile does this point fall in
     */
    int getTile(Vector3d p) {
        for (int i = 0; i < MAX_TILES; i++) {
            if ((i + 1) * TILE_WIDTH > p.y) return i;
        }
        return MAX_TILES - 1;
    }

    /**
     * find local hill to point, if any
     */
    Hill myHill(Vector3d p) {
        int tile = getTile(p);
        if (!loaded[tile]) return null;

        for (int i = 0; i < hills[tile].size(); i++) {
            Hill hill = (Hill) hills[tile].elementAt(i);
            if (hill.contains(p.x, p.y)) return hill;
        }
        return null;
    }

    float getHeight(float x, float y) {
        Hill hill = myHill(new Vector3d(x, y, 0));
        if (hill == null) return 0;
        return hill.getHeight(x, y);
    }

    @Override
    public Vector3d getFocus() {
        float yCenter = currentTile * TILE_WIDTH + TILE_WIDTH / 2;
        return new Vector3d(0, yCenter, 0);
    }

    /**
     * look in from near corner (at cloudbase)
     */
    @Override
    public Vector3d getEye() {
        float yCenter = currentTile * TILE_WIDTH + TILE_WIDTH / 2;
        return new Vector3d(TILE_WIDTH / 2, yCenter - TILE_WIDTH / 2, 2);
    }
}
