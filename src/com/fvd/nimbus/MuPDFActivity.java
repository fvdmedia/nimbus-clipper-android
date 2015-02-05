package com.fvd.nimbus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fvd.paint.Shape;
import com.fvd.paint.Text;
import com.fvd.paint.DrawView.Mode;
import com.fvd.pdf.AsyncTask;
import com.fvd.pdf.FilePicker;
import com.fvd.pdf.FilePicker.FilePickerSupport;
import com.fvd.pdf.Hit;
import com.fvd.pdf.MuPDFAlert;
import com.fvd.pdf.MuPDFCore;
import com.fvd.pdf.MuPDFPageAdapter;
import com.fvd.pdf.MuPDFReaderView;
import com.fvd.pdf.MuPDFReflowAdapter;
import com.fvd.pdf.MuPDFView;
import com.fvd.pdf.OutlineActivityData;
import com.fvd.pdf.OutlineItem;
import com.fvd.pdf.ReaderView;
import com.fvd.pdf.SafeAnimatorInflater;
import com.fvd.pdf.SearchTask;
import com.fvd.pdf.SearchTaskResult;
import com.fvd.pdf.Annotation;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.helper;
import com.fvd.utils.serverHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;


class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity implements OnClickListener, FilePickerSupport,AsyncTaskCompleteListener<String, String>
{
	/* The core rendering instance */
	enum TopBarMode {Annot, Delete, Accept};
	enum AcceptMode {Highlight, Underline, StrikeOut, Ink, CopyText};

	private final int    OUTLINE_REQUEST=0;
	private final int    PRINT_REQUEST=1;
	private final int    FILEPICK_REQUEST=2;
	private MuPDFCore    core;
	private String       mFileName;
	private MuPDFReaderView mDocView;
	private View         mButtonsView;
	private boolean      mButtonsVisible=false;
	private EditText     mPasswordView;
	private LinearLayout topBar;
	private int          mPageSliderRes;
	private ImageButton  mOutlineButton;
	private ImageButton  mSaveNimbus;
	private ImageButton  mSaveSD;
	private ImageButton  mSharePdf;
	private TextView     mAnnotTypeText;
	private ViewAnimator mBottomBarSwitcher;
	private TopBarMode   mTopBarMode = TopBarMode.Annot;
	private AcceptMode   mAcceptMode;
	private AlertDialog.Builder mAlertBuilder;
	private boolean    mLinkHighlight = false;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive= false;
	private boolean mReflow = false;
	private AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;
	private FilePicker mFilePicker;
	private Context ctx;
	

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid stalling
		// the UI. Some calls can lead to javascript-invoked requests to display an
		// alert dialog and collect a reply from the user. The task has to be blocked
		// until the user's reply is received. This method creates an asynchronous task,
		// the purpose of which is to wait of these requests and produce the dialog
		// in response, while leaving the core blocked. When the dialog receives the
		// user's response, it is sent to the core via replyToAlert, unblocking it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void,Void,MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for(int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1: index=0; break;
							case AlertDialog.BUTTON2: index=1; break;
							case AlertDialog.BUTTON3: index=2; break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType)
				{
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType)
				{
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}
	
	String mFilePath="";
	private MuPDFCore openFile(String path)
	{
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1
					? path
					: path.substring(lastSlashPos+1));
		System.out.println("Trying to open "+path);
		try
		{
			core = new MuPDFCore(this, path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
			mFilePath=path;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[])
	{
		System.out.println("Trying to open byte buffer");
		try
		{
			core = new MuPDFCore(this, buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	
        }
		ctx = this;
		
		mButtonsView = getLayoutInflater().inflate(R.layout.buttons,null);
		mButtonsView.findViewById(R.id.pdf_DrawPensil).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdf_DrawRect).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdf_DrawArrow).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdf_DrawLine).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdf_DrawComment).setOnClickListener(this);
        
		mButtonsView.findViewById(R.id.pdfColor1).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdfColor2).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdfColor3).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdfColor4).setOnClickListener(this);
		mButtonsView.findViewById(R.id.pdfColor5).setOnClickListener(this);

        (( Button )mButtonsView.findViewById(R.id.pdf_ApplyText)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideTools();
				hideKeyboard();
				MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
				if (pageView != null){
					String text = ((EditText)mButtonsView.findViewById(R.id.pdf_EditorText)).getText().toString();
					if(text.length()>0){
						try{
							//String utf8=new String(text.getBytes("windows-1251"));
							pageView.AddText(txX, txY, text);
						}
						catch (Exception e){}
					}
				}
			}
		});
		serverHelper.getInstance().setCallback(this, this);
		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore)getLastNonConfigurationInstance();

			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file manager
					// Hopefully other file managers will use this same scheme, if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String reason = null;
						if (str == null) {
							try {
								InputStream is = getContentResolver().openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							}
							catch (java.lang.OutOfMemoryError e)
							{
								System.out.println("Out of memory during buffer reading");
								reason = e.toString();
							}
							catch (Exception e) {
								reason = e.toString();
							}
							if (reason != null)
							{
								buffer = null;
								Resources res = getResources();
								AlertDialog alert = mAlertBuilder.create();
								setTitle(String.format(res.getString(R.string.cannot_open_document_Reason), reason));
								alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										});
								alert.show();
								return;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				requestPassword(savedInstanceState);
				return;
			}
			if (core != null && core.countPages() == 0)
			{
				core = null;
			}
		}
		if (core == null)
		{
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}
		

		createUI(savedInstanceState);
	}

	public void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(this);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (core.authenticatePassword(mPasswordView.getText().toString())) {
					createUI(savedInstanceState);
				} else {
					requestPassword(savedInstanceState);
				}
			}
		});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.show();
	}

	float txX=0;
	float txY=0;
	boolean waitForTouch=false;
	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				super.onMoveToChild(i);
			}
			
			@Override
			protected void onTouch(float x, float y) {
				txX=x;
				txY=y;
				if(waitForTouch){
					showTextPopup(this);
				}
				super.onTouch(x, y);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else if(mButtonsView.findViewById(R.id.pdf_text_field).getVisibility()!=View.VISIBLE){
					if (mTopBarMode == TopBarMode.Annot)
						hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				if(mButtonsView.findViewById(R.id.pdf_text_field).getVisibility()!=View.VISIBLE)
					hideButtons();
			}

			@Override
			protected void onCommentHit(String comment) {
				AlertDialog.Builder builder = new AlertDialog.Builder(MuPDFActivity.this);
				builder.setTitle("Comment")
						.setMessage(comment)
						.setCancelable(false)
						.setNegativeButton("ÎÊ",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
			@Override
			protected void onHit(Hit item) {
				if(waitForTouch){
					waitForTouch=false;
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					mTopBarMode=TopBarMode.Annot;
					mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					return;
				}
				switch (mTopBarMode) {
				case Annot:
					if (item == Hit.Annotation) {
						showButtons();
						mTopBarMode = TopBarMode.Delete;
						mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					}
					break;
				case Delete:
					mTopBarMode = TopBarMode.Annot;
					mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
				// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages()-1,1);
		mPageSliderRes = ((10 + smax - 1)/smax) * 2;
		
		
		mSaveSD.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				if(core.hasChanges()) {
					new SaveTask(0).execute();
			    }
			}
		});
		
		mSharePdf.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(true || core.hasChanges()) {
					new SaveTask(1).execute();
				}
			}
		});
		
		mSaveNimbus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (appSettings.sessionId.length() == 0) {
					showSettings();
					if(core.hasChanges()) core.save();
				}
				else {
					if(core.hasChanges()) new SaveTask(2).execute();
				else {
        					serverHelper.getInstance().sendRequest("notes:getFolders", "","");
					}
				}
			}
		});

		if (core.hasOutline()) {
			mOutlineButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					OutlineItem outline[] = core.getOutline();
					if (outline != null) {
						OutlineActivityData.get().items = outline;
						Intent intent = new Intent(MuPDFActivity.this, OutlineActivity.class);
						startActivityForResult(intent, OUTLINE_REQUEST);
						overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
					}
				}
			});
		} else {
			mOutlineButton.setVisibility(View.GONE);
		}

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page"+mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if(savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		if(savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false))
			reflowModeSet(true);

		// Stick the document view and the buttons overlay into a parent view
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		
		setContentView(layout);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUTLINE_REQUEST:
			if (resultCode >= 0)
				mDocView.setDisplayedViewIndex(resultCode);
			break;
		case PRINT_REQUEST:
			if (resultCode == RESULT_CANCELED)
				showInfo(getString(R.string.print_failed));
			break;
		case FILEPICK_REQUEST:
			if (mFilePicker != null && resultCode == RESULT_OK)
				mFilePicker.onPick(data.getData());
			break;
		case 4:
			if (resultCode==RESULT_OK){
    			String id=data.getStringExtra("id").toString();
    			serverHelper.getInstance().shareShot(id);
    		}
			break;
		case 5:
	    		if (resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER){
	    			userMail=data.getStringExtra("userMail");
	    			userPass=data.getStringExtra("userPass");
	    			if(resultCode==RESULT_OK)	serverHelper.getInstance().sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass),"");
	    			else serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",userMail,userPass), "");
	    		}
			break;
		case 7:
			if (resultCode==RESULT_OK && data!=null){
	    		
				String parent = data.getStringExtra("id");
				serverHelper.getInstance().uploadPdfFile(helper.extractFileName(mFilePath), parent, mFilePath);
	    		}
			break;
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	String userMail="";
	String userPass="";
	public Object onRetainNonConfigurationInstance()
	{
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow)
	{
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
		if (reflow) setLinkHighlight(false);
		mDocView.refresh(mReflow);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		if (mReflow)
			outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}

	public void onDestroy()
	{
		if (mDocView != null) {
			mDocView.applyToChildren(new ReaderView.ViewMapper() {
				public void applyToView(View view) {
					((MuPDFView)view).releaseBitmaps();
				}
			});
		}
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		super.onDestroy();
	}

	private void setLinkHighlight(boolean highlight) {
		mLinkHighlight = highlight;
		mDocView.setLinksEnabled(highlight);
	}

	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
		
			Animation anim = new TranslateAnimation(0, 0, -topBar.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					topBar.setVisibility(View.VISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {}
			});
			topBar.startAnimation(anim);
			
			if(core.isUnencryptedPDF() && !core.wasOpenedFromBuffer()){
				anim = new TranslateAnimation(0, 0, mBottomBarSwitcher.getHeight(), 0);
				anim.setDuration(200);
				anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {
						mBottomBarSwitcher.setVisibility(View.VISIBLE);
					}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {}
				});
				mBottomBarSwitcher.startAnimation(anim);
			}
			else Toast.makeText(ctx, getString(R.string.pdf_file_is_protected), Toast.LENGTH_LONG).show();
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();
			hideTools();	
			

			Animation anim = new TranslateAnimation(0, 0, 0, -topBar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					topBar.setVisibility(View.INVISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {}
			});
			topBar.startAnimation(anim);
			
			if(core.isUnencryptedPDF() && !core.wasOpenedFromBuffer()){
				anim = new TranslateAnimation(0, 0, 0, mBottomBarSwitcher.getHeight());
				anim.setDuration(200);
				anim.setAnimationListener(new Animation.AnimationListener() {
					public void onAnimationStart(Animation animation) {}
					public void onAnimationRepeat(Animation animation) {}
					public void onAnimationEnd(Animation animation) {
						mBottomBarSwitcher.setVisibility(View.INVISIBLE);
					}
				});
				mBottomBarSwitcher.startAnimation(anim);
			}
			
			mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		}
	}

	private void searchModeOn() {

	}

	private void searchModeOff() {
		
	}

	private void updatePageNumView(int index) {
		if (core == null)
			return;
	}

	private void printDoc() {
		if (!core.fileFormat().startsWith("PDF")) {
			showInfo(getString(R.string.format_currently_not_supported));
			return;
		}

		Intent myIntent = getIntent();
		Uri docUri = myIntent != null ? myIntent.getData() : null;

		if (docUri == null) {
			showInfo(getString(R.string.print_failed));
		}

		if (docUri.getScheme() == null)
			docUri = Uri.parse("file://"+docUri.toString());

		Intent printIntent = new Intent(this, PrintDialogActivity.class);
		printIntent.setDataAndType(docUri, "aplication/pdf");
		printIntent.putExtra("title", mFileName);
		startActivityForResult(printIntent, PRINT_REQUEST);
		overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	}

	private void showInfo(String message) {
	}

	private void makeButtonsView() {
		
		mOutlineButton = (ImageButton)mButtonsView.findViewById(R.id.outlineButton);
		mSaveNimbus = (ImageButton)mButtonsView.findViewById(R.id.pdfSave2Nimbus);
		mSaveSD = (ImageButton)mButtonsView.findViewById(R.id.pdfSave2SD);
		mSharePdf = (ImageButton)mButtonsView.findViewById(R.id.pdfShare);
		mAnnotTypeText = (TextView)mButtonsView.findViewById(R.id.annotType);
		mBottomBarSwitcher = (ViewAnimator)mButtonsView.findViewById(R.id.switcher);
		topBar = (LinearLayout)mButtonsView.findViewById(R.id.topbar);
		mBottomBarSwitcher.setVisibility(View.INVISIBLE);
	}

	public void OnMoreButtonClick(View v) {

	}

	public void OnCancelMoreButtonClick(View v) {
		mTopBarMode = TopBarMode.Annot;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
	}

	public void OnPrintButtonClick(View v) {
		printDoc();
	}

	public void OnCopyTextButtonClick(View v) {

	}

	public void OnEditAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.Annot;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.Annot;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
	}

	public void OnHighlightButtonClick(View v) {
		hideTools();
		mTopBarMode = TopBarMode.Accept;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Highlight;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.highlight);
		showInfo(getString(R.string.select_text));
	}

	public void OnUnderlineButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Underline;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.underline);
		showInfo(getString(R.string.select_text));
	}

	public void OnStrikeOutButtonClick(View v) {
		hideTools();
		mTopBarMode = TopBarMode.Accept;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.StrikeOut;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.strike_out);
		showInfo(getString(R.string.select_text));
	}

	public void OnInkButtonClick(View v) {
		showToolsPopup(v);
	}
	
	public void OnColorButtonClick(View v) {
		showColorPopup(v);
	}

	public void OnCancelAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectText();
			pageView.cancelDraw();
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		switch (mAcceptMode) {
		default:
			mTopBarMode = TopBarMode.Annot;
			break;
		}
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		boolean success = false;
		switch (mAcceptMode) {
		case Highlight:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case Underline:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.UNDERLINE);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case StrikeOut:
			if (pageView != null)
				success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.no_text_selected));
			break;

		case Ink:
			if (pageView != null)
				success = pageView.saveDraw();
			mTopBarMode = TopBarMode.Annot;
			if (!success)
				showInfo(getString(R.string.nothing_to_save));
			break;
		default:	
			break;
		}
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
	}

	public void OnCancelSearchButtonClick(View v) {
		searchModeOff();
	}

	public void OnDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null)
			pageView.deleteSelectedAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null)
			pageView.deselectAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(findViewById(R.id.pdf_EditorText), 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(findViewById(R.id.pdf_EditorText).getWindowToken(), 0);
	}

	private void search(int direction) {
		hideKeyboard();
	}

	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		if (core != null)
		{
			core.startAlerts();
			createAlertWaiter();
		}
		overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (core != null)
		{
			destroyAlertWaiter();
			core.stopAlerts();
		}

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if(findViewById(R.id.pdf_text_field).getVisibility()!=View.GONE)
		{
			findViewById(R.id.pdf_text_field).setVisibility(View.GONE);
			return;
		}
		else if(findViewById(R.id.pdf_color_menu).getVisibility()!=View.GONE)
		{
			findViewById(R.id.pdf_color_menu).setVisibility(View.GONE);
			return;
		}
		else if(findViewById(R.id.pdf_draw_tools).getVisibility()!=View.GONE)
		{
			findViewById(R.id.pdf_draw_tools).setVisibility(View.GONE);
			return;
		}
		if (core.hasChanges()) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == AlertDialog.BUTTON_POSITIVE)
						core.save();

					finish();
				}
			};
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle("Nimbus Clipper");
			alert.setMessage(getString(R.string.document_has_changes_save_them_));
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
			alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
			alert.show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void performPickFor(FilePicker picker) {
		mFilePicker = picker;
		Intent intent = new Intent(this, ChoosePDFActivity.class);
		intent.setAction(ChoosePDFActivity.PICK_KEY_FILE);
		startActivityForResult(intent, FILEPICK_REQUEST);
		overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	}
	
	private class SaveTask extends AsyncTask<Void, Void, Void>{
		private ProgressDialog pd;
		int mode=0;
		public SaveTask(int m){ this.mode=m;}
		
		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		   
		   pd = new ProgressDialog(ctx);
		   pd.setMessage(getString(R.string.please_wait));
		   pd.setCancelable(false);
		   pd.show();    
		}

		@Override
		protected Void doInBackground(Void... params) {
			core.save();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		   super.onPostExecute(result);
		   pd.dismiss();
		   switch (mode) {
		case 0:
			Toast.makeText(ctx, getString(R.string.saved), Toast.LENGTH_LONG).show();
			break;
		case 1:
			Intent share = new Intent(Intent.ACTION_SEND);
			share.setType("application/pdf");
			share.putExtra(Intent.EXTRA_STREAM, Uri.parse(mFilePath));
			startActivity(Intent.createChooser(share, "Share PDF Document"));
			break;
		case 2:
			if(serverHelper.getInstance().getSession().length()>0) {
				serverHelper.getInstance().sendRequest("notes:getFolders", "","");
			} else showSettings();
			break;

		default:
			break;
		}
		   
		}
		
	}

	@SuppressLint("NewApi")
	@Override
	public void onTaskComplete(String result, String action) {
		// TODO Auto-generated method stub
		if(result.isEmpty()){
			Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_LONG).show();
			return;
		}
    	try{
        	JSONObject root = new JSONObject(result);
        	int error = root.getInt("errorCode");
        	if (error==0){
        		if (action.equalsIgnoreCase("user:auth")){
        			appSettings.sessionId = root.getJSONObject("body").getString("sessionid");
        			serverHelper.getInstance().setSessionId(appSettings.sessionId);
        			appSettings.storeUserData(ctx, userMail, userPass, appSettings.sessionId); 
        			Toast.makeText(getApplicationContext(), "user authorized", Toast.LENGTH_LONG).show();
        			serverHelper.getInstance().sendRequest("notes:getFolders", "","");
        		}
        		else if(action.equalsIgnoreCase("notes:getfolders")){
        			
        			ArrayList<String>items=new ArrayList<String>();
        			try{
        				result = URLDecoder.decode(result,"UTF-16"); 
       	        	        String id="";
        	        		String title="";
        	        		JSONArray arr =  root.getJSONObject("body").getJSONArray("notes");
        	        		for(int i=0; i<arr.length();i++){
        	        			JSONObject obj = new JSONObject(arr.getString(i));
        	        			title= obj.getString("title");
        	        			id=obj.getString("global_id");
        	        			items.add(title+"::"+id);
        	        		}
        	        		Intent intent = new Intent(getApplicationContext(), tagsActivity.class);
        	        		intent.putExtra("items", items);
        	        		intent.putExtra("current", appSettings.prefsReadString(this,"remFolderId", "default"));
        	        		intent.putExtra("hideTags", true);
        	    	    	startActivityForResult(intent,7);
        	    	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        	        		
        	        }
        	        catch (Exception Ex){
        	        	appSettings.appendLog("prefs:onTaskComplete "+Ex.getMessage());
        	        }
        		}
        		else if("notes:update".equals(action)){
        			
        			showShotSuccess(root.getString("global_id"));
        		}
        		else if("notes:share".equalsIgnoreCase(action)){
        			String url=root.getString("url");
        			showShareDlg(url);
        		}
        		else if("user_register".equals(action)){
        			serverHelper.getInstance().sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",appSettings.userMail,appSettings.userPass),"");
        		}
        	}
        	else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
        }
        catch (Exception Ex){
        }
	}
	
	private void showShotSuccess(String id)
    {
    	Intent i = new Intent(getApplicationContext(), ShotSuccess.class);
    	i.putExtra("id", id);
    	i.putExtra("isPDF", true);
    	startActivityForResult(i, 4);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
	
	private void showShareDlg(String url)
    {
    	Intent i = new Intent(getApplicationContext(), ArticleDlg.class);
    	i.putExtra("url", url);
    	startActivity(i);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
	
	private void showSettings()
    {
    	Intent i = new Intent(getApplicationContext(), LoginDlg.class);
    	i.putExtra("userMail", appSettings.userMail==null?"":appSettings.userMail);
    	startActivityForResult(i, 5);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
	
	public void showToolsPopup(View view) {
		if(mButtonsView.findViewById(R.id.pdf_draw_tools).getVisibility()!=View.VISIBLE){
			hideTools();
			mButtonsView.findViewById(R.id.pdf_draw_tools).setVisibility(View.VISIBLE);
		} else hideTools();

    }
	
	void hideTools(){
	if (mButtonsView.findViewById(R.id.pdf_color_menu).getVisibility()!=View.GONE)
		mButtonsView.findViewById(R.id.pdf_color_menu).setVisibility(View.GONE);
	if (mButtonsView.findViewById(R.id.pdf_text_field).getVisibility()!=View.GONE)
		mButtonsView.findViewById(R.id.pdf_text_field).setVisibility(View.GONE);
	if (mButtonsView.findViewById(R.id.pdf_draw_tools).getVisibility()!=View.GONE)
		mButtonsView.findViewById(R.id.pdf_draw_tools).setVisibility(View.GONE);
	}
	
	public void showColorPopup(View view) {
		if(mButtonsView.findViewById(R.id.pdf_color_menu).getVisibility()!=View.VISIBLE){
			hideTools();
			mButtonsView.findViewById(R.id.pdf_color_menu).setVisibility(View.VISIBLE);
		} else hideTools();
    }
	
	private void setSelectedFoot(int id){
    }

	void setDrawMode(){
		mTopBarMode = TopBarMode.Accept;
		mBottomBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Ink;
		mDocView.setMode(MuPDFReaderView.Mode.Drawing);
		findViewById(R.id.inkButton).setSelected(false);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pdf_DrawPensil:
    		setDrawMode();
    		mDocView.setShape(0);
    		hideTools();
    		mAnnotTypeText.setText(R.string.ink);
    		break;
    	case R.id.pdf_DrawRect:
    		setDrawMode();
    		mDocView.setShape(1);
    		setSelectedFoot(3);
    		hideTools();
    		mAnnotTypeText.setText(R.string.rect);
    		break;
    	case R.id.pdf_DrawArrow:
    		setDrawMode();
    		mDocView.setShape(2);
    		setSelectedFoot(3);
    		hideTools();
    		mAnnotTypeText.setText(R.string.arrow);
    		break;
    	case R.id.pdf_DrawLine:
    		setDrawMode();
    		mDocView.setShape(3);
    		hideTools();
    		mAnnotTypeText.setText(R.string.line);
    		break;
    	case R.id.pdfColor1:
    		mDocView.setColor(0);
    		((ImageButton)findViewById(R.id.colorButton)).setImageResource(R.drawable.icon_color_blue);
    		hideTools();
    		break;
    	case R.id.pdfColor2:
    		mDocView.setColor(1);
    		((ImageButton)findViewById(R.id.colorButton)).setImageResource(R.drawable.icon_color_red);
    		hideTools();
    		break;
    	case R.id.pdfColor3:
    		mDocView.setColor(2);
    		((ImageButton)findViewById(R.id.colorButton)).setImageResource(R.drawable.icon_color_yellow);
    		hideTools();
    		break;
    	case R.id.pdfColor4:
    		mDocView.setColor(3);
    		((ImageButton)findViewById(R.id.colorButton)).setImageResource(R.drawable.icon_color_white);
    		hideTools();
    		break;
    	case R.id.pdfColor5:
    		mDocView.setColor(4);
    		((ImageButton)findViewById(R.id.colorButton)).setImageResource(R.drawable.icon_color_black);
    		hideTools();
    		break;
    		case R.id.pdf_DrawComment:
    			waitForTouch=true;
    			hideTools();
    			Toast.makeText(ctx, getString(R.string.please_tap), Toast.LENGTH_LONG).show();
    		break;
		}
	}
	
	public void showTextPopup(View view) {
        if(mButtonsView.findViewById(R.id.pdf_text_field).getVisibility()!=View.VISIBLE){
			hideTools();
			((EditText)findViewById(R.id.pdf_EditorText)).setText("");
			mButtonsView.findViewById(R.id.pdf_text_field).setVisibility(View.VISIBLE);
			mButtonsView.findViewById(R.id.pdf_EditorText).setFocusable(true);
			mButtonsView.findViewById(R.id.pdf_EditorText).requestFocus();
			showKeyboard();
        }
    }
}
