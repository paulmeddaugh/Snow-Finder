package snowFinder;

import java.awt.Image;
import java.io.IOException;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class TaskBarDialog {
	
	/**
	 * This method is intended to wrap a JOptionPane dialog in a JFrame to provide
	 * it an icon in the taskbar.
	 * 
	 * @param s A Supplier functional interface whose result this function returns, such
	 * as a result as a JOptionPane.
	 * @return The result of the Supplier parameter.
	 */
	public static Object wrapInJFrame(Supplier<Object> s) {
		JFrame frame = new JFrame("Snow Finder");

        frame.setUndecorated( true );
        frame.setVisible( true );
        frame.setLocationRelativeTo( null );
        
        Image snowIcon = null;
		try {
			snowIcon = ImageIO.read(TaskBarDialog.class.getResourceAsStream("/resources/icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        frame.setIconImage(snowIcon);
        
        Object result = s.get();
        frame.dispose();
        
        return result;
	}
}
