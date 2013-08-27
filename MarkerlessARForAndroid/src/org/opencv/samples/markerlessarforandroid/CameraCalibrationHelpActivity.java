package org.opencv.samples.markerlessarforandroid;

import org.opencv.markerlessarforandroid.R;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class CameraCalibrationHelpActivity extends Activity {
	
	TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_calibration_help);
		
		text = (TextView) findViewById(R.id.calibration_help_text);
		text.setText(Html.fromHtml(getString(R.string.hello_world)));
	}


}
