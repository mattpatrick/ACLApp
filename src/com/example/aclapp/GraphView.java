package com.example.aclapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
	
	
	private Bitmap  mBitmap; // Bitmap to draw the graph line
	private Bitmap 	mBitmapBackground; // Bitmap for the background
	private Paint   mPaint = new Paint(); // style for drawing the line
    private Canvas  mCanvas = new Canvas(); // Holds draw calls
    
	private float   mSpeed = 50.0f; // 
	private float   mLastX; // Marker for where the line is currently drawing
    private float   mScale; // Value for scaling the graph size
    private float   mLastValue; // Value for most recent graph point
    private float   mYOffset; // Vertical offset for graph
    private int     mColor; // Color of graph line
    private float   mWidth; // Width of graph line
    private float   maxValue = 1024f; // Limiting the y values of the plot
    
    // To initialize GraphView 
    public GraphView(Context context) {
        super(context); //
        init();
    }
    
    // To initialize GraphView settings
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    // Initializing style settings
    private void init(){
    	mColor = Color.argb(192, 64, 128, 64);
    	mPaint.setStrokeWidth(3);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);  
    }
    
    // To add a new data point to the graph
    public void addDataPoint(float value){
        final Paint paint = mPaint;
        float newX = mLastX + mSpeed; // X coordinate
        final float v = mYOffset + value * mScale; // Y coordinate
        
        paint.setColor(mColor);
        mCanvas.drawLine(mLastX, mLastValue, newX, v, paint); //Draw point using coordinates newX and v
        mLastValue = v;
        mLastX += mSpeed; 
        
		invalidate();
    }
    
    // Limiting the maximum value of the graph
    public void setMaxValue(int max){
    	maxValue = max;
    	mScale = - (mYOffset * (1.0f / maxValue));
    }
    
    // Set speed that the graph moves
    public void setSpeed(float speed){
    	mSpeed = speed;
    }
    
    // Modifies the bitmap in the case that the screen size is changed
    // Not used in this application
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);

        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFF111111);
        mYOffset = h;
        mScale = - (mYOffset * (1.0f / maxValue));
        mWidth = w;
        mLastX = mWidth;
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    // When drawing, if the line has filled the canvas, restart from the beginning
    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                if (mLastX >= mWidth) {
                    mLastX = 0;
                    final Canvas cavas = mCanvas;
                    cavas.drawColor(0xFF111111);
                    mPaint.setColor(0xFF777777);
                    cavas.drawLine(0, mYOffset, mWidth, mYOffset, mPaint);
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
        } 
    }
}
