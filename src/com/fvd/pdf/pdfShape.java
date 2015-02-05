package com.fvd.pdf;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;

public class pdfShape {
	//protected Path path=new Path();
	protected int col_in=0;
	protected Annotation.Type type=Annotation.Type.INK;
	protected ArrayList<PointF> points=new ArrayList<PointF>();
	protected String text="";
	float X;
	float Y;
	float scale=1.0f;
	public pdfShape(int c){
		col_in=c;
	}
	
	public pdfShape(float xx, float yy, int c){
		X=xx;
		Y=yy;
		col_in=c;
	}
	
	public float getScale() {
		return scale;
	}
	
	public float getX(){return X;}
	public float getY(){return Y;}

	public void setEnd(float x, float y){};
	
	public void setText(String s){
		text=s;
	}
	/*public Path getPath() {
		return path;
	}*/
	public ArrayList<PointF> getPoints() {
		return points;
	}
	
	int size(){
		return points.size();
	}
	
	/*Annotation.Type getType(){
		return type;
	}*/
	
	public int getColor () {
		switch (col_in) {
		case 0 : {
			return Color.CYAN;
		}
		case 1 : {
			return Color.RED;
		}
		case 2 : {
			return Color.GREEN;
		}
		case 3 : {
			return Color.WHITE;
		}
		case 4 : {
			return Color.BLACK;
		}
		case 5 : {
			return Color.YELLOW;
		}
		
		default:
			return Color.RED;
	}
	}
}
