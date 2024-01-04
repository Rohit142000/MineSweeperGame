
/**
 * @author Venkatesh Edera
 * Student Name:Venkatesh Edera
 * Student Number:999903541
 * Course Name:Advanced Programming concepts
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Venkatesh Edera
 */
public class Minesweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	Object lostMsg = "It's game over. Good Luck Next Time!";
	Object timeoutmsg = "Time to go. Best of luck the next time!";
	Object wonMsg = "You have won the Game!\nTime: ";
	Object restartMsg = "Are you certain you want to restart the game with the modified settings?";
	String[] imagenames = { "", "flag.png", "question_mark.png", "cross.png", "mine.png", "mine1.png", "clock.png" };
	public Icon[] upperImages;
	Image mine_image;
	int numberOfRows, numberOfCols, numberOfMines, timeElapsed, timeRemaining;
	boolean isGameOver;
	Dimension size;
	JMenu optionsmenu, thememenu;
	JMenuItem aboutitem;
	JMenuBar menubar;
	String[] optionsString = { "Beginner(11 mines, 6 x 9)", "Intermediate(35 mines, 12 x 18)",
			"Advanced(92 mines, 21 x 26)" };
	ButtonGroup optionsGroup = new ButtonGroup();
	JRadioButtonMenuItem[] options = new JRadioButtonMenuItem[3];
	MyJButton[][] button;
	Point[] mine;
	JPanel gamePanel = new JPanel(), scorePanel;
	JLabel timer, flag;
	TextField timerText, flagText;
	Timer clock;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Minesweeper();
			}
		});
	}

	public Minesweeper() {
		super("Minesweeper Game");
		upperImages = new Icon[7];
		for (int i = 0; i < upperImages.length; i++) {
			upperImages[i] = new ImageIcon(getClass().getResource("/resources/" + imagenames[i]));
		}
		mine_image = new ImageIcon(getClass().getResource("/resources/" + imagenames[5])).getImage();
		this.setIconImage(mine_image);
		numberOfRows = 6;
		numberOfCols = 9;
		numberOfMines = 11;
		timeRemaining = 60;
		size = new Dimension(numberOfRows * 45, numberOfCols * 35);
		setPreferredSize(size);
		menubar = new JMenuBar();
		optionsmenu = new JMenu("Change Level");
		options[0] = new JRadioButtonMenuItem(optionsString[0], true);
		options[0].setMnemonic(KeyEvent.VK_B);
		options[1] = new JRadioButtonMenuItem(optionsString[1], false);
		options[1].setMnemonic(KeyEvent.VK_I);
		options[2] = new JRadioButtonMenuItem(optionsString[2], false);
		options[2].setMnemonic(KeyEvent.VK_A);
		for (JRadioButtonMenuItem option : options) {
			optionsGroup.add(option);
			option.addActionListener(this);
			optionsmenu.add(option);
		}
		optionsmenu.setMnemonic(KeyEvent.VK_O);
		aboutitem = new JMenuItem("About", KeyEvent.VK_U);
		aboutitem.addActionListener(this);
		optionsmenu.add(aboutitem);
		menubar.add(optionsmenu);
		setJMenuBar(menubar);
		startGame();
		setVisible(true);
		requestFocusInWindow();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * The game is launched using the default level.
	 */
	public void startGame() {
		this.setPreferredSize(size);
		isGameOver = false;
		timeElapsed = 0;
		MyJButton.flagCount = 0;
		setLayout(new BorderLayout());
		scorePanel = new JPanel();
		scorePanel.setBorder(new EmptyBorder(0, 20, 0, 20));
		timer = new JLabel(upperImages[6]);
		timerText = new TextField(Integer.toString(timeRemaining), 1);
		timerText.setEditable(false);
		scorePanel.add(timer);
		scorePanel.add(timerText);
		flag = new JLabel(upperImages[1]);
		flagText = new TextField(1);
		flagText.setEditable(false);
		scorePanel.add(flag);
		scorePanel.add(flagText);
		add(scorePanel, BorderLayout.NORTH);
		gamePanel.setBorder(new EmptyBorder(0, 20, 20, 20));
		add(gamePanel, BorderLayout.CENTER);
		gamePanel.setLayout(new GridLayout(numberOfRows, numberOfCols));
		createButtons();
		clock = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				timerText.setText(Integer.toString(--timeRemaining));
				if (timeRemaining == 0) {
					timeUp();
				}
			}
		});
		paint();
		pack();
	}

	public void paint() {
		flagText.setText(Integer.toString(numberOfMines - MyJButton.flagCount));
	}

	/**
	 * Creates the buttons according on the user's level selection.
	 */
	private void createButtons() {
		button = new MyJButton[numberOfRows][numberOfCols];
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				button[i][j] = new MyJButton(i, j, this);
				gamePanel.add(button[i][j]);
			}
		}
	}

	/**
	 * Mines get randomly generated
	 * 
	 * @param firstButtonOpened
	 */
	private void generateMines(Point firstButtonOpened) {
		mine = new Point[numberOfMines];
		Random r = new Random();
		for (int i = 0; i < numberOfMines; i++) {
			mine[i] = new Point(r.nextInt(numberOfRows), r.nextInt(numberOfCols));
			if (replaceMine(mine[i], firstButtonOpened)) {
				i--;
				continue;
			}
			for (int j = 0; j < i; j++) {
				if (mine[i].equals(mine[j])) {
					i--;
					break;
				}
			}
		}
	}

	/**
	 *  Mines replaced
	 * 
	 * @param mine
	 * @param firstButtonOpened
	 * @return
	 */
	private boolean replaceMine(Point mine, Point firstButtonOpened) {
		int x = firstButtonOpened.x, y = firstButtonOpened.y;
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++) {
				if (mine.equals(new Point(i, j))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Inserts the mines into the JFrame
	 */
	private void plantMines() {
		for (int i = 0; i < numberOfMines; i++) {
			button[mine[i].x][mine[i].y].lower_limit = -1;
		}
	}

	/**
	 * When the user presses a button, the adjacent buttons are cleared.
	 */
	private void surroundMines() {
		for (int i = 0; i < numberOfMines; i++) {
			for (int j = mine[i].x - 1; j <= mine[i].x + 1; j++) {
				if (j >= 0 && j < numberOfRows) {
					for (int k = mine[i].y - 1; k <= mine[i].y + 1; k++) {
						if (k >= 0 && k < numberOfCols) {
							if (button[j][k].lower_limit >= 0) {
								button[j][k].lower_limit += 1;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * When the user clicks on the button, it opens to reveal the mines or numbers.
	 * 
	 * @param b
	 */
	public void openButton(MyJButton b) {
		if (b.isEnabled() && b.upper_limit == 0) {
			switch (b.lower_limit) {
			case -1:
				b.setIcon(upperImages[4]);
				if (!isGameOver && timeRemaining != 0) {
					String pathname = "resources/Explosion2.wav";
					try {
						AudioInputStream audioInputStream = AudioSystem
								.getAudioInputStream(new File(pathname).getAbsoluteFile());
						Clip clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						clip.start();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (UnsupportedAudioFileException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (LineUnavailableException e) {
						e.printStackTrace();
					}
					isGameOver = true;
					gameLost();
				}
				break;
			case 0:
				b.setEnabled(false);
				b.setText("");
				if (timeElapsed == 0) {
					clock.start();
					timeElapsed++;
					generateMines(new Point(b.row, b.col));
					plantMines();
					surroundMines();
				}
				for (int i = b.row - 1; i <= b.row + 1; i++) {
					if (i >= 0 && i < numberOfRows) {
						for (int j = b.col - 1; j <= b.col + 1; j++) {
							if (j >= 0 && j < numberOfCols) {
								openButton(button[i][j]);
							}
						}
					}
				}
				break;
			default:
				b.setEnabled(false);
				b.setText(Integer.toString(b.lower_limit));
				break;
			}
		}
	}

	/*
	 * Sets the buttons in the JFrame dependent on the game level.
	 */
	private void openAllButtons() {
		for (int i = 0; i < numberOfRows; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				if (button[i][j].lower_limit != -1 && button[i][j].upper_limit >= 1) {
					button[i][j].setIcon(upperImages[3]);
				}
				openButton(button[i][j]);
			}
		}
	}

	/**
	 * If the player wins, the gameWon() function is called.
	 *
	 */
	public void checkIfWon() {
		boolean gameWonFlag = true;
		for (int i = 0; i < numberOfRows && gameWonFlag; i++) {
			for (int j = 0; j < numberOfCols; j++) {
				if (button[i][j].lower_limit != -1 && button[i][j].isEnabled()) {
					gameWonFlag = false;
					break;
				}
			}
		}
		if (gameWonFlag) {
			isGameOver = true;
			gameWon();
		}
	}

	/**
	 * When he wins the game, he opens a JOptionPane to show the Game Won Message to the participants.
	 * 
	 */
	private void gameWon() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, wonMsg + timerText.getText() + " seconds", "You Won",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * 
	 * When the timer for finishing the level expires, a JOptionPane is opened to display the Game Lost Message to the players.
	 */
	public void timeUp() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, timeoutmsg, "You Lost", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * 
	 * When he clicks on the mine or the timer runs out, it opens a JOptionPane and displays the Game Lost Message to the participants.
	 */
	public void gameLost() {
		clock.stop();
		openAllButtons();
		Object[] options = { "Play Again", "Exit" };
		int selection = JOptionPane.showOptionDialog(this, lostMsg, "You Lost", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (selection == JOptionPane.YES_OPTION) {
			size = getSize();
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
		} else {
			System.exit(0);
		}
	}

	/**
	 * 
	 * This method restarts the game by setting the rows, columns, and time for that level.
	 * 
	 * @param arg0
	 */
	private void restartGame(ActionEvent arg0) {
		int selection = JOptionPane.showConfirmDialog(this, restartMsg);
		if (selection == JOptionPane.YES_OPTION) {
			clock.stop();
			if (arg0.getSource() == options[0]) {
				numberOfRows = 6;
				numberOfCols = 9;
				numberOfMines = 11;
				timeRemaining = 60;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[1]) {
				numberOfRows = 12;
				numberOfCols = 18;
				numberOfMines = 36;
				timeRemaining = 180;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[2]) {
				numberOfRows = 21;
				numberOfCols = 26;
				numberOfMines = 92;
				timeRemaining = 600;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			}
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
			setLocationRelativeTo(null);
		} else {
			switch (numberOfMines) {
			case 10:
				options[0].setSelected(true);
				break;
			case 35:
				options[1].setSelected(true);
				break;
			case 91:
				options[2].setSelected(true);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == aboutitem) {
			JOptionPane.showMessageDialog(this, "MineSweeper Game!", getTitle(), JOptionPane.INFORMATION_MESSAGE);
		} else {
			restartGame(arg0);
		}
	}

}

/**
 * This class has access to all of the parent class's variables and methods.
 * 
 * @author Venkatesh Edera
 * 
 */
class restartGame extends Minesweeper {
	private static final long serialVersionUID = 1L;

	restartGame(ActionEvent arg0) {
		int selection = JOptionPane.showConfirmDialog(this, restartMsg);
		if (selection == JOptionPane.YES_OPTION) {
			clock.stop();
			if (arg0.getSource() == options[0]) {
				numberOfRows = 6;
				numberOfCols = 9;
				numberOfMines = 11;
				timeRemaining = 60;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[1]) {
				numberOfRows = 12;
				numberOfCols = 18;
				numberOfMines = 36;
				timeRemaining = 180;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			} else if (arg0.getSource() == options[2]) {
				numberOfRows = 21;
				numberOfCols = 26;
				numberOfMines = 92;
				timeRemaining = 600;
				size = new Dimension(numberOfRows * 45, numberOfCols * 35);
			}
			remove(scorePanel);
			gamePanel.removeAll();
			startGame();
			setLocationRelativeTo(null);
		} else {
			switch (numberOfMines) {
			case 10:
				options[0].setSelected(true);
				break;
			case 35:
				options[1].setSelected(true);
				break;
			case 91:
				options[2].setSelected(true);
				break;
			default:
				break;
			}
		}

	}
}

class surroundMines extends Minesweeper {
	private static final long serialVersionUID = 0;

	public surroundMines() {
		for (int i = 0; i < numberOfMines; i++) {
			for (int j = mine[i].x - 1; j <= mine[i].x + 1; j++) {
				if (j >= 0 && j < numberOfRows) {
					for (int k = mine[i].y - 1; k <= mine[i].y + 1; k++) {
						if (k >= 0 && k < numberOfCols) {
							if (button[j][k].lower_limit >= 0) {
								button[j][k].lower_limit += 1;
							}
						}
					}
				}
			}
		}

	}
}