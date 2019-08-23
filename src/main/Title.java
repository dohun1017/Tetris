package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Title extends JPanel implements MouseListener, MouseMotionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int mouseX, mouseY;
	private Rectangle bounds_1p, bounds_2p;
	private boolean leftClick = false;
	private BufferedImage title, instructions, play_1p, play_2p;
	private Window window;
	private BufferedImage[] playButton_1p = new BufferedImage[2];
	private BufferedImage[] playButton_2p = new BufferedImage[2];
	private Timer timer;
	private int player = 0;
	
	public Title(Window window){
		setBackground(Color.BLACK);
		try {
			title = ImageIO.read(Board.class.getResource("/Title.png"));
			instructions = ImageIO.read(Board.class.getResource("/arrow.png"));
			play_1p = ImageIO.read(Board.class.getResource("/1pPlay.png"));
			play_2p = ImageIO.read(Board.class.getResource("/2pPlay.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		timer = new Timer(1000/60, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
			
		});
		timer.start();
		mouseX = 0;
		mouseY = 0;
		
		playButton_1p[0] = play_1p.getSubimage(0, 0, 100, 80);
		playButton_1p[1] = play_1p.getSubimage(100, 0, 100, 80);
		
		bounds_1p = new Rectangle(315, Window.HEIGHT/2 - 100, 100, 80);

		playButton_2p[0] = play_2p.getSubimage(0, 0, 100, 80);
		playButton_2p[1] = play_2p.getSubimage(100, 0, 100, 80);
		
		bounds_2p = new Rectangle(475, Window.HEIGHT/2 - 100, 100, 80);
		this.window = window;
		
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		if(leftClick && bounds_1p.contains(mouseX, mouseY)) {
			player = 1;
			leftClick = false;
			window.startTetris();
		}
		else if(leftClick && bounds_2p.contains(mouseX, mouseY)) {
			player = 2;
			leftClick = false;
			window.startTetris();
		}
			
		g.setColor(Color.BLACK);
		
		g.fillRect(0, 0, Window.WIDTH, Window.HEIGHT);
		
		g.drawImage(title, Window.WIDTH/2 - title.getWidth()/2, Window.HEIGHT/2 - title.getHeight()/2 - 200, null);
		g.drawImage(instructions, Window.WIDTH/2 - instructions.getWidth()/2,
				Window.HEIGHT/2 - instructions.getHeight()/2 + 150, null);
		
		if(bounds_1p.contains(mouseX, mouseY))
			g.drawImage(playButton_1p[0], 315, Window.HEIGHT/2 - 100, null);
		else
			g.drawImage(playButton_1p[1], 315, Window.HEIGHT/2 - 100, null);
		
		if(bounds_2p.contains(mouseX, mouseY))
			g.drawImage(playButton_2p[0], 475, Window.HEIGHT/2 - 100, null);
		else
			g.drawImage(playButton_2p[1], 475, Window.HEIGHT/2 - 100, null);
		
		
	}
	

	public int getPlayer() {
		return player;
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	@Override
	public void mouseClicked(MouseEvent e) {	
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			leftClick = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			leftClick = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

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
}
