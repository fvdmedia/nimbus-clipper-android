package com.fvd.paint;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class Arrow extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	private Matrix m = new Matrix();
	public Arrow(){
		super();
	}
	public Arrow(float x, float y){
		super();
		paint.setStyle(Paint.Style.FILL);
		selPaint.setStyle(Paint.Style.STROKE);
		selPaint.setStrokeWidth(paint.getStrokeWidth()-5);
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX,startY,endX ,endY);
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
	}
	
	@Override
	public void setScale(float sc){
		//iScale = sc;
		paint.setStyle(Paint.Style.FILL);
		selPaint.setStyle(Paint.Style.STROKE);
		selPaint.setStrokeWidth(4/iScale);
		path.reset();
		float ln = (float)Math.hypot((endX-startX),(endY-startY));
		int body = (int)(ln-30/iScale);
		path.moveTo(startX ,startY);
		path.lineTo(startX-12/iScale ,startY-body);
		path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX ,startY-ln);
		path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX+12/iScale ,startY-body);
		path.lineTo(startX ,startY);
		
		float angle = getAngle(endY-startY,endX-startX);
		m.setRotate(angle,startX,startY);
		path.transform(m);
	}
	
	public Arrow(Context c, float x, float y, int color, int width, float sc){
		super(c, color, width,sc);
		//iScale =sc;
		paint.setStyle(Paint.Style.FILL);
		selPaint.setStyle(Paint.Style.STROKE);
		selPaint.setStrokeWidth(4/iScale);
		//_width = width;
		_progress = width;
		_width = (width/10+1)*5;
		iScale=(float)(1-0.04*(_width-2));
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX,startY,endX ,endY);
		//closeRect.set(startX+20,startY-20,startX+20+70,startY+50);
		closeRect.set(Math.max(startX, endX)+20,Math.min(endY, startY)-20,startX+20+70,startY+50);
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
	}
	
		
	float getAngle(float dx, float dy){
		double inRads = Math.atan2(-dy,-dx);
	    if (inRads < 0) inRads = Math.abs(inRads);
	    else inRads = 2*Math.PI - inRads;
	    return (float)Math.toDegrees(inRads);
	}
	
	@Override
	public void setEnd(float x, float y){
		paint.setStyle(Paint.Style.FILL);
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		setCloseRect();
		path.reset();

		float ln = (float)Math.hypot((x-startX),(y-startY));
		int body = (int)(ln-30/iScale);
		path.moveTo(startX ,startY);
		path.lineTo(startX-12/iScale ,startY-body);
		path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX ,startY-ln);
		path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX+12/iScale ,startY-body);
		path.lineTo(startX ,startY);
		float angle = getAngle(endY-startY,endX-startX);
		m.setRotate(angle,startX,startY);
		
		path.transform(m);
		
	}
	
	@Override
	public boolean contains(float x, float y){
		if (!visible) return false;
		if (selected) {
			if(inCloseRect(x,y)) {
				visible=false;
				return false;
			}
		}
		float xln = (float) Math.hypot((x-startX),(y-startY));
		xln += (float) Math.hypot((x-endX),(y-endY));
		float ln = (float) Math.hypot((endX-startX),(endY-startY));
		select(xln>ln-20 && xln<ln+20);
		
		return selected;
	}
	
	
	@Override
	public void setLineWidth(int w){
		pushState();
		_progress=w;
		_width = (w/10+1)*5;
		iScale=(float)(1-0.04*(_width-2));
		setCloseRect();
		path.reset();
		float ln = (float)Math.hypot((endX-startX),(endY-startY));
		//iScale+=(scaleX);
		
		int body = (int)(ln-30/iScale);
		path.moveTo(startX ,startY);
		path.lineTo(startX-12/iScale ,startY-body);
		path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX ,startY-ln);
		path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX+12/iScale ,startY-body);
		path.lineTo(startX ,startY);
		float angle = getAngle(endY-startY,endX-startX);
		m.setRotate(angle,startX,startY);
		path.transform(m);
		//paint.setStrokeWidth(w/iScale);
		//selPaint.setStrokeWidth((w+6)/iScale);
	}
	
	@Override
	public boolean inCloseRect(float x, float y){
		final float  d=30;
		return x>Math.min(closeRect.left, closeRect.right)-d && x<Math.max(closeRect.left, closeRect.right)+d && y>Math.min(closeRect.top, closeRect.bottom)-d && y<Math.max(closeRect.top, closeRect.bottom)+d;         
	}
	
	void setCloseRect(){
		//int iScale=1;
		//RectF r = new RectF();
		//path.computeBounds(r, false);
		if(startX<endX){
			closeRect.set(endX-150/*/iScale*/, endY-(startY>endY?100:0),endX+150 ,endY);
		}
		else{
			closeRect.set(endX+100/*/iScale*/, endY-(startY>endY?100:0),endX+150 ,endY);
		}
		
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		startX -= dx;
		startY -= dy;
		endX -= dx;
		endY -= dy;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		
		setCloseRect();
		path.reset();
		float ln = (float)Math.hypot((endX-startX),(endY-startY));
		int body = (int)(ln-30/iScale);
		path.moveTo(startX ,startY);
		path.lineTo(startX-12/iScale ,startY-body);
		path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX ,startY-ln);
		path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX+12/iScale ,startY-body);
		path.lineTo(startX ,startY);
		
		float angle = getAngle(endY-startY,endX-startX);
		m.setRotate(angle,startX,startY);
		path.transform(m);
		
	}
	
	@Override
	public void scale(float dx, float dy){
		if (scaleX==dx && scaleY==dy) return;
		super.scale(dx, dy);
		scaleX=dx;
		scaleY=dy;
		float dw = Math.abs((rect.right - rect.left)*dx*0.5f*scC);
		float dh = Math.abs((rect.bottom - rect.top)*dy*0.5f*scC);
		if (dx>1f){
			if (startX<=endX){
				startX -= dw;
				endX += dw;
			}
			else {
				startX += dw;
				endX -= dw;
			}
		}
		else {
			if (startX<=endX){
				startX += dw;
				endX -= dw;
			}
			else {
				startX -= dw;
				endX += dw;
			}
		}
		if (dy>1f){
			if(startY<=endY){
				startY -= dh;
				endY += dh;
			}
			else {
				startY += dh;
				endY -= dh;
			}
		}
		else {
			if(startY<=endY){
				startY += dh;
				endY -= dh;
			}
			else {
				startY -= dh;
				endY += dh;
			}
		}
		
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		//if (dx<1f)iScale*=1.07; else iScale*=0.93;
		setCloseRect();
		path.reset();
		float ln = (float)Math.hypot((endX-startX),(endY-startY));
		//iScale+=(scaleX);
		
		int body = (int)(ln-30/iScale);
		path.moveTo(startX ,startY);
		path.lineTo(startX-12/iScale ,startY-body);
		path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX ,startY-ln);
		path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
		path.lineTo(startX+12/iScale ,startY-body);
		path.lineTo(startX ,startY);
		float angle = getAngle(endY-startY,endX-startX);
		m.setRotate(angle,startX,startY);
		path.transform(m);
	}
	
	@Override
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), new RectF(startX, startY,endX,endY)));
	}
	
	@Override
	public boolean undo(){
		boolean b=false;
		if(super.undo()){
			startX=rect.left;
			startY = rect.top;
			endX = rect.right;
			endY = rect.bottom;
			rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
			
			setCloseRect();
			path.reset();

			float ln = (float)Math.hypot((endX-startX),(endY-startY));
			
			int body = (int)(ln-30/iScale);
			path.moveTo(startX ,startY);
			path.lineTo(startX-12/iScale ,startY-body);
			path.lineTo(startX-24/iScale ,startY-(body-3/iScale));
			path.lineTo(startX ,startY-ln);
			path.lineTo(startX+24/iScale ,startY-(body-3/iScale));
			path.lineTo(startX+12/iScale ,startY-body);
			path.lineTo(startX ,startY);
			float angle = getAngle(endY-startY,endX-startX);
			m.setRotate(angle,startX,startY);
			path.transform(m);
			b=true;
		}
		return b;
	}

}
