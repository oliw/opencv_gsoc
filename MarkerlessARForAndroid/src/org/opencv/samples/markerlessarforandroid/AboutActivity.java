package org.opencv.samples.markerlessarforandroid;

import org.opencv.markerlessarforandroid.R;
import android.os.Bundle;
import android.app.Activity;
import android.webkit.WebView;

/**
 * This activity displays an about screen about the application. The information is presented as formatted HTML text.
 * @author Oliver Wilkie
 *
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		WebView web = (WebView)findViewById(R.id.webview);
		web.loadUrl("file:///android_asset/readme.html");
	}

}
