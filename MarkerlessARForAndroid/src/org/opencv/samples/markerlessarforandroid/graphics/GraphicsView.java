package org.opencv.samples.markerlessarforandroid.graphics;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GraphicsView extends GLSurfaceView {
		
	public GraphicsView(Context context) {
		super(context);
		init();
	}

	public GraphicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8,8,8,8,16,0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}
		
}
