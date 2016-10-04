package com.r2jp2.colorsensor;

import java.util.ArrayList;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Delay;

public class ColorSensorAndFilter {

	public static void main(String[] args) {
		ColorSensorAndFilter foo = new ColorSensorAndFilter();
	}
	
	public void SensorAndFilterSample() {
	    /* Steps to initialize a sensor */
	    Brick brick = BrickFinder.getDefault();
	    Port s4 = brick.getPort("S4");
	    EV3ColorSensor sensor = new EV3ColorSensor(s4);

	    /* The sensor support various modes, display these modes */
	    System.out.println("Supported modes");
	    ArrayList<String> allModes = sensor.getAvailableModes();
	    for (String mode : allModes) {
	      System.out.println(mode);
	    }

	    /* Wait for Enter to continue */
	    brick.getKey("Enter").waitForPressAndRelease();
	    ;

	    /*
	     * Get the Red mode of the sensor.
	     * 
	     * In Red mode the sensor emmits red light and measures reflectivity of a
	     * surface. Red mode is the best mode for line following.
	     * 
	     * Alternatives to the method below are: sensor.getMode(1) or
	     * sensor.getRedMode()
	     */
	    SampleProvider redMode = sensor.getRedMode();

	    /*
	     * Use a filter on the sample. The filter needs a source (a sensor or
	     * another filter) for the sample. The source is provided in the constructor
	     * of the filter
	     */
	    SampleProvider reflectedLight = new autoAdjustFilter(redMode);

	    /*
	     * Create an array of floats to hold the sample returned by the
	     * sensor/filter
	     * 
	     * A sample represents a single measurement by the sensor. Some modes return
	     * multiple values in one measurement, hence the array
	     */
	    int sampleSize = reflectedLight.sampleSize();
	    float[] sample = new float[sampleSize];

	    Key escape = brick.getKey("Escape");
	    while (!escape.isDown()) {
	      /*
	       * Get a fresh sample from the filter. (The filter gets it from its
	       * source, the redMode)
	       */
	      reflectedLight.fetchSample(sample, 0);
	      /* Display individual values in the sample. */
	      for (int i = 0; i < sampleSize; i++) {
	        System.out.print(sample[i] + " ");
	      }
	      System.out.println();
	      Delay.msDelay(50);
	    }
	    /* When ready close the sensor */
	    sensor.close();

	  }
	
	/**
	   * This filter dynamicaly adjust the samples value to a range of 0-1. The
	   * purpose of this filter is to autocalibrate a light Sensor to return values
	   * between 0 and 1 no matter what the light conditions. Once the light sensor
	   * has "seen" both white and black it is calibrated and ready for use.
	   * 
	   * The filter could be used in a line following robot. The robot could rotate
	   * to calibrate the sensor.
	   * 
	   * @author Aswin
	   * 
	   */
	  public class autoAdjustFilter extends AbstractFilter {
	    /* These arrays hold the smallest and biggest values that have been "seen: */
	    private float[] minimum;
	    private float[] maximum;

	    public autoAdjustFilter(SampleProvider source) {
	      super(source);
	      /* Now the source and sampleSize are known. The arrays can be initialized */
	      minimum = new float[sampleSize];
	      maximum = new float[sampleSize];
	      reset();
	    }

	    public void reset() {
	      /* Set the arrays to their initial value */
	      for (int i = 0; i < sampleSize; i++) {
	        minimum[i] = Float.POSITIVE_INFINITY;
	        maximum[i] = Float.NEGATIVE_INFINITY;
	      }
	    }

	    /*
	     * To create a filter one overwrites the fetchSample method. A sample must
	     * first be fetched from the source (a sensor or other filter). Then it is
	     * processed according to the function of the filter
	     */
	    public void fetchSample(float[] sample, int offset) {
	      super.fetchSample(sample, offset);
	      for (int i = 0; i < sampleSize; i++) {
	        if (minimum[i] > sample[offset + i])
	          minimum[i] = sample[offset + i];
	        if (maximum[i] < sample[offset + i])
	          maximum[i] = sample[offset + i];
	        sample[offset + i] = (sample[offset + i] - minimum[i]) / (maximum[i] - minimum[i]);
	      }
	    }

	  }
}
