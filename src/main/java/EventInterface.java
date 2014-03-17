import java.awt.event.KeyEvent;

/*
	interface to be implemented by objects (eg actors) that respond 
	to user pressing keys
	10 sep 2001
*/

interface EventInterface
{
	public void keyPressed(KeyEvent e);
	public void keyReleased(KeyEvent e);
}
