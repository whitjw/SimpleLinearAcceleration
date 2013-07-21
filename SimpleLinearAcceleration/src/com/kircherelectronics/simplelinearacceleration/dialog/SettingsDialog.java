package com.kircherelectronics.simplelinearacceleration.dialog;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kircherelectronics.simplelinearacceleration.R;
import com.kircherelectronics.simplelinearacceleration.filter.LowPassFilter;
import com.kircherelectronics.simplelinearacceleration.filter.MeanFilter;
import com.kircherelectronics.simplelinearacceleration.filter.SimpleLinearAcceleration;

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
 * A special dialog for the settings of the application. Allows the user to
 * select what filters are plotted and set filter parameters.
 * 
 * @author Kaleb
 * @version %I%, %G%
 */
public class SettingsDialog extends Dialog implements
		NumberPicker.OnValueChangeListener, OnCheckedChangeListener
{
	private boolean showAccelerationSetAlpha = false;


	private boolean lpfAccelerationActive = false;

	private boolean meanFilterAccelerationActive = false;

	private LayoutInflater inflater;

	private View settingsFiltersAccelerationView;

	private View settingsLPFAccelerationDynamicAlphaView;

	private View settingsLPFAccelerationSetAlphaView;
	private View settingsMeanFilterAccelerationSetWindowView;

	private View settingsLPFAccelerationToggleSetAlphaView;

	private NumberPicker lpfAccelerationAlphaNP;

	private NumberPicker meanFilterAccelerationWindowNP;

	private TextView accelerationFilterTextView;
	private TextView accelerationAlphaTextView;
	private TextView accelerationWindowTextView;

	private DecimalFormat df;

	private CheckBox accelerationLPFSetAlphaCheckBox;

	private CheckBox accelerationLPFActiveCheckBox;

	private CheckBox accelerationMeanFilterActiveCheckBox;

	private RelativeLayout accelerationLPFSetAlphaView;

	private RelativeLayout accelerationMeanFilterSetWindowView;

	private RelativeLayout accelerationLPFToggleSetAlphaView;

	private LowPassFilter lpfAcceleration;

	private MeanFilter meanFilterAcceleration;

	private float accelerationLPFAlpha;
	private int accelerationMeanFilterWindow;

	private SimpleLinearAcceleration sensorFusion;

	/**
	 * Create a dialog.
	 * 
	 * @param context
	 *            The context.
	 * @param lpfAcceleration
	 *            The Wikipedia LPF.
	 * @param lpfMagnetic
	 *            The Android Developer LPF.
	 */
	public SettingsDialog(Context context,
			SimpleLinearAcceleration sensorFusion,
			LowPassFilter lpfAcceleration,MeanFilter meanFilterAcceleration)
	{
		super(context);

		this.sensorFusion = sensorFusion;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		this.lpfAcceleration = lpfAcceleration;
		this.meanFilterAcceleration = meanFilterAcceleration;

		readPrefs();

		inflater = getLayoutInflater();

		View settingsView = inflater.inflate(R.layout.settings, null, false);

		LinearLayout layout = (LinearLayout) settingsView
				.findViewById(R.id.layout_settings_content);

		createAccelerationFilterSettings();

		layout.addView(settingsFiltersAccelerationView);
		
		this.setContentView(settingsView);

		df = new DecimalFormat("#.####");
	}

	@Override
	public void onStop()
	{
		super.onStop();

		writePrefs();
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal)
	{
		if (picker.equals(lpfAccelerationAlphaNP))
		{
			accelerationAlphaTextView.setText(df.format(newVal * 0.001));

			if (showAccelerationSetAlpha)
			{
				accelerationLPFAlpha = newVal * 0.001f;

				lpfAcceleration.setAlpha(accelerationLPFAlpha);
			}
		}

		if (picker.equals(meanFilterAccelerationWindowNP))
		{
			accelerationWindowTextView.setText(df.format(newVal));

			accelerationMeanFilterWindow = newVal;

			meanFilterAcceleration.setWindowSize(accelerationMeanFilterWindow);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView.equals(this.accelerationLPFActiveCheckBox))
		{
			if (isChecked)
			{
				lpfAccelerationActive = true;

				showAccelerationToggleSetLPFAlphaView();

			}
			else
			{
				lpfAccelerationActive = false;

				removeAccelerationToggleSetLPFAlphaView();
			}

			this.sensorFusion.setLpfAccelerationActive(lpfAccelerationActive);
		}

		if (buttonView.equals(this.accelerationLPFSetAlphaCheckBox))
		{
			if (isChecked)
			{
				showAccelerationSetAlpha = true;

				showAccelerationSetLPFAlphaView();

				lpfAcceleration.setAlphaStatic(showAccelerationSetAlpha);
			}
			else
			{
				showAccelerationSetAlpha = false;

				removeAccelerationSetLPFAlphaView();

				lpfAcceleration.setAlphaStatic(showAccelerationSetAlpha);
			}
		}

		if (buttonView.equals(this.accelerationMeanFilterActiveCheckBox))
		{
			if (isChecked)
			{
				meanFilterAccelerationActive = true;

				showAccelerationSetMeanFilterWindow();
			}
			else
			{
				meanFilterAccelerationActive = false;

				removeAccelerationSetMeanFilterWindow();
			}

			this.sensorFusion
					.setMeanFilterAccelerationActive(meanFilterAccelerationActive);
		}
	}

	private void createAccelerationFilterSettings()
	{
		settingsFiltersAccelerationView = inflater.inflate(
				R.layout.settings_filter, null, false);

		settingsLPFAccelerationDynamicAlphaView = inflater.inflate(
				R.layout.settings_toggle_set_value, null, false);

		accelerationLPFActiveCheckBox = (CheckBox) settingsFiltersAccelerationView
				.findViewById(R.id.check_box_lpf);

		accelerationLPFActiveCheckBox.setOnCheckedChangeListener(this);

		if (lpfAccelerationActive)
		{
			accelerationLPFActiveCheckBox.setChecked(true);
		}
		else
		{
			accelerationLPFActiveCheckBox.setChecked(false);
		}

		accelerationMeanFilterActiveCheckBox = (CheckBox) settingsFiltersAccelerationView
				.findViewById(R.id.check_box_mean_filter);

		accelerationMeanFilterActiveCheckBox.setOnCheckedChangeListener(this);

		if (meanFilterAccelerationActive)
		{
			accelerationMeanFilterActiveCheckBox.setChecked(true);
		}
		else
		{
			accelerationMeanFilterActiveCheckBox.setChecked(false);
		}

		accelerationFilterTextView = (TextView) settingsFiltersAccelerationView
				.findViewById(R.id.label_filter_name);

		accelerationFilterTextView.setText("Acceleration");

		showAccelerationToggleSetLPFAlphaView();
	}

	/**
	 * Create the Android Developer Settings.
	 */
	private void showAccelerationToggleSetLPFAlphaView()
	{
		if (lpfAccelerationActive)
		{
			if (settingsLPFAccelerationToggleSetAlphaView == null)
			{
				settingsLPFAccelerationToggleSetAlphaView = inflater.inflate(
						R.layout.settings_toggle_set_value, null, false);
			}

			accelerationLPFSetAlphaCheckBox = (CheckBox) settingsLPFAccelerationToggleSetAlphaView
					.findViewById(R.id.check_box_static_alpha);

			accelerationLPFSetAlphaCheckBox.setOnCheckedChangeListener(this);

			if (showAccelerationSetAlpha)
			{
				accelerationLPFSetAlphaCheckBox.setChecked(true);
			}
			else
			{
				accelerationLPFSetAlphaCheckBox.setChecked(false);
			}

			accelerationLPFToggleSetAlphaView = (RelativeLayout) settingsFiltersAccelerationView
					.findViewById(R.id.layout_toggle_lpf_values);

			accelerationLPFToggleSetAlphaView.removeAllViews();

			accelerationLPFToggleSetAlphaView
					.addView(settingsLPFAccelerationToggleSetAlphaView);
		}
	}

	/**
	 * Show the Android Developer Settings.
	 */
	private void showAccelerationSetLPFAlphaView()
	{
		if (showAccelerationSetAlpha)
		{
			if (settingsLPFAccelerationSetAlphaView == null)
			{
				settingsLPFAccelerationSetAlphaView = inflater.inflate(
						R.layout.settings_filter_set_value, null, false);
			}

			accelerationAlphaTextView = (TextView) settingsLPFAccelerationSetAlphaView
					.findViewById(R.id.value);
			accelerationAlphaTextView.setText(String.valueOf(accelerationLPFAlpha));
			
			TextView accelerationLabelAlphaTextView = (TextView) settingsLPFAccelerationSetAlphaView
					.findViewById(R.id.label_value);
			accelerationLabelAlphaTextView.setText("Alpha:");

			lpfAccelerationAlphaNP = (NumberPicker) settingsLPFAccelerationSetAlphaView
					.findViewById(R.id.numberPicker1);
			lpfAccelerationAlphaNP.setMaxValue(1000);
			lpfAccelerationAlphaNP.setMinValue(0);
			lpfAccelerationAlphaNP.setValue((int) (accelerationLPFAlpha*100));

			lpfAccelerationAlphaNP.setOnValueChangedListener(this);

			accelerationLPFSetAlphaView = (RelativeLayout) settingsFiltersAccelerationView
					.findViewById(R.id.layout_set_lpf_values);

			accelerationLPFSetAlphaView
					.addView(settingsLPFAccelerationSetAlphaView);
		}
	}

	private void showAccelerationSetMeanFilterWindow()
	{
		if (meanFilterAccelerationActive)
		{
			if (settingsMeanFilterAccelerationSetWindowView == null)
			{
				settingsMeanFilterAccelerationSetWindowView = inflater.inflate(
						R.layout.settings_filter_set_value, null, false);
			}

			accelerationWindowTextView = (TextView) settingsMeanFilterAccelerationSetWindowView
					.findViewById(R.id.value);
			accelerationWindowTextView.setText(String.valueOf(accelerationMeanFilterWindow));

			meanFilterAccelerationWindowNP = (NumberPicker) settingsMeanFilterAccelerationSetWindowView
					.findViewById(R.id.numberPicker1);
			meanFilterAccelerationWindowNP.setMaxValue(100);
			meanFilterAccelerationWindowNP.setMinValue(0);
			meanFilterAccelerationWindowNP.setValue(accelerationMeanFilterWindow);

			meanFilterAccelerationWindowNP.setOnValueChangedListener(this);

			accelerationMeanFilterSetWindowView = (RelativeLayout) settingsFiltersAccelerationView
					.findViewById(R.id.layout_set_mean_filter_values);

			accelerationMeanFilterSetWindowView
					.addView(settingsMeanFilterAccelerationSetWindowView);
		}
	}
	
	/**
	 * Remove the Wikipedia Settings.
	 */
	private void removeAccelerationSetMeanFilterWindow()
	{
		if (!meanFilterAccelerationActive)
		{
			accelerationMeanFilterSetWindowView
					.removeView(settingsMeanFilterAccelerationSetWindowView);

			settingsFiltersAccelerationView.invalidate();
		}
	}

	/**
	 * Remove the Wikipedia Settings.
	 */
	private void removeAccelerationToggleSetLPFAlphaView()
	{
		if (!lpfAccelerationActive)
		{
			accelerationLPFToggleSetAlphaView = (RelativeLayout) settingsFiltersAccelerationView
					.findViewById(R.id.layout_toggle_lpf_values);

			accelerationLPFToggleSetAlphaView.removeAllViews();

			accelerationLPFToggleSetAlphaView.invalidate();
		}
	}

	/**
	 * Remove the Android Developer Settings.
	 */
	private void removeAccelerationSetLPFAlphaView()
	{
		if (!showAccelerationSetAlpha)
		{
			accelerationLPFSetAlphaView = (RelativeLayout) settingsFiltersAccelerationView
					.findViewById(R.id.layout_set_lpf_values);

			accelerationLPFSetAlphaView
					.removeView(settingsLPFAccelerationSetAlphaView);

			settingsFiltersAccelerationView.invalidate();
		}
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readPrefs()
	{
		SharedPreferences prefs = this.getContext().getSharedPreferences(
				"filter_prefs", Activity.MODE_PRIVATE);
		
		this.accelerationLPFAlpha = prefs
				.getFloat("lpf_acceleration_static_alpha_value", 0.4f);

		this.accelerationMeanFilterWindow = prefs
				.getInt("mean_filter_acceleration_window_value", 10);

		
		this.showAccelerationSetAlpha = prefs
				.getBoolean("lpf_acceleration_static_alpha", false);

		this.lpfAccelerationActive = prefs
				.getBoolean("lpf_acceleration", false);

		this.meanFilterAccelerationActive = prefs.getBoolean(
				"mean_filter_acceleration", false);
	}

	/**
	 * Write the preferences.
	 */
	private void writePrefs()
	{
		// Write out the offsets to the user preferences.
		SharedPreferences.Editor editor = this.getContext()
				.getSharedPreferences("filter_prefs", Activity.MODE_PRIVATE)
				.edit();
		
		editor.putFloat("lpf_acceleration_static_alpha_value", this.accelerationLPFAlpha);
		
		editor.putInt("mean_filter_acceleration_window_value", this.accelerationMeanFilterWindow);
		
		editor.putBoolean("lpf_acceleration_static_alpha", this.showAccelerationSetAlpha);

		editor.putBoolean("lpf_acceleration", this.lpfAccelerationActive);

		editor.putBoolean("mean_filter_acceleration",
				this.meanFilterAccelerationActive);

		editor.commit();
	}
}
