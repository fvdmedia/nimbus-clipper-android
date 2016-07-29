package com.fvd.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;


import android.R.string;
import android.os.AsyncTask;
import android.text.format.DateFormat;
//import android.util.Log;

public class HttpGetTask extends AsyncTask<String, Void, String>
{
	private static DefaultHttpClient httpClient;
	private static BasicHttpContext localContext;
    private AsyncTaskCompleteListener<String, String> callback;
    private String action = "";
    private long uploaded=0;
    private  long totalSize=0;

    public HttpGetTask(AsyncTaskCompleteListener<String, String> callback)
    {
       this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params)
    {
       String result = "";
    	if (params[0].equalsIgnoreCase("GET"))
    	result = get(params[1]);
    	else 
    	if (params[0].equalsIgnoreCase("POST")){
    		action = params[1];
    		result = post(params[2], params[3],params[4],"");
    	}
    	else 
    	if (params[0].equalsIgnoreCase("UPLOAD")){
        		action = params[1];
        		result = post(params[2], params[3],params[4],params[5]);
    	}
    	if (params[0].equalsIgnoreCase("SEND")){
    		action = params[1];
    		result = SendFile(params[2], params[3],params[4]);
	}
    	return result;
    }

    @Override
    protected void onPostExecute(String result)
    {
       callback.onTaskComplete(result,action);
    }
    
    public DefaultHttpClient getHttpClient()
    {
        if (/*true || */httpClient == null)
        {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 90000);  // allow 5 seconds to create the server connection
            HttpConnectionParams.setSoTimeout(httpParameters, 90000);  // and another 5 seconds to retreive the data
            httpClient = new DefaultHttpClient(httpParameters);
        }
        else{
        	if (httpClient.getCookieStore()!=null) httpClient.getCookieStore().clear();
        }
        return httpClient;
    }

    public HttpContext getLocalContext()
    {
        if (localContext == null)
        {
            localContext = new BasicHttpContext();
        }
        return localContext;
    }
    
    private String get(String url){
    	StringBuilder builder = new StringBuilder();
        HttpGet request = new HttpGet(url);
        try
        {
            HttpResponse response = getHttpClient().execute(request, getLocalContext());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line;
                while ((line = reader.readLine()) != null)
                {
                    builder.append(line).append("\n");
                }
            }
            else
            {
            	builder.append("");
            }
            entity.consumeContent();
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
        return builder.toString();
    }
    
    private String post(String sessId, String url, String data, String boundary){
    	
    	String result="";
    	try {
		HttpPost httppost = new HttpPost(url/*+"&rand="+String.valueOf(System.currentTimeMillis())*/);
		httppost.addHeader("X-Requested-With","XMLHttpRequest");
		httppost.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		httppost.addHeader("Connection", "close");
		if (sessId.length()>0) httppost.addHeader("EverHelper-Session-ID",sessId);
		if(boundary.length()==0) {
			httppost.addHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
			httppost.setEntity(new ByteArrayEntity(data.getBytes("UTF-8")));
			
		}
		else {
			httppost.addHeader("Content-Type",String.format("multipart/form-data; boundary=%s", boundary));
			httppost.setEntity(new StringEntity(data));
		}
		
			HttpResponse response = getHttpClient().execute(httppost);
			InputStream content = response.getEntity().getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null){
				if(contentEncoding.getValue().equalsIgnoreCase("gzip")) {
					content = new GZIPInputStream(content);	
				}
				else if(contentEncoding.getValue().equalsIgnoreCase("deflate")) {
						content = new InflaterInputStream(content);	
				}
			}
			
			ByteArrayOutputStream bout =new ByteArrayOutputStream(512);
			try{
				int b;
		    	while ((b = content.read()) != -1) {
		    		bout.write(b);
		    	}
		    	content.close();
		    	bout.close();
		    	result =new String(bout.toByteArray());
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
	
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return result.toString();
		}
		return result;
    }
    
    String fileLocation = "";
    String getUploadLink()
    {
        return "http://sync.everhelper.me/files:preupload" + (uploaded != 0 ? String.format("?append=%s", fileLocation) : "");
    }
    
    FileInputStream fileInputStream=null;
    int chunkSize = 512000;
    String uploadResponse="";
    		
    public String SendFile(String sessId, String filepath, String filename) {
    String response = "error";
    String CRLF = "\r\n";
    String boundary = String.format("----------%d", System.currentTimeMillis());
    String query1 = "--" + boundary + CRLF + "Content-Disposition: form-data; name=\"File\"; filename=\"%s\"" + CRLF +
            "Content-Type: application/pdf" + CRLF + CRLF;
    String query2 = CRLF + "--" + boundary + "--" + CRLF;
    
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    
    try {
    	if(fileInputStream==null){
    		File f=new File(filepath);
    		fileInputStream = new FileInputStream(f);
    		totalSize=f.length();
    	}

        String connstr = String.format(query1, helper.extractFileName(filepath));

        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, chunkSize);
        buffer = new byte[bufferSize];

        // Read file
        while (uploaded<totalSize){
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        	bos.write(connstr.getBytes("UTF-8"));
        	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        	try {
        		bos.write(buffer, 0, bytesRead);
        		uploaded+=bytesRead;
        	} catch (OutOfMemoryError e) {
        		e.printStackTrace();
        		response = "outofmemoryerror";
        		return response;
        	}
        
        	bos.write(query2.getBytes("UTF-8"));
        	response=post(sessId, getUploadLink(), bos.toString("iso-8859-1"), boundary);
        	bos.close();
        	
        	if(fileLocation.length()==0) {
        		uploadResponse=response;
        		fileLocation=new JSONObject(response).getJSONObject("body").getJSONObject("files").getString("File");
        	}
        }
        
        fileInputStream.close();
    	response=uploadResponse;
        
    } catch (Exception ex) {
        // Exception handling
        response = "error";
        ex.printStackTrace();
    }
    return response;
}
    
    public String uploadAttachment(String noteGlobalId, String attachmentGlobalId, String format, String mimeType) {
        String tempName = null;
        try {
            /*String uploadUrl = Account.URL_UPLOAD;
            String filePath = getAttachLocalPath(attachmentGlobalId).substring(7);
            String serverKeyName = attachmentGlobalId + "." + format;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(uploadUrl);
            StringBody filename = new StringBody(serverKeyName);
            File f = new File(filePath);
            FileBody bin = new FileBody(f, mimeType);

            httppost.setHeader("EverHelper-Session-ID", Account.SESSION_ID);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file1", bin);
            reqEntity.addPart("filename", filename);

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            InputStream in = resEntity.getContent();
            InflaterInputStream inputStream = new InflaterInputStream(in);
            //stream length--start
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            int tempSize = 1024;
            byte[] tempBuff;
            tempBuff = new byte[tempSize];
            while ((len = inputStream.read(tempBuff, 0, tempSize)) != -1) {
                bos.write(tempBuff, 0, len);
            }
            tempBuff = bos.toByteArray();
            bos.close();
            //--finish
            //String json = new String(tempBuff, HTTP.UTF_8);
            //Log.d("MyLog", "jsonNotesGetResponse = " + json);
            inputStream.close();*/

            /*JsonElement jsonRootElement = new JsonParser().parse(json);
            JsonObject jsonRootObject = jsonRootElement.getAsJsonObject();
            JsonElement jsonBodyElement = jsonRootObject.get("body");
            JsonObject jsonBodyObject = jsonBodyElement.getAsJsonObject();
            JsonElement jsonFilesElement = jsonBodyObject.get("files");
            JsonObject jsonFilesObject = jsonFilesElement.getAsJsonObject();
            JsonElement jsonFileElement = jsonFilesObject.get("file1");
            jsonRootObject.add("file1", jsonFileElement);
            jsonRootObject.remove("body");

            UploadAttachmentResponse attachmentResponse = new Gson().fromJson(jsonRootObject, UploadAttachmentResponse.class);
            tempName = attachmentResponse.getFile1();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempName;
    }
}
