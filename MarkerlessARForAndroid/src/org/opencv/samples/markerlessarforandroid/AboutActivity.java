package org.opencv.samples.markerlessarforandroid;

import org.opencv.markerlessarforandroid.R;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		TextView text = (TextView)findViewById(R.id.AboutTextView);
		text.setText(Html.fromHtml(getString(R.string.about_the_app)));
	}

}
