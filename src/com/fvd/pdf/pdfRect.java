package com.fvd.pdf;

import android.graphics.PointF;

public class pdfRect extends pdfShape{
	public pdfRect(float xx, float yy, int c){
		super(xx,yy, c);
		type=Annotation.Type.POLYLINE;
	}
	
	@Override
	public void setEnd(float xx, float yy) {
		points.clear();
		points.add(new PointF(X,Y));
		points.add(new PointF(X,yy));
		points.add(new PointF(xx,yy));
		points.add(new PointF(xx,Y));
		points.add(new PointF(X,Y));
	}
}
