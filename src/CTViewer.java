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
import java.util.Hashtable;

public class CTViewer extends JFrame {
    /*
     * The event handler for toggling histogram equalization on/off.
     */
    private class HistogramEqActivation implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Top Down View
            if (event.getSource() == topDownHistToggle) {
                // Swap the value
                topDownHistSwitch = !topDownHistSwitch;
                
                // Get the current slice again.
                ctImageTopDown.setIcon(new ImageIcon(ct.getImage(sliceSliderTopDown.getValue(), "Z", topDownHistSwitch)));
            }
            
            // Front View
            if (event.getSource() == frontHistToggle) {
                // Swap the value
                frontHistSwitch = !frontHistSwitch;
                
                // Get the current slice again.
                ctImageFront.setIcon(new ImageIcon(ct.getImage(sliceSliderFront.getValue(), "Y", frontHistSwitch)));
            }
            
            // Side View
            if (event.getSource() == sideHistToggle) {
                // Swap the value
                sideHistSwitch = !sideHistSwitch;
                
                // Get the current slice again.
                ctImageSide.setIcon(new ImageIcon(ct.getImage(sliceSliderSide.getValue(), "X", sideHistSwitch)));
            }
        }
    }
    
    /*
     * The event handler for displaying thumbnails.
     */
    private class ThumbnailDisplay implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            // Top Down View
            if (event.getSource() == topDownThumbs) {
                // Initialize the thumbnail class.
                CTThumbs ctt = new CTThumbs(file, Integer.parseInt(zLimit), Integer.parseInt(yLimit), Integer.parseInt(xLimit), "Z", topDownHistSwitch);
                
                // Close this form.
                dispose();
            }
            
            // Front View
            if (event.getSource() == frontThumbs) {
                // Initialize the thumbnail class.
                CTThumbs ctt = new CTThumbs(file, Integer.parseInt(zLimit), Integer.parseInt(yLimit), Integer.parseInt(xLimit), "Y", frontHistSwitch);
                
                // Close this form.
                dispose();
            }
            
            // Side View
            if (event.getSource() == sideThumbs) {
                // Initialize the thumbnail class.
                CTThumbs ctt = new CTThumbs(file, Integer.parseInt(zLimit), Integer.parseInt(yLimit), Integer.parseInt(xLimit), "X", sideHistSwitch);
                
                // Close this form.
                dispose();
            }
        }
    }
    
    /*
     The change listener for the JSliders.
     */
    private class GUIChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent event){
            // TopDown view
            if(event.getSource() == sliceSliderTopDown){
                // Display the image.
                ctImageTopDown.setIcon(new ImageIcon(ct.getImage(sliceSliderTopDown.getValue(), "Z", topDownHistSwitch)));
            }
            
            // Front View
            if(event.getSource() == sliceSliderFront){
                // Display the image.
                ctImageFront.setIcon(new ImageIcon(ct.getImage(sliceSliderFront.getValue(), "Y", frontHistSwitch)));
            }
            
            // Side View
            if(event.getSource() == sliceSliderSide){
                // Display the image.
                ctImageSide.setIcon(new ImageIcon(ct.getImage(sliceSliderSide.getValue(), "X", sideHistSwitch)));
            }
        }
    }
    
    /* The CTOpen class. */
    protected CTOpen ct;
    
    /* File details. */
    protected File file;
    protected String zLimit;
    protected String yLimit;
    protected String xLimit;
    
    /* GUI Related variables. */
    protected Container container; // The JFrame container.
    protected JFileChooser fileChooser = new JFileChooser();
    
    protected JLabel ctImageTopDown; // The label to display the top down CT image.
    protected JLabel ctImageTopDownMIP; // THe label to display the top down CT image (MIP).
    protected JSlider sliceSliderTopDown; // The slider to control which Top Down slice is being viewed.
    protected JButton topDownHistToggle; // The button to toggle histogram equalization on/off.
    protected boolean topDownHistSwitch = false; // The boolean switch for the histogram equalisation.
    protected JButton topDownThumbs; // The button to display top down thumbnails.
    
    protected JLabel ctImageFront; // The label to display the front CT image.
    protected JLabel ctImageFrontMIP; // THe label to display the front view CT image (MIP).
    protected JSlider sliceSliderFront; // The slider to control which front slice is being viewed.
    protected JButton frontHistToggle; // The button to toggle histogram equalization on/off.
    protected boolean frontHistSwitch = false; // The boolean switch for the histogram equalisation.
    protected JButton frontThumbs; // The button to display top down thumbnails.
    
    protected JLabel ctImageSide; // The label to display the side CT image.
    protected JLabel ctImageSideMIP; // The label to display the side of the CT image (MIP).
    protected JSlider sliceSliderSide; // The slider to control which side slice is being viewed.
    protected JButton sideHistToggle; // The button to toggle histogram equalization on/off.
    protected boolean sideHistSwitch = false; // The boolean switch for the histogram equalisation.
    protected JButton sideThumbs; // The button to display top down thumbnails.
    
    /*
     * Display the open file dialog box.
     */
    private File fileDialog() {
        // Set the default directory (this one).
        fileChooser.setCurrentDirectory(null);
     
        // Get the file they wish to open
        int returnVal = fileChooser.showOpenDialog(CTViewer.this);
        
        // Did they actually open it or did they cancel it?
        // If it's been opened, we'll process that and get some information!
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // Get the selected file.
             return fileChooser.getSelectedFile();
        } else if(returnVal == JFileChooser.CANCEL_OPTION) {
            System.exit(0);
        }
        
        return fileDialog();
    }
     
     /* Change the slider. */
     public void changeSlider(String axis, int slice){
        if(axis.equals("X")){
            sliceSliderSide.setValue(slice);
        } else if(axis.equals("Y")){
            sliceSliderFront.setValue(slice);
        } else {
            sliceSliderTopDown.setValue(slice);
        }
     }
     
    /*
     * Set up the GUI.
     */
     public void display() {
        // Get the file to be opened.
        file = fileDialog();
        
        // Now get the z, y and x values.
        zLimit = JOptionPane.showInputDialog(null, "What is the z value?", "Enter the z value", JOptionPane.QUESTION_MESSAGE);
        xLimit = JOptionPane.showInputDialog(null, "What is the x value?", "Enter the x value", JOptionPane.QUESTION_MESSAGE);
        yLimit = JOptionPane.showInputDialog(null, "What is the y value?", "Enter the y value", JOptionPane.QUESTION_MESSAGE);
    
        // Load the dataset.
        try {
            ct = new CTOpen(file, Integer.parseInt(zLimit), Integer.parseInt(yLimit), Integer.parseInt(xLimit));
        } catch(IOException e){
            System.out.println(e);
            System.exit(0);
        }
        // Set some info for the frame
        this.container = getContentPane();
        setLayout(null);
        setVisible(true);
        setLocationRelativeTo(null);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setResizable(false);
        
        /*******************************************************************************************************************/
        /* Top down View */
        /*******************************************************************************************************************/
        // Set the JLabel up which we'll use to display the image (but with no image at the minute).
        this.ctImageTopDown = new JLabel(new ImageIcon(ct.getImage(0, "Z")));
        this.ctImageTopDown.setBounds(PADDING, PADDING, Integer.parseInt(xLimit), Integer.parseInt(yLimit));
        container.add(this.ctImageTopDown);
        
        // Create the slice slider
        this.sliceSliderTopDown = new JSlider(0, Integer.parseInt(zLimit)-1);
        this.sliceSliderTopDown.setValue(0);
        
        // Add the slice slider paint ticks.
        this.sliceSliderTopDown.setPaintTicks(true);
        this.sliceSliderTopDown.setMinorTickSpacing(SLIDER_MINOR_TICKS);
        this.sliceSliderTopDown.setMajorTickSpacing(SLIDER_MAJOR_TICKS);
        
        // Slider event handlers and positioning.
        this.sliceSliderTopDown.addChangeListener(new GUIChangeListener());
        this.sliceSliderTopDown.setBounds(PADDING, this.ctImageTopDown.getHeight()+(PADDING*2), this.ctImageTopDown.getWidth(), 45);
        container.add(this.sliceSliderTopDown);
        
        // Hist Eq button
        this.topDownHistToggle = new JButton("Histogram Eq.");
        this.topDownHistToggle.setBounds(PADDING, this.ctImageTopDown.getHeight()+this.sliceSliderTopDown.getHeight()+(PADDING*3),
                                     (int)(this.ctImageTopDown.getWidth()/2)-((int)PADDING/2), 25);
        this.topDownHistToggle.addActionListener(new HistogramEqActivation());
        container.add(this.topDownHistToggle);
        
        // Thumbnail viewer
        this.topDownThumbs = new JButton("Thumbnails");
        this.topDownThumbs.setBounds(this.topDownHistToggle.getWidth()+(PADDING*2),
                                     this.ctImageTopDown.getHeight()+this.sliceSliderTopDown.getHeight()+(PADDING*3),
                                     (int)(this.ctImageTopDown.getWidth()/2)-((int)PADDING/2), 25);
        this.topDownThumbs.addActionListener(new ThumbnailDisplay());
        container.add(this.topDownThumbs);
        
        // MIP image for top down.
        this.ctImageTopDownMIP = new JLabel(new ImageIcon(ct.getImage("Z", true)));
        this.ctImageTopDownMIP.setBounds(PADDING,this.ctImageTopDown.getHeight()+this.sliceSliderTopDown.getHeight()+this.topDownHistToggle.getHeight()+(PADDING*4),
                                         this.ctImageTopDown.getWidth(), this.ctImageTopDown.getHeight());
        container.add(this.ctImageTopDownMIP);
        /*******************************************************************************************************************/
        /* Front View */
        /*******************************************************************************************************************/
        // Set the JLabel up which we'll use to display the image (but with no image at the minute).
        this.ctImageFront = new JLabel(new ImageIcon(ct.getImage(0, "Y")));
        this.ctImageFront.setBounds(this.ctImageTopDown.getWidth()+(PADDING*2), PADDING, Integer.parseInt(xLimit), Integer.parseInt(yLimit));
        container.add(this.ctImageFront);
        
        // Create the slice slider
        this.sliceSliderFront = new JSlider(0, Integer.parseInt(yLimit)-1);
        this.sliceSliderFront.setValue(0);
        
        // Add the slice slider labels/paint ticks.
        this.sliceSliderFront.setPaintTicks(true);
        this.sliceSliderFront.setMinorTickSpacing(SLIDER_MINOR_TICKS);
        this.sliceSliderFront.setMajorTickSpacing(SLIDER_MAJOR_TICKS);
        
        // Slider event handlers and positioning.
        this.sliceSliderFront.addChangeListener(new GUIChangeListener());
        this.sliceSliderFront.setBounds(this.ctImageTopDown.getWidth()+(PADDING*2), this.ctImageFront.getHeight()+(PADDING*2), this.ctImageFront.getWidth(), 45);
        container.add(this.sliceSliderFront);
        
        // Hist Eq button
        this.frontHistToggle = new JButton("Histogram Eq.");
        this.frontHistToggle.setBounds(this.ctImageTopDown.getWidth()+(PADDING*2), this.ctImageFront.getHeight()+this.sliceSliderFront.getHeight()+(PADDING*3),
                                      (int)(this.ctImageFront.getWidth()/2)-((int)PADDING/2), 25);
        this.frontHistToggle.addActionListener(new HistogramEqActivation());
        container.add(this.frontHistToggle);
        
        // Thumbnail viewer
        this.frontThumbs = new JButton("Thumbnails");
        this.frontThumbs.setBounds(this.ctImageTopDown.getWidth()+this.frontHistToggle.getWidth()+(PADDING*3),
                                   this.ctImageFront.getHeight()+this.sliceSliderFront.getHeight()+(PADDING*3),
                                   (int)(this.ctImageFront.getWidth()/2)-((int)PADDING/2), 25);
        this.frontThumbs.addActionListener(new ThumbnailDisplay());
        container.add(this.frontThumbs);
        
        // MIP image for front view.
        this.ctImageFrontMIP = new JLabel(new ImageIcon(ct.getImage("Y", true)));
        this.ctImageFrontMIP.setBounds(this.ctImageTopDown.getWidth()+(PADDING*2),
                                       this.ctImageFront.getHeight()+this.sliceSliderFront.getHeight()+this.frontHistToggle.getHeight()+(PADDING*4),
                                       this.ctImageFront.getWidth(), this.ctImageFront.getHeight());
        container.add(this.ctImageFrontMIP);
        /*******************************************************************************************************************/
        /* Side View */
        /*******************************************************************************************************************/
        // Set the JLabel up which we'll use to display the image (but with no image at the minute).
        this.ctImageSide = new JLabel(new ImageIcon(ct.getImage(0, "X")));
        this.ctImageSide.setBounds(this.ctImageTopDown.getWidth()+this.ctImageFront.getWidth()+(PADDING*3), PADDING,
                                   Integer.parseInt(xLimit), Integer.parseInt(yLimit));
        container.add(this.ctImageSide);
        
        // Create the slice slider
        this.sliceSliderSide = new JSlider(0, Integer.parseInt(xLimit)-1);
        this.sliceSliderSide.setValue(0);
        
        // Add the slice slider labels/paint ticks.
        this.sliceSliderSide.setPaintTicks(true);
        this.sliceSliderSide.setMinorTickSpacing(SLIDER_MINOR_TICKS);
        this.sliceSliderSide.setMajorTickSpacing(SLIDER_MAJOR_TICKS);
        
        // Slider event handlers and positioning.
        this.sliceSliderSide.addChangeListener(new GUIChangeListener());
        this.sliceSliderSide.setBounds(this.ctImageTopDown.getWidth()+this.ctImageFront.getWidth()+(PADDING*3),
                                       this.ctImageSide.getHeight()+(PADDING*2), this.ctImageSide.getWidth(), 45);
        container.add(this.sliceSliderSide);
        
        // Hist Eq button
        this.sideHistToggle = new JButton("Histogram Eq.");
        this.sideHistToggle.setBounds(this.ctImageTopDown.getWidth()+this.ctImageFront.getWidth()+(PADDING*3), this.ctImageSide.getHeight()+this.sliceSliderSide.getHeight()+(PADDING*3),
                                     (int)(this.ctImageSide.getWidth()/2)-((int)PADDING/2), 25);
        this.sideHistToggle.addActionListener(new HistogramEqActivation());
        container.add(this.sideHistToggle);
        
        // Thumbnail viewer
        this.sideThumbs = new JButton("Thumbnails");
        this.sideThumbs.setBounds(this.ctImageTopDown.getWidth()+this.ctImageFront.getWidth()+this.sideHistToggle.getWidth()+(PADDING*4),
                                  this.ctImageSide.getHeight()+this.sliceSliderSide.getHeight()+(PADDING*3),
                                  (int)(this.ctImageSide.getWidth()/2)-((int)PADDING/2), 25);
        this.sideThumbs.addActionListener(new ThumbnailDisplay());
        container.add(this.sideThumbs);
                    
        // MIP image for side.
        this.ctImageSideMIP = new JLabel(new ImageIcon(ct.getImage("X", true)));
        this.ctImageSideMIP.setBounds(this.ctImageTopDown.getWidth()+this.ctImageFront.getWidth()+(PADDING*3),
                                      this.ctImageSide.getHeight()+this.sliceSliderSide.getHeight()+this.sideHistToggle.getHeight()+(PADDING*4),
                                      this.ctImageSide.getWidth(), this.ctImageSide.getHeight());
        container.add(this.ctImageSideMIP);
        /*******************************************************************************************************************/
        
        // Repaint the pane
        repaint();
    }
     
    public static void main(String[] args) throws IOException {
        CTViewer ctv = new CTViewer();
        ctv.display();
        ctv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /* All the constants for the GUI. */
    private static final int FRAME_WIDTH = 825;
    private static final int FRAME_HEIGHT = 675;
    private static final int PADDING = 10;
    private static final int SLIDER_MINOR_TICKS = 10;
    private static final int SLIDER_MAJOR_TICKS = 30;
}