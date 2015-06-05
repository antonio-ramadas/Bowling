package graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImagePontuation {
	private BufferedImage image;

	public ImagePontuation()
	{
		try {
			image = ImageIO.read(getClass().getResourceAsStream("/bowling_scoresheet.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void process(int x, int y, String write, int size, Color textColor) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage img = new BufferedImage(
				w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.setPaint(textColor);
		g2d.setFont(new Font("Serif", Font.BOLD, size));
				
		g2d.drawString(write, x, y);
		g2d.dispose();
		
		image = img;
	}
	
	public void exportImage()
	{
		try {
			ImageIO.write(image, "png", new File("bin/scoreSheet.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public void writePlayersName(String nome1, String nome2)
	{
		process(20, 190, nome1, 20, Color.black);
		process(20, 290, nome2, 20, Color.black);
	}
	
	public void writeScoreHalfPlay(int player, int play, int turn, String points)
	{
		int x, y;
		y = 145;
		x = 185;
		
		if (player != 1)
		{
			y = 245;
		}

		for (int i = 1; i < play; i++)
		{
			x += 25;
			x += 47;
		}
		
		if (turn == 2)
		{
			x += 25;
		}
		
		if (turn == 3)
		{
			x += 50;
		}
		
		if (points.equals("/"))
		{
			process(x, y, "/", 30, Color.blue);
		}
		else
		{
			process(x, y, points, 20, Color.blue);
		}
	}
	
	public void writeScorePlay(int player, int play, String points)
	{
		int x, y;
		y = 200;
		x = 170;
		
		if (player != 1)
		{
			y = 300;
		}

		for (int i = 1; i < play; i++)
		{
			x += 70;
		}
		
		process(x, y, points, 35, Color.red);
	}
	
	public void writeFinalScore(int pointsPlayer1, int pointsPlayer2)
	{
		process(910, 200, Integer.toString(pointsPlayer1), 40, Color.green);
		process(910, 300, Integer.toString(pointsPlayer2), 40, Color.green);
	}
}
