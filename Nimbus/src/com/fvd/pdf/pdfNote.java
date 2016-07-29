package com.fvd.pdf;

public class pdfNote extends pdfShape{

	public pdfNote(float xx, float yy, String text, float scale) {
		super(xx, yy, 5);
		this.text=text;
		this.scale=scale;
		type=Annotation.Type.TEXT;
		// TODO Auto-generated constructor stub
	}

}
