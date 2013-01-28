package com.example.demos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CommonImgEffectView extends View {  
	  
    private Context context ;  
    private Bitmap mainBmp , controlBmp ;  
    private int mainBmpWidth , mainBmpHeight , controlBmpWidth , controlBmpHeight ;  
    private Matrix matrix ;   
    private float [] srcPs , dstPs ;  
    private RectF srcRect , dstRect ;  
    private Paint paint ,paintRect , paintFrame;  
    private float deltaX = 0, deltaY = 0; //λ��ֵ   
    private float scaleValue = 1; //����ֵ   
    private Point lastPoint ;  
    private Point prePivot , lastPivot;  
    private float preDegree , lastDegree ;  
    private short currentSelectedPointindex;        //��ǰ���������   
    private Point symmetricPoint  = new Point();    //��ǰ������ԳƵ�   
      
    /** 
     * ͼƬ�������� 
     */  
    public static final int OPER_DEFAULT = -1;      //Ĭ��   
    public static final int OPER_TRANSLATE = 0;     //�ƶ�   
    public static final int OPER_SCALE = 1;         //����   
    public static final int OPER_ROTATE = 2;        //��ת   
    public static final int OPER_SELECTED = 3;      //ѡ��   
    public int lastOper = OPER_DEFAULT;  
      
    /* ͼƬ���Ƶ� 
     * 0---1---2 
     * |       | 
     * 7   8   3 
     * |       | 
     * 6---5---4  
     */  
    public static final int CTR_NONE = -1;  
    public static final int CTR_LEFT_TOP = 0;  
    public static final int CTR_MID_TOP = 1;  
    public static final int CTR_RIGHT_TOP = 2;  
    public static final int CTR_RIGHT_MID = 3;  
    public static final int CTR_RIGHT_BOTTOM = 4;  
    public static final int CTR_MID_BOTTOM = 5;  
    public static final int CTR_LEFT_BOTTOM = 6;  
    public static final int CTR_LEFT_MID = 7;  
    public static final int CTR_MID_MID = 8;  
    public int current_ctr = CTR_NONE;  
      
    public CommonImgEffectView(Context context){  
        super(context);  
        this.context = context ;  
    }  
      
    public CommonImgEffectView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        this.context = context ;  
        initData();  
    }  
      
    /** 
     * ��ʼ������ 
     * @author �Ž� 
     */  
    private void initData(){  
        mainBmp = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.bg);  
        controlBmp = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.ic_launcher);  
        mainBmpWidth = mainBmp.getWidth();  
        mainBmpHeight = mainBmp.getHeight();  
        controlBmpWidth = controlBmp.getWidth();  
        controlBmpHeight = controlBmp.getHeight();  
          
        srcPs = new float[]{  
                                0,0,   
                                mainBmpWidth/2,0,   
                                mainBmpWidth,0,   
                                mainBmpWidth,mainBmpHeight/2,  
                                mainBmpWidth,mainBmpHeight,   
                                mainBmpWidth/2,mainBmpHeight,   
                                0,mainBmpHeight,   
                                0,mainBmpHeight/2,   
                                mainBmpWidth/2,mainBmpHeight/2  
                            };  
        dstPs = srcPs.clone();  
        srcRect = new RectF(0, 0, mainBmpWidth, mainBmpHeight);  
        dstRect = new RectF();  
          
        matrix = new Matrix();  
          
        prePivot = new Point(mainBmpWidth/2, mainBmpHeight/2);  
        lastPivot = new Point(mainBmpWidth/2, mainBmpHeight/2);  
          
        lastPoint = new Point(0,0);  
          
        paint = new Paint();  
          
        paintRect = new Paint();  
        paintRect.setColor(Color.RED);  
        paintRect.setAlpha(100);  
        paintRect.setAntiAlias(true);  
          
        paintFrame = new Paint();  
        paintFrame.setColor(Color.GREEN);  
        paintFrame.setAntiAlias(true);  
          
        setMatrix(OPER_DEFAULT);  
    }  
      
    /** 
     * ����任���ﵽͼ��ƽ�Ƶ�Ŀ�� 
     * @author �Ž� 
     */  
    private void setMatrix(int operationType){  
        switch (operationType) {  
        case OPER_TRANSLATE:  
            matrix.postTranslate(deltaX , deltaY);  
            break;  
        case OPER_SCALE:  
            matrix.postScale(scaleValue, scaleValue, symmetricPoint.x, symmetricPoint.y);  
            break;  
        case OPER_ROTATE:  
            matrix.postRotate(preDegree - lastDegree, dstPs[CTR_MID_MID * 2], dstPs[CTR_MID_MID * 2 + 1]);  
            break;  
        }  
          
        matrix.mapPoints(dstPs, srcPs);  
        matrix.mapRect(dstRect, srcRect);  
    }  
      
    private boolean isOnPic(int x , int y){  
        if(dstRect.contains(x, y)){  
            return true;  
        }else   
            return false;  
    }  
      
    private int getOperationType(MotionEvent event){  
  
        int evX = (int)event.getX();  
        int evY = (int)event.getY();  
        int curOper = lastOper;  
        switch(event.getAction()) {  
            case MotionEvent.ACTION_DOWN:  
                current_ctr = isOnCP(evX, evY);  
                Log.i("img", "current_ctr is "+current_ctr);  
                if(current_ctr != CTR_NONE || isOnPic(evX, evY)){  
                    curOper = OPER_SELECTED;  
                }  
                break;  
            case MotionEvent.ACTION_MOVE:  
                    if(current_ctr > CTR_NONE && current_ctr < CTR_MID_MID ){  
                        curOper = OPER_SCALE;  
                    }else if(current_ctr == CTR_MID_MID ){  
                        curOper = OPER_ROTATE;  
                    }else if(lastOper == OPER_SELECTED){  
                        curOper = OPER_TRANSLATE;  
                    }  
                break;  
            case MotionEvent.ACTION_UP:  
                curOper = OPER_SELECTED;  
                break;  
            default:  
                break;  
        }  
        Log.d("img", "curOper is "+curOper);  
        return curOper;  
          
    }  
      
     /** 
     * �жϵ����ڵĿ��Ƶ� 
     * @param evX 
     * @param evY 
     * @return 
     */   
    private int isOnCP(int evx, int evy) {  
        Rect rect = new Rect(evx-controlBmpWidth/2,evy-controlBmpHeight/2,evx+controlBmpWidth/2,evy+controlBmpHeight/2);  
        int res = 0 ;  
        for (int i = 0; i < dstPs.length; i+=2) {  
            if(rect.contains((int)dstPs[i], (int)dstPs[i+1])){  
                return res ;  
            }  
            ++res ;   
        }  
        return CTR_NONE;  
    }  
      
      
      
    @Override  
    public boolean dispatchTouchEvent(MotionEvent event) {  
        int evX = (int)event.getX();  
        int evY = (int)event.getY();  
          
        int operType = OPER_DEFAULT;  
        operType = getOperationType(event);  
          
        switch (operType) {  
        case OPER_TRANSLATE:  
            translate(evX, evY);  
            break;  
        case OPER_SCALE:  
            scale(event);  
            break;  
        case OPER_ROTATE:  
            rotate(event);  
            break;  
        }  
      
        lastPoint.x = evX;  
        lastPoint.y = evY;    
          
        lastOper = operType;  
        invalidate();//�ػ�   
        return true;  
    }  
  
    /** 
     * �ƶ� 
     * @param evx 
     * @param evy 
     * @author zhang_jin1 
     */  
    private void translate(int evx , int evy){  
          
        prePivot.x += evx - lastPoint.x;  
        prePivot.y += evy -lastPoint.y;  
          
        deltaX = prePivot.x - lastPivot.x;  
        deltaY = prePivot.y - lastPivot.y;  
          
        lastPivot.x = prePivot.x;  
        lastPivot.y = prePivot.y;  
          
        setMatrix(OPER_TRANSLATE); //���þ���   
          
    }  
      
    /** 
     * ���� 
     * 0---1---2 
     * |       | 
     * 7   8   3 
     * |       | 
     * 6---5---4 
     * @param evX 
     * @param evY 
     */  
    private void scale(MotionEvent event) {  
          
        int pointIndex = current_ctr*2 ;  
          
        float px = dstPs[pointIndex];  
        float py = dstPs[pointIndex+1];  
          
        float evx = event.getX();  
        float evy = event.getY();  
          
        float oppositeX = 0 ;  
        float oppositeY = 0 ;  
        if(current_ctr<4 && current_ctr >= 0){  
             oppositeX = dstPs[pointIndex+8];  
             oppositeY = dstPs[pointIndex+9];  
        }else if(current_ctr >= 4){  
             oppositeX = dstPs[pointIndex-8];  
             oppositeY = dstPs[pointIndex-7];  
        }  
        float temp1 = getDistanceOfTwoPoints(px,py,oppositeX,oppositeY);  
        float temp2 = getDistanceOfTwoPoints(evx,evy,oppositeX,oppositeY);  
          
        this.scaleValue = temp2 / temp1 ;  
        symmetricPoint.x = (int) oppositeX;  
        symmetricPoint.y = (int)oppositeY;  
          
        Log.i("img", "scaleValue is "+scaleValue);  
        setMatrix(OPER_SCALE);  
    }  
      
    /** 
     * ��תͼƬ 
     * 0---1---2 
     * |       | 
     * 7   8   3 
     * |       | 
     * 6---5---4  
     * @param evX 
     * @param evY 
     */  
    private void rotate(MotionEvent event) {  
         
        if(event.getPointerCount() == 2){  
            preDegree = computeDegree(new Point((int)event.getX(0), (int)event.getY(0)), new Point((int)event.getX(1), (int)event.getY(1)));  
        }else{  
            preDegree = computeDegree(new Point((int)event.getX(), (int)event.getY()), new Point((int)dstPs[16], (int)dstPs[17]));  
        }  
        setMatrix(OPER_ROTATE);  
        lastDegree = preDegree;  
    }  
      
      
    /** 
     * ���������봹ֱ����н� 
     * @param p1 
     * @param p2 
     * @return 
     */  
    public float computeDegree(Point p1, Point p2){  
        float tran_x = p1.x - p2.x;  
        float tran_y = p1.y - p2.y;  
        float degree = 0.0f;  
        float angle = (float)(Math.asin(tran_x/Math.sqrt(tran_x*tran_x + tran_y* tran_y))*180/Math.PI);  
        if(!Float.isNaN(angle)){  
            if(tran_x >= 0 && tran_y <= 0){//��һ����   
                degree = angle;  
            }else if(tran_x <= 0 && tran_y <= 0){//�ڶ�����   
                degree = angle;  
            }else if(tran_x <= 0 && tran_y >= 0){//��������   
                degree = -180 - angle;  
            }else if(tran_x >= 0 && tran_y >= 0){//��������   
                degree = 180 - angle;  
            }  
        }  
        return degree;  
    }  
      
    /** 
     * ����������֮��ľ��� 
     * @param p1 
     * @param p2 
     * @return 
     */  
    private float getDistanceOfTwoPoints(Point p1, Point p2){  
        return (float)(Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));  
    }  
      
    private float getDistanceOfTwoPoints(float x1,float y1,float x2,float y2){  
        return (float)(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));  
    }  
      
      
    @Override  
    public void onDraw(Canvas canvas){  
        drawBackground(canvas);//���Ʊ���,�Ա���Ծ��ε�ӳ��   
        canvas.drawBitmap(mainBmp, matrix, paint);//������ͼƬ   
        drawFrame(canvas);//���Ʊ߿�,�Ա���Ե��ӳ��   
        drawControlPoints(canvas);//���ƿ��Ƶ�ͼƬ   
    }  
      
    private void drawBackground(Canvas canvas){  
        canvas.drawRect(dstRect, paintRect);  
    }  
      
    private void drawFrame(Canvas canvas){  
        canvas.drawLine(dstPs[0], dstPs[1], dstPs[4], dstPs[5], paintFrame);  
        canvas.drawLine(dstPs[4], dstPs[5], dstPs[8], dstPs[9], paintFrame);  
        canvas.drawLine(dstPs[8], dstPs[9], dstPs[12], dstPs[13], paintFrame);  
        canvas.drawLine(dstPs[0], dstPs[1], dstPs[12], dstPs[13], paintFrame);  
        canvas.drawPoint(dstPs[16], dstPs[17], paintFrame);  
    }  
      
    private void drawControlPoints(Canvas canvas){  
          
        for (int i = 0; i < dstPs.length; i+=2) {  
            canvas.drawBitmap(controlBmp, dstPs[i]-controlBmpWidth/2, dstPs[i+1]-controlBmpHeight/2, paint);  
        }  
          
    }  
      
}  