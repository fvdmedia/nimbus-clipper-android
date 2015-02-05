package com.fvd.pdf;

import android.graphics.PointF;

public class pdfArrow extends pdfShape{
	float iScale=0.35f;
	public pdfArrow(float xx, float yy, int c){
		super(xx,yy,c);
		type=Annotation.Type.POLYLINE;
	}
	
	@Override
	public void setEnd(float xx, float yy) {
		float ln = (float)Math.hypot((xx-X),(yy-Y));
		float body = (int)(ln-30/iScale);
		//float slopeAngle =(float)(getAngle(xx-X,yy-Y));
		float slopeAngle = (float)(Math.atan2(yy - Y, xx - X));
		points.clear();
		/*points.add(new PointF(X ,Y));
		points.add(rotate(X-12/iScale ,Y-body,angle));
		points.add(rotate(X-24/iScale ,Y-(body-3/iScale),angle));
		points.add(rotate(X ,Y-ln,angle));
		points.add(rotate(X+24/iScale ,Y-(body-3/iScale),angle));
		points.add(rotate(X+12/iScale ,Y-body,angle));
		points.add(new PointF(X ,Y));*/
		
		points.add(new PointF(X,Y));
		points.add(rotate(body, 10/iScale, slopeAngle));
		points.add(rotate(body-5/iScale, 20/iScale, slopeAngle));
		points.add(rotate(ln, 0, slopeAngle));
		points.add(rotate(body - 5/iScale, -20/iScale, slopeAngle));
		points.add(rotate(body, -10/iScale, slopeAngle));
		points.add(new PointF(X,Y));
		
		//m.setRotate(angle,startX,startY);
	}
	
	float getAngle(float dx, float dy){
		double inRads = Math.atan2(-dy,-dx);
	    if (inRads < 0) inRads = Math.abs(inRads);
	    else inRads = 2*Math.PI - inRads;
	    //return (float)Math.toDegrees(inRads);
	    return (float)inRads;
	}
	
	PointF rotate(float x, float y, float angle){
		return new PointF(this.X+(float)(x * Math.cos(angle) - y * Math.sin(angle)),this.Y+(float)(x * Math.sin(angle) + y * Math.cos(angle)));
		//return new PointF(X+x,Y+y);
	}
}
