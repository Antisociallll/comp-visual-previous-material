import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

// Canvas for image display
class ImageCanvas extends Canvas {
	BufferedImage image;
	
	//Array list to store the set of line points. 
	ArrayList<Integer> x0list = new ArrayList();
	ArrayList<Integer> x1list = new ArrayList();
	ArrayList<Integer> y0list = new ArrayList();
	ArrayList<Integer> y1list = new ArrayList();
	
	//Array list to store the set of circle points and radius.
	ArrayList<Integer> xlist = new ArrayList();
	ArrayList<Integer> ylist = new ArrayList();
	ArrayList<Integer> rlist = new ArrayList();
	
	// initialize the image and mouse control
	public ImageCanvas(BufferedImage input) {
		//input = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		image = input;
		addMouseListener(new ClickListener());
	}
	public ImageCanvas(int width, int height) {
		//image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		addMouseListener(new ClickListener());
	}

	// redraw the canvas
	public void paint(Graphics g) {
		// draw boundary
		g.setColor(Color.gray);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);
		// compute the offset of the image.
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		g.drawImage(image, xoffset, yoffset, this);
		
		//draw line
		int size1 = x0list.size();
		for ( int i = 0; i < size1; i++ ){
			g.setColor(Color.RED);
			g.drawLine(x0list.get(i), y0list.get(i), x1list.get(i), y1list.get(i));
		}
		x0list.clear();
		x1list.clear();
		y0list.clear();
		y1list.clear();
		
		//draw circle
		int size2 = xlist.size();
		for ( int i = 0; i < size2; i++ ){
			g.setColor(Color.RED);
			g.drawOval(xlist.get(i), ylist.get(i), rlist.get(i)*2, rlist.get(i)*2);
		}
		xlist.clear();
		ylist.clear();
		rlist.clear();
	}
	
	//a function to add the line starting points and the line ending points.
	public void addLinePoint(int x0, int y0, int x1, int y1){
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		x0list.add(x0+xoffset);
		y0list.add(y0+yoffset);
		x1list.add(x1+xoffset);
		y1list.add(y1+yoffset);
	}
	
	//a function to add the circle center points and radius.
	public void addCirclePoint(int x, int y, int R ){
		int xoffset = (getWidth() - image.getWidth()) / 2;
		int yoffset = (getHeight() - image.getHeight()) / 2;
		xlist.add((x+xoffset)-R);
		ylist.add((y+yoffset)-R);
		rlist.add(R);
	}
	
	// reset an empty image
	public void resetBuffer(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		repaint();
	}
	// reset image based on the input
	public void copyImage(BufferedImage input) {
		Graphics2D g2D = image.createGraphics();
		g2D.drawImage(input, 0, 0, null);
		repaint();
	}

	// listen to mouse click
	class ClickListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON3 )
				try {
					ImageIO.write(image, "png", new File("saved.png"));
				} catch ( Exception ex ) {
					ex.printStackTrace();
				}
		}
	}
}