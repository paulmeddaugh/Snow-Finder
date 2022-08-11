package renderer;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class PlaceHolderTextField extends JTextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The text for the placeholder in the JTextfield. */
	private String text;
	
	public PlaceHolderTextField (String placeHolderText) {
		this.text = placeHolderText;
		
		this.setText(text);
		this.setForeground(Color.GRAY);
		
		this.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
		        if (((JTextField) e.getSource()).getText().equals(
		        		text)) {
		        	((JTextField) e.getSource()).setText("");
		        	((JTextField) e.getSource()).setForeground(Color.BLACK);
		        }
		    }
		});
		
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				JTextField me = (JTextField) e.getSource();
				
				if (me.getText().equals(e.getKeyChar() + text)) {
					
		        	me.setText(Character.toString(e.getKeyChar()));
		        	me.setForeground(Color.BLACK);
		        	
		        } else if (me.getText().equals("")) {
					setTextToPlaceHolder();
					me.setCaretPosition(0);
				}
			}
		});
	}
	
	public void setTextToPlaceHolder() {
		this.setText(text);
		this.setForeground(Color.GRAY);
	}
}
