package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board2_2p extends JPanel implements KeyListener, MouseListener, MouseMotionListener {

	private static final int TOTALROW = 22;
	private static final long serialVersionUID = 1L;

	private BufferedImage blocks, pause, refresh, back;
	// 플레이 할 수 있는 넓이
	private final int boardHeight = 22, boardWidth = 10;
	// 블록 사이즈
	private final int blockSize = 30;
	// 필드
	private int[][] board = new int[boardHeight][boardWidth];
	// 모든 도형
	private Shape2_2p[] shapes = new Shape2_2p[7];
	// 현재도형, 다음도형, 다다음도형, 홀드도형
	private static Shape2_2p currentShape, nextShape, n_nextShape;
	Shape2_2p holdShape;
	// 게임 루프
	private Timer looper;
	// 마우스 이벤트
	private int mouseX, mouseY;
	private boolean leftClick = false;
	private Rectangle stopBounds, refreshBounds, backBounds;
	private boolean gamePaused = false;
	private boolean gameOver = false;

	// 홀드 할 수 있는지
	private boolean holdPossible = true;
	// 마우스 버튼 누른시간 체크(0.6초를 누르고 있으면 2번 해당 기능 2번 실행)
	private Timer buttonLapse = new Timer(300, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			buttonLapse.stop();
		}
	});
	// 점수
	private int score = 0;
	// 현재 도형 인덱스
	private int currentIndex;
	// 다음 도형 인덱스
	private int nextIndex[] = { (int) (Math.random() * shapes.length), (int) (Math.random() * shapes.length) };
	private boolean Up, Down, Right, Left, Quick, Hold;

	public Board2_2p() {
		// 블록 불러오기
		blocks = ImageLoader.loadImage("/tiles.png");
		// 정지, 새로고침 버튼
		pause = ImageLoader.loadImage("/pause.png");
		refresh = ImageLoader.loadImage("/refresh.png");
		back = ImageLoader.loadImage("/back.png");

		setBackground(new Color(255, 255, 240));

		// 디폴트 마우스 위치
		mouseX = 0;
		mouseY = 0;

		// 정지, 새로고침 영역
		stopBounds = new Rectangle(350, 500, pause.getWidth(), pause.getHeight() + pause.getHeight() / 2);
		refreshBounds = new Rectangle(350, 500 - refresh.getHeight() - 20, refresh.getWidth(),
				refresh.getHeight() + refresh.getHeight() / 2);
		backBounds = new Rectangle(350, 500 + back.getHeight() + 20, back.getWidth(),
				back.getHeight() + back.getHeight() / 2);
		// 게임 루퍼 생성
		looper = new Timer(90, new GameLooper());

		// 도형들 생성
		shapes[0] = new Shape2_2p(new int[][] { { 1, 1, 1, 1 } // I shape;
		}, blocks.getSubimage(0, 0, blockSize, blockSize), this, 1);

		shapes[1] = new Shape2_2p(new int[][] { { 1, 1, 1 }, { 0, 1, 0 }, // T shape;
		}, blocks.getSubimage(blockSize, 0, blockSize, blockSize), this, 2);

		shapes[2] = new Shape2_2p(new int[][] { { 1, 1, 1 }, { 1, 0, 0 }, // L shape;
		}, blocks.getSubimage(blockSize * 2, 0, blockSize, blockSize), this, 3);

		shapes[3] = new Shape2_2p(new int[][] { { 1, 1, 1 }, { 0, 0, 1 }, // J shape;
		}, blocks.getSubimage(blockSize * 3, 0, blockSize, blockSize), this, 4);

		shapes[4] = new Shape2_2p(new int[][] { { 0, 1, 1 }, { 1, 1, 0 }, // S shape;
		}, blocks.getSubimage(blockSize * 4, 0, blockSize, blockSize), this, 5);

		shapes[5] = new Shape2_2p(new int[][] { { 1, 1, 0 }, { 0, 1, 1 }, // Z shape;
		}, blocks.getSubimage(blockSize * 5, 0, blockSize, blockSize), this, 6);

		shapes[6] = new Shape2_2p(new int[][] { { 1, 1 }, { 1, 1 }, // O shape;
		}, blocks.getSubimage(blockSize * 6, 0, blockSize, blockSize), this, 7);

	}

	private void update() {
		// 게임 정지버튼 눌렀을 때(정지, 재시작)
		if (stopBounds.contains(mouseX - 445, mouseY) && leftClick && !buttonLapse.isRunning() && !gameOver) {
			buttonLapse.start();
			gamePaused = !gamePaused;
			Board2_1p.gamePaused = !Board2_1p.gamePaused;
			if (gamePaused) {
				if (holdPossible) {
					holdPossible = false;
				}
			} else {
				if (currentShape.getHoldUse() == 0)
					holdPossible = true;
			}
		}

		// 새로고침버튼 눌렀을 때
		if (refreshBounds.contains(mouseX - 445, mouseY) && leftClick) {
			Board2_1p.gameRefresh = true;
			startGame();
		}

		// 뒤로가기버튼 눌렀을 때
		if (backBounds.contains(mouseX - 445, mouseY) && leftClick) {
			leftClick = !leftClick;
			Window.whereBoard = 2;
			Window.goTitle();
			return;
		}

		// 게임정지, 게임오버 (아무것도 안할 때)
		if (gamePaused || gameOver) {
			return;
		}

		// 현재도형 업데이트
		currentShape.update();
	}

	// 페인트 부분
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// 쌓인 블록 그리기
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				if (board[row][col] != 0) {
					g.drawImage(blocks.getSubimage((board[row][col] - 1) * blockSize, 0, blockSize, blockSize),
							col * blockSize, row * blockSize, null);
				}
			}
		}
		// 다음 도형 첫 번째 그리기
		for (int row = 0; row < nextShape.getCoords().length; row++) {
			for (int col = 0; col < nextShape.getCoords()[0].length; col++) {
				if (nextShape.getCoords()[row][col] != 0) {
					g.drawImage(nextShape.getBlock(), col * 30 + 310, row * 30 + 20, null);
				}
			}
		}
		// 다음 도형 두 번째 도형 그리기
		for (int row = 0; row < n_nextShape.getCoords().length; row++) {
			for (int col = 0; col < n_nextShape.getCoords()[0].length; col++) {
				if (n_nextShape.getCoords()[row][col] != 0) {
					g.drawImage(n_nextShape.getBlock(), col * 30 + 310, row * 30 + 110, null);
				}
			}
		}
		// 홀드 도형 그리기
		if (holdShape != null)
			for (int row = 0; row < holdShape.getCoords().length; row++) {
				for (int col = 0; col < holdShape.getCoords()[0].length; col++) {
					if (holdShape.getCoords()[row][col] != 0) {
						g.drawImage(holdShape.getBlock(), col * 30 + 310, row * 30 + 220, null);
					}
				}
			}

		// 게임오버가 아닐 때 현재도형 그리기
		if (!gameOver)
			currentShape.render(g);

		// 게임 정지버튼 위에 올려놨을 때 버튼 모양

		if (stopBounds.contains(mouseX - 445, mouseY))
			g.drawImage(
					pause.getScaledInstance(pause.getWidth() + 3, pause.getHeight() + 3, BufferedImage.SCALE_DEFAULT),
					stopBounds.x + 3, stopBounds.y + 3, null);
		// 게임 정지버튼 위에서 해제 버튼 모양
		else {
			g.drawImage(pause, stopBounds.x, stopBounds.y, null);
		}
		// 게임 재시작버튼 위에 올려놨을 때 버튼 모양
		if (refreshBounds.contains(mouseX - 445, mouseY))
			g.drawImage(refresh.getScaledInstance(refresh.getWidth() + 3, refresh.getHeight() + 3,
					BufferedImage.SCALE_DEFAULT), refreshBounds.x + 3, refreshBounds.y + 3, null);
		// 게임 재시작버튼 위에서 해제 버튼 모양
		else
			g.drawImage(refresh, refreshBounds.x, refreshBounds.y, null);

		// 게임 뒤로가기버튼 위에 올려놨을 때 버튼 모양
		if (backBounds.contains(mouseX - 445, mouseY))
			g.drawImage(back.getScaledInstance(back.getWidth() + 3, back.getHeight() + 3, BufferedImage.SCALE_DEFAULT),
					backBounds.x + 3, backBounds.y + 3, null);
		// 게임 뒤로가기버튼 위에서 해제 버튼 모양
		else
			g.drawImage(back, backBounds.x, backBounds.y, null);

		// 게임 정지시
		if (gamePaused) {
			String gamePausedString = "Game Paused";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Georgia", Font.BOLD, 30));
			g.drawString(gamePausedString, 35, Window.HEIGHT / 2);
		}
		// 게임 오버시
		if (gameOver) {
			String gameOverString = "Game Over";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Georgia", Font.BOLD, 30));
			g.drawString(gameOverString, 50, Window.HEIGHT / 2);
		}
		// 점수 텍스트 색깔, 폰트, 그리기
		g.setColor(Color.BLACK);
		g.setFont(new Font("Georgia", Font.BOLD, 20));
		g.drawString("SCORE", 320, Window.HEIGHT / 2 + 40);
		g.drawString(score + "", 320, Window.HEIGHT / 2 + 70);

		Graphics2D g2d = (Graphics2D) g;
		// 선굵기, 색깔
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(new Color(0, 0, 0, 100));

		// 가로줄 그리기
		for (int i = 0; i <= TOTALROW; i++) {
			g2d.drawLine(0, i * blockSize, boardWidth * blockSize, i * blockSize);
			if (i == 1) {
				g2d.setStroke(new BasicStroke(4.0f));
			} else
				g2d.setStroke(new BasicStroke(2));
		}

		// 세로줄 그리기
		for (int j = 0; j <= boardWidth; j++) {
			g2d.drawLine(j * blockSize, 0, j * blockSize, boardHeight * 30);
		}

		// 홀드영역 표시
		g2d.drawRoundRect(305, 200, 128, 120, 5, 5);
	}

	// 게임 시작 시 호출
	public void startGame() {
		stopGame();
		setNextShape();
		setCurrentShape();
		holdShape = null;
		if (gamePaused)
			holdPossible = false;
		else
			holdPossible = true;
		currentShape.setHoldUse(0);
		gameOver = false;
		looper.start();
	}

	// 게임 정지 시 호출(게임오버)
	public void stopGame() {
		score = 0;
		holdPossible = false;
		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				board[row][col] = 0;
			}
		}
		looper.stop();
	}

	// 게임 시작
	class GameLooper implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			moveShape();
			update();
			repaint();
		}

	}

	// 기능부분

	// 현재 도형 설정
	public void setCurrentShape() {
		currentShape = nextShape;
		currentIndex = nextIndex[0];
		setNextShape();
		holdPossible = true;

	}

	// 다음 도형 설정(모든 도형)
	public void setNextShape() {
		nextIndex[0] = nextIndex[1];
		nextIndex[1] = (int) (Math.random() * shapes.length);
		nextShape = new Shape2_2p(shapes[nextIndex[0]].getCoords(), shapes[nextIndex[0]].getBlock(), this,
				shapes[nextIndex[0]].getColor());
		n_nextShape = new Shape2_2p(shapes[nextIndex[1]].getCoords(), shapes[nextIndex[1]].getBlock(), this,
				shapes[nextIndex[1]].getColor());
	}

	// 게임 오버 검사
	public boolean isGameOver(Shape2_2p currentShape) {

		for (int col = 0; col < board[0].length; col++) {
			if (board[2][col] != 0 && currentShape.getLand()) {
				gameOver = true;
			}

		}
		return gameOver;
	}

	public int[][] getBoard() {
		return board;
	}

	// 도형 홀드 메소드
	public void holdShape() {
		if (holdPossible) {
			if (currentShape.getHoldUse() == 0) {
				if (holdShape == null) {
					holdShape = new Shape2_2p(shapes[currentIndex].getCoords(), shapes[currentIndex].getBlock(), this,
							shapes[currentIndex].getColor());
					currentShape = nextShape;
					setNextShape();
				} else {
					Shape2_2p temp;
					temp = holdShape;
					holdShape = new Shape2_2p(shapes[currentIndex].getCoords(), shapes[currentIndex].getBlock(), this,
							shapes[currentIndex].getColor());
					currentShape = temp;
				}
			}
			currentShape.setHoldUse(1);
		}
		holdPossible = false;
	}

	// 키 눌렀을 때 이벤트들
	public void moveShape() {
		if (Up) {
			currentShape.setDirection(1);
			currentShape.rotateShape();
			Up = false;
		}
		if (Right)
			currentShape.setDeltaX(1);
		if (Left)
			currentShape.setDeltaX(-1);
		if (Down)
			currentShape.speedUp();
		if (Hold)
			holdShape();
		if (Quick) {
			currentShape.quickDown();
			Quick = false;
		}
		if (!Down)
			currentShape.speedDown();
		else
			return;
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			Up = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			Right = true;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			Left = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			Down = true;
		if (e.getKeyCode() == KeyEvent.VK_L)
			Hold = true;
		if (e.getKeyCode() == KeyEvent.VK_K)
			Quick = true;
	}

	// 내려가는 키 뗄 때 원래의 속도
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			Up = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			Right = false;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			Left = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			Down = false;
		if (e.getKeyCode() == KeyEvent.VK_L)
			Hold = false;
		if (e.getKeyCode() == KeyEvent.VK_K)
			Quick = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	// 마우스 관련
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			leftClick = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			leftClick = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public void addScore(int line) {
		score += line;
	}

	// 게터 세터 부분
	public void setGamePause(boolean gamePaused) {
		this.gamePaused = gamePaused;
	}

	public boolean getGameOver() {
		return gameOver;
	}

	public boolean getGamePause() {
		return gamePaused;
	}

	public int getScore() {
		return score;
	}
}
