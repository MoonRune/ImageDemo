package com.example.demos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

@SuppressLint("NewApi")
public class ImageActivity extends Activity {

	private static final String TAG = "IMAGEACTIVITY";
	private ImageView mImageView = null;
	private Bitmap mBitmap = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		toggleFullScreenWindow(true);
		setContentView(R.layout.image_view);
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		initCompoments();

	}

	public void initCompoments() {
		mImageView = (ImageView) findViewById(R.id.imageView_content);
		mImageView.setImageBitmap(mBitmap);
		mImageView.setOnTouchListener(new MulitPointTouchListener());
	}

	protected void toggleFullScreenWindow(boolean fullScreen) {
		if (fullScreen) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public void saveImage(View view) {
	
	}

	public class MulitPointTouchListener implements OnTouchListener {
		private static final String TAG = "MulitPointTouchListener";
		Matrix matrix = new Matrix();
		Matrix savedMatrix = new Matrix();
	
		static final int NONE = 0;
		static final int DRAG = 1;
		static final int ZOOM = 2;
		int mode = NONE;

		PointF start = new PointF();
		PointF mid = new PointF();
		float oldDist = 1f;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ImageView view = (ImageView) v;
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				matrix.set(view.getImageMatrix());
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				Log.v("okit", "start x="+start.x+" y="+start.y);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				Log.v("bqfche", "ACTION_POINTER_DOWN");
				oldDist = spacing(event);
				if (oldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mid, event);
					Log.v("okit", "midpoint x="+mid.x+" y="+mid.y);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				
				if (mode == DRAG) {
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);

				} else if (mode == ZOOM) {
					Log.v("okit", "event 0 x="+event.getX(0)+" y="+event.getY(0));
					Log.v("okit", "event 1 x="+event.getX(1)+" y="+event.getY(1));
					float newDist = spacing(event);
					Log.d(TAG, "newDist=" + newDist);
					if (event.getHistorySize() >= 1&&newDist > 10f) {
						 PointF p=getNowMid(event);
						 matrix.set(savedMatrix);
						 float xMove=p.x-mid.x;
						 float yMove=p.y-mid.y;
						  matrix.postTranslate(xMove,yMove );
						  matrix.postScale(newDist/oldDist,newDist/oldDist,p.x,p.y);
						  float now =Float.parseFloat(String.valueOf((finalradio(p.x,start.x+xMove,event.getX(0),
								  p.y,start.y+yMove ,event.getY(0)))));
						  matrix.postRotate(now,p.x,p.y);
					}
				}
				break;
			}
			view.setImageMatrix(matrix);
			return true; 
		}

		private PointF getNowMid(MotionEvent e)
		{
			PointF p=new PointF();
			p.set((e.getX(0)+e.getX(1))/2,(e.getY(0)+e.getY(1))/2);
			return p;
		}
		private double finalradio(float x1,float x2,float x3,float y1,float y2,float y3)
		{
			float xx1=x2-x1;
			float yy1=y2-y1;
			float xx2=x3-x1;
			float yy2=y3-y1;
			float cosa=dotProduct(xx1,xx2,yy1,yy2)/(length(xx1,yy1)*length(xx2,yy2));
			if(isLagerThanPI(xx1,xx2,yy1,yy2))
			{
				return 360-Math.acos(cosa)*180/Math.PI;
			}
			else
			{
				return Math.acos(cosa)*180/Math.PI;
			}
			
		}
		private boolean isLagerThanPI(float x1,float x2,float y1,float y2)
		{
			return (x1*y2-x2*y1)<0;
		}
		private float length(float x1,float y1)
		{
			return  FloatMath.sqrt(x1*x1+y1*y1);
		}
		private float dotProduct(float x1,float x2,float y1,float y2)
		{
			return x1*x2+y1*y2;
		}

		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			
			return FloatMath.sqrt(x * x + y * y);
		}
	

		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}
	}
}
