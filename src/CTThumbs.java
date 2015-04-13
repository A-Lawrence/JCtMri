/*
 * I am aware of the University policy on unfair practice and I certify that this coursework is
 * the result of my own independent work except where otherwise stated, and that all other sources
 * are explicitly acknowledged.
 *
 * Anthony Lawrence - 555804
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class CTThumbs {

    protected CTOpen ct;

    /* GUI Related variables. */
    protected BufferedImage[] images; // The array of images.
    protected JButton[] thumbs; // The array of thumbnails.
    protected Container container; // The container for the frame.
    protected JFrame frame; // The thumbnail frame.
    
    /*
     * Display the thumbnails
     */
    public CTThumbs(File chosenFile, int z, int y, int x, String axis, boolean histEq) {   
        // Initialize the CTOpen class for the requested class.
        try {
            ct = new CTOpen(chosenFile, z, y, x);
        } catch(IOException e){
            System.out.println(e);
            System.exit(0);
        }
                
        // Get the number of thumbnails.
        int limit = 0;
        if(axis.equals("X")){
            limit = x;
        } else if(axis.equals("Y")){
            limit = y;
        } else {
            limit = z;
        }
        
        // Get the max limit
        int maxLimit = Math.max(Math.max(x, y), z);
        
        // Determine the scaling value.
        float scale = (float)Math.floor((float)maxLimit/(float)THUMB_WIDTH);
        System.out.println(scale);
        
        // Initialize the arrays.
        thumbs = new JButton[limit];
        images = new BufferedImage[limit];
        
        // Set up the frame.
        frame = new JFrame("Thumbnails for axis "+axis);
        container = frame.getContentPane();
        frame.setLayout(new FlowLayout());
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT); 
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        
        // Loop through 0 -> axis length creating thumbnails.
        for(int i=0; i<limit; i++){
            // Create the new buffered image so that we can resize it!
            images[i] = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        
            // Create the label.
            thumbs[i] = new JButton(new ImageIcon(resizeThumbnail(images[i], axis, i, scale)));

            // Redeclare i and axis for the inner class.
            final int redecI = i;
            final String redecAxis = axis;
            
            // Add an action listener to the button.
            thumbs[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    // Create a new instance of CTViewer
                    CTViewer ctv = new CTViewer();
                    
                    // Display the form
                    ctv.display();
                    
                    // Change the slider.
                    ctv.changeSlider(redecAxis, redecI);
                    
                    // Close this form.
                    frame.dispose();
                }
            });
            
            // Add to the container
            container.add(thumbs[i]);
        }
        
        // Repaint the pane
        frame.repaint();
    }
    
    public BufferedImage resizeThumbnail(BufferedImage bufImage, String axis, int slice, float scaleAmount){
        // Initialize the colour and datum variables.
        float col;
        short datum;
        
        // Get the ctData
        short[][][] ctData = ct.getData();
        
        // Get the width and height of the image.
        int width = bufImage.getWidth();
        int height = bufImage.getHeight();
           
        // Get the image data.
        byte[] data = ct.GetImageData(bufImage);
        
        // Loop through X and Y as before (in CTOpen).
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                //at this point (x,y) is a single pixel in the image
                //here you would need to do something to (x,y) if the image size
                //does not match the slice size (e.g. during an image resizing operation
                //If you don't do this, your x, y could be outside the array bounds
                //In the framework, the image is 256x256 and the data set slices are 256x256
                //so I don't do anything - this also leaves you something to do for the assignment
                
                // Determine which datum we need for the requested information.
                if(axis.equals("X")){ // SIDE
                    datum = ctData[(int)((j*ct.getEnlargementValue())*scaleAmount)][(int)(i*scaleAmount)][slice];
                } else if(axis.equals("Y")){ // FRONT
                    datum = ctData[(int)((j*ct.getEnlargementValue())*scaleAmount)][slice][(int)(i*scaleAmount)];
                } else {
                    datum = ctData[slice][(int)(j*scaleAmount)][(int)(i*scaleAmount)];
                }
                
                for (int c=0; c<3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //calculate the colour by performing a mapping from [min,max] -> [0,255]
                    col = (255.0f*((float)datum-(float)ct.getLimits("min"))/((float)(ct.getLimits("max")-ct.getLimits("min"))));
                    
                    //set the colour component c of pixel (x,y)
                    data[c+3*i+3*j*width] = (byte) col;
                } // colour loop
            } // column loop
        } // row loop
        
        return bufImage;
    }
    
    /* All the constants for the GUI. */
    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 800;
    private static final int PADDING = 10;
    private static final int THUMBS_PER_ROW = 3;
    private static final int THUMB_WIDTH = 32;
    private static final int THUMB_HEIGHT = 32;

}