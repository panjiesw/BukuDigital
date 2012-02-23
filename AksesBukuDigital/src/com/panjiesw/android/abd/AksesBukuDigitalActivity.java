package com.panjiesw.android.abd;

import android.app.Activity;
import android.os.Bundle;

public class AksesBukuDigitalActivity extends Activity {
	
	private static final String[] titles = {"Plain Tales from the Hills","Washington Square"};
	private static final String[] authors = {"Rudyard Kipling","Henry James"};
	private static final String[] path = {"books/","books/"};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}