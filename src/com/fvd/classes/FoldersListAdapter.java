package com.fvd.classes;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.fvd.classes.FolderListLoader.TempGroupSortAZComparator;
import com.fvd.nimbus.R;

/**
 * Created by scijoker on 26.04.15.
 */
public class FoldersListAdapter extends ArrayAdapter<FolderListItem> {
	private ArrayList<FolderListItem> items;
    private ArrayList<FolderListItem> openedItems;
    private String currentId;
    private OnFolderListClickListener listClickListener;
    private LayoutInflater inflater;
    //private ArrayList<String> allOpenedFolder;
    private Context context;

    public FoldersListAdapter(Context context, ArrayList<FolderListItem> groupItems, String cid, OnFolderListClickListener clickListener) {
        super(context, R.layout.item_folders_list_material, groupItems);
        this.context = context;
        this.items = groupItems;
        Collections.sort(this.items, new TempGroupSortAZComparator());
        
        HashMap<String, FolderListItem> map = new HashMap<String, FolderListItem>();
        for (FolderListItem folderListItem : items) {
			map.put(folderListItem.getGlobalId(), folderListItem);
		}
        
        currentId = cid;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listClickListener = clickListener;
        //allOpenedFolder = new ArrayList<String>();
        
        openedItems = new ArrayList<FolderListItem>();
        openedItems.clear();
        for (FolderListItem folderItem : items) {
            if (folderItem.getParentId().equals("root")) {
                folderItem.setLevel(0);
                openedItems.add(folderItem);
            } else /*if(!"default".equals(folderItem.getParentId()))*/{
            	map.get(folderItem.getParentId()).setHasChild(true);
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /*public ArrayList<String> getAllOpenFolders() {
        return allOpenedFolder;
    }*/

    private void getOpenFolder(ArrayList list, GroupItem groupItem) {
        if (groupItem.isClicked()) {
            list.add(groupItem);
            if (groupItem.getSubFolders().size() > 0) {
                getOpenFolder(list, groupItem);
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final FolderListItem groupItem = openedItems.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_folders_list_material, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.llContainer = (LinearLayout) convertView.findViewById(R.id.ll_container);
            viewHolder.tvStub = (TextView) convertView.findViewById(R.id.tv_stub);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_text);
            //viewHolder.tvCount = (TextView) convertView.findViewById(R.id.tv_subtext);
            viewHolder.ivArrow = (ImageView) convertView.findViewById(R.id.iv_arrow_item_folders_list);
            viewHolder.divider = (View) convertView.findViewById(R.id.divider);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (groupItem != null) {
            viewHolder.tvTitle.setText(groupItem.getTitle());
            viewHolder.tvStub.setText("");
            if (groupItem.getLevel() > 0) {
                makeFolderLevelMargin(viewHolder.tvStub, groupItem);
                viewHolder.divider.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.divider.setVisibility(View.VISIBLE);
            }
            if (groupItem.HasChild()) {
                viewHolder.ivArrow.setVisibility(View.VISIBLE);
                if (groupItem.isClicked()) {
                    viewHolder.ivArrow.setImageResource(R.drawable.ic_content_subup);
                } else {
                    viewHolder.ivArrow.setImageResource(R.drawable.ic_content_subdown);
                }
            } else {
                viewHolder.ivArrow.setVisibility(View.INVISIBLE);
            }
            viewHolder.ivArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (groupItem.isClicked()) {
                        groupItem.setClicked(false);
                        //allOpenedFolder.remove(groupItem.getGlobalId());
                        collapse(groupItem);
                        notifyDataSetChanged();
                    } else {
                        groupItem.setClicked(true);
                        expand(groupItem);
                        //allOpenedFolder.add(groupItem.getGlobalId());
                        notifyDataSetChanged();
                    }
                }
            });
            
            viewHolder.llContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listClickListener != null) {
                    	
                        listClickListener.onClick(groupItem);
                        currentId=groupItem.getGlobalId();
                        notifyDataSetChanged();
                    }
                }
            });
            
            viewHolder.llContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (listClickListener != null) {
                    	listClickListener.onLongClick(groupItem);
                    }
                    return true;
                }
            });
            
            if(currentId.equals(groupItem.getGlobalId())){
				viewHolder.tvTitle.setTextSize(20);// Typeface(null, Typeface.BOLD);
				viewHolder.tvTitle.setTypeface(null, Typeface.BOLD);
				viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.text_normal));
				//viewHolder.tvTitle.setImageResource(R.drawable.icon_folder);
			}
			else {
				viewHolder.tvTitle.setTextSize(15);//.setTypeface(null, Typeface.NORMAL);
				viewHolder.tvTitle.setTypeface(null, Typeface.NORMAL);
				viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.text_normal));
				//((ImageView)convertView.findViewById(R.id.itemIcon)).setImageResource(R.drawable.icon_folder);
			}
        }

        return convertView;
    }

    void expand(FolderListItem item){
    	int index=openedItems.indexOf(item);
    	int shift=1;
    	if(index>-1){
    		String gid = item.getGlobalId(); 
    		int level = item.getLevel();
    		for (FolderListItem folderItem : items) {
                if (folderItem.getParentId().equals(gid)) {
                    folderItem.setLevel(level+1);
                    folderItem.setClicked(false);
                    openedItems.add(index+shift, folderItem);
                    shift++;
                }
            }
    	}
    	item.setHasChild(shift!=1);
    	//notifyDataSetChanged();
    }
    
    void collapse(FolderListItem item){
    	int index=openedItems.indexOf(item);
    	if(index<openedItems.size()-1){
    		//String gid = item.getGlobalId(); 
    		int level = item.getLevel();
    		while(index+1<openedItems.size() && level<openedItems.get(index+1).getLevel()/*gid.equals(openedItems.get(index+1).getParentId())*/){
    			openedItems.remove(index+1);
    		}
    	}
   	
    	//notifyDataSetChanged();
    }
    
    private void makeFolderLevelMargin(TextView textView, FolderListItem groupItem) {
        textView.setText(makeMarginStrings(groupItem.getLevel()));
    }

    private String makeMarginStrings(int level) {
        if (level == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("      ");
        }
        return builder.toString();
    }

    private void setFolderNotesCountStrLine(TextView textView, GroupItem groupItem) {
        String s = "";
        /*int notesCount = groupItem.getGroupFolder().getCount();
        if (notesCount > 0) {
            if (notesCount == 1) {
                s = notesCount + " " + context.getString(R.string.text_note_count);
            } else if (notesCount > 1 && notesCount < 5) {
                s = notesCount + " " + context.getString(R.string.text_4_notes_count);
            } else {
                s = notesCount + " " + context.getString(R.string.text_notes_count);
            }
        }*/
        textView.setText(s);
    }

    private void setFolderSubfoldersStrLine(TextView textView, GroupItem groupItem) {
        String s = textView.getText().toString();
        /*int folderSubfoldersCount = groupItem.getSubFolders().size();
        if (folderSubfoldersCount > 0) {
            if (!s.equals("")) {
                s += ", ";
            }
            if (folderSubfoldersCount == 1) {
                s += folderSubfoldersCount + context.getString(R.string.text_subfolder);
            } else {
                s += folderSubfoldersCount + context.getString(R.string.text_subfolders);
            }
        }*/
    }

    @Override
    public int getCount() {
        return openedItems.size();
    }

    @Override
    public void add(FolderListItem item) {
    	item.setHasChild(false);
    	currentId = item.getGlobalId();
    	boolean f=false;
    	for(int i=0; i<items.size();i++){
    		if(item.getTitle().compareToIgnoreCase(items.get(i).getTitle())<0){
    			items.add(i, item);
    			f=true;
    			break;
    		}
    	}
    	if(!f)items.add(item);
    	
    	//items.add(item);
    	f=false;
    	if(item.getParentId().equals("root")){
    		for(int i=0; i<openedItems.size();i++){
        		if(openedItems.get(i).getLevel()==0&&item.getTitle().compareToIgnoreCase(openedItems.get(i).getTitle())<0){
        			openedItems.add(i, item);
        			f=true;
        			break;
        		}
        	}
    		if(!f) openedItems.add(item);
    		
    	} else {
    		f=false;
    		int p=-1;
    		for(int i=0; i<openedItems.size();i++){
        		if(openedItems.get(i).getGlobalId().equals(item.getParentId())){
        			p=i;
        			break;
        		}
        	}
    		if(p!=-1){
    			openedItems.get(p).setHasChild(true);
    			openedItems.get(p).setClicked(true);
    			if(p==openedItems.size()-1){
    				openedItems.add(item);
    			}
    			else {
    				FolderListItem parent=openedItems.get(p);
    				int level = parent.getLevel();
    				p++;
    				
    				/*while(p<openedItems.size()){
    					if(!openedItems.get(p).getParentId().equals(item.getParentId())){
    	    				f=true;
    	    				break;
    	    			}
    					else if(item.getTitle().compareToIgnoreCase(openedItems.get(p).getTitle())<0){
    						f=true;
    	    				break;
    					}
    					else {
    						p++;
    					}
    				}*/
    	    		while(p<openedItems.size() && level<openedItems.get(p).getLevel()){
    	    			openedItems.remove(p);
    	    		}
    	    		expand(parent);
    	    		
    	    		
    				/*if(f) openedItems.add(p,item);
    				else openedItems.add(item);*/
    			}
    		}
    		
    	}
    	notifyDataSetChanged();
    }

    @Override
    public void clear() {
        openedItems.clear();
        super.clear();
    }

    private class ViewHolder {
        TextView tvStub;
        LinearLayout llContainer;
        ImageView ivArrow;
        TextView tvTitle;
        //TextView tvCount;
        View divider;
    }

    public static interface OnFolderListClickListener {
        public void onClick(FolderListItem groupItem);
        public void onLongClick(FolderListItem groupItem);
    }
    
    private class TempGroupSortAZComparator implements Comparator<FolderListItem> {
        @Override
        public int compare(FolderListItem item, FolderListItem item2) {
            /*if (item.getParentId().equals("root") && item2.getParentId().equals("root")) {
                return item.getTitle().compareToIgnoreCase(item2.getTitle());
            }
            return 0;*/
        	return item.getTitle().compareToIgnoreCase(item2.getTitle());
        }
    } 
}
