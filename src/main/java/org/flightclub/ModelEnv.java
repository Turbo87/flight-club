/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.Dimension;
import java.awt.Image;
import java.io.DataInputStream;

public interface ModelEnv {
    Image getImage(String s);

    //Image createImage(int w, int h);
    void play(String s);

    DataInputStream openFile(String s);

    Dimension getSize();
}
