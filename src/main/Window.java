package main;

import java.awt.Color;

import javax.swing.JFrame;

public class Window {
	public static final int WIDTH = 890, HEIGHT = 629;
	public static int whereBoard = 0;
	private static Board board;
	private static Board2_1p board2_1;
	private static Board2_2p board2_2;
	private static Title title;
	private static JFrame window;

	public Window() {
		
		window = new JFrame("Tetris");
		window.setSize(WIDTH, 695);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setResizable(false);
		title = new Title(this);
		window.addMouseMotionListener(title);
		window.addMouseListener(title);
		window.add(title);
		
		board = new Board();
		board2_1 = new Board2_1p();
		board2_2 = new Board2_2p();
		
		window.setVisible(true);
	}
	class MyRunnable implements Runnable{
		public void run() {
			
		}
	}

	public void startTetris() {
		window.remove(title);
		window.removeMouseMotionListener(title);
		window.removeMouseListener(title);
		window.setLayout(null);

		if (title.getPlayer() == 1) {
			title.setPlayer(0);
			
			board.setBounds(222, 0, 445, 700);
			window.add(board);

			window.addKeyListener(board);
			window.addMouseMotionListener(board);
			window.addMouseListener(board);
			
			board.setGamePause(false);
			board.startGame();
		} else if (title.getPlayer() == 2) {
			title.setPlayer(0);

			board2_2.setBounds(445, 0, 445, 700);
			window.add(board2_2);
			board2_1.setBounds(0, 0, 445, 700);
			window.add(board2_1);

			window.addKeyListener(board2_1);
			window.addKeyListener(board2_2);
			window.addMouseMotionListener(board2_2);
			window.addMouseListener(board2_2);
			
			board2_1.setGamePause(false);
			board2_2.setGamePause(false);
			board2_1.startGame();
			board2_2.startGame();
		}
		window.revalidate();
	}

	public static void goTitle() {
		if (whereBoard == 1) {
			whereBoard = 0;
			window.remove(board);
			window.removeKeyListener(board);
			window.removeMouseListener(board);
			window.removeMouseMotionListener(board);
		} else if (whereBoard == 2) {
			whereBoard = 0;
			window.remove(board2_1);
			window.remove(board2_2);
			window.removeKeyListener(board2_1);
			window.removeKeyListener(board2_2);
			window.removeMouseListener(board2_2);
			window.removeMouseListener(board2_2);
		}
		window.add(title);
		window.addMouseMotionListener(title);
		window.addMouseListener(title);
	}

	public static void main(String[] args) {
		new Window();
	}

}
