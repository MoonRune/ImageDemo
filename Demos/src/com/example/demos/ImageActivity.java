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

public class ImageActivity extends Activity {

	private static final String TAG = "IMAGEACTIVITY";
	private ImageView mImageView = null;
	private Bitmap mBitmap = null;
	private DisplayMetrics dm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.image_view);

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);



		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);

		initCompoments();

	}

	public void initCompoments() {
		mImageView = (ImageView) findViewById(R.id.imageView_content);
		mImageView.setImageBitmap(mBitmap);
		mImageView.setOnTouchListener(new MulitPointTouchListener());
	}

	

	public void saveImage(View view) {

	}

	public class MulitPointTouchListener implements OnTouchListener {
		private static final String TAG = "MulitPointTouchListener";
		// 当前的状态
		private Matrix matrix = new Matrix();
		// 保存的状态
		private Matrix savedMatrix = new Matrix();

		private static final int NONE = 0;
		private static final int DRAG = 1;
		private static final int ZOOM = 2;
		private int mode = NONE;

		private PointF start = new PointF();

		// AB中点
		private PointF mid = new PointF();
		private float oldDist = 1f;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ImageView view = (ImageView) v;
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				matrix.set(view.getImageMatrix());
				// 一只手指先点下去了 保存矩阵,并保存A点
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				Log.v(TAG, "start x=" + start.x + " y=" + start.y);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				Log.v(TAG, "ACTION_POINTER_DOWN");
				// 第二只手指点下去了 ,保存矩阵并保存中点,与AB长度
				oldDist = spacing(event);

				savedMatrix.set(matrix);
				midPoint(mid, event);
				Log.v(TAG, "midpoint x=" + mid.x + " y=" + mid.y);
				mode = ZOOM;

				break;
			case MotionEvent.ACTION_UP:
				mode = NONE;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				// 动作完成
				mode = NONE;
				break;
			case MotionEvent.ACTION_MOVE:

				if (mode == DRAG) {
					// 一只手指拉动
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);

				} else if (mode == ZOOM) {
					// 两只手指 做手势的时候
					Log.v(TAG,
							"event 0 x=" + event.getX(0) + " y="
									+ event.getY(0));
					Log.v(TAG,
							"event 1 x=" + event.getX(1) + " y="
									+ event.getY(1));
					// A'B'的长度
					float newDist = spacing(event);
					Log.d(TAG, "newDist=" + newDist);
					// A'B'中点
					PointF p = getNowMid(event);
					matrix.set(savedMatrix);
					// AB移动到A''B'
					float xMove = p.x - mid.x;
					float yMove = p.y - mid.y;
					matrix.postTranslate(xMove, yMove);
					// A''B'缩放到A'''B'
					matrix.postScale(newDist / oldDist, newDist / oldDist, p.x,
							p.y);
					// A'''B'旋转到A'B' 旋转角度是0-2PI之间的
					float now = (float) (finalradio(p.x, start.x + xMove,
							event.getX(0), p.y, start.y + yMove, event.getY(0)));
					matrix.postRotate(now, p.x, p.y);
				}
				break;
			}
			view.setImageMatrix(matrix);
			return true;
		}

		/**
		 * 
		 * @function 获取中点
		 * 
		 * */
		private PointF getNowMid(MotionEvent e) {
			PointF p = new PointF();
			p.set((e.getX(0) + e.getX(1)) / 2, (e.getY(0) + e.getY(1)) / 2);
			return p;
		}

		/**
		 * 
		 * @function 用向量获取旋转角度
		 * 
		 * */
		private double finalradio(float x1, float x2, float x3, float y1,
				float y2, float y3) {
			float xx1 = x2 - x1;
			float yy1 = y2 - y1;
			float xx2 = x3 - x1;
			float yy2 = y3 - y1;
			float cosa = getCosa(xx1, xx2, yy1, yy2)
					/ (length(xx1, yy1) * length(xx2, yy2));
			if (isLagerThanPI(xx1, xx2, yy1, yy2)) {
				return 360 - Math.acos(cosa) * 180 / Math.PI;
			} else {
				return Math.acos(cosa) * 180 / Math.PI;
			}

		}

		/**
		 * 
		 * 旋转角度是0-360的所以要判断他是不是大于PI
		 * */
		private boolean isLagerThanPI(float x1, float x2, float y1, float y2) {
			return (x1 * y2 - x2 * y1) < 0;
		}
		/**
		 * 
		 * 向量len
		 * */
		private float length(float x1, float y1) {
			return FloatMath.sqrt(x1 * x1 + y1 * y1);
		}

		private float getCosa(float x1, float x2, float y1, float y2) {
			return x1 * x2 + y1 * y2;
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
