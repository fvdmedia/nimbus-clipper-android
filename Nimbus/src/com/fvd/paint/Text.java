package com.fvd.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Rect;
import android.graphics.RectF;

public class Text extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	private String[] lines;
	float tH=0;
	float tW=0;
	float lH=0;
	
	public Text(Context c, float x, float y, int color, int width, String t,float sc){
		super(c, color, width,sc);
		paint.setStrokeWidth((width/6)/sc);
		paint.setAntiAlias(true);
       
		Rect rr=new Rect();
		lines= t.split("\n");
		paint.setTextSize(width/sc);
		selPaint.setStrokeWidth(2/sc);
		for(int i=0; i<lines.length; i++){
			paint.getTextBounds(lines[i], 0, lines[i].length(), rr);
			if ((rr.right - rr.left)>tW)	tW =rr.right - rr.left;
			tH += (rr.bottom-rr.top);
			lH = (rr.bottom-rr.top);
		}
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX-10, startY-lH-10, endX+tW+10 , startY-lH+tH+20);
		closeRect.set(rect.right,rect.top-20,rect.right+70,rect.top+50);
		saveOnchange = true;

	}
	
	public void setText(String t){
		saveOnchange = true;		
		lines= t.split("\n");
		Rect rr=new Rect();
		for(int i=0; i<lines.length; i++){
			paint.getTextBounds(lines[i], 0, lines[i].length(), rr);
			if ((rr.right - rr.left)>tW)	tW =rr.right - rr.left;
			tH += (rr.bottom-rr.top);
			lH = (rr.bottom-rr.top);
		}
		rect.set(startX-10/iScale, startY-lH-10/iScale, endX+tW+10/iScale , startY-lH+tH+20/iScale);
		closeRect.set(rect.right,rect.top-25/iScale,rect.right+50/iScale,rect.top+25/iScale);
	}
	
	public void setFontSize(int w){
		pushState();
		_width = w;
		paint.setTextSize(w/iScale);
		paint.setStrokeWidth((w/6)/iScale);
		tH=0;
		tW=0;
		Rect rr=new Rect();
		for(int i=0; i<lines.length; i++){
			paint.getTextBounds(lines[i], 0, lines[i].length(), rr);
			if ((rr.right - rr.left)>tW)	tW =rr.right - rr.left;
			tH += (rr.bottom-rr.top);
			lH = (rr.bottom-rr.top);
		}
		rect.set(startX-10/iScale, startY-lH-10/iScale, endX+tW+10/iScale , startY-lH+tH+20/iScale);

		closeRect.set(rect.right,rect.top-25/iScale,rect.right+50/iScale,rect.top+25/iScale);
	}
	
	public String getText(){
		StringBuilder sb=new StringBuilder();
		for(String line:lines){
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString().trim();
	}
	
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
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
		select(rect.contains(x, y));
		
		return selected;
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		startX -= dx;
		startY -= dy;
		endX -= dx;
		endY -= dy;
		rect.set(startX-10/iScale, startY-lH-10/iScale, endX+tW+10/iScale , startY-lH+tH+20/iScale);
		closeRect.set(rect.right,rect.top-25/iScale,rect.right+50/iScale,rect.top+25/iScale);
	}
	
	boolean stroke = false;
	public void setStroke(boolean b) {
		stroke = b;
	}
	
	public boolean getStroke(){
		return stroke;
	}
	
	public void drawMultiline(Canvas m_canvas, int x, int y)
    {
        for (String line: lines)
        {
        	if(stroke){
        		paint.setShadowLayer(3,8,8,0xff222222);
        		paint.setStyle(Paint.Style.STROKE);
        		paint.setColor(Color.WHITE);
        		m_canvas.drawText(line, x, y, paint);
        	}
        	paint.setShadowLayer(stroke?0:3,8,8,0xff222222);
        	paint.setStyle(Paint.Style.FILL);
        	paint.setColor(_color);
        	m_canvas.drawText(line, x, y, paint);
            rect.bottom=y+20/iScale;
            y += -paint.ascent() + paint.descent();
              
        }
    }
	
	@Override
	public void draw(final Canvas canvas, float scale) {
		if(visible){
			drawMultiline(canvas, (int)startX, (int)startY);
			if (selected) {
				canvas.drawRect(rect, selPaint);
				canvas.drawBitmap(iconClose, closeSrc,closeRect, selPaint);
			}
		}
	}
	
	@Override
	public int getFontSize(){
		return _width;
	}
	
	@Override
	public int getStrokeWidth(){
		return -1;
	}
	
	@Override
	public void setLineWidth(int w){
		
	}
	
	@Override
	public void setScale(float sc) {
		iScale = sc;
		tH=0;
		tW=0;
		Rect rr=new Rect();
		for(int i=0; i<lines.length; i++){
			paint.getTextBounds(lines[i], 0, lines[i].length(), rr);
			if ((rr.right - rr.left)>tW)	tW =rr.right - rr.left;
			tH += (rr.bottom-rr.top);
			lH = (rr.bottom-rr.top);
		}
		rect.set(startX-10/iScale, startY-lH-10/iScale, endX+tW+10/iScale , startY-lH+tH+20/iScale);
		closeRect.set(rect.right,rect.top-25/iScale,rect.right+50/iScale,rect.top+25/iScale);
	}
	
	@Override
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), new RectF(startX, startY,endX,endY),new RectF(paint.getTextSize(),lH,tH,tW)));
	}
	
	@Override
	public boolean undo(){
		boolean b=false;
		if(super.undo()){
			startX=rect.left;
			startY = rect.top;
			endX = rect.right;
			endY = rect.bottom;
			rect.set(startX-10/iScale, startY-lH-10/iScale, endX+tW+10/iScale , startY-lH+tH+20/iScale);
			closeRect.set(rect.right,rect.top-25/iScale,rect.right+50/iScale,rect.top+25/iScale);
			paint.setTextSize(params.left);
			lH=params.top;
			tH = params.right;
			tW = params.bottom;
			b=true;
		}
		return b;
	}

}
