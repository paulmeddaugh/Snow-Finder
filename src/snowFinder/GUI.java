package snowFinder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;

public class GUI extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8832085278681447439L;

	static JTextArea area = new JTextArea("Loading...");
	
	private Float mileRadius;
	private Integer zip;
	
	private Integer searchingMethod;
	
	public static final int NEAR_ZIP_CODE = 0;
	public static final int MAJOR_CITIES = 1;
	
	/**
	 * Prompts for the inputs to receive the snow depth in a specified location,
	 * and loads a JFrame with a JTextArea to display the results found.
	 */
	public GUI () {
		
		final String[] possibleValues = { "A Zip Code", 
			"Major U.S. Cities" };
		
		Object selectedValue = TaskBarDialog.wrapInJFrame(() -> 
			JOptionPane.showInputDialog(this, 
					"Where would you like to search for snow?", "Snow Searching Location",
					JOptionPane.INFORMATION_MESSAGE, null,
					possibleValues, possibleValues[0])
		);
		
		if (selectedValue == null) { // Closed
			System.exit(0);
			
		} else if (selectedValue.toString().equals(possibleValues[0])) { // Zip with Mile Radius
			
			searchingMethod = NEAR_ZIP_CODE;
			
			JTextField zipField = new JTextField();
			JTextField mileRadiusField = new JTextField();
			mileRadiusField.setText("Within less then 500 miles");
			mileRadiusField.setForeground(Color.GRAY);
			mileRadiusField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
			        if (((JTextField) e.getSource()).getText().equals(
			        		"Within less then 500 miles")) {
			        	((JTextField) e.getSource()).setText("");
			        	((JTextField) e.getSource()).setForeground(Color.BLACK);
			        }
			    }
			});
			
			Object[] fields = {
					"Please enter a zip code: ", zipField,
					"Please enter the mile radius to search within: ", mileRadiusField
			};
			
			while (true) {
				
				String errorMessage = "";
				Integer option = (Integer) TaskBarDialog.wrapInJFrame(() -> 
					JOptionPane.showConfirmDialog(this, fields, 
						"Snow Depth Near a Zip Code", JOptionPane.OK_CANCEL_OPTION)
				);
				
				if (option == null || option == JOptionPane.CANCEL_OPTION) {
					System.exit(0);
				}
				
				// Checks zip-code
				try {
					if (!zipField.getText().matches("[0-9]{5}")) {
						throw new NumberFormatException();
					} else {
						this.zip = Integer.parseInt(zipField.getText());
					}
				} catch (NumberFormatException nfe) {
					errorMessage = "Invalid zip code.";
				}
				
				// Checks mile radius range
				try {
					Float radius = null;
					if ((radius = Float.parseFloat(mileRadiusField.getText())) >= 0 && 
							radius < 500) {
						this.mileRadius = radius;
					} else {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException nfe) {
					errorMessage = (errorMessage != "") ? 
							errorMessage.substring(0, errorMessage.length() - 1)
							+ ", and m" : "M";
					
					errorMessage += "ile radius must be number between 0 and 500.";
				}
				
				if (!errorMessage.equals("")) {
					
					JOptionPane.showInternalMessageDialog(null, errorMessage,
						"What Do You Take Me For?", JOptionPane.INFORMATION_MESSAGE);
					
					zipField.setText("");
					mileRadiusField.setText("");
					
				} else {
					break;
				}
			}
			
		} else if (selectedValue.equals(possibleValues[1])) { // Major Cities
			
			searchingMethod = MAJOR_CITIES;
			
		}
		
		area.setMargin(new Insets(10, 10, 10, 10));
		area.setEditable(false);
		
		JScrollPane scrollable = new JScrollPane(area);
		scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);  
		scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		
		this.getContentPane().add(scrollable, BorderLayout.CENTER);
		
		// Loads icon
		Image icon = null;
		try {
			icon = ImageIO.read(GUI.class.getResourceAsStream("/resources/icon.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.setTitle("Snow Finder");
		this.setIconImage(icon);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(400, 500));
		this.setResizable(false);
		this.setVisible(true);
		
		this.pack();
		this.setLocationRelativeTo(null);
	}
	
	public Float getMileRadius() {
		return mileRadius;
	}

	public Integer getZip() {
		return zip;
	}
	
	public Integer getSearchingMethod () {
		return searchingMethod;
	}
	
	public String getText() {
		return area.getText();
	}

	public void setText(String text) {
		area.setText(text);
	}
	
	
}
