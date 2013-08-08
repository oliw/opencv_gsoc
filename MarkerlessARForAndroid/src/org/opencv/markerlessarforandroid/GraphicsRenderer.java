package org.opencv.markerlessarforandroid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

public class GraphicsRenderer implements GLSurfaceView.Renderer {
	
    private static final String TAG = "GraphicsRenderer";

	private NativeFrameProcessor processor;
	
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];

	private Axis xAxis;

	public GraphicsRenderer(NativeFrameProcessor processor) {
		this.processor = processor;
	}

	// This is called whenever it’s time to draw a new frame.
	// Note we don't use GL10 we use the static methods in GLES20 instead
	@Override
	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		
		// Set the camera position (View matrix)
	    Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

	    // Calculate the projection and view transformation
	    Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		xAxis.draw(mMVPMatrix);
	}

	// This is called whenever the surface changes; for example, when switching
	// from portrait to landscape. It is also called after the surface has been
	// created.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		float ratio = (float) width / height;
		
		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}

	// This method is called when the surface is first created. It will also be
	// called if we lose our surface context and it is later recreated by the
	// system.
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Set the background frame color to grey
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		
		xAxis = new Axis(1.0f,0.0f,0.0f);
	}
	
	// Loads and compiles shaders
	public static int loadShader(int type, String shaderCode){
	    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    // add the source code to the shader and compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);

	    return shader;
	}

}

class Axis {
	
	// The Vertex Shader renders the vertices of your shape
	private final String vertexShaderCode =
			"uniform mat4 uMVPMatrix;" +
		    "attribute vec4 vPosition;" +
		    "void main() {" +
		    "  gl_Position = vPosition * uMVPMatrix;" +
		    "}";

	// The Fragment Shader renders face of the shape with color or texture
	private final String fragmentShaderCode =
		    "precision mediump float;" +
		    "uniform vec4 vColor;" +
		    "void main() {" +
		    "  gl_FragColor = vColor;" +
		    "}";
	
    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
    
    // Set color with red, green, blue and alpha (opacity) values
    float color[];
	
	private FloatBuffer vertexBuffer;
	private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 3;
	static float squareCoords[] = { -0.5f, 0.5f, 0.0f, // top left
			-0.5f, -0.5f, 0.0f, // bottom left
			0.5f, -0.5f, 0.0f, // bottom right
			0.5f, 0.5f, 0.0f }; // top right

	private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

	public Axis(float r, float g, float b) {
		color = new float[] {r,g,b,1.0f};
		
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				squareCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);

		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(
		// (# of coordinate values * 2 bytes per short)
				drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		
		int vertexShader = GraphicsRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader = GraphicsRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
	    
	    mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
	    GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
	    GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
	}
	
	public void draw(float[] mvpMatrix) {
		   // Add program to OpenGL ES environment
	    GLES20.glUseProgram(mProgram);

	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 vertexStride, vertexBuffer);

	    // get handle to fragment shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

	    // Set color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);
	    
	    // get handle to shape's transformation matrix
	    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	    
	    // Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
	    
	    // Draw the squares
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	}
}
