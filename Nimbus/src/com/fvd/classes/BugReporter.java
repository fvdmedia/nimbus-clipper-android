package com.fvd.classes;

import java.net.URLEncoder;

import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.HttpGetTask;
import com.fvd.utils.appSettings;

public class BugReporter {
	private static final String mail="wmpostbox@gmail.com";
    private static final String url = "http://www.flashvideodownloader.org/fvd-suite/contact2/report.php";
    private static String _body;
    private static String _subj;
	public static void Send(String subj ,String report) {
		_subj="Android Clipper."+subj;
		_body=report;
		String parametersString = "email=" +URLEncoder.encode(mail) + "&text=" + URLEncoder.encode(report) + "&type=Report" + "&subj=" + URLEncoder.encode(_subj);
		new HttpGetTask(new AsyncTaskCompleteListener<String, String>() {
			
			@Override
			public void onTaskComplete(String result, String adv) {
				// TODO Auto-generated method stub
				if("".equals(result)){
					
				}
			}
		}).execute(new String[] {"POST","report", "",url,parametersString,""});//("", url, parametersString, "");
	}
}
