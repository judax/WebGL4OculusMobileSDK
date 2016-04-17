package com.judax.webgl2opengl.oculusmobilesdk;

import org.xwalk.core.XWalkView;

import com.judax.webgl2opengl.Triangle;
import com.judax.webgl2opengl.WebGLMessage;
import com.judax.webgl2opengl.WebGLMessageProcessorImpl;
import com.judax.webgl2opengl.xwalk.WebGLXWalkExtension;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class WebGL2OpenGLOculusMobileSDKActivity extends Activity implements SurfaceHolder.Callback
{
	private static final boolean USE_XWALK = true;
	private static final boolean DRAW_TRIANGLE = false;
	
	private Triangle triangle = null;
	
	private float[] projectionMatrix = new float[16];
	private float[] modelViewMatrix = new float[16];
	private int jsCameraModelViewMatrixId;
	private int jsProjectionMatrixId;
	
	private String url;
		
	// Load the gles3jni library right away to make sure JNI_OnLoad() gets called as the very first thing.
	static
	{
		System.loadLibrary( "WebGL2OpenGL4OculusMobileSDK" );
	}

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private long nativePointer = 0;
	
	private XWalkView xwalkView = null;
	private WebGLXWalkExtension webGLXWalkExtension = null;

	private void updateFromNative()
	{
		if (DRAW_TRIANGLE)
		{
			if (triangle == null)
			{
				triangle = new Triangle();
			}
		}
		
		// DIRTY HACK BEGIN ===================================
		// TODO: Get rid of this! It is just a test to verify that it is possible to have frame accurate VR!
		// From making the queue public to this whole hack, it should be handled internally using some configuration data.
//		synchronized(webGLMessageProcessor)
//		{
//			
//			if (!webGLMessageProcessor.webGLMessagesQueueCopy.isEmpty())
//				System.out.println("JUDAX: updateFrmNative: webGLMessagesQueueCopy.size() = " + webGLMessageProcessor.webGLMessagesQueueCopy.size());
//			
//			for (WebGLMessage webGLMessage: webGLMessageProcessor.webGLMessagesQueueCopy)
//			{
//				String message = webGLMessage.getMessage();
//				
//				System.out.println("JUDAX: updateFromName: message = " + message);
//				
//				if (message.contains("getUniformLocation"))
//				{
//					boolean cameraMatrix = message.contains("uCameraModelViewMatrix");
//					boolean projectionMatrix = !cameraMatrix && message.contains("uProjectionMatrix");
//					if (cameraMatrix || projectionMatrix)
//					{
//						try
//						{
//							JSONObject messageJSON = new JSONObject(message);
//							if (cameraMatrix)
//							{
//								jsCameraModelViewMatrixId = messageJSON.getInt("extId");
//							}
//							else if (projectionMatrix)
//							{
//								jsProjectionMatrixId = messageJSON.getInt("extId");
//							}
//						}
//						catch(JSONException e)
//						{
//							System.err.println("JUDAX: " + e.toString());
//						}
//					}
//				}
//				webGLMessage.run();
//			}		
//			// Get rid of all the messages
//			webGLMessageProcessor.webGLMessagesQueueCopy.clear();
//		}
		// DIRTY HACK END ===================================
		
		webGLMessageProcessor.update();
	}
	
	private void renderFrameFromNative()
	{
		webGLMessageProcessor.renderFrame();
		
		if (DRAW_TRIANGLE)
		{
			if (triangle != null)
			{
				triangle.draw(projectionMatrix, modelViewMatrix, null);
			}
		}
	}
	
	private WebGLMessageProcessorImpl webGLMessageProcessor = new WebGLMessageProcessorImpl()
	{
		// DIRTY HACK BEGIN ===================================
//		private boolean showFirstFrame = true;
		// TODO: Get rid of this! It is just a test to verify that it is possible to have frame accurate VR!
//		@Override
//		public synchronized void renderFrame()
//		{
//			for (WebGLMessage webGLMessage: webGLMessagesQueueInsideAFrameCopy)
//			{
//				String message = webGLMessage.getMessage();
//				
//				if (showFirstFrame) System.out.println("JUDAX: renderFrame: " + message);
//				
//				boolean messageProcessed = false;
//				if (message.contains("uniformMatrix4fv"))
//				{
//					try
//					{
//						JSONObject messageJSON = new JSONObject(message);
//						JSONArray args = messageJSON.getJSONArray("args");
//						int jsId = args.getJSONObject(0).getInt("extId");
//						float[] matrix = null;
//						if (jsId == jsCameraModelViewMatrixId)
//						{
//							matrix = modelViewMatrix;
//						}
//						else if (jsId == jsProjectionMatrixId)
//						{
//							matrix = projectionMatrix;
//						}
//						if (matrix != null)
//						{
//							int location = webGLMessage.getNativeIdFromJSId(jsId);
//							int count = 1;
//							boolean transpose = args.getBoolean(1);
//							int offset = 0;
//							GLES20.glUniformMatrix4fv(location, count, transpose, matrix, offset);
//							messageProcessed = true;
//						}
//					}
//					catch(JSONException e)
//					{
//						System.err.println("JUDAX: " + e.toString());
//					}
//				}
//				if (!messageProcessed)
//				{
//					webGLMessage.run();
//				}
//			}
//			
//			if (showFirstFrame && !webGLMessagesQueueInsideAFrameCopy.isEmpty()) showFirstFrame = false;
//		}		
		// DIRTY HACK END ===================================

	};

	@Override protected void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		
		surfaceView = new SurfaceView( this );
		setContentView( surfaceView );
		surfaceView.getHolder().addCallback( this );

		// Force the screen to stay on, rather than letting it dim and shut off
		// while the user is watching a movie.
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

		// Force screen brightness to stay at maximum
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = 1.0f;
		getWindow().setAttributes( params );

		url = "";
		Intent intent = getIntent();
		if (intent != null)
		{
			Bundle extras = intent.getExtras(); 
			if (extras != null) 
			{
				url = extras.getString("url");
			}
		}
		
		nativePointer = nativeOnCreate( this );
	}

	@Override protected void onStart()
	{
		super.onStart();
		nativeOnStart( nativePointer );
	}

	@Override protected void onResume()
	{
		super.onResume();
		nativeOnResume( nativePointer );
	}

	@Override protected void onPause()
	{
		nativeOnPause( nativePointer );
		super.onPause();
	}

	@Override protected void onStop()
	{
		nativeOnStop( nativePointer );
		super.onStop();
	}

	@Override protected void onDestroy()
	{
		if ( surfaceHolder != null )
		{
			nativeOnSurfaceDestroyed( nativePointer );
		}
		nativeOnDestroy( nativePointer );
		super.onDestroy();
		nativePointer = 0;
	}

	@Override public void surfaceCreated( SurfaceHolder holder )
	{
		if ( nativePointer != 0 )
		{
			nativeOnSurfaceCreated( nativePointer, holder.getSurface() );
			surfaceHolder = holder;
		}
		
		if (USE_XWALK && xwalkView == null)
		{
			xwalkView = new XWalkView(this);
			xwalkView.clearCache(true);
			webGLXWalkExtension = new WebGLXWalkExtension(webGLMessageProcessor);
			xwalkView.load(url, null);
		}
	}

	@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
	{
		if ( nativePointer != 0 )
		{
			nativeOnSurfaceChanged( nativePointer, holder.getSurface() );
			surfaceHolder = holder;
		}
	}
	
	@Override public void surfaceDestroyed( SurfaceHolder holder )
	{
		if ( nativePointer != 0 )
		{
			nativeOnSurfaceDestroyed( nativePointer );
			surfaceHolder = null;
		}
	}

	@Override public boolean dispatchKeyEvent( KeyEvent event )
	{
		if ( nativePointer != 0 )
		{
			int keyCode = event.getKeyCode();
			int action = event.getAction();
			if ( action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_UP )
			{
				return super.dispatchKeyEvent( event );
			}
			if ( action == KeyEvent.ACTION_UP )
			{
//				Log.v( TAG, "GLES3JNIActivity::dispatchKeyEvent( " + keyCode + ", " + action + " )" );
			}
			nativeOnKeyEvent( nativePointer, keyCode, action );
		}
		return true;
	}

	@Override public boolean dispatchTouchEvent( MotionEvent event )
	{
		if ( nativePointer != 0 )
		{
			int action = event.getAction();
			float x = event.getRawX();
			float y = event.getRawY();
			if ( action == MotionEvent.ACTION_UP )
			{
//				Log.v( TAG, "GLES3JNIActivity::dispatchTouchEvent( " + action + ", " + x + ", " + y + " )" );
			}
			nativeOnTouchEvent( nativePointer, action, x, y );
		}
		return true;
	}
	
	private static String matrixToString(float[] matrix)
	{
		String s = "[";
		for (int i = 0; i < matrix.length; i++)
		{
			s += matrix[i] + (i < matrix.length - 1 ? ", " :  "");
		}
		s += "]";
		return s;
	}
	
	private void setProjectionMatrixFromNative(float[] projectionMatrix)
	{
		WebGLMessage.setProjectionMatrixFromNative(projectionMatrix);
	}

	private void setModelViewMatrixFromNative(float[] modelViewMatrix)
	{
		WebGLMessage.setModelViewMatrixFromNative(modelViewMatrix);
	}
	
	
	// Activity lifecycle
	private native long nativeOnCreate( Activity obj );
	private native void nativeOnStart( long handle );
	private native void nativeOnResume( long handle );
	private native void nativeOnPause( long handle );
	private native void nativeOnStop( long handle );
	private native void nativeOnDestroy( long handle );

	// Surface lifecycle
	public native void nativeOnSurfaceCreated( long handle, Surface s );
	public native void nativeOnSurfaceChanged( long handle, Surface s );
	public native void nativeOnSurfaceDestroyed( long handle );

	// Input
	private native void nativeOnKeyEvent( long handle, int keyCode, int action );
	private native void nativeOnTouchEvent( long handle, int action, float x, float y );
}