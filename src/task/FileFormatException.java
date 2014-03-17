/** This exception may be thrown by parsers. */

package flightclub.task;

import java.io.*;

public class FileFormatException extends IOException {
    public FileFormatException(String s) { super(s); }
}
