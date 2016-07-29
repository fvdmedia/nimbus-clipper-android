package com.fvd.paint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;

public class MaskRect extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	private Bitmap b=null;
	private int ls, ts;
	private boolean cut = false;
	private boolean stroke=false;
	private RectF DispRect=new RectF();
	private Rect src=new Rect();
	private Rect dst = new Rect();
	float[] scale= new float[9];

	public MaskRect(int x, int y, int width, int height, Bitmap bt, float l, float t){
		uid = System.currentTimeMillis();
		DispRect.set(x,y,width,height);
		b=PixelMask.pixelate(bt,16);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		PathEffect effects = new DashPathEffect(new float[]{7,5},1);
		paint.setPathEffect(effects);
		paint.setStrokeWidth(3);
		ls=(int)l;
		ts=(int)t;
		
	}
	
	
	public void setStart(float x, float y){
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX, startY, endX , endY);
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		
		closeRect.set(rect.right,rect.top-20,rect.right+70,rect.top+50);
		
		cut=true;
		stroke = true;
	}
	
	public void complete(){
		stroke = false;
	}
	
	@Override
	public void draw(final Canvas canvas, float scale) {
		if(!visible) return;
		paint.setStrokeWidth(3/scale);
		if (b!=null) {
			
			if (cut) {
			src.set(0,0,(int)(rect.left-ls),b.getHeight());
			dst.set(src.left+ls,src.top+ts,src.right+ls,src.bottom+ts);
			canvas.drawBitmap(b, src,dst,paint);
			
			src.set((int)(rect.left-ls),0,(int)(rect.right-ls),(int)(rect.top-ts));
			dst.set(src.left+ls,src.top+ts,src.right+ls,src.bottom+ts);
			canvas.drawBitmap(b, src,dst,paint);
			
			src.set((int)(rect.right-ls),0,b.getWidth(),b.getHeight());
			dst.set(src.left+ls,src.top+ts,src.right+ls,src.bottom+ts);
			canvas.drawBitmap(b, src,dst,paint);
			
			src.set((int)(rect.left-ls),(int)(rect.bottom-ts),(int)(rect.right-ls),b.getHeight());
			dst.set(src.left+ls,src.top+ts,src.right+ls,src.bottom+ts);
			canvas.drawBitmap(b, src,dst,paint);
			if (stroke) canvas.drawRect(rect, paint);
			
			}
			else canvas.drawBitmap(b, ls,ts, paint);
		}
		
	}	
	
	@Override
	public void updateRect(Rect cr, float l, float t ){
		visible = false;
	}
	
	@Override
	public boolean undo(){
		visible=false;
		return true;
	}
}
