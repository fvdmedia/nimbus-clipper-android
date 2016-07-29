package com.fvd.classes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by scijoker on 04.07.14.
 */
public class GroupItem implements Serializable {

    private FolderListItem folderItem;
    private ArrayList<GroupItem> folderItems;
    private boolean isClicked;
    private boolean isHasSubfolders;

    public GroupItem(FolderListItem groupFolder, ArrayList<GroupItem> groupSubfolders) {
        this.folderItem = groupFolder;
        this.folderItems = groupSubfolders;
    }

    public FolderListItem getGroupFolder() {
        return folderItem;
    }

    public ArrayList<GroupItem> getSubFolders() {
        return folderItems;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }

    public boolean isHasSubfolders() {
        return isHasSubfolders;
    }

    public void addSubfolder(GroupItem groupItem) {
        this.folderItems.add(groupItem);
    }

    public void deleteSubfolders(GroupItem groupItem) {
        this.folderItems.remove(groupItem);
    }

    public void deleteSubfolders() {
        this.folderItems.clear();
    }

    public void setHasSubfolders(boolean isHasSubfolders) {
        this.isHasSubfolders = isHasSubfolders;
    }
}
