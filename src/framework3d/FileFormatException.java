/** This exception may be thrown by parsers. */

package flightclub.framework3d;

import java.io.*;

public class FileFormatException extends IOException {
    public FileFormatException(String s) { super(s); }
}
