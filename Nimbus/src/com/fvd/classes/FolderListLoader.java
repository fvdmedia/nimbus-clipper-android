package com.fvd.classes;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by scijoker on 26.03.15.
 */
public class FolderListLoader {
    
    private Cursor cursor;
    private ArrayList<FolderListItem> folders;
    private String currFolderGlobalId;
    private boolean isReturnGroupItems;
    private String excludedFolderGlobalId;
    private boolean escapeAddingCurrentFolderAsFirst;

    public FolderListLoader(ArrayList<FolderListItem> items, boolean isReturnGroupItems) {
        this.isReturnGroupItems = isReturnGroupItems;
        folders=items;
    }


    public FolderListLoader(String currFolderGlobalId, Context context) {
        this.currFolderGlobalId = currFolderGlobalId;

    }

    public FolderListLoader(String currFolderGlobalId, Context context, boolean escapeAddingCurrentFolderAsFirst) {
        this.currFolderGlobalId = currFolderGlobalId;
        this.escapeAddingCurrentFolderAsFirst = escapeAddingCurrentFolderAsFirst;
     }

    public void setExcludedFolderGlobalId(String excludedFolderGlobalId) {
        this.excludedFolderGlobalId = excludedFolderGlobalId;
    }

    public ArrayList<GroupItem> getGroupItems(){
    	final ArrayList<GroupItem> tempGroups = new ArrayList<GroupItem>();
        GroupItem groupItem = null;
        for (FolderListItem folderItem : folders) {
            if (folderItem.getParentId().equals("root")) {
                int level = 0;
                ArrayList<GroupItem> temgGroupItem = addSubFolders(level, folderItem);
                groupItem = new GroupItem(folderItem, temgGroupItem);
                if (temgGroupItem.size() > 0) {
                    groupItem.setHasSubfolders(true);
                }
                tempGroups.add(groupItem);
            }
        }

        if (!isReturnGroupItems) {
            if (tempGroups != null && tempGroups.size() > 0) {
                Collections.sort(tempGroups, new TempGroupSortAZComparator());
            }
            folders.clear();
            if (currFolderGlobalId != null && !escapeAddingCurrentFolderAsFirst) {
                folders.add(new FolderListItem("root", currFolderGlobalId, "bzzzz",0));
            }
            for (GroupItem tempGroup : tempGroups) {
                fillFolderList(0, tempGroup);
            }
            //return folders;
        }
        if (tempGroups != null && tempGroups.size() > 0) {
            Collections.sort(tempGroups, getSortComparator());
        }
        return tempGroups;
    }
    
    /*@Override
    public ArrayList loadInBackground() {
        Log.d("Folder", "In background");
        folders = new ArrayList<FolderListItem>();
        folders.clear();
        //повторно запускается, если база закрыта
        
        Log.d("FolderListLoader", "In background folders.size(): " + folders.size());
        final ArrayList<GroupItem> tempGroups = new ArrayList<GroupItem>();
        GroupItem groupItem = null;
        for (FolderListItem folderItem : folders) {
            if (folderItem.getParentId().equals("root")) {
                int level = 0;
                ArrayList<GroupItem> temgGroupItem = addSubFolders(level, folderItem);
                groupItem = new GroupItem(folderItem, temgGroupItem);
                if (temgGroupItem.size() > 0) {
                    groupItem.setHasSubfolders(true);
                }
                tempGroups.add(groupItem);
            }
        }

        if (!isReturnGroupItems) {
            if (tempGroups != null && tempGroups.size() > 0) {
                Collections.sort(tempGroups, new TempGroupSortAZComparator());
            }
            folders.clear();
            if (currFolderGlobalId != null && !escapeAddingCurrentFolderAsFirst) {
                folders.add(new FolderListItem(0, currFolderGlobalId, "titlttt", 0, "0", "0", 0, "root"));
            }
            for (GroupItem tempGroup : tempGroups) {
                fillFolderList(0, tempGroup);
            }
            return folders;
        }
        if (tempGroups != null && tempGroups.size() > 0) {
            Collections.sort(tempGroups, getSortComparator());
        }
        return tempGroups;
    }*/

    private int getFolderSize(String folderGlobalId) {
        int folderSize = 0;
        
        return folderSize;
    }

    private ArrayList<GroupItem> addSubFolders(int level, FolderListItem groupFolder) {
        groupFolder.setLevel(level);
        ArrayList<GroupItem> groupItems1 = new ArrayList<GroupItem>();
        for (FolderListItem folderItem : folders) {
            if (folderItem.getParentId().equals(groupFolder.getGlobalId())) {
                groupItems1.add(new GroupItem(folderItem, addSubFolders(level + 1, folderItem)));
            }
        }
        return groupItems1;
    }

    private void fillFolderList(int level, GroupItem groupItem) {
        FolderListItem folderListItem = groupItem.getGroupFolder();
        folderListItem.setLevel(level);
        folders.add(folderListItem);
        ArrayList<GroupItem> groupItems = groupItem.getSubFolders();
        if (groupItems != null && groupItems.size() > 0) {
            for (GroupItem item : groupItems) {
                fillFolderList(folderListItem.getLevel() + 1, item);
            }
        }
    }

    private Comparator<GroupItem> getSortComparator() {
        return new TempGroupSortAZComparator();
    }
    
    static class TempGroupSortAZComparator implements Comparator<GroupItem> {
        @Override
        public int compare(GroupItem item, GroupItem item2) {
            if (item.getGroupFolder().getParentId().equals("root") && item2.getGroupFolder().getParentId().equals("root")) {
                return item.getGroupFolder().getTitle().compareToIgnoreCase(item2.getGroupFolder().getTitle());
            }
            return 0;
        }
    } 
}