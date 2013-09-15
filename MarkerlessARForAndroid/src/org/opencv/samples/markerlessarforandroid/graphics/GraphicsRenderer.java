package org.opencv.samples.markerlessarforandroid.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.samples.markerlessarforandroid.calibration.CameraCalibration;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/*
 * This class has the job of rendering graphics onto Graphics View.
 * It uses OpenGL ES 2.0 to do the rendering.
 */
public class GraphicsRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "GraphicsRenderer";

	private CameraCalibration cameraCalib;
	private Mat patternPose;
	private boolean patternPresent;

	private boolean started;

	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];

	// Subclasses containing the drawing logic for each axis
	private Axis xAxis;
	private Axis yAxis;
	private Axis zAxis;

	public GraphicsRenderer(CameraCalibration cameraCalib) {
		this.cameraCalib = cameraCalib;
	}

	public void start() {
		patternPose = new Mat(4, 4, CvType.CV_32F);
		patternPresent = false;
		started = true;
	}

	public void stop() {
		started = false;
	}

	/**
	 * This method is called when the surface is first created. It will also be
	 * called if we lose our surface context and it is later recreated by the
	 * system.
	 * 
	 * This method instantiates the three items we which to be drawn on this
	 * surface; three axes each a different color and each starting at the
	 * origin and pointing outwards.
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		xAxis = new Axis(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
		yAxis = new Axis(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		zAxis = new Axis(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f);
	}

	// This is called whenever it’s time to draw a new frame.
	// Note we don't use GL10 we use the static methods in GLES20 instead
	@Override
	public synchronized void onDrawFrame(GL10 unused) {

		// Clear the background
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (started) {
			// Set the camera to be at
			// The eye point of the camera is at 0,0,3 in the world coordinates
			// The position of the reference point is 0,0,0. This means the
			// camera
			// is gazing so that the world origin is in the center of the
			// screen.
			// The up vector is 0,1,0 which means the camera considers the y
			// direction to be up

			if (patternPresent) {
				// Set mVMatrix with the object post relative to the camera
				// The matrix that maps from
				// camera to marker pose
				patternPose.get(0, 0, mVMatrix);
			} else {
				Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3f, 0f, 0f, 0f, 0f, 1.0f,
						0.0f);
			}

			// Calculate the projection and view transformation
			Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

			// Draw each object under the current mMVPMatrix
			xAxis.draw(mMVPMatrix);
			yAxis.draw(mMVPMatrix);
			zAxis.draw(mMVPMatrix);
		}
	}

	/**
	 * This is called whenever the surface changes; for example, when switching
	 * from portrait to landscape. It is also called after the surface has been
	 * created.
	 * 
	 * OpenGL assumes that the coordinate space is square and uniform even if
	 * the actual screen is not square and uniform. This means a unit square
	 * drawn in OpenGL will appear stretched unless something is done about it.
	 * 
	 * This method calculates the projection matrix for the scene which adjusts
	 * object coordinates so that unit objects are indeed unit size when
	 * rendered.
	 * 
	 * The projection matrix is the final matrix which is applied to a
	 * renderable point. Afterwards, the point is now in normalized device
	 * coordinates. The bottom left corner will be (-1,-1) and the top right
	 * will be (1,1). OpenGL does the job of mapping these onto the surface's
	 * viewport.
	 * 
	 * Our projection matrix is a perspective projection which makes distant
	 * objects appear smaller.
	 * 
	 */
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {

		GLES20.glViewport(0, 0, width, height);

		if (started) {
			float nearPlane = 0.01f; // Near clipping distance
			float farPlane = 100.0f; // Far clipping distance

			// Camera parameters
			float fx = cameraCalib.getFx(); // Focal length in x axis
			float fy = cameraCalib.getFy(); // Focal length in y axis
			float cx = cameraCalib.getCx(); // Camera primary point x
			float cy = cameraCalib.getCy(); // Camera primary point y

			// Source: http://opencv.willowgarage.com/wiki/Posit
			// Build Projection Matrix in OpenGL Column-major format
			mProjMatrix[0] = - 2.0f * fx / width;
			mProjMatrix[1] = 0.0f;
			mProjMatrix[2] = 0.0f;
			mProjMatrix[3] = 0.0f;

			mProjMatrix[4] = 0.0f;
			mProjMatrix[5] = 2.0f * fy / height;
			mProjMatrix[6] = 0.0f;
			mProjMatrix[7] = 0.0f;

			mProjMatrix[8] = 2.0f * (cx / width) - 1.0f;
			mProjMatrix[9] = 2.0f * (cy / height) - 1.0f;
			mProjMatrix[10] = -(farPlane + nearPlane)
					/ (farPlane - nearPlane);
			mProjMatrix[11] = -1.0f;

			mProjMatrix[12] = 0.0f;
			mProjMatrix[13] = 0.0f;
			mProjMatrix[14] = -2.0f * farPlane * nearPlane
					/ (farPlane - nearPlane);
			mProjMatrix[15] = 0.0f;
		}
	}

	// Loads and compiles shaders
	/**
	 * Loads a shader into the current context.
	 * 
	 * @param type
	 *            GLES20.GL_VERTEX_SHADER or GLES20.GL_FRAGMENT_SHADER
	 * @param shaderCode
	 *            the shader source code in string format
	 * @return
	 */
	public static int loadShader(int type, String shaderCode) {
		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 * 
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 * 
	 * If the operation is not successful, the check throws an error.
	 * 
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(String glOperation) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}

	public synchronized void setPatternPose(Mat pose) {
		patternPresent = pose != null;
		patternPose = pose;
	}

	public void clearPose() {
		patternPresent = false;
		patternPose = null;
	}
}

/**
 * This helper class embodies the drawing of an axis line.
 * 
 */
class Axis {

	// The Vertex Shader renders the vertices of the axis.
	// The uMVPMatrix is accessible from outside the shader.
	// The vPosition resembles the position of the vertex to draw in the world.
	// gl_Position is the final position to render.
	private final String vertexShaderCode = "uniform mat4 uMVPMatrix;"
			+ "attribute vec4 vPosition;" + "void main() {"
			+ "  gl_Position = uMVPMatrix * vPosition; " + "}";

	// The Fragment Shader renders face of the shape with color or texture
	private final String fragmentShaderCode = "precision mediump float;"
			+ "uniform vec4 vColor;" + "void main() {"
			+ "  gl_FragColor = vColor;" + "}";

	private int vertexCount;
	private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

	// Set color with red, green, blue and alpha (opacity) values
	float color[];

	// OpenGL Buffer for storing shape coordinates
	private FloatBuffer vertexBuffer;

	// Rendering program for shape
	private final int mProgram;

	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	float lineCoords[];

	public Axis(float x, float y, float z, float r, float g, float b) {

		lineCoords = new float[] { 0.0f, 0.0f, 0.0f, x, y, z };
		vertexCount = lineCoords.length / COORDS_PER_VERTEX;
		color = new float[] { r, g, b, 1.0f };

		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				lineCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(lineCoords);
		vertexBuffer.position(0);

		int vertexShader = GraphicsRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragmentShader = GraphicsRenderer.loadShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL ES Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables
	}

	public void draw(float[] mvpMatrix) {
		// Add program to OpenGL ES environment
		GLES20.glUseProgram(mProgram);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the axis vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the axis coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		// get handle to fragment shader's vColor member
		mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

		// Set color
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);

		// get handle to shape's transformation matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GraphicsRenderer.checkGlError("glGetUniformLocation");

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GraphicsRenderer.checkGlError("glUniformMatrix4fv");

		GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

}
