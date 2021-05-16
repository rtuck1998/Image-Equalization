package Control;

// Skeletal program for the "Image Histogram" assignment
// Written by:  Minglun Gong

import java.util.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.imageio.*;

// Main class
public class ImageHistogram extends Frame implements ActionListener {
	BufferedImage input;
	int width, height;
	TextField texRad, texThres;
	ImageCanvas source, target;
	PlotCanvas plot;
        String fileLoc;
	// Constructor
	public ImageHistogram(String name) {
		super("Image Histogram");
		// load image
		try {
			input = ImageIO.read(new File("Images/" +name));
		}
		catch ( Exception ex ) {
			ex.printStackTrace();
		}
		width = input.getWidth();
		height = input.getHeight();
                setBackground(Color.cyan);
		// prepare the panel for image canvas.
		Panel main = new Panel();
		source = new ImageCanvas(input);
		plot = new PlotCanvas();
		target = new ImageCanvas(input);
		main.setLayout(new GridLayout(1, 13, 100, 100));
		main.add(source);
		main.add(plot);
		main.add(target);
		// prepare the panel for buttons.
		Panel controls = new Panel();
                texRad = new TextField("Enter image", 13);
		controls.add(texRad);
		Button button = new Button("Submit");
		button.addActionListener(this);
		controls.add(button);
		button = new Button("Histogram Equalization");
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
	public void actionPerformed(ActionEvent e) 
    {

        /**if ( ((Button)e.getSource()).getLabel().equals("Display Histogram") )
        {
            ImageOperator imageOp = new ImageOperator(input);
            plot.plotColors(imageOp.getIntensity());
        }**/
        
        /**if ( ((Button)e.getSource()).getLabel().equals("Submit") )
        {
            fileLoc = texRad.getText();
        }**/
        
        if ( ((Button)e.getSource()).getLabel().equals("Histogram Equalization") )
        {
            ImageOperator imageOp = new ImageOperator(input);
            ImageOperator  stretchedImage = imageOp.getEqualization();
            target.resetImage(stretchedImage.getImage());
            plot.plotColors(stretchedImage.getIntensity());
        }
    }

    public static void main(String[] args) {
		new ImageHistogram(args.length==1 ? args[0] : "oot.png");
                //new ImageHistogram(args.length==1 ? args[0] : fileLoc);
	}
}

// Canvas for plotting histogram
class PlotCanvas extends Canvas {
	// lines for plotting axes and mean color locations
	LineSegment x_axis, y_axis;
	LineSegment red, green, blue;
	ArrayList<LineSegment> lines;
        boolean showMean = false;

	public PlotCanvas() {
		lines = new ArrayList<>();
        x_axis = new LineSegment(Color.BLACK, -10, 0, 256+10, 0);
		y_axis = new LineSegment(Color.BLACK, 0, -10, 0, 200+10);
	}
	// set mean image color for plot
	public void setMeanColor(Color clr) {
		red = new LineSegment(Color.RED, clr.getRed(), 0, clr.getRed(), 100);
		green = new LineSegment(Color.GREEN, clr.getGreen(), 0, clr.getGreen(), 100);
		blue = new LineSegment(Color.BLUE, clr.getBlue(), 0, clr.getBlue(), 100);
		showMean = true;
		repaint();
	}
         //plots inensity histogram
    public void plotColors(float[][] rawIntensity) 
    {
            
        int[][] intIntensity = new int[rawIntensity.length][rawIntensity[0].length] ;
        for ( int c=0 ; c<rawIntensity.length ; c++ )
        {
            for ( int k=0 ; k<rawIntensity[c].length ; k++ )
            {
                intIntensity[c][k] = (int) (Math.ceil(rawIntensity[c][k] * 210));
//                System.out.println("HERE:" + rawIntensity[c][k] * 200);
//                System.out.println(intIntensity[c][k]);
            }
        }
        if(intIntensity.length ==3)
        {
            for(int i =0, x = 0 ; i<intIntensity[0].length-1;i++, x++)
            {
                this.lines.add(new LineSegment(Color.RED,   x, intIntensity[0][i], x+1, intIntensity[0][i+1]));
                this.lines.add(new LineSegment(Color.GREEN, x, intIntensity[1][i], x+1, intIntensity[1][i+1]));
                this.lines.add(new LineSegment(Color.BLUE,  x, intIntensity[2][i], x+1, intIntensity[2][i+1]));
            }
        }
        else
        {
            for(int i =0; i<intIntensity[0].length-1;i++)
            {
                this.lines.add(new LineSegment(Color.BLACK ,i,intIntensity[0][i],i+1,intIntensity[0][i+1]));
               
            }
        }
        repaint();
    }
	// redraw the canvas
	public void paint(Graphics g) {
		// draw axis
                int xoffset = (getWidth() - 256) / 2;
		int yoffset = (getHeight() - 200) / 2;
		x_axis.draw(g, xoffset, yoffset, getHeight());
		y_axis.draw(g, xoffset, yoffset, getHeight());
                for(LineSegment line: this.lines )
                {
                    line.draw(g, xoffset, yoffset, getHeight());                
                }
	}
}

// LineSegment class defines line segments to be plotted
class LineSegment {
	// location and color of the line segment
	int x0, y0, x1, y1;
	Color color;
	// Constructor
	public LineSegment(Color clr, int x0, int y0, int x1, int y1) {
		color = clr;
		this.x0 = x0; this.x1 = x1;
		this.y0 = y0; this.y1 = y1;
	}
	public void draw(Graphics g, int xoffset, int yoffset, int height) {
		g.setColor(color);
		g.drawLine(x0+xoffset, height-y0-yoffset, x1+xoffset, height-y1-yoffset);
	}
}

class ImageOperator 
{
    private float[][] intensity; 
    float maxIntensity = 0;
    float minIntensity;
    BufferedImage image;
    // Constructor
    public ImageOperator(BufferedImage inImage)
    {
        image = inImage;
        minIntensity = image.getHeight()*image.getWidth();
        
        setIntensity();
    }

    private void setIntensity()
    {   
        if(isGrayScale()) //if gray scale only fills one array of intensities as all intensities of other colors will be the same
        {
            intensity = new float[1][256];
            Arrays.fill(intensity[0], 0);//gray
            for ( int y=0, i=0 ; y<image.getHeight() ; y++ ) 
            {
                for ( int x=0 ; x<image.getWidth() ; x++, i++ ) 
                {
                    Color clr = new Color(image.getRGB(x, y));
                    intensity[0][clr.getRed()]++;
                }
            }
        }
        else
        {
            intensity = new float[3][256];
            Arrays.fill(intensity[0], 0);//red
            Arrays.fill(intensity[1], 0);//green
            Arrays.fill(intensity[2], 0);//blue
            for ( int y=0; y< image.getHeight(); y++ ) 
            {
                for ( int x=0 ; x< image.getWidth(); x++) 
                {
                    Color clr = new Color(image.getRGB(x, y));
                    intensity[0][clr.getRed()]++;
                    intensity[1][clr.getGreen()]++;
                    intensity[2][clr.getBlue()]++;
                }
            }
        }
        for (float[] clrVal : intensity) // Gets max and min intensity
        {
            for (int k = 0; k < clrVal.length; k++) 
            {
                maxIntensity =( clrVal[k] >= maxIntensity ? clrVal[k]: maxIntensity);
                minIntensity =( clrVal[k] <= minIntensity ? clrVal[k]: minIntensity);
            }
        }

        for (float[] clrVal : intensity)  // Normalizes intensity scales to max value
        {
            for (int k = 0; k < clrVal.length; k++) 
            {
                clrVal[k] = (clrVal[k] / maxIntensity);
            }
        }
    }

    public float[][] getIntensity()
    {   
        return intensity;
    }

    

    public BufferedImage getImage()
    {   
        return image;
    }

    public ImageOperator getEqualization()
    {   
        BufferedImage outImage = image;
        
        if(isGrayScale())
        {
            // Get cumulative frequencies.
            // Set the default values for each frequency to 0.
            float[] grayFrequencies = new float[256];
            for (int x = 0; x < grayFrequencies.length; x++)
            {
                grayFrequencies[x] = 0;
            }
            
            // Count the instances of each intensity.
            for (int i = 0; i < image.getHeight(); i++)
            {
                for (int j = 0; j < image.getWidth(); j++)
                {
                    Color clr = new Color(image.getRGB(i, j));
                    int gray = clr.getRed();
                    
                    grayFrequencies[gray] = grayFrequencies[gray] + 1;
                    
                }
            }
            
            // Calculate the cumulative frequency.
            for (int i = 0; i < image.getHeight(); i++)
            {
                for (int j = 0; j < image.getWidth(); j++)
                {
                    Color clr = new Color(image.getRGB(i, j));
                    int gray = clr.getRed();
                    
                    int newGray = (((int) grayFrequencies[gray])/255) * gray;
                    
                    if (newGray > 255)
                    {
                        newGray = 255;
                    }
                    
                    if (newGray < 0)
                    {
                        newGray = 0;
                    }
                    
                    int rgb = new Color(newGray,newGray,newGray).getRGB();
                    //System.out.println("rgb-" + rgb);
                    outImage.setRGB(i, j, rgb);
                }
                
            }
                    
                    
                    
        }
        
        // If the image isn't monochrome.
        else
        {
            // Get cumulative frequencies.
            // Set the default values for each frequency to 0.
            float[] redFrequencies = new float[256];
            float[] greenFrequencies = new float[256];
            float[] blueFrequencies = new float[256];
            for (int x = 0; x < redFrequencies.length; x++)
            {
                redFrequencies[x] = 0;
                greenFrequencies[x] = 0;
                blueFrequencies[x] = 0;
            }

            // Count the instances of each intensity.
            for (int i = 0; i < image.getHeight(); i++)
            {
                for (int j = 0; j < image.getWidth(); j++)
                {
                    Color cor = new Color(image.getRGB(i, j));

                    redFrequencies[cor.getRed()] = redFrequencies[cor.getRed()] + 1;
                    greenFrequencies[cor.getGreen()] = greenFrequencies[cor.getGreen()] + 1;
                    blueFrequencies[cor.getBlue()] = blueFrequencies[cor.getBlue()] + 1;
                }
            }

            // Calculate the cumulative frequency.
            for ( int y=0; y< image.getHeight(); y++ ) 
            {
                for ( int x=0 ; x< image.getWidth(); x++) 
                {
                    Color clr = new Color(image.getRGB(x, y));

                    int red = clr.getRed();
                    int green = clr.getGreen();
                    int blue = clr.getBlue();

                    int newRed = (((int) redFrequencies[red])/255) * red;
                    int newGreen = (((int) greenFrequencies[green])/255) * green;
                    int newBlue = (((int) blueFrequencies[blue])/255) * blue;

                    if (newRed > 255)
                    {
                        newRed = 255;
                    }

                    if (newGreen > 255)
                    {
                        newGreen = 255;
                    }

                    if (newBlue > 255)
                    {
                        newBlue = 255;
                    }

                    if (newRed < 0)
                    {
                        newRed = 0;
                    }

                    if (newGreen < 0) 
                    {
                        newGreen = 0;
                    }

                    if (newBlue < 0)
                    {
                        newBlue = 0;
                    }

                    int rgb = new Color(newRed,newGreen,newBlue).getRGB();
                    //System.out.println("rgb-" + rgb);
                    outImage.setRGB(x, y, rgb);
                }
            }

            //System.out.println("Equalization works!");


        }
            return new ImageOperator(outImage);
        
        
    }

    private boolean isGrayScale()
    {   
        int type = image.getColorModel().getColorSpace().getType();
        return type == ColorSpace.TYPE_GRAY;
    }    
	
}
