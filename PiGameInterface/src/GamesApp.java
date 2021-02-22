 import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;

/*
* @author Pascal Polchow, s0564053
* @version 1.0
*/

public class GamesApp {
	private static JFrame mainFrame;
	private static JLabel contentLabel;
	private static JLabel warnLabel;
	private static int gamePointer;
	private static LinkedList<JTextField> gameFields;
	private static boolean inShutDownPrompt;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					setMainFrame();
					showStartScreen();
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
		});
		addGPIOButtonHandling();
	}
	
	private static void setMainFrame() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Game Interface");
		mainFrame.setUndecorated(true);
		mainFrame.setBounds(0,0, 720, 480);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private static void showStartScreen() {
		try {
			BufferedImage startPicture = ImageIO.read(GamesApp.class.getResource("gameStart.png"));
			contentLabel = new JLabel(new ImageIcon(startPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			contentLabel = new JLabel();
		}
		contentLabel.setBounds(0,0, 720, 480);
		gamePointer = 0;
		inShutDownPrompt = false;
		addFieldToLabelPerInFolder();
		setColorOfPointedField();
		contentLabel.setVisible(true);
		mainFrame.getContentPane().add(contentLabel);
	}
	
	private static void addFieldToLabelPerInFolder() {
		gameFields = new LinkedList<JTextField>();
		File directory = new File(System.getProperty("user.dir") + "/gameJars");
		File[] files = directory.listFiles();
		if(files != null) {
			int yPos = 90;
			for (File file : files) {
				JTextField gameField = new JTextField(file.getName());
				gameField.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
				gameField.setEditable(false);
				gameField.setHorizontalAlignment(JTextField.CENTER);
				gameField.setBounds(210, yPos, 300, 60);
				gameFields.add(gameField);
				contentLabel.add(gameField);
				yPos += 75;
			}
		}
		else {
			System.out.println("Keine Datei unter " + directory.getAbsolutePath() + " gefunden.");
		}
	}
	
	private static void addGPIOButtonHandling() {
		final GpioController gpioCon = GpioFactory.getInstance();
		final GpioPinDigitalInput upButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_22, PinPullResistance.PULL_DOWN); // wirinPi pin 22 ^= physischem pin #31
		final GpioPinDigitalInput downButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_DOWN); // wirinPi pin 25 ^= physischem pin #37
		final GpioPinDigitalInput startButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_26, PinPullResistance.PULL_DOWN); // wirinPi pin 26 ^= physischem pin #32
		final GpioPinDigitalInput endButton = gpioCon.provisionDigitalInputPin(RaspiPin.GPIO_27, PinPullResistance.PULL_DOWN); // wirinPi pin 27 ^= physischem pin #36
		
		upButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(gamePointer > 0 ) {
						gamePointer = gamePointer - 1;
						setColorOfPointedField();
					}
				}
			}
		});
		
		downButton.addListener(new GpioPinListenerDigital() { 
			@Override
			public void handleGpioPinDigitalStateChangeEvent (GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(gamePointer < gameFields.size()-1) {
						gamePointer++;
						setColorOfPointedField();
					}
				}
			}
		});
		
		startButton.addListener(new GpioPinListenerDigital() { 
			@Override 
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(inShutDownPrompt == true) {
						shutDown();
					}
					else {
						if(gameFields.size() > 0) {
							startPointedGame();	
						}
					}
				}
			}
		});
	
		endButton.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent (GpioPinDigitalStateChangeEvent pinEvent) {
				if(pinEvent.getState().toString() == "HIGH") {
					if(inShutDownPrompt == false) {
						showShutDownPrompt();
					}
					else {
						inShutDownPrompt = false;
						warnLabel.setVisible(false);
						mainFrame.remove(warnLabel);
						mainFrame.revalidate();
						mainFrame.repaint();
					}
				}
			}
		});
	}
	
	private static void setColorOfPointedField() {
		for (int i = 0; i < gameFields.size(); i++) {
			JTextField textField = gameFields.get(i);
			if(gamePointer == i) {
				textField.setBackground(new Color(248,24,148));
			} 
			else {
				textField.setBackground(new Color(255,255,255));
			}
		}
	}
		
	private static void startPointedGame() {
		if(gameFields.size() > 0) {
			String command = "java -jar gameJars/" + gameFields.get(gamePointer).getText();
			try {
				Process runningGame = Runtime.getRuntime().exec(command);			
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			closeApplication();	
		}
	}
	
	private static void closeApplication() {
		JButton endElement = new JButton();
		contentLabel.add(endElement);
		Container frame = endElement.getParent();
		do
			frame = frame.getParent();
		while (frame instanceof JFrame == false);
		((JFrame)frame).dispose();
		System.exit(0);
	}
	
	private static void showShutDownPrompt() {
		try {
			BufferedImage warnPicture = ImageIO.read(GamesApp.class.getResource("shutDown.png"));
			warnLabel = new JLabel(new ImageIcon(warnPicture));
		}
		catch(IOException e1) {
			e1.printStackTrace();
			warnLabel = new JLabel();
		}
		warnLabel.setBounds(370,247, 350, 233);
		contentLabel.add(warnLabel);
		mainFrame.revalidate();
		mainFrame.repaint();
		inShutDownPrompt = true;
	}
	
	private static void shutDown() {
		String command = "sudo shutdown -h now";
		try {
			Process shutDown = Runtime.getRuntime().exec(command);			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
