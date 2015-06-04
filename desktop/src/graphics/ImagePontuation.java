package graphics;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class ImagePontuation {
	 private BufferedImage image;

	    public TextOverlay() {
	        try {
	            image = ImageIO.read(getClass().getResourceAsStream("/arrow.png"));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        image = process(image);
	    }

	    @Override
	    public Dimension getPreferredSize() {
	        return new Dimension(image.getWidth(), image.getHeight());
	    }

	    private BufferedImage process(BufferedImage old) {
	        int w = old.getWidth();
	        int h = old.getHeight();
	        BufferedImage img = new BufferedImage(
	                w, h, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g2d = img.createGraphics();
	        g2d.drawImage(old, 0, 0, null);
	        g2d.setPaint(Color.red);
	        g2d.setFont(new Font("Serif", Font.BOLD, 20));
	        String s = "Hello, world!";
	        FontMetrics fm = g2d.getFontMetrics();
	        int x = img.getWidth() - fm.stringWidth(s) - 5;
	        int y = fm.getHeight();
	        g2d.drawString(s, x, y);
	        g2d.dispose();
	        return img;
	    }

	    private static void create() {
	        JFrame f = new JFrame();
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        f.add(new TextOverlay());
	        f.pack();
	        f.setVisible(true);
	    }
}
