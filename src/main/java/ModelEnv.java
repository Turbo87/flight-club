import java.io.*;
import java.awt.*;

interface ModelEnv {
	Image getImage(String s);
	//Image createImage(int w, int h);
	void play(String s);
	DataInputStream openFile(String s);
	Dimension getSize();
}
