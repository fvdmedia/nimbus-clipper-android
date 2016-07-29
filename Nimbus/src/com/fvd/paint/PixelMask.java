package com.fvd.paint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class PixelMask extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	private Bitmap b=null;
	private float ls, ts;
	private Rect imgRect = new Rect(0,0,0,0);
	
	public PixelMask(float x, float y, int color, int width){
		uid = System.currentTimeMillis();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setColor(Color.argb(180, 35, 35, 35));
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(1);
		startX = x>0?x:0;
		startY = y>0?y:0;
		endX = x>0?x:0;
		endY = y>0?y:0;
		rect.set(startX, startY, endX , endY);
		
	}
	
	public void setBitmap(Bitmap bm, float l, float t){
		imgRect.set(0, 0, bm.getWidth(), bm.getHeight());
		this.b = pixelate(bm,32);
		
		
		ls = l;
		ts = t;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,0,0);
		rect.left+=ls;
		rect.top+=ts;
		rect.right = rect.left+bm.getWidth();
		rect.bottom = rect.top+bm.getHeight();
				
		
		paint.setColor(Color.WHITE);
		bm.recycle();
	}
	
	public int getWidth(){
		return (int)Math.abs(endX - startX);
	}
	
	public int getHeight(){
		return (int)Math.abs(endY - startY);
	}
	
	public int getLeft(){
		
		return (int)rect.left;
	}
	
	public int getTop(){
		
		return (int)rect.top;
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX , startY>endY?startY:endY);
		
	}
	
	public static int determineColor(Bitmap img, int x, int y, int w, int h) {
	    int cx = x + (int)(w / 2);
	    int cy = y + (int)(h / 2);
	    cx=Math.min(cx,(int)img.getWidth()-1);
	    cy=Math.min(cy,(int)img.getHeight()-1);
	    int c=0; 
	    try {
	    	c = img.getPixel(cx, cy);
	    }
	    catch (Exception e){
	    	
	    }
	    return c;
	}
	
	public static Bitmap pixelate(Bitmap sourceImg, int blockSize){
		Bitmap g = sourceImg.copy(Bitmap.Config.ARGB_8888, true);//Bitmap.createBitmap(sourceImg);
		Canvas canvas=new Canvas(g);
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		
		for (int i = 0; i < sourceImg.getWidth(); i += blockSize) {
		    for (int j = 0; j < sourceImg.getHeight(); j += blockSize) {
		    	p.setColor(determineColor(sourceImg, i, j, blockSize, blockSize));
		        canvas.drawRect(i, j, i+blockSize, j+blockSize, p);
		    }
		}
		return g;
	}
	
	
	@Override
	public void draw(final Canvas canvas, float scale) {
		if (!visible) return;
		if (b==null) canvas.drawRect(rect, paint);
		else canvas.drawBitmap(b, imgRect,rect, paint);
	}
	
	
	@Override
	public void updateRect(Rect cr, float l, float t ){
		visible = false;
		if(rect.left<=cr.left+l) {
			imgRect.left += (int)(cr.left  - rect.left);
			if(imgRect.right>cr.right) imgRect.right=imgRect.left + cr.right-cr.left;
			rect.left = l;
			rect.right = rect.left+imgRect.right-imgRect.left;
		}
		else {
			
			rect.set(0,0,imgRect.right-imgRect.left,imgRect.bottom-imgRect.top);
		}
		if(rect.top<cr.top) rect.top =0;
		
		
	}
		
	@Override
	public boolean undo(){
		visible=false;
		return true;
	}
}
