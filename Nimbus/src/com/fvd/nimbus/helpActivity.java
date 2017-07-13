package com.fvd.nimbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.fvd.utils.appSettings;


public class helpActivity extends Activity {

	private static final String KEY_TITLE = "title";
	
	private static final String KEY_IMAGE_RES_ID = "image_res";
	
	private ViewFlipper flipper;
	private RadioGroup btnsGroup;
	
	private ArrayList<Map<String, Object>> pages;
	private Map<String, Object> contentMap;
	
	private Animation inFromRight;
	private Animation outToLeft;
	private Animation inFromLeft;
	private Animation outToRight;
	
	private GestureDetector gestureDetector;
	private OnTouchListener onTouchListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try{
			setContentView(R.layout.layout_help);
	     	if (getResources().getInteger(R.integer.is_tablet)==0) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			flipper = (ViewFlipper) findViewById(R.id.flipper);
			btnsGroup = (RadioGroup) findViewById(R.id.radioGroup);
			
			inFromRight = AnimationUtils.loadAnimation(this, R.anim.in_from_right_animation);
			outToLeft = AnimationUtils.loadAnimation(this, R.anim.out_to_left_animation);
			outToRight = AnimationUtils.loadAnimation(this, R.anim.out_to_right_animation);
			inFromLeft = AnimationUtils.loadAnimation(this, R.anim.in_from_left_animation);
			
			gestureDetector = new GestureDetector(this, new MGestureListener());
			onTouchListener = new View.OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
						if (gestureDetector.onTouchEvent(event)) {
							return true;
						} else {
							return false;
						}
				}
			};
			
			flipper/*findViewById(R.id.llRoot)*/.setOnTouchListener(onTouchListener);
			
			try{
				initPagesList();
			}
			catch (Exception e){
				appSettings.appendLog("help:onCreate  "+e.getMessage());
			}
			
			View pageView;
			TextView titleTextView;
			
			ImageView imageView;
			RadioButton radioBtn;
			
			int i = 0;
			boolean isfirst=true;
			for (Map<String, Object> map : pages) {
				pageView = getLayoutInflater().inflate(R.layout.layout_help_single, null);
				
				titleTextView = (TextView) pageView.findViewById(R.id.title);
				imageView = (ImageView) pageView.findViewById(R.id.image);
				
				String title = (String) map.get(KEY_TITLE);
				int imageResId = (Integer) map.get(KEY_IMAGE_RES_ID);
				
				titleTextView.setText(title);
				imageView.setImageResource(imageResId);
				
				flipper.addView(pageView);
				
				radioBtn = new RadioButton(this);
				radioBtn.setButtonDrawable(R.drawable.help_radio_btn);
				radioBtn.setBackgroundResource(0);
				radioBtn.setPadding(20, 10, 0, 10);
				isfirst=false;
				radioBtn.setOnCheckedChangeListener(new CheckedChangeListener(i));
				
				btnsGroup.addView(radioBtn);
				
				i++;
			}
			
				if (btnsGroup.getChildCount() > 0) {
					btnsGroup.getChildAt(0).performClick();
				}
			}
			catch (Exception e){
				appSettings.appendLog("help:onCreate  "+e.getMessage());
			}
		
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return gestureDetector.onTouchEvent(ev);
	}
	
	private void initPagesList() {
		pages = new ArrayList<Map<String,Object>>();
		
		contentMap = new HashMap<String, Object>();
		contentMap.put(KEY_TITLE, getString(R.string.h1_title));
		contentMap.put(KEY_IMAGE_RES_ID, R.drawable.hp1);
		pages.add(contentMap);
		
		contentMap = new HashMap<String, Object>();
		contentMap.put(KEY_TITLE, getString(R.string.h2_title));
		contentMap.put(KEY_IMAGE_RES_ID, R.drawable.hp2);
		pages.add(contentMap);
		
		contentMap = new HashMap<String, Object>();
		contentMap.put(KEY_TITLE, getString(R.string.h3_title));
		contentMap.put(KEY_IMAGE_RES_ID, R.drawable.hp3);
		pages.add(contentMap);
		
		contentMap = new HashMap<String, Object>();
		contentMap.put(KEY_TITLE, getString(R.string.h4_title));
		contentMap.put(KEY_IMAGE_RES_ID, R.drawable.hp4);
		pages.add(contentMap);
		
		contentMap = new HashMap<String, Object>();
		contentMap.put(KEY_TITLE, getString(R.string.h5_title));
		contentMap.put(KEY_IMAGE_RES_ID, R.drawable.hp5);
		pages.add(contentMap);
	}

	
	/**
	 * Shows the previous child of flipper 
	 */
	private void showPreviousSlideView() {
		if (flipper.getDisplayedChild() > 0) {
			flipper.setInAnimation(inFromLeft);
			flipper.setOutAnimation(outToRight);
			flipper.showPrevious();
		}
	}
	
	/**
	 * Shows the next child of flipper 
	 */
	private void showNextSlideView() {
		if (flipper.getDisplayedChild() < flipper.getChildCount() - 1) {
			flipper.setInAnimation(inFromRight);
			flipper.setOutAnimation(outToLeft);
			flipper.showNext();
		}
		else finish();
	}
	
	private class CheckedChangeListener implements OnCheckedChangeListener {

		private int position;
		
		public CheckedChangeListener(int position) {
			this.position = position;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
			try{
			if (isChecked) {
				flipper.setInAnimation(null);
				flipper.setOutAnimation(null);
				flipper.setDisplayedChild(position);
			}
			}
			catch (Exception e){
				appSettings.appendLog("help:onCheckedChanged  "+e.getMessage());
			}
		}
		
	}
	
	class MGestureListener extends SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

			final int SWIPE_MIN_DISTANCE = 50;
			final int SWIPE_MAX_OFF_PATH = 250;
			final int SWIPE_THRESHOLD_VELOCITY = 50; 

			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				
				if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showPreviousSlideView();
				}  else if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					showNextSlideView();
				}
				
            	int current = flipper.getDisplayedChild();
            	((RadioButton) btnsGroup.getChildAt(current)).setChecked(true);
				
			} catch (Exception e) {
				appSettings.appendLog("help:onFling  "+e.getMessage());
			}
            
            return false;
        }
    }
	
	public void onClose(View v) {
		this.finish();
	}
}
