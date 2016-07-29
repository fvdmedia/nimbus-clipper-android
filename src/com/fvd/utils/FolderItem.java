package com.fvd.utils;

public class FolderItem{
	public final String name;
	public final String id;
	public FolderItem(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	public FolderItem(String data) {
		this.name = data.substring(0,data.indexOf("::"));
		this.id = data.substring(data.indexOf("::")+2);
	}
}
