// Skeletal program for the "Image Threshold" assignment
// Written by:  Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageThreshold extends Frame implements ActionListener {
	BufferedImage input;
	BufferedImage output;
	int width, height;
	TextField texThres, texOffset;
	ImageCanvas source, target;
	PlotCanvas plot;
	// Constructor
	public ImageThreshold(String name) {
		super("Image Histogram");
		// load image
		try {
			input = ImageIO.read(new File("/Users/Ming/Documents/workspace/ImageThresholding/src/sonnet.png"));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		target = new ImageCanvas(output);
		plot = new PlotCanvas(256, 200);
		target = new ImageCanvas(width, height);
		target.copyImage(output);
		main.setLayout(new GridLayout(1, 3, 10, 10));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
		controls.add(new Label("Threshold:"));
		texThres = new TextField("128", 2);
		controls.add(texThres);
		Button button = new Button("Manual Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Automatic Selection");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Ostu's Method");
		button.addActionListener(this);
		controls.add(button);
		controls.add(new Label("Offset:"));
		texOffset = new TextField("10", 2);
		controls.add(texOffset);
		button = new Button("Adaptive Mean-C");
		button.addActionListener(this);
		controls.add(button);
		// add two panels
		add("Center", main);
		add("South", controls);
		addWindowListener(new ExitListener());
		setSize(width*2+400, height+100);
		setVisible(true);
	}
	class ExitListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
	// Action listener for button click events
	public void actionPerformed(ActionEvent e) {
		// example -- compute the average color for the image
		/*if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) {
			int threshold;
			try {
				threshold = Integer.parseInt(texThres.getText());
			} catch (Exception ex) {
				texThres.setText("128");
				threshold = 128;
			}
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 100));
		}*/
		
		/**Manual selection**/
		if ( ((Button)e.getSource()).getLabel().equals("Manual Selection") ) {
			int threshold = Integer.parseInt(texThres.getText());
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < height; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getRed() < threshold ){
						red[y][x] = 0; //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getRed() >= threshold ){
						red[y][x] = 255; // set white
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() < threshold ){
						green[y][x] = 0;  //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() >= threshold ){
						green[y][x] = 255;  //set white
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() < threshold ){
						blue[y][x] = 0;  //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() >= threshold ){
						blue[y][x]= 255; //set white
					}
					ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			/**draw threshold value line**/
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.BLACK, threshold, 100));
			/**get the output image**/
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.copyImage(output);
		}
		
		/**Automatic selection**/
		if ( ((Button)e.getSource()).getLabel().equals("Automatic Selection") ) {
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			int[][] ImageMatrix = new int[height][width];
			int rthreshold; int gthreshold; int bthreshold;
			rthreshold = gthreshold = bthreshold = 128; //initialize the threshold value.
			//automatic_selection(rthreshold, gthreshold, bthreshold);
			/**apply automatic selection for each three colors**/
			rthreshold = auto_red(rthreshold);
			gthreshold = auto_green(gthreshold);
			bthreshold = auto_blue(bthreshold);
			/**apply threshold value to the image**/
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < height; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getRed() < rthreshold ){
						red[y][x] = 0; //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getRed() >= rthreshold ){
						red[y][x] = 255; // set white;
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() < gthreshold ){
						green[y][x] = 0;
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() >= gthreshold ){
						green[y][x] = 255;
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() < bthreshold ){
						blue[y][x] = 0;
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() >= bthreshold ){
						blue[y][x]= 255;
					}
					ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			/**draw the threshold value line**/
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, rthreshold, 100));
			plot.addObject(new VerticalBar(Color.GREEN, gthreshold, 100));
			plot.addObject(new VerticalBar(Color.BLUE, bthreshold, 100));
			/**get the output image**/
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.copyImage(output);
		}
		
		/**Ostu's Methos**/
		if ( ((Button)e.getSource()).getLabel().equals("Ostu's Method") ) {
			int[][] ImageMatrix = new int[height][width];
			int[] rhist = new int[256];
			int[] ghist = new int[256];
			int[] bhist = new int[256];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			float[] rPDF = new float[256];
			float[] gPDF = new float[256];
			float[] bPDF = new float[256];
			/**obtain pixel value from the image**/
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < width; x++ ){
					rhist[(new Color(source.image.getRGB(y, x))).getRed()]++;
					ghist[(new Color(source.image.getRGB(y, x))).getGreen()]++;
					bhist[(new Color(source.image.getRGB(y, x))).getBlue()]++;
				}
			}
			/**calculate the PDF of the pixel value**/
			for ( int i = 0; i < 256; i++ ){
				rPDF[i] = (float)rhist[i]/(height*width);
				gPDF[i] = (float)ghist[i]/(height*width);
				bPDF[i] = (float)bhist[i]/(height*width);
			}
			/**apply Ostu's method**/
			int rthreshold = OstuThreshold(rPDF);
			//System.out.println(rthreshold);
			int gthreshold = OstuThreshold(gPDF);
			//System.out.println(gthreshold);
			int bthreshold = OstuThreshold(bPDF);
			//System.out.println(bthreshold);
			/**apply threshold value to the image**/
			for ( int y = 0; y < height; y++ ){
				for ( int x = 0; x < height; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getRed() < rthreshold ){
						red[y][x] = 0; //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getRed() >= rthreshold ){
						red[y][x] = 255; // set white
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() < gthreshold ){
						green[y][x] = 0; //set back
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() >= gthreshold ){
						green[y][x] = 255; //set white
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() < bthreshold ){
						blue[y][x] = 0;  //set black
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() >= bthreshold ){
						blue[y][x]= 255;  //set white
					}
					ImageMatrix[y][x] = red[y][x]<<16 | green[y][x]<<8 | blue[y][x];
				}
			}
			/**draw the threshold value line**/
			plot.clearObjects();
			plot.addObject(new VerticalBar(Color.RED, rthreshold, 100));
			plot.addObject(new VerticalBar(Color.GREEN, gthreshold, 100));
			plot.addObject(new VerticalBar(Color.BLUE, bthreshold, 100));
			/**get the output image**/
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.copyImage(output);
		}
		
		/**Mean-C adaptive approach**/
		if ( ((Button)e.getSource()).getLabel().equals("Adaptive Mean-C") ) {
			int[][] ImageMatrix = new int[height][width];
			int[][] red = new int[height][width];
			int[][] green = new int[height][width];
			int[][] blue = new int[height][width];
			ImageMatrix = mean_c(ImageMatrix, red, green, blue); //apply mean-C approach
			/**get the output image**/
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					output.setRGB(y, x, ImageMatrix[y][x]);
				}
			}
			target.copyImage(output);
			plot.clearObjects();
		}
	}
	
	/**Mean-C adaptive approach**/
	public int[][] mean_c(int[][] ImageMatrix, int[][] rmatrix, int[][] gmatrix, int[][] bmatrix) {
        int blue, red, green, rmean, gmean, bmean, count;
        int maskSize = 7; 
        int C = Integer.parseInt(texOffset.getText());
        for(int q = 0; q < height; q++){
            for(int p = 0; p < width; p++){
                red = green = blue = 0;
                count = 0;
                for(int v = q - (maskSize / 2); v <= q + (maskSize / 2); v++){
                    for(int u = p - (maskSize / 2); u <= p + (maskSize / 2); u++){
                        if(v < 0 || v >= height || u < 0 || u >= width){
                            /** Some portion of the mask is outside the image. */
                            continue;
                        }else{
                            try{
                            	/**the sum value for each color**/
                            	red += (new Color(source.image.getRGB(u, v))).getRed();
                            	green += (new Color(source.image.getRGB(u, v))).getGreen();
                                blue += (new Color(source.image.getRGB(u, v))).getBlue();
                                count++;
                            }catch(ArrayIndexOutOfBoundsException e){
                            }
                        }
                    }
                }
                /**calculate the mean value for each color**/
                rmean = red/count - C;
                gmean = green/count - C;
                bmean = blue/count - C;
                /**Use mean value as the threshold value**/
                if((new Color(source.image.getRGB(p, q))).getBlue() >= rmean){
                    rmatrix[p][q] = 0xffffffff;     //WHITE 
                }else{
                	rmatrix[p][q] = 0xff000000;     //BLACK 
                }
                if((new Color(source.image.getRGB(p, q))).getBlue() >= gmean){
                    gmatrix[p][q] = 0xffffffff;     //WHITE
                }else{
                	gmatrix[p][q] = 0xff000000;     //BLACK
                }
                if((new Color(source.image.getRGB(p, q))).getBlue() >= bmean){
                    bmatrix[p][q] = 0xffffffff;     //WHITE
                }else{
                	bmatrix[p][q] = 0xff000000;     //BLACK
                }
                ImageMatrix[p][q] = rmatrix[p][q]<<16 | gmatrix[p][q]<<8 | bmatrix[p][q];
            }
        }
		return ImageMatrix;
	}
	
	/**Ostu's Thresholding**/ 
	public int OstuThreshold(float[] PDF) {
		double max_var = 0; int threshold = 0;
		/**calculate w0, w1, u0, u1**/
		for ( int i = 0; i < 256; i++ ){
			double w0 = 0; double w1 = 0; double u0 = 0; double u1 = 0; double var = 0; double u = 0; double u0temp = 0; double u1temp = 0;
			for ( int j = 0; j < 256; j++ ){
				if ( j < i ){ 
					w0 += PDF[j];
					u0temp += j*PDF[j];
				}
				if( j >= i ) { 
					w1 += PDF[j]; 
					u1temp += j*PDF[j];
				}
			}
			u0 = u0temp/w0;
			u1 = u1temp/w1;
			u = u0temp + u1temp;
			var = (w0*Math.pow(u0-u, 2))+(w1*Math.pow(u1-u, 2)); //calculate intra-variance.
			/**Get the minimum intra-variance**/
			if ( var > max_var ){
				max_var = var;
				threshold = i;
			}
		}
		return threshold;
	}

	/**automatic selection for red**/
	public int auto_red(int rthreshold){
		int rtemp = 0; 
		int rsum0 = 0; 
		int rsum1 = 0; 
		double ru0 = 0; double ru1 = 0; int rcount0 = 0; int rcount1 = 0; double num = 0.1;
		/**calculate the two different groups of red value**/
		while( Math.abs(rthreshold - rtemp) > num ) {
			rtemp = rthreshold;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getRed() < rthreshold ){
						rsum0 += (new Color(source.image.getRGB(y, x))).getRed();
						rcount0++;
					}
					if ( (new Color(source.image.getRGB(y, x))).getRed() >= rthreshold ){
						rsum1 += (new Color(source.image.getRGB(y, x))).getRed();
						rcount1++;
					}
				}
			}
			/**apply the automatic selection function to calculate the red threshold**/
			ru0 = rsum0/rcount0; ru1 = rsum1/rcount1;
			rthreshold = (int)(ru0+ru1)/2;
			//System.out.println(rthreshold);
	 	}
		return rthreshold;
	}
	
	/**automatic selection for green**/
	public int auto_green(int gthreshold){
		int gtemp = 0;
		int gsum0 = 0;  
		int gsum1 = 0;  double gu0 = 0; double gu1 = 0;
		int gcount0 = 0; int gcount1 = 0; double num = 0.1;
		/**calculate the two different groups of green value**/
		while( Math.abs(gthreshold - gtemp) > num){
			gtemp = gthreshold;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getGreen() < gthreshold ){
						gsum0 += (new Color(source.image.getRGB(y, x))).getGreen();
						gcount0++;
					}
					if ( (new Color(source.image.getRGB(y, x))).getGreen() >= gthreshold ){
						gsum1 += (new Color(source.image.getRGB(y, x))).getGreen();
						gcount1++;
					}
				}
			}
			/**apply the automatic selection function to calculate the green threshold**/
			gu0 = gsum0/gcount0; gu1 = gsum1/gcount1;
			gthreshold = (int)(gu0+gu1)/2;
			//System.out.println(gthreshold);
		}
		return gthreshold;
	}
	
	/**automatic selection for blue**/
	public int auto_blue(int bthreshold){
		int btemp = 0;
		int bsum0 = 0;
		int bsum1 = 0;
		double bu0 = 0; double bu1 = 0; int bcount0 = 0; int bcount1 = 0;
		double num = 0.1;
		/**calculate the two different groups of blue value**/
		while( Math.abs(bthreshold - btemp) > num ){
			btemp = bthreshold;
			for ( int y = 0; y < height; y++){
				for ( int x = 0; x < width; x++ ){
					if ( (new Color(source.image.getRGB(y, x))).getBlue() < bthreshold ){
						bsum0 += (new Color(source.image.getRGB(y, x))).getBlue();
						bcount0++;
					}
					if ( (new Color(source.image.getRGB(y, x))).getBlue() >= bthreshold ){
						bsum1 += (new Color(source.image.getRGB(y, x))).getBlue();
						bcount1++;
					}
				}
			}
			/**apply the automatic selection function to calculate the blue threshold**/
			bu0 = bsum0/bcount0; bu1 = bsum1/bcount1; 
			bthreshold = (int)(bu0+bu1)/2;
			//System.out.println(bthreshold);
		}
		return bthreshold;
	}
	
	public static void main(String[] args) {
		new ImageThreshold(args.length==1 ? args[0] : "sonnet.png");
	}
}
