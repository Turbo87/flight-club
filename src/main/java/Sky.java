/*
  Sky.java (part of 'Flight Club')
	
  This code is covered by the GNU General Public License
  detailed at http://www.gnu.org/copyleft/gpl.html
	
  Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
  Dan Burton , Nov 2001 
*/

import java.util.Vector;

/*
  Manages clouds and related met data
  NB Thermal triggers create clouds
*/

class Sky {
    private final Vector<Cloud> clouds;    //hills in order from south to north
    final ModelViewer app;
    private static float cloudBase;

    final static float RANGE = 8;    //for next /prev - dist per unit height i.e. glide angle
    private final static float BASE_HIGH = 3;
    private final static float BASE_LOW = 2;

    public Sky(ModelViewer theApp) {
        app = theApp;
        clouds = new Vector<>();
        setLow();
    }

    void addCloud(Cloud cloud) {
    /*
	  todo - keep sorted list of clouds 
	*/
        clouds.addElement(cloud);
    }

    void removeCloud(Cloud cloud) {
        clouds.removeElement(cloud);
    }

    void setHigh() {
        cloudBase = BASE_HIGH;
    }

    void setLow() {
        cloudBase = BASE_LOW;
    }

    Cloud nextCloud(Vector3d p) {
	/*
	  return first cloud downwind of p within glide
	*/

        int j = -1;
        float dy_min = RANGE * p.z;

        for (int i = 0; i < clouds.size(); i++) {
            Cloud cloud = clouds.elementAt(i);
            //if (cloud.getY(p.z) >= p.y && cloud.age < 10) {
            if (cloud.getY(p.z) >= p.y && !cloud.decaying) {
                float dy = cloud.getY(p.z) - p.y;
                if (dy < dy_min) {
                    j = i;
                    dy_min = dy;
                }
            }
        }
        if (j != -1) return clouds.elementAt(j);

        //System.out.println("Next cloud returning null !");
        return null;
    }

    Cloud prevCloud(Vector3d p) {
	/*
	  return first cloud upwind of p
	  useful when gaggle get ahead of user
	  and reach end of a tile
	*/
        int j = -1;
        float dy_min = RANGE * p.z;

        for (int i = clouds.size() - 1; i >= 0; i--) {
            Cloud cloud = clouds.elementAt(i);
            if (cloud.getY(p.z) <= p.y && cloud.age < 10) {
                float dy = p.y - cloud.getY(p.z);
                if (dy < dy_min) {
                    j = i;
                    dy_min = dy;
                }
            }
        }

        if (j != -1) return clouds.elementAt(j);

        //System.out.println("Prev cloud returning null");
        return null;
    }

    Cloud myCloud(Vector3d p) {
        for (int i = 0; i < clouds.size(); i++) {
            Cloud cd = clouds.elementAt(i);
            if (cd.isUnder(p)) return cd;
        }
        return null;
    }

    static float getCloudBase() {
        return cloudBase;
    }

    static float getWind() {
        //units of unit distance (km) per unit time (minute)
        return (float) 0.3;
    }

}
