/*
 * I am aware of the University policy on unfair practice and I certify that this coursework is
 * the result of my own independent work except where otherwise stated, and that all other sources
 * are explicitly acknowledged.
 *
 * Anthony Lawrence - 555804
 */

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.nio.*;
import java.util.*;
import java.util.Arrays;

public class CTOpen {
    
    /* Variables required for the processing/displaying of the slices. */
    private File ctFile; // The CT file.
    private short[][][] ctData; // Store the 3D volume data set.
        
    private BufferedImage bufferedImageZ; // Memory storage for images (Z).
    private BufferedImage bufferedImageZMIP; // Memory storage for images (Z).   
    private BufferedImage bufferedImageY; // Memory storage for images (Y).
    private BufferedImage bufferedImageYMIP; // Memory storage for images (Y).
    private BufferedImage bufferedImageX; // Memory storage for images (X).
    private BufferedImage bufferedImageXMIP; // Memory storage for images (X).
    
    private int[] histogram; //Histogram.
    private float[] histogramMapping; //
    
    private int zLimit = 0; // The number of z values.
    private int xLimit = 0; // The number of x values.
    private int yLimit = 0; // The number of y values.
    private int maxLimit = 0; // The maximum of y, x and z.
    private int totalSize = 0; // The total size of the data set (x*y*z).
    private double enlargementValue = 0.0; // The enlargement value to fix the issue with non "cubic" files.
    private short range = 0; // The range between min and max.
    
    // Set the min and max to extreme values for later when we find the highest/lowest values.
    private short min = Short.MAX_VALUE; //z
    private short max = Short.MIN_VALUE; //z
    
    /* Constructor for the class: Takes the file (along with the z, y and x values) to be opened and then opens it. */
    public CTOpen(File chosenFile, int z, int y, int x) throws IOException {
        // Store the filename.
        ctFile = chosenFile;
        
        // Set the limits
        zLimit = z;
        yLimit = y;
        xLimit = x;
        
        // Determine the maximum value of Z, Y and X so that the full image
        // is output on alternative views.
        maxLimit = Math.max(Math.max(z, y), x);
        
        // Get the total size
        totalSize = x*y*z;

        // Determine the enlargement value to fill the image space.
        enlargementValue = (double)zLimit/(double)yLimit;
        
        // Create the image buffer to store the buffered images.
        bufferedImageZ = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImageZMIP = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImageY = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImageYMIP = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImageX = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImageXMIP = new BufferedImage(xLimit, yLimit, BufferedImage.TYPE_3BYTE_BGR);
                
        // Try and open the file.
        try {
            this.openFile();
        } catch(IOException e) {
            System.out.println("###### ERROR: "+e.getMessage()+".");
        }
    }
    
    /* Get the ctdata. */
    public short[][][] getData(){
        return ctData;
    }
    
    /* Get the limits of the requested axis. */
    public int getLimits(String axis){
        if(axis.equals("M")){
            return maxLimit;
        } else if(axis.equals("min")){
            return min;
        } else if(axis.equals("max")){
            return max;
        } else if(axis.equals("X")){
            return xLimit;
        } else if(axis.equals("Y")){
            return yLimit;
        } else {
            return zLimit;
        }
    }
    public float getEnlargementValue(){
        return (float)enlargementValue;
    }
        
    /* Opens the file and stores all the images in the data array. */
    public void openFile() throws IOException{
        // Check this file exists.
        if(!ctFile.exists()){
            throw new IOException("File does not exist.");
        }
        
        // Read the data in from the file.
        DataInputStream ctFileDIS = new DataInputStream(new BufferedInputStream(new FileInputStream(ctFile)));
        
        // Declare a few things before we messing with the file.
        short read = 0; //value read in
        int byte1, byte2; //data is wrong Endian for Java so we need to swap the bytes around.
        
        // Allocate the memory
        ctData = new short[maxLimit][maxLimit][maxLimit];
        
        //loop through the data reading it in
        for (int z=0; z<zLimit; z++) { // z axis
            for (int y=0; y<yLimit; y++) { // y axis
                for (int x=0; x<xLimit; x++) { // x axis
                    //Because the Endianess is wrong, it needs to be read byte at a time and swapped
                    byte1 = ((int) ctFileDIS.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    byte2 = ((int) ctFileDIS.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read = (short) (byte2 << 8 | byte1 << 0); //and swizzle the bytes around
                    
                    // Update the minimum and maximum values.
                    if (read < min){
                        min = read;
                    }
                    if (read > max){
                        max = read;
                    }
                    
                    // Add the short to the array.
                    ctData[z][y][x] = read;
                }
            }
        } //End of the z slices loop.
        
        // Set the range of values
        range = (short)(max+(min*-1)+1);
        
        // Initialize the histogram array.
        histogram = new int[range];
        Arrays.fill(histogram, 0);
        
        //Loop back through the data to get information for histogram equalisation.
        int count = 0;
        for (int k = 0; k < zLimit; k++) {
            for (int j = 0; j < yLimit; j++) {
                for (int i = 0; i < xLimit; i++) {
                    // Default index
                    int index = ctData[k][j][i]-min;
                    
                    // Increment the histogram counter
                    histogram[index]++;
                }
            }
        }
        
        // Initialize the and mapping array.
        histogramMapping = new float[range];
        
        // Loop through the histogram data to compute the cummulative
        // distribution array and the mapping array.
        int t_i = 0;
        for (int i=0; i<range; i++) {
            // Calculate the cummulative distribution value.
            t_i+= histogram[i];
            
            // Create the mapping
            histogramMapping[i] = 255.0f*((float)t_i/(float)totalSize);
        }
                
        // Diagnostic output.
        System.out.println("Total size of data set: "+totalSize);
        System.out.println("Minimum colour value: "+min);
        System.out.println("Maximum colour value: "+max);
        System.out.println("Range/Number of Levels: "+range);
    }
    
    /*
        This function will return a pointer to an array
        of bytes which represent the image data in memory.
        Using such a pointer allows fast access to the image
        data for processing (rather than getting/setting
        individual pixels)
    */
    public static byte[] GetImageData(BufferedImage image) {
            WritableRaster WR = image.getRaster();
            DataBuffer DB = WR.getDataBuffer();
            
            if (DB.getDataType() != DataBuffer.TYPE_BYTE)
                throw new IllegalStateException("That's not of type byte");
          
            return ((DataBufferByte) DB).getData();
    }
    
    public BufferedImage getImage(int slice, String axis) {
        return getImage(slice, axis, false);
    }
    
    public BufferedImage getImage(int slice, String axis, boolean histEq) {
        //Obtain pointer to data for fast processing
        byte[] data;
        float col = 0f;
        short datum;
        
        // Set the correct loop info & initiate the histogram arrays.
        if(axis.equals("X")){
            data = GetImageData(bufferedImageX);
        } else if(axis.equals("Y")){
            data = GetImageData(bufferedImageY);
        } else {
            data = GetImageData(bufferedImageZ);
        }
        
        // Loop through each pixel (and colour).
        for (int j = 0; j < maxLimit; j++) {
            for (int i = 0; i < maxLimit; i++) {
                //at this point (x,y) is a single pixel in the image
                //here you would need to do something to (x,y) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your x, y could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                
                // Determine which datum we need for the requested information.
                if(axis.equals("X")){ // SIDE
                    datum = ctData[(int)(j*enlargementValue)][i][slice];
                } else if(axis.equals("Y")){ // FRONT
                    datum = ctData[(int)(j*enlargementValue)][slice][i];
                } else {
                    datum = ctData[slice][j][i];
                }
                
                if(histEq){
                    col = histogramMapping[datum-min];
                }
                
                for (int c=0; c<3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //calculate the colour by performing a mapping from [min,max] -> [0,255]
                    if(!histEq){
                        col = (255.0f*((float)datum-(float)min)/((float)(max-min)));
                    }
                    
                    //set the colour component c of pixel (x,y)
                    data[c+3*i+3*j*xLimit] = (byte) col;
                } // colour loop
            } // column loop
        } // row loop
        
        if(axis.equals("X")){
            return bufferedImageX;
        } else if(axis.equals("Y")){
            return bufferedImageY;
        } else {
            return bufferedImageZ;
        }
    }
    
    public BufferedImage getImage(String axis, boolean MIP) {
        // Initialize the loop variables.
        float mipMax = min;
        
        //Obtain pointer to data for fast processing
        byte[] data;
        float col;
        short datum;

        // Get the image data.
        if(axis.equals("X")){
          data = GetImageData(bufferedImageXMIP);
        } else if(axis.equals("Y")) {
          data = GetImageData(bufferedImageYMIP);
        } else {
          data = GetImageData(bufferedImageZMIP);
        }


        // Trace the ray!
        for(int j = 0; j < yLimit; j++){
          for(int i = 0; i < xLimit; i++){
            // Reset the maximum value from the mip.
            mipMax = min;

            for(int k = 0; k < zLimit; k++){
              if(axis.equals("X")){
                mipMax = Math.max(ctData[(int)(j*enlargementValue)][i][k], mipMax);
              } else if(axis.equals("Y")) {
                mipMax = Math.max(ctData[(int)(j*enlargementValue)][k][i], mipMax);
              } else {
                mipMax = Math.max(ctData[k][j][i], mipMax);
              }
            }

            // Adjust the colour.
            col = (255.0f*((float)mipMax-(float)min)/((float)(max-min)));

            // Modify the colour (BGR) stuff!
            for (int c=0; c<3; c++) {
                //set the colour component c of pixel (x,y)
                data[c+3*i+3*j*xLimit] = (byte) col;
            } // colour loop
          }
        } // j loop

        // Get the image data.
        if(axis.equals("X")){
          return bufferedImageXMIP;
        } else if(axis.equals("Y")) {
          return bufferedImageYMIP;
        } else {
          return bufferedImageZMIP;
        }

    }
}