package com.fvd.classes;

import java.io.Serializable;

/**
 * Created by scijoker on 5/7/14.
 */
public class FolderListItem implements Serializable {
    private String title;
    private int level;
    private String globalId;
    private String parentId;
    private boolean hasChild;
    private boolean clicked;

    public FolderListItem(String globalId, String parentId, String title, int level) {
        this.globalId = globalId;
        this.parentId = parentId;
        this.title = title;
        this.level = level;
        hasChild=false;
        clicked=false;
    }

    

    public String getTitle() {
        return title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getParentId() {
        return parentId;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean HasChild() {
        return hasChild;
    }
    
    void setHasChild(boolean b){
    	hasChild=b;
    }
    
    public boolean isClicked() {
    	return clicked;
	}
    
    public void setClicked(boolean b) {
		clicked = b;
	}
}
