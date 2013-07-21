package com.kircherelectronics.simplelinearacceleration.filter;

import com.kircherelectronics.simplelinearacceleration.statistics.StdDev;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.util.Log;

/*
 * Simple Linear Acceleration
 * Copyright (C) 2013, Kaleb Kircher - Boki Software, Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * An implementation of linear acceleration from an acceleration sensor only.
 * 
 * @author Kaleb
 * @see http://en.wikipedia.org/wiki/Low-pass_filter
 * @version %I%, %G%
 */
public class SimpleLinearAcceleration
{

	private boolean lpfAccelerationActive = false;

	private boolean meanFilterAccelerationActive = false;

	// The gravity components of the acceleration signal.
	private float[] components = new float[3];

	private float[] linearAcceleration = new float[]
	{ 0, 0, 0 };

	// Raw accelerometer data
	private float[] acceleration = new float[]
	{ 0, 0, 0 };

	private LowPassFilter lpfAcceleration;

	private MeanFilter meanFilterAcceleration;

	// The rotation matrix R transforming a vector from the device
	// coordinate system to the world's coordinate system which is
	// defined as a direct orthonormal basis. R is the identity
	// matrix when the device is aligned with the world's coordinate
	// system, that is, when the device's X axis points toward East,
	// the Y axis points to the North Pole and the device is facing
	// the sky. NOTE: the reference coordinate-system used by
	// getOrientation() is different from the world
	// coordinate-system defined for the rotation matrix R and
	// getRotationMatrix().
	private float[] r = new float[9];

	private StdDev varianceAccel;

	private Context context;

	public SimpleLinearAcceleration(Context context,
			LowPassFilter lpfAcceleration, 
			MeanFilter meanFilterAcceleration)
	{
		super();

		this.context = context;
		this.lpfAcceleration = lpfAcceleration;
		this.meanFilterAcceleration = meanFilterAcceleration;

		readPrefs();

		// Create the RMS Noise calculations
		varianceAccel = new StdDev();
	}

	/**
	 * Add a sample.
	 * 
	 * @param acceleration
	 *            The acceleration data.
	 * @return Returns the output of the filter.
	 */
	public float[] addSamples(float[] acceleration)
	{
		// Get a local copy of the sensor values
		System.arraycopy(acceleration, 0, this.acceleration, 0,
				acceleration.length);
		
		if (lpfAccelerationActive)
		{
			System.arraycopy(lpfAcceleration.addSamples(this.acceleration), 0,
					this.acceleration, 0, this.acceleration.length);
		}

		if (meanFilterAccelerationActive)
		{
			this.acceleration = meanFilterAcceleration
					.filterFloat(this.acceleration);
		}

		float magnitude = (float) (Math.sqrt(Math.pow(this.acceleration[0], 2)
				+ Math.pow(this.acceleration[1], 2)
				+ Math.pow(this.acceleration[2], 2)) / SensorManager.GRAVITY_EARTH);

		double var = varianceAccel.addSample(magnitude);

		// Attempt to estimate the gravity components when the device is
		// stable and not experiencing linear acceleration.
		if (var < 0.05)
		{
			// Find the gravity component of the X-axis

			components[0] = this.acceleration[0];
			// Find the gravity component of the Y-axis

			components[1] = this.acceleration[1];

			// Find the gravity component of the Z-axis

			components[2] = this.acceleration[2];
		}

		// Subtract the gravity component of the signal
		// from the input acceleration signal to get the
		// tilt compensated output.
		linearAcceleration[0] = (this.acceleration[0] - components[0])
				/ SensorManager.GRAVITY_EARTH;
		linearAcceleration[1] = (this.acceleration[1] - components[1])
				/ SensorManager.GRAVITY_EARTH;
		linearAcceleration[2] = (this.acceleration[2] - components[2])
				/ SensorManager.GRAVITY_EARTH;

		return linearAcceleration;
	}

	public void setLpfAccelerationActive(boolean lpfAccelerationActive)
	{
		this.lpfAccelerationActive = lpfAccelerationActive;
	}

	public void setMeanFilterAccelerationActive(
			boolean meanFilterAccelerationActive)
	{
		this.meanFilterAccelerationActive = meanFilterAccelerationActive;
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readPrefs()
	{
		SharedPreferences prefs = this.context.getSharedPreferences(
				"filter_prefs", Activity.MODE_PRIVATE);

		this.lpfAccelerationActive = prefs
				.getBoolean("lpf_acceleration", false);

		this.meanFilterAccelerationActive = prefs.getBoolean(
				"mean_filter_acceleration", false);
	}
}
