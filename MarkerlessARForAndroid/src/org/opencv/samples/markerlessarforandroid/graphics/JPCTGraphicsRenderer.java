package org.opencv.samples.markerlessarforandroid.graphics;

import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.markerlessarforandroid.R;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class JPCTGraphicsRenderer implements GLSurfaceView.Renderer{
	
	private Context context;
	private final String TAG = "JPCTGraphicsRenderer";
	
	private FrameBuffer fb;
	private World world;
	private Object3D logo;
	
	public JPCTGraphicsRenderer(Context context) {
		this.context = context;
		world = new World();
		world.setAmbientLight(100, 100, 100);
	}
	
	public void init() {
		//Load logo
		logo = loadModel();
		logo.build();
		
		// Add logo to scene
		world.addObject(logo);
		
		// Place camera
		Camera cam = world.getCamera();
		cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
		cam.lookAt(logo.getTransformedCenter());
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		if (logo != null)
			logo.rotateY(0.005f);
		fb.clear();
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(width, height);
	}
	

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Do nothing
	}
	
    private Object3D loadModel() {
    	InputStream objStream = context.getResources().openRawResource(R.drawable.opencv_logo);
		InputStream mtlStream = context.getResources().openRawResource(R.drawable.opencv_logo_texture);
		Object3D[] model = Loader.loadOBJ(objStream, mtlStream, 1);
        Object3D o3d = new Object3D(0);
        Object3D temp = null;
        for (int i = 0; i < model.length; i++) {
            temp = model[i];
            temp.setCenter(SimpleVector.ORIGIN);
            temp.rotateX((float)( -0.5*Math.PI));
            temp.rotateMesh();
            temp.setRotationMatrix(new Matrix());
            o3d = Object3D.mergeObjects(o3d, temp);
            o3d.build();
        }
        return o3d;
    }

}
