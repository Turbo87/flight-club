/**
 This code is covered by the GNU General Public License
 detailed at http://www.gnu.org/copyleft/gpl.html

 Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 Dan Burton , Nov 2001
 */

package org.flightclub;

public class XCGameFrame extends ModelFrame {
    public XCGameFrame() {
        super(new XCGame(), "Flight Club", 640, 490);
    }

    public static void main(String s[]) {
        XCGameFrame f = new XCGameFrame();
    }
}
