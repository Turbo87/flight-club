package flightclub.startup;

import java.awt.event.KeyEvent;

public interface ModelViewerThin {
    public void start();
    public void stop();
    public boolean handleEvent (KeyEvent e);
    public void init(ModelEnv x);
}
