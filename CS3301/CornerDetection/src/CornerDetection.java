import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.*;

/**
 * Assignment5: Corner Detection
 * Name: Weiming Chen
 * Student number: 201504727
 * 
 * This program is coding to detect the corner in an image, which is using Harris corner detection method.
 * The "Derivatives" method is to display three derivatives product using three color channels.
 * The "Corner response" method is use for calculating the the corner response in an image.
 * The "Thresholding' method is use for thresholding the corner response value which have calculated in the corner response method.
 * The "Non-max suppression" method is use for choosing the highest corner value in a corner, because Harris method is not invariant to image scaling.
 * The "Display corner" method is use for drawing the small circles centered at the corner.
 * **/


// Main class
public class CornerDetection extends Frame implements ActionListener {
	BufferedImage input;
	BufferedImage output;
	int width, height;
	double sensitivity=.1;
	int threshold=20;
	
	//2D array to hold the pixel value of an image
	double[][] Image;
	
	//ArrayList to hold the x coordinate of a pixel value
	ArrayList<Integer> x_points;
	
	//ArrayList to hold the y coordinate of a pixel value
	ArrayList<Integer> y_points;
	
	ImageCanvas source, target;
	CheckboxGroup metrics = new CheckboxGroup();

	// Constructor
	public CornerDetection(String name) {
		super("Corner Detection");
		// load image
		try {
			input = ImageIO.read(new File("/Users/Ming/Documents/workspace/CornerDetection/src/signal_hill.png"));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		Image = new double[height][width];
		x_points = new ArrayList();
		y_points = new ArrayList();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		//target = new ImageCanvas(width, height);
		main.setLayout(new GridLayout(1, 2, 10, 10));
		main.add(source);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		Button button = new Button("Derivatives");
		button.addActionListener(this);
		controls.add(button);
		// Use a slider to change sensitivity
		JLabel label1 = new JLabel("sensitivity=" + sensitivity);
		controls.add(label1);
		JSlider slider1 = new JSlider(1, 25, (int)(sensitivity*100));
		slider1.setPreferredSize(new Dimension(50, 20));
		controls.add(slider1);
		slider1.addChangeListener(changeEvent -> {
			sensitivity = slider1.getValue() / 100.0;
			label1.setText("sensitivity=" + (int)(sensitivity*100)/100.0);
		});
		button = new Button("Corner Response");
		button.addActionListener(this);
		controls.add(button);
		JLabel label2 = new JLabel("threshold=" + threshold);
		controls.add(label2);
		JSlider slider2 = new JSlider(0, 100, threshold);
		slider2.setPreferredSize(new Dimension(50, 20));
		controls.add(slider2);
		slider2.addChangeListener(changeEvent -> {
			threshold = slider2.getValue();
			label2.setText("threshold=" + threshold);
		});
		button = new Button("Thresholding");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Non-max Suppression");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Display Corners");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(Math.max(width*2+100,850), height+110);
		setVisible(true);
	}
	
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		
		/**generate dxy and square of dx, dy, 
		 * display dx square using red channel
		 * display dy square using green channel
		 * display dxy square using blue channel
		 */
		if ( ((Button)e.getSource()).getLabel().equals("Derivatives") ){
			derivativesProducts();
		}
		
		/**calculate corner response under user specified sensitivity
		 * display a result as a gray scale image
		 */
		if ( ((Button)e.getSource()).getLabel().equals("Corner Response") ){
			cornerResponse();
			
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					output.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB() );
				}
			}
			target.copyImage(output);
		}
		
		/**do the thresholding for the image and show the result**/
		if ( ((Button)e.getSource()).getLabel().equals("Thresholding") ){
			thresholding();
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					output.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB() );
				}
			}
			target.copyImage(output);
		}
		
		/**perform non-max suppression and display the detected corner pixels**/
		if ( ((Button)e.getSource()).getLabel().equals("Non-max Suppression") ){
			nonMaxSuppression();
			double r, g, b;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					r = clamp(Image[y][x]);
					g = clamp(Image[y][x]);
					b = clamp(Image[y][x]);
					output.setRGB(y, x, new Color((int)r,(int)g,(int)b).getRGB() );
				}
			}
			target.copyImage(output);
		}
		
		/**draw small circles centered at detected corners**/
		if ( ((Button)e.getSource()).getLabel().equals("Display Corners") ){
			addCornerPoints();
			target.copyImage(source.image);
			target.repaint();
		}
	}
	
	/**A function to calculate three derivatives products and display it using three color channels**/
	void derivativesProducts(){
		 
		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
						     -4, -10, 0, 10, 4, 
						     -7, -17, 0, 17, 7,
						     -4, -10, 0, 10, 4,
						     -1, -2 , 0, 2 , 1 };
		
		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
				            -2, -10, -17, -10, -2, 
				             0,  0 ,  0 ,  0 ,  0, 
				             2,  10,  17,  10,  2,
				             1,  4 ,  7 ,  4 ,  1 };
		
		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
							 4, 16, 24, 16, 1,
							 6, 24, 36, 24, 6, 
							 4, 16, 24, 16, 4,
							 1, 4 , 6 , 4 , 1 };
		
		
		int pos = 2;
		Color clr;
		int gray;
		int[] tempx = new int[25];
		int[] tempy = new int[25];
		int[] tempxy = new int[25];
		int temp_dx = 0;
		int temp_dy = 0;
		int temp_dxy = 0;
		double dx, dy, dxy;
		double dx2, dy2;
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
				temp_dxy = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						tempx[i] = gray*gaussianX[i];
						tempy[i] = gray*gaussianY[i];
						tempxy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					temp_dx += tempx[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					temp_dy += tempy[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					temp_dxy += tempxy[t];
				}
				dx = temp_dx/58;
				dx2 = dx*dx*0.05;
				if ( dx2 > 255 ) { dx2 = 255; }
				dy = temp_dy/58;
				dy2 = dy*dy*0.05;
				if ( dy2 > 255 ) { dy2 = 255; }
				//dxy = temp_dxy/256*0.05;
				dxy = dx*dy*0.09;
				if ( dxy < 0 ) { dxy = 0; }
				if ( dxy > 255 ) { dxy = 255; }
				target.image.setRGB(q, p, new Color((int)dx2, (int)dy2, (int)dxy).getRGB());
			}
		}
		target.repaint();
	}
	
	/**A function to calculate corner response value**/
	
	void cornerResponse(){
		//target.copyImage(source.image);
		initializeImage();
		
		int[] gaussianX  = { -1, -2 , 0, 2 , 1, 
			     			 -4, -10, 0, 10, 4, 
			     			 -7, -17, 0, 17, 7,
			     			 -4, -10, 0, 10, 4,
			     			 -1, -2 , 0, 2 , 1 };

		int[] gaussianY = { -1, -4 , -7 , -4 , -1,
	            			-2, -10, -17, -10, -2, 
	            			 0,  0 ,  0 ,  0 ,  0, 
	            			 2,  10,  17,  10,  2,
	            			 1,  4 ,  7 ,  4 ,  1 };

		int[] gaussianXY = { 1, 4 , 6 , 4 , 1,
				 			 4, 16, 24, 16, 1,
				 			 6, 24, 36, 24, 6, 
				 			 4, 16, 24, 16, 4,
				 			 1, 4 , 6 , 4 , 1 };

		double[] A = new double[4];
		
		int pos = 2;
		double R;
		Color clr;
		double gray;
		double[] tempx = new double[25];
		double[] tempy = new double[25];
		double[] tempxy = new double[25];
		double temp_dx = 0;
		double temp_dy = 0;
		double temp_dxy = 0;
		double dx, dy, dxy;
		double dx2, dy2;
		
		for ( int q = pos; q < height - pos; q++ ){
			for ( int p = pos; p < width - pos; p++ ){
				int i = 0;
				temp_dx = 0;
				temp_dy = 0;
				temp_dxy = 0;
				for ( int v = -pos; v <= pos; v++ ){
					for ( int u = -pos; u <= pos; u++ ){
						clr = new Color(source.image.getRGB(q+v,p+u));
						gray = (clr.getRed() + clr.getGreen() + clr.getBlue())/3;
						tempx[i] = gray*gaussianX[i];
						tempy[i] = gray*gaussianY[i];
						tempxy[i] = gray*gaussianXY[i];
						i++;
					}
				}
				for ( int t = 0; t < gaussianX.length; t++ ){
					temp_dx += tempx[t];
				}
				for ( int t = 0; t < gaussianY.length; t++ ){
					temp_dy += tempy[t];
				}
				for ( int t = 0; t < gaussianXY.length; t++ ){
					temp_dxy += tempxy[t];
				}
				dx = (temp_dx/58);
				dx2 = dx*dx;
				//System.out.println("dx2: "+dx2);
				if ( dx2 > 255 ) { dx2 = 255; }
				dy = (temp_dy/58);
				dy2 = dy*dy;
				//System.out.println("dy2: "+dy2);
				if ( dy2 > 255 ) { dy2 = 255; }
				dxy = temp_dxy/256;
				//System.out.println("dxy: "+dxy);
				if ( dxy < 0 ) { dxy = 0; }
				A[0] = dx2;
				A[1] = dxy;
				A[2] = dxy;
				A[3] = dy2;
				R = ((A[0]*A[3]-A[1]*A[2]) - sensitivity*Math.pow(A[0]+A[3], 2));
				if ( R < 0 ){ R = 0; }
				Image[q][p] = R;
			}
		}
		scaleResponseValue();
	}
	
	/**A function to perform the non-max suppression for the corner response value**/
	
	void nonMaxSuppression(){
		
		int suppression = 3;
		int xIndex = 0;
		int yIndex = 0;
		
		for ( int y = suppression, maxY = height - y; y < maxY; y++ ){
			for ( int x = suppression, maxX = width - x; x < maxX; x++ ){
				double currentValue = Image[y][x];
				double max = Image[y][x];
				for ( int i = -suppression; (currentValue != 0) && (i <= suppression); i++ ){
					for ( int j = -suppression; j <= suppression; j++ ){
						
						if ( Image[y+i][x+j] < currentValue ){
							Image[y+i][x+j] = 0;
						}
						else if ( Image[y+i][x+j] > currentValue){
							xIndex = x+j;
							yIndex = y+i;
						}
					}
				}
				x_points.add(yIndex);
				y_points.add(xIndex);
			}
		}
	}
	
	/**A function to add (x, y) coordinates of the detected corner pixels into the arrays **/
	void addCornerPoints(){
		int size = x_points.size();
		for ( int i = 0; i < size; i++ ){
			target.addXpoints(x_points.get(i));
			target.addYpoints(y_points.get(i));
		}
		x_points.clear();
		y_points.clear();
	}
	
	/**A function to initialize all the pixel value of an image to 0**/
	void initializeImage(){
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				Image[y][x] = 0;
			}
		}
	}
	
	/**A function to scale the corner response value**/
	void scaleResponseValue(){
		double mean = findMean();
		//double max = findMax();
		double scale = 255/mean;
		//double scale = 255/max;
		//System.out.println(scale);
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
					Image[y][x] = (int)(Image[y][x]*0.01);
					/*
					if ( Image[y][x] < mean ){
						Image[y][x] = 0;
						//System.out.println(Image[y][x]);
					}
					else{
						Image[y][x] = (Image[y][x]*scale+128/mean);
						//System.out.println("corner" + Image[y][x]);
					}*/
			}
		}
	}
	
	/**A function to thresholding the corner response value of an image**/

	void thresholding(){
		//target.copyImage(source.image);
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > threshold*5 ) {
					//this.Image[y][x] = 0;
					continue;
				}
				else {
					Image[y][x] = 0;
				}
			}
		}	
	}
	
	/**A function to find mean value for the image pixels**/
	
	double findMean(){
		double sum = 0;
		double mean = 0;
		double count = 0;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > 0){
					sum += Image[y][x];
					count++;
				}
			}
		}
		mean = sum/count;
		return mean;
	}
	
	/**A function to find max value for the image pixels**/
	double findMax(){
		double max = 0;
		for ( int y = 0; y < height; y++ ){
			for ( int x = 0; x < width; x++ ){
				if ( Image[y][x] > max ){
					max  = Image[y][x];
				}
			}
		}
		return max;
	}
	
	/**A function to clamp the pixel value between 0 and 255**/
	double clamp(double t){
		if ( t < 0 ){ t = 0; }
		if ( t > 255 ){ t = 255; }
		return t;
	}
	
	
	public static void main(String[] args) {
		new CornerDetection(args.length==1 ? args[0] : "signal_hill.png");
	}
	
	
	// moravec implementation
	void derivatives() {
		int l, t, r, b, dx, dy;
		Color clr1, clr2;
		int gray1, gray2;
		int valx, valy, valxy;

		for ( int q=0 ; q<height ; q++ ) {
			t = q==0 ? q : q-1;
			b = q==height-1 ? q : q+1;
			for ( int p=0 ; p<width ; p++ ) {
				l = p==0 ? p : p-1;
				r = p==width-1 ? p : p+1;
				clr1 = new Color(source.image.getRGB(l,q));
				clr2 = new Color(source.image.getRGB(r,q));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dx = (gray2 - gray1) / 3;
				clr1 = new Color(source.image.getRGB(p,t));
				clr2 = new Color(source.image.getRGB(p,b));
				gray1 = clr1.getRed() + clr1.getGreen() + clr1.getBlue();
				gray2 = clr2.getRed() + clr2.getGreen() + clr2.getBlue();
				dy = (gray2 - gray1) / 3;
				dx = Math.max(-128, Math.min(dx, 127));
				dy = Math.max(-128, Math.min(dy, 127));
				target.image.setRGB(p, q, new Color(dx+128, dy+128, 128).getRGB());
			}
		}
		target.repaint();
	}
	
}
