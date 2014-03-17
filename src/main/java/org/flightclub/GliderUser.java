/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * a glider that the user may control
 */
public class GliderUser extends Glider implements EventInterface {
    final Variometer vario;

    public GliderUser(ModelViewer app, Vector3d p) {
        //set flag so camera will follow my cuts when in mode 1
        //(see glider.gotoNextLiftSource)
        super(app, p, true);
        app.eventManager.addNotification(this);
        vario = new Variometer(app, this);
    }

    protected void createTail() {
        int tailLength = 60;    //80
        tail = new Tail(app, tailLength, new Color(120, 120, 120));
        tail.init(p);
    }

    void checkBounds() {
        if (p.x > Landscape.TILE_WIDTH / 2) moveManager.setTargetPoint(new Vector3d(0, p.y, 0));
        if (p.x < -Landscape.TILE_WIDTH / 2) moveManager.setTargetPoint(new Vector3d(0, p.y, 0));
        if (p.y < -Landscape.TILE_WIDTH / 2) moveManager.setTargetPoint(new Vector3d(p.x, 0, 0));
    }

    @Override
    public void tick(Clock c) {
        super.tick(c);
        checkBounds();
        vario.tick();
    }

    void toggleSpeed() {
        if (polarIndex == 0)
            setPolarIndex(1);
        else
            setPolarIndex(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case 122://z
            case 122 - 32:
                //hack
                tryLater = 0;
                demoMode = false;
                moveManager.setNextMove(MovementManager.LEFT);
                break;
            case 120://x
            case 120 - 32:
                tryLater = 0;
                demoMode = false;
                moveManager.setNextMove(MovementManager.RIGHT);
                break;
            case 32://space
                //slow down, if i was fast
                setPolarIndex(0);
                moveManager.workLift();
                break;
            case 97://a
            case 97 - 32:
                toggleSpeed();
                break;
            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case 122://z
            case 122 - 32:
                moveManager.setNextMove(MovementManager.STRAIGHT);
                break;
            case 120://x
            case 120 - 32:
                moveManager.setNextMove(MovementManager.STRAIGHT);
                break;
            default:
        }
    }
}

