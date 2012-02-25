package com.panjiesw.android.abd.views;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.panjiesw.android.abd.R;

public class EpubReaderView extends Activity {
	public static final String OPEN_EPUB = "com.panjiesw.android.abd.action.OPEN_EPUB";
	WebView epubView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.epubreader);
		epubView = (WebView)findViewById(R.id.epubWebView);
		epubView.getSettings().setJavaScriptEnabled(true);
		epubView.getSettings().setAllowFileAccess(true);
		epubView.getSettings().setAllowContentAccess(true);
		final String[] js = getIntent().getStringArrayExtra("jsonData");
		epubView.setWebViewClient(new WebViewClient(){
			@Override  
			  public void onPageFinished(WebView view, String url) {
				view.loadUrl("javascript:callFromActivity('"+js[0]+"','"+js[1]+"')");
				Log.d("anuanuanuanu", "javascript:callFromActivity('"+js[0]+"','"+js[1]+"')");
			  }
		});
		epubView.loadUrl("file:///android_asset/index.html");
	}
	
	@Override
	protected void onDestroy() {
		File f = new File(getDir("epubtemp", MODE_WORLD_READABLE).getPath()+"/component");
		deleteRecursive(f);
		super.onDestroy();
	}
	
	private void deleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            deleteRecursive(child);

	    fileOrDirectory.delete();
	}
}
