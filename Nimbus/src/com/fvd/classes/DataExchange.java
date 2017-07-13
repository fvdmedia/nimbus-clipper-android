package com.fvd.classes;

import java.io.Serializable;

public class DataExchange implements Serializable{
	
	public DataExchange (/*String d*/) {
		//content=d;
	}
	String content;
	public String getContent() {
		if (content==null) return "";
		return content;
	}
	
	public void setContent(String s) {
		content=s;
	}
	
	String title;
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String s) {
		title=s;
	}
	
	String tags;
	public String getTags() {
		return tags;
	}
	
	public void setTags(String s) {
		tags=s;
	}
	
	String id;
	public String getId() {
		return id;
	}
	
	public void setId(String s) {
		id=s;
	}
	
	String data;
	public String getData() {
		return data;
	}
	
	public void setData(String s) {
		data=s;
	}
	
}
