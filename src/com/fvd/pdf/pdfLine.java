package com.fvd.pdf;

import android.graphics.PointF;

public class pdfLine extends pdfShape{
	public pdfLine(float xx, float yy, int c){
		super(xx,yy, c);
		type=Annotation.Type.LINE;
		/*path.moveTo(X ,Y);
		path.lineTo(X ,Y);*/
		//points.add(new PointF(xx,yy));
	}
	
	@Override
	public void setEnd(float xx, float yy) {
		/*path.reset();
		path.moveTo(X ,Y);
		path.lineTo(xx ,yy);*/
		points.clear();
		points.add(new PointF(X,Y));
		points.add(new PointF(xx,yy));
	}

}
