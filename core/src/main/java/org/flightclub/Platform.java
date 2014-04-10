package org.flightclub;

public class Platform {

    public static boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

}
