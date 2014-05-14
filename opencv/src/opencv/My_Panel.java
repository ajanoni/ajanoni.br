package opencv;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

class My_Panel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;

	// Create a constructor method
	public My_Panel() {
		super();
	}

	/**
	 * Converts/writes a Mat into a BufferedImage.
	 * 
	 * @param matrix
	 *            Mat of type CV_8UC3 or CV_8UC1
	 * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
	 */
	public boolean MatToBufferedImage(Mat matBGR) {
		long startTime = System.nanoTime();
		int width = matBGR.width(), height = matBGR.height(), channels = matBGR
				.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		matBGR.get(0, 0, sourcePixels);
		// create new image and get reference to backing data
		image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		long endTime = System.nanoTime();
//		System.out.println(String.format("Elapsed time: %.2f ms",
//				(float) (endTime - startTime) / 1000000));
		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (this.image == null)
			return;
		g.drawImage(this.image, 10, 10, this.image.getWidth(),
				this.image.getHeight(), null);
		// g.drawString("This is my custom Panel!",10,20);
	}
}

class processor {
	private CascadeClassifier face_cascade;

	// Create a constructor method
	public processor() {
		face_cascade = new CascadeClassifier(
				"C:/java/opencv/opencv/sources/data/haarcascades/haarcascade_frontalface_alt2.xml");
		if (face_cascade.empty()) {
			System.out.println("--(!)Error loading A\n");
			return;
		} else {
			System.out.println("Face classifier loooaaaaaded up");
		}
	}

	public Mat detect(Mat inputframe) {
		Mat mRgba = new Mat();
		Mat mGrey = new Mat();
		MatOfRect faces = new MatOfRect();
		inputframe.copyTo(mRgba);
		inputframe.copyTo(mGrey);
		Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(mGrey, mGrey);
		face_cascade.detectMultiScale(mGrey, faces);
		System.out.println(String.format("Detected %s faces",
				faces.toArray().length));
		for (Rect rect : faces.toArray()) {
			Point center = new Point(rect.x + rect.width * 0.5, rect.y
					+ rect.height * 0.5);
			Core.ellipse(mRgba, center, new Size(rect.width * 0.5,
					rect.height * 0.5), 0, 0, 360, new Scalar(255, 0, 255), 4,
					8, 0);
		}
		return mRgba;
	}
	



}

class processor2 {
	// Create a constructor method
	static int thresh = 10;
	static int N = 11;
	final static String wndname = "Square Detection Demo";
	public Mat testMat;

	public processor2() {
	}
	
	public Mat detect(Mat inputframe) {
		
		Mat mRgba = new Mat();
		inputframe.copyTo(mRgba);
		List<MatOfPoint> ret  = new ArrayList<MatOfPoint>();
		ret = find_squares(inputframe);
		
		Core.polylines(mRgba, ret, true, new Scalar(255, 0, 255));
		return mRgba;
	}

	// helper function:
	// finds a cosine of angle between vectors
	// from pt0->pt1 and from pt0->pt2
	public static double angle(Point pt1, Point pt2, Point pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2)
				/ Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
						+ 1e-10);
	}
	
	public  List<MatOfPoint> find_squares(Mat image)
	{
		
		List<MatOfPoint> ret = new ArrayList<MatOfPoint>();

	    // blur will enhance edge detection
	    Mat blurred = new Mat();
	    Imgproc.medianBlur(image, blurred, 9);
	   

	    Mat gray = new Mat();
	    Mat tgray = new Mat(blurred.size(), CvType.CV_8U);
	    
	    Mat pyr = new Mat();
	    Mat timg = new Mat();
	    blurred.copyTo(timg);
	    List <MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    
	    
	    Imgproc.pyrDown(timg, pyr, new Size(image.cols()/2, image.rows()/2));
	    Imgproc.pyrUp(pyr, timg, new Size(image.cols(), image.rows()));
	    
	    
	    // find squares in every color plane of the image
	    for (int c = 0; c < 3; c++)
	    {
	    	
	        int ch[] = {c, 0};
	        
	        MatOfInt a = new MatOfInt(ch);
	        Core.mixChannels(Arrays.asList(timg), Arrays.asList(tgray), a);
	        

	        // try several threshold levels
	        final int threshold_level = 3;
	        for (int l = 0; l < threshold_level; l++)
	        {
	            // Use Canny instead of zero threshold level!
	            // Canny helps to catch squares with gradient shading
	            if (l == 0)
	            {
	            	
	                Imgproc.Canny(tgray, gray, 0, thresh, 5, false);
//	                // Dilate helps to remove potential holes between edge segments
	                Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
	                
	            }
	            else
	            {
	            	Imgproc.threshold(tgray, gray, (l+1)*255/N, 255, Imgproc.THRESH_BINARY);
	            }
	           

	            // Find contours and store them in a list. //TODO
	            Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

	            // Test contours
	            MatOfPoint2f approx = new MatOfPoint2f();
	            for (int i = 0; i < contours.size(); i++)
	            {
	                // approximate contour with accuracy proportional
	                // to the contour perimeter
	            	MatOfPoint2f contours2f = new MatOfPoint2f();
	            	contours2f.fromArray(contours.get(i).toArray());
	                double epilson = Imgproc.arcLength(contours2f, true);
	                epilson *= 0.02;
	                Imgproc.approxPolyDP(contours2f, approx, epilson, true);

	                // Note: absolute value of an area is used because
	                // area may be positive or negative - in accordance with the
	                // contour orientation
                    MatOfPoint ppp = new MatOfPoint(approx.toArray());

	                if (approx.size().area() == 4 &&
	                        Math.abs(Imgproc.contourArea(approx)) > 1000 && 
	                        Imgproc.isContourConvex(ppp))
	                {
	                    double maxCosine = 0;

	                    for (int j = 2; j < 5; j++)
	                    {
	                    	Point[] myPoint = ppp.toArray();
	                        double cosine = Math.abs(angle(myPoint[j % 4], myPoint[j - 2], myPoint[j - 1]));
	                        maxCosine = Math.max(maxCosine, cosine);
	                    }

	                    if (maxCosine < 0.3){
	                        ret.add(ppp);
	                    }
	                }
	            }
	        }
	    }
	    
	    return ret;

	}
	
	

}