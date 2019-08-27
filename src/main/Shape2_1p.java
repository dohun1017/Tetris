package main;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.xml.ws.handler.MessageContext.Scope;

public class Shape2_1p {

	private int color;
	private int x, y, moveWall;
	private long time, lastTime;
	private int normal = 600, fast = 50;
	private int delay;
	private BufferedImage block;
	private int[][] coords;
	private int[][] reference;
	private int deltaX;
	private Board2_1p board;

	private int holdUse = 0;
	private boolean moveX = false, moveY = false;
	private boolean land = false, reachWall = false;
	private int direction = 0;

	private boolean QuickDown = false;

	public Shape2_1p(int[][] coords, BufferedImage block, Board2_1p board, int color) {
		this.coords = coords;
		this.block = block;
		this.board = board;
		this.color = color;
		deltaX = 0;
		x = 4;
		y = 0;

		// 속도 난이도 변경
		if (this.board.getScore() >= 110)
			normal = 70;
		else
			normal -= (this.board.getScore() / 10) * 50;
		delay = normal;

		time = 0;
		lastTime = System.currentTimeMillis();
		reference = new int[coords.length][coords[0].length];

		System.arraycopy(coords, 0, reference, 0, coords.length);

	}

	public void update() {
		// 움직 일 수 있게 함
		moveX = true;
		moveY = true;

		if (QuickDown)
			return;

		// 시간경과마다 한칸씩 내려오게하기위한 변수 초기화
		time += System.currentTimeMillis() - lastTime;
		lastTime = System.currentTimeMillis();
		checkX();
		checkY();
		deltaX = 0;
		if (moveY) {
			if (time > delay) {
				y++;
				time = 0;
			}
		} else {
			drawBlock();
		}
	}

	public void render(Graphics g) {

		// 현재 도형(움직이는 역할 X)
		for (int row = 0; row < coords.length; row++) {
			for (int col = 0; col < coords[0].length; col++) {
				if (coords[row][col] != 0) {
					g.drawImage(block, col * 30 + x * 30, row * 30 + y * 30, null);
				}
			}
		}
	}

	// 도형 떨어졌을 때 그리는 부분
	public void drawBlock() {
		if ((time > 500 || QuickDown)) {
			for (int row = 0; row < coords.length; row++) {
				for (int col = 0; col < coords[0].length; col++) {
					if (coords[row][col] != 0) {
						// 현재 보드에 있는 쌓인 블록들과 합침
						board.getBoard()[y + row][x + col] = color;
					}
				}
			}
			// 줄 검사(삭제)
			checkLine();
			// 현재 보드에 도형 최신화
			board.isGameOver(this);
			if(!board.getGameOver())
				board.setCurrentShape();
		}
	}

	// 가로축 블록 안겹치게 함, 좌우 이동
	public void checkX() {
		// !(x(4) + 움직인 위치 + 움직이는 도형의 X축 > 10) 그리고 !(x(4) + 움직인 위치 < 0)
		if (!(x + deltaX + coords[0].length > 10) && !(x + deltaX < 0)) {
			for (int row = 0; row < coords.length; row++) {
				for (int col = 0; col < coords[row].length; col++) {
					if (coords[row][col] != 0) { // 현재 도형의 배열 중 속성이 0이 아닐 때
						// 바닥 혹은 쌓인 블록에 닿은 경우 가로축 이동 안됨)
						if (board.getBoard()[y + row][x + deltaX + col] != 0) {
							moveX = false;
						}
					}
				}
			}
			// 가로축이동이 안되면 x(4) + 움직인 위치
			if (moveX)
				x += deltaX;
		}
	}

	// 내려올때 블록 겹치게함 방지, 내려옴
	public void checkY() {
		land = false;
		// !(y(0) + 1 + 현재 도형의 y축 > 20)
		if (!(y + 1 + coords.length > 22)) {
			for (int row = 0; row < coords.length; row++) {
				for (int col = 0; col < coords[row].length; col++) {
					if (coords[row][col] != 0) { // 현재 도형의 배열 중 속성이 0이 아닐 때
						if (board.getBoard()[y + 1 + row][x + col] != 0) { // 현재 보드의[y+1+세로][x+가로]가 0이 아닐 때
							if (moveY) {
								moveY = false;
								land = true;
							}
						}
					}
				}
			}
		}
		// 다 내려 왔을 때
		else {
			if (moveY) {
				land = true;
				moveY = false;
			}
		}
	}

	// 줄 삭제 부분
	private void checkLine() {
		/*
		 * 세로(22) * 가로(10) 한줄씩 검사 첫 번째 if 해당칸에 블록이 차있으면 count++ 두 번째 if count가 10보다 작으면
		 * size-- / count가 10보다 크거나 같으면 size--를 하지 않음 => 몇줄 없앴는지 알 수 있음 가로줄이 꽉차있으면 그 위의
		 * 줄의 내용을 덮어 씌움
		 */
		int size = board.getBoard().length - 1;
		for (int i = board.getBoard().length - 1; i >= 0; i--) {
			int count = 0;
			for (int j = 0; j < board.getBoard()[0].length; j++) {
				if (board.getBoard()[i][j] != 0)
					count++;
				board.getBoard()[size][j] = board.getBoard()[i][j];
			}
			if (count < board.getBoard()[0].length) {
				size--;
			}
		}
		if (size >= 0)
			board.addScore(size + 1);
	}

	// 도형 회전 메소드
	public void rotateShape() {
		if (QuickDown && land)
			return;

		int[][] rotatedShape = null;

		if (getDirection() == 1)
			rotatedShape = transposeMatrixRight(coords);
		else
			rotatedShape = transposeMatrixLeft(coords);
		
		//바뀔 모양이 보드의 y축보다 길어질 때 
		if ((y + rotatedShape.length > 22)) {
			return;
		}
		//바뀔 모양이 보드의 x축보다 길어질 때
		if (x + rotatedShape[0].length > 10) {
			reachWall = true;
			moveWall = 0;
			for(;x+rotatedShape[0].length != 10;) {
				x--;
				moveWall++;
			}
		} else {
			if (reachWall) {
				x+=moveWall;
				reachWall = false;
			}
		}
		
		for (int row = 0; row < rotatedShape.length; row++) {
			for (int col = 0; col < rotatedShape[row].length; col++) {
				if (rotatedShape[row][col] != 0) {
					if (board.getBoard()[y + row][x + col] != 0) {
						return;
					}
				}
			}
		}
		coords = rotatedShape;
	}

	// 도형 배열 왼쪽 회전
	private int[][] transposeMatrixLeft(int[][] matrix) {
		int[][] temp = new int[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[0].length; j++)
				temp[(matrix[0].length) - j - 1][i] = matrix[i][j];
		return temp;
	}

	// 도형 배열 오른쪽 회전
	private int[][] transposeMatrixRight(int[][] matrix) {
		int[][] temp = new int[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[0].length; j++)
				temp[j][matrix.length - i - 1] = matrix[i][j];
		return temp;
	}

	// 아래키 눌렀을 때 호출되는 메소드
	public void speedUp() {
		delay = fast;
	}

	// 아래키 뗐을 때 호출되는 메소드
	public void speedDown() {
		delay = normal;
	}

	// 스페이스키 눌렀을 때 호출되는 메소드
	public void quickDown() {
		if (board.getGameOver() || board.getGamePause())
			return;
		while (true) {
			checkY();
			if (moveY)
				y++;
			else
				break;
		}
		QuickDown = true;
		land = true;
		drawBlock();
	}
	public void previewBlock() {
		if (board.getGameOver() || board.getGamePause())
			return;
		while (true) {
			checkY();
			if (moveY)
				y++;
			else
				break;
		}
	}

	// 각종 게터, 세터
	public int getColor() {
		return color;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setDeltaX(int deltaX) {
		this.deltaX = deltaX;
	}

	public BufferedImage getBlock() {
		return block;
	}

	public int[][] getCoords() {
		return coords;
	}

	public void setCoords(int[][] coords) {
		this.coords = coords;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getHoldUse() {
		return holdUse;
	}

	public void setHoldUse(int holdUse) {
		this.holdUse = holdUse;
	}

	public boolean getLand() {
		return land;
	}
}
