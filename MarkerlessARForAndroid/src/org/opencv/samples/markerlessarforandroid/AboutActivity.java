package org.opencv.samples.markerlessarforandroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;

import org.opencv.markerlessarforandroid.R;
import org.opencv.samples.markerlessarforandroid.util.IoUtils;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.TextView;

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
