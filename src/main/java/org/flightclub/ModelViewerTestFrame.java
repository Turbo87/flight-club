/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class ModelViewerTestFrame extends ModelFrame {
    public ModelViewerTestFrame() {
        super(new ModelViewerTest(), "Model Viewer Test", 550, 350);
    }

    public static void main(String s[]) {
        ModelViewerTestFrame f = new ModelViewerTestFrame();
    }
}
