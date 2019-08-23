package main;

import javax.swing.JFrame;

public class Window{
	//413
	public static final int WIDTH = 890, HEIGHT = 629;

	private Board board;
	private Board2_1p board2_1;
	private Board2_2p board2_2;
	private Title title;
	private JFrame window;
	
	public Window(){
		
		window = new JFrame("Tetris");
		window.setSize(WIDTH, 695);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		window.setResizable(false);	
		
//		board = new Board();
		board2_1 = new Board2_1p();
		board2_2 = new Board2_2p();
		title = new Title(this);
		
//		window.addKeyListener(board);		
		window.addKeyListener(board2_1);
		window.addKeyListener(board2_2);
		window.addMouseMotionListener(title);
		window.addMouseListener(title);
		
		window.add(title);
		
		window.setVisible(true);
	}
	public void startTetris(){
		window.remove(title);
		window.setLayout(null);
		
//		window.addMouseMotionListener(board);
//		window.addMouseListener(board);
//		board.setBounds(0, 0, 445, 700);
//		window.add(board);
//		board.startGame();

		window.addMouseMotionListener(board2_1);
		window.addMouseListener(board2_1);
		board2_1.setBounds(0, 0, 445, 700);
		window.add(board2_1);
		board2_1.startGame();

		window.addMouseMotionListener(board2_2);
		window.addMouseListener(board2_2);
		board2_2.setBounds(445,0,445,700);
		window.add(board2_2);
		board2_2.startGame();

		window.revalidate();
	}
	
	public static void main(String[] args) {
		new Window();
	}

}
