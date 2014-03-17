/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * canvas manager - draws world, dragging on canvas moves camera
 *
 * This class is based on the framework outlined in a book called
 * 'Java Games Programming' by Niel Bartlett
 */
public class ModelCanvas extends Canvas {
    public final Color backColor = Color.white;
    public final Image backImg = null;
    private Image imgBuffer;
    int width, height;
    protected ModelViewer app = null;
    boolean dragging = false;
    private Graphics graphicsBuffer;

    private int x0 = 0, y0 = 0;
    private int dx = 0, dy = 0;

    public ModelCanvas(ModelViewer theApp) {
        app = theApp;
    }

    void init() {
        width = getSize().width;
        height = getSize().height;

        imgBuffer = app.createImage(width, height);
        graphicsBuffer = imgBuffer.getGraphics();

        //event handlers
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x0 = e.getX();
                y0 = e.getY();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dx = 0;
                dy = 0;
                dragging = false;
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                dx = e.getX() - x0;
                dy = e.getY() - y0;
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                app.eventManager.handleEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                app.eventManager.handleEvent(e);
            }
        });
    }

    void tick() {
        if (dragging) {
            //float dtheta = (float) dx/width;
            float dtheta = 0;
            float unitStep = (float) Math.PI / (app.getFrameRate() * 8);//4 seconds to 90 - sloow!

            if (dx > 20) dtheta = -unitStep;
            if (dx < -20) dtheta = unitStep;

            app.cameraMan.rotateEyeAboutFocus(-dtheta, -dy);
        }
    }

    @Override
    public void paint(Graphics g) {
        if (imgBuffer == null) return;

        updateImgBuffer();
        g.drawImage(imgBuffer, 0, 0, this);
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    public void updateImgBuffer() {
        if (backImg == null) {
            graphicsBuffer.setColor(backColor);
            graphicsBuffer.fillRect(0, 0, width, height);
        } else
            graphicsBuffer.drawImage(backImg, 0, 0, this);

        //TODO optimize - build vector of objs in FOV, need only draw these
        app.cameraMan.setMatrix();
        app.obj3dManager.sortObjects(app.cameraMan.getEye());

        for (int layer = 0; layer < app.obj3dManager.MAX_LAYERS; layer++) {
            for (int i = 0; i < app.obj3dManager.os.get(layer).size(); i++) {
                Object3d o = (Object3d) app.obj3dManager.os.get(layer).elementAt(i);
                o.film(app.cameraMan);
                o.draw(new org.flightclub.compat.Graphics(graphicsBuffer));
            }
        }

        //Text
        if (app.textMessage != null) {
            Font font = new Font("SansSerif", Font.PLAIN, 10);
            graphicsBuffer.setFont(font);
            graphicsBuffer.setColor(Color.lightGray);

            String s;
            if (!app.clock.paused) {
                s = app.textMessage;
            } else {
                s = app.textMessage + " [ paused ]";
            }
            graphicsBuffer.drawString(s, 15, height - 15);
        }

        if (app.compass != null) {
            app.compass.draw(new org.flightclub.compat.Graphics(graphicsBuffer));
        }

        if (app.slider != null) {
            app.slider.draw(new org.flightclub.compat.Graphics(graphicsBuffer));
        }

    }

}

