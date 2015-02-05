package com.fvd.pdf;

import android.graphics.PointF;

public class pdfFree extends pdfShape{
	public pdfFree(float xx, float yy, int c){
		super(xx,yy,c);
		/*path.moveTo(X ,Y);
		path.lineTo(X ,Y);*/
		points.add(new PointF(xx,yy));
	}
	
	@Override
	public void setEnd(float xx, float yy) {
		//path.quadTo(X, Y, (X + xx) / 2, (Y + yy) / 2);
		/*X=xx;
		Y=yy;*/
		points.add(new PointF(xx,yy));
	}
}
