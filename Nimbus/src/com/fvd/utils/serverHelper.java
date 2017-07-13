package com.fvd.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.fvd.nimbus.R;

import android.R.string;
import android.app.ProgressDialog;
import android.content.Context;
//import android.util.Log;

public class serverHelper implements AsyncTaskCompleteListener<String, String>{
	private static serverHelper singleton;
	private ProgressDialog progressDialog;
	private Context ctx=null;
	private boolean onProcess=false;
	private String noteId="";
	private String shotTitle="";
	private int mode = 0;
	public static serverHelper getInstance(){
		if (singleton == null)
        {
			singleton = new serverHelper();
        }
        return singleton;
	}
	
	private serverHelper() {
		
	}
	
	public void completed() {
		onProcess=false;
	}
	
	public boolean canShare(){
		return noteId.length()>0;
	}
	
	public void setMode(int c){
		mode = c;
	}
	
	
	private AsyncTaskCompleteListener<String, String> callback = null;
	public void setCallback(AsyncTaskCompleteListener<String, String> callback, Context context) {
		this.callback = callback;
		this.ctx = context;
	}
	
	/*private String session="";
	public void setSessionId(String sid){
		this.session = sid;
	}
	
	public String getSession(){
		return this.session;
	}*/
	
	public void sendRequest(String action, String data, String boundary){
		
		if(!onProcess){
			new HttpGetTask(this).execute(new String[] {"POST",action, appSettings.sessionId,"https://sync.everhelper.me/",buildRequest(action,data),boundary});
			progressDialog = ProgressDialog.show(ctx, action.equals("notes:update")?ctx.getString(R.string.uploading_to_Nimbus):"Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
	}
	
	public void hideProgress() {
		if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog=null;
		}
	} 
	public void sendCallbackRequest(String action, String data,AsyncTaskCompleteListener<String, String> callback){
		
		if(!onProcess){
			
			new HttpGetTask(callback).execute(new String[] {"POST",action, appSettings.sessionId,"https://sync.everhelper.me/",buildRequest(action,data),""});
			//progressDialog = ProgressDialog.show(ctx, action.equals("notes:update")?ctx.getString(R.string.uploading_to_Nimbus):"Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			progressDialog=null;
			onProcess=true;
		}
	}
static final String pocket_key="57845-81d972abbed06f44c5c9d59d";	
public void getPocketRequest(String url, AsyncTaskCompleteListener<String, String> callback){
		
		if(!onProcess){
			String action = String.format("https://text.getpocket.com/v3/text?images=1&consumer_key=%s&url=%s", pocket_key,urlDataEncode(url));
			//progressDialog = ProgressDialog.show(ctx, "Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			new HttpGetTask(callback).execute(new String[] {"GET",action});
			//progressDialog = ProgressDialog.show(ctx, action.equals("notes:update")?ctx.getString(R.string.uploading_to_Nimbus):"Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			progressDialog=null;
			onProcess=true;
		}
}

public static String buildPocketQuery(String params) {
	return String.format("{\"consumer_key\":\"%s\", %s }", pocket_key,params);
}

public void postPocket(String url, String data, boolean json, AsyncTaskCompleteListener<String, String> callback) {
	if(!onProcess){
		new HttpGetTask(callback).execute(new String[] {"XPOST",url,data,json?"1":"0"});
		//progressDialog = ProgressDialog.show(ctx, action.equals("notes:update")?ctx.getString(R.string.uploading_to_Nimbus):"Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
		progressDialog=null;
		onProcess=true;
	}
}
	
public void sendCallbackRequestP(String action, String data,AsyncTaskCompleteListener<String, String> callback){
		
		if(!onProcess){
			progressDialog = ProgressDialog.show(ctx, action.equals("notes:update")?ctx.getString(R.string.uploading_to_Nimbus):"Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			new HttpGetTask(callback).execute(new String[] {"POST",action, appSettings.sessionId,"https://sync.everhelper.me/",buildRequest(action,data),""});
			//progressDialog=null;
			onProcess=true;
		}
	}
	
	public void sendOldRequest(String action, String data, String boundary){
		
		if(!onProcess){
			new HttpGetTask(this).execute(new String[] {"POST",action, appSettings.sessionId,"https://sync.everhelper.me/",data,boundary});
			progressDialog = ProgressDialog.show(ctx, "Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
	}
	
	public void sendQuietRequest(String action, String data, String boundary){
		if(!onProcess){
			new HttpGetTask(this).execute(new String[] {"POST",action, appSettings.sessionId,"https://sync.everhelper.me/",buildRequest(action,data),boundary});
			progressDialog =null;
			onProcess=true;
		}
	}
	
	public static String getRandomString(int len)
    {
        String str = (Long.toHexString(Double.doubleToLongBits(Math.random()))).substring(0, len);
        return str;
    } 
	
	public static String getDate(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date();
    	return (dateFormat.format(date));
    }
	
	public static String errorMsg(int code){
		switch(code){
		case -1:
		case -8:
		case -9: 
			return "format error";
		case -2: 
			return "param is missed";
		case -3: 
			return "unknown action";
		case -4:
			return "user alredy exists";
		case -5:
		case -10:
		case -11:
		case -12:
			return "server error";
		case -6: return "wrong username/password";
		case -7: return "user not exists";
		case -14: 
			return "access denied";
		case -16: 
			return "data too large";
		case -19: 
			return "not found";	
		case -20:
			return "You exhausted your month traffic limit. You can purchase Nimbus Pro to continue";
		case -21: 
			return "data alredy exists";	
			default:
				return String.format("unknown error (%d)", code);
		}
	}
	
	public static String urlEncode(String text){
		return quote(text);
	}
	
	 public static String quote(String string) {
         if (string == null || string.length() == 0) {
             return "";
         }

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 4);
         String       t;

        
         for (i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
             case '/':
                 sb.append('\\');
                 sb.append(c);
                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\r");
                break;
             default:
                 if (c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         return sb.toString();
     }
	
	public void uploadNote(String title, String note, String url, String pid, String tag){
		String bodyMask = "\"store\": {\"notes\": [{\"global_id\": \"%s\", \"parent_id\": \"%s\", \"index\": 1,\"type\": \"note\", \"title\": \"%s\",\"text\": \"%s\","+
                "\"url\": \"%s\", \"location_lat\": \"1234.123456\", \"location_lng\": \"1234.123456\", \"tags\": [%s], \"role\": \"clip\", \"_analyze_text\": {\"enabled\": true}}]}";
		noteId = getRandomString(16);
		String[] tags=tag.split(",");
		String ta="";
		for (String t : tags) {
			ta+=((ta.length()>0?",":"") +"\""+t.trim()+"\"");
		}
		sendRequest("notes:update",String.format(bodyMask, noteId, pid, urlEncode(title),urlEncode(note),urlEncode(url),ta),"");
	}
	
	public void MkFolder(String title, String note_id, String pid, AsyncTaskCompleteListener<String, String> cb){
		String bodyMask = "\"store\": {\"notes\": [{\"global_id\": \"%s\", \"parent_id\": \"%s\", \"index\": 1,\"type\": \"folder\", \"title\": \"%s\",\"text\": \"\","+
                "\"url\": \"\", \"location_lat\": \"\", \"location_lng\": \"\", \"tags\": []}]}";
		//progressDialog = ProgressDialog.show(ctx, "Nimbus Clipper", ctx.getString(R.string.please_wait), true, false);
		sendCallbackRequest("notes:update",String.format(bodyMask, note_id, pid, urlEncode(title)),cb);
		
	}
	
	public void shareNote(){
		if(!onProcess){
			String mask="\"global_id\": [\"%s\"]";
			String data = String.format(mask, noteId);
			new HttpGetTask(this).execute(new String[] {"POST","notes:share", appSettings.sessionId,"https://sync.everhelper.me/",buildRequest("notes:share",data),""});
			progressDialog = ProgressDialog.show(ctx, ctx.getString(R.string.uploading_to_Nimbus), ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
	}
	
	public void shareShot(String id){
		if(!onProcess){
			noteId = id;
			String mask="\"global_id\": [\"%s\"]";
			String data = String.format(mask, id);
			new HttpGetTask(this).execute(new String[] {"POST","notes:share", appSettings.sessionId,"https://sync.everhelper.me/",buildRequest("notes:share",data),""});
			progressDialog = ProgressDialog.show(ctx, ctx.getString(R.string.uploading_to_Nimbus), ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
	}
	
	public void uploadShot(String title, String parent, String tag, String buff){
		if (!onProcess){
			shotTitle = title;
			shot_parent=parent;
			shot_tag=tag;
			final String CRLF = "\r\n";
			final String FIELD_MASK = "--%s" + CRLF + "Content-Disposition: form-data; name=\"%s\"" + CRLF + CRLF+"%s"+CRLF;
			String boundary = getRandomString(12);
			String query ="--"+boundary+CRLF+"Content-Disposition: form-data; name=\"File\"; filename=\"shotfile." + (mode==0?"png":"jpeg") +"\""+CRLF+
					"Content-Type: image/"+(mode==0?"png":"jpeg")+CRLF+CRLF+
                	  	buff+CRLF+
                	  	"--" + boundary + "--" + CRLF;
			new HttpGetTask(this).execute(new String[] {"UPLOAD","upload", appSettings.sessionId,/*"http://everhelper.me/t.php"*/"http://sync.everhelper.me/files:preupload",query,boundary});
			progressDialog = ProgressDialog.show(ctx, ctx.getString(R.string.uploading_to_Nimbus), ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
		
	}
	public static String urlDataEncode(String s){
		try{
		return URLEncoder.encode(s,"UTF-8");
		}
		catch (Exception e) {
			return "";
		}
	}
	String pdf_title="";
	String pdf_parent="default";
	String pdf_tags="androidclipper";
	
	public void uploadPdfFile(String filename, String parent, String filepath){
		if (!onProcess){
			pdf_title = filename;
			pdf_parent=parent;
			
			final String CRLF = "\r\n";
			new HttpGetTask(this).execute(new String[] {"SEND","upload_pdf", appSettings.sessionId,filepath,urlDataEncode(filename)});
			appSettings.appendLog(String.format("1. send file: %s\r\n", filename));
			progressDialog = ProgressDialog.show(ctx, ctx.getString(R.string.uploading_to_Nimbus), ctx.getString(R.string.please_wait), true, false);
			onProcess=true;
		}
		
	}
	
	String pdf_noteid="";
	String shot_parent="default";
    String shot_tag="";
	String shot_noteid="";
	
	@Override
    public void onTaskComplete(String result, String action)
    {
		onProcess=false;
			try{
        		JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
           		if (action.equalsIgnoreCase("upload")){
           			if(error==0){
            			/*String fileId = root.getJSONObject("body").getJSONObject("files").getString("File");
            			String bodyMask = "\"screen\": {\"tempname\":\"%s\", \"type\": \"uploadcare\", \"title\": \"%s\",\"parent_id\": \"%s\", \"tags\": [\"androidclipper\"]}";
            			String data = String.format(bodyMask, fileId, urlEncode(shotTitle), shot_parent);
            			new HttpGetTask(this).execute(new String[] {"POST","screenshots:save", appSettings.sessionId,"https://sync.everhelper.me/",buildRequest("screenshots:save",data),""});
            			onProcess=true;*/
           				String fileId = root.getJSONObject("body").getJSONObject("files").getString("File");
            			//appSettings.appendLog(String.format("2. receive file id: %s\r\n", fileId));
            			String bodyMask = "\"store\": { \"notes\" : [{\"global_id\": \"%s\", \"parent_id\": \"%s\", \"date_updated_user\": \"%d\" , \"type\": \"note\", \"title\": \"%s\", \"text\": \"<img src=\'#attacheloc:%s#\'/>\", \"text_short\": \"\", \"display_name\": \"\", \"url\": \"\", \"location_lat\": \"0\", \"location_lng\": \"0\", \"index\": 0, \"attachements\": [{\"global_id\": \"%s\", \"type\": \"file\", \"tempname\": \"%s\",\"display_name\": \"%s\", \"in_list\": \"0\"}], \"todo\": [], \"tags\": [%s] }]}";
            			shot_noteid = getRandomString(16);
            			String img_id= getRandomString(16);
            			String[] tags=shot_tag.split(",");
            			String ta="";
            			for (String t : tags) {
            				ta+=((ta.length()>0?",":"") +"\""+t.trim()+"\"");
            			}
            			String data = String.format(bodyMask, shot_noteid, shot_parent, System.currentTimeMillis() / 1000L ,urlEncode(shotTitle),img_id, img_id, fileId,"",ta);
            			//appSettings.appendLog(String.format("3. send note: id=%s\r\n\r\n%s\r\n\r\n",pdf_noteid, data));
            			new HttpGetTask(this).execute(new String[] {"POST","screenshots:save", appSettings.sessionId,"https://sync.everhelper.me/",buildRequest("notes:update",data),""});
            			onProcess=true;
           			}
           			else {
           				if (progressDialog != null){
                            progressDialog.dismiss();
                            progressDialog=null;
               			}
           				
               			if (callback!=null) callback.onTaskComplete(result,action);
           			}
            	}
           		else if (action.equalsIgnoreCase("upload_pdf")){
           			if(error==0){
            			String fileId = root.getJSONObject("body").getJSONObject("files").getString("File");
            			//appSettings.appendLog(String.format("2. receive file id: %s\r\n", fileId));
            			String bodyMask = "\"store\": { \"notes\" : [{\"global_id\": \"%s\", \"parent_id\": \"%s\", \"date_updated_user\": \"%d\" , \"type\": \"note\", \"title\": \"%s\", \"text\": \"\", \"text_short\": \"\", \"display_name\": \"\", \"url\": \"\", \"location_lat\": \"0\", \"location_lng\": \"0\", \"index\": 0, \"attachements\": [{\"global_id\": \"%s\", \"type\": \"file\", \"tempname\": \"%s\",\"display_name\": \"%s\", \"in_list\": \"1\"}], \"todo\": [], \"tags\": [\"androidclipper\"] }]}";
            			pdf_noteid = getRandomString(16);
            			String data = String.format(bodyMask, pdf_noteid, pdf_parent, System.currentTimeMillis() / 1000L ,urlEncode(pdf_title), getRandomString(16), fileId,urlEncode(pdf_title));
            			//appSettings.appendLog(String.format("3. send note: id=%s\r\n\r\n%s\r\n\r\n",pdf_noteid, data));
            			new HttpGetTask(this).execute(new String[] {"POST","notes:update", appSettings.sessionId,"https://sync.everhelper.me/",buildRequest("notes:update",data),""});
            			onProcess=true;
           			}
           			else {
           				if (progressDialog != null){
                            progressDialog.dismiss();
                            progressDialog=null;
               			}
           				
               			if (callback!=null) callback.onTaskComplete(result,action);
           			}
           		}
           		else if("notes:update".equalsIgnoreCase(action)){
           			if (progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog=null;
           			}
           			appSettings.appendLog(String.format("4. receive ans: id=%s\r\n",pdf_noteid));
           			if (callback!=null) callback.onTaskComplete("{\"errorCode\":0,\"global_id\":\""+pdf_noteid+"\"}",action);
           		}
           		else if ("notes:share".equalsIgnoreCase(action)){
           			if (progressDialog != null){
                        progressDialog.dismiss();
                        progressDialog=null;
           			}
           			if(error==0){
           				String url=root.getJSONObject("body").getString(noteId);
           				if (callback!=null) callback.onTaskComplete("{\"errorCode\":0,\"url\":\""+url+"\"}",action);
           				
           			} else{
               			if (callback!=null) callback.onTaskComplete(result,action);
           			}
           		}
           		
           		else 
           			{	
           				if (progressDialog != null){
           					try{
           						if(progressDialog.isShowing()) progressDialog.dismiss();
               					progressDialog=null;
           					}catch (Exception x){
           						
           					}
           				}
           				if ("screenshots:save".equalsIgnoreCase(action)){
           					result=String.format("{\"errorCode\":0,\"body\":{\"global_id\":\"%s\"}}", shot_noteid);
           				}
           				if (callback!=null) callback.onTaskComplete(result,action);
           			}
                }
            catch (Exception Ex){
            	if (progressDialog != null){
                    progressDialog.dismiss();
                    progressDialog=null;
       			}
       			if (callback!=null) callback.onTaskComplete("",action);
            }
    }
	
	private String buildRequest(String action, String data){
    	return String.format("{\"action\":\"%s\",\"body\":{%s},\"_client_software\":\"clipper_android\"}",action, data);
    }
}
