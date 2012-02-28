package com.panjiesw.android.abd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.panjiesw.android.abd.tools.Misc;
import com.panjiesw.android.abd.tools.MonocleScriptInjector;
import com.panjiesw.android.abd.views.EpubReaderView;

public class AksesBukuDigitalActivity extends ListActivity {
	
	private static final String[] titles = {"Plain Tales from the Hills","Washington Square","Princess Silver Tears and One Feather","Lake Loves Dolphins"};
	private static final String[] authors = {"Rudyard Kipling","Henry James","Marie Rose","Lake Gifford"};
	private static final String[] path = {"books/kipling-plain-tales-from-the-hills.epub","books/james-washington-square.epub","books/Princess_Silver_Tears_and_One_Feather.epub","books/Lake_Loves_Dolphins.epub"};
	
	private LayoutInflater mInflater;
	private List<RowData> data;
	RowData rd;
	
	private ProgressDialog mProgressDialog;
	public static final int DIALOG_EBOOK_OPEN_PROGRESS = 0;
	
	public static final String ABD_PREF = "BukuDigitalPreferences";
	boolean isMonocleFolder;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        data = new ArrayList<RowData>();
        
        for (int i = 0; i < titles.length; i++) {
			try {
				rd = new RowData(i, titles[i], authors[i]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			data.add(rd);
		}
        
        EbookListAdapter adapter = new EbookListAdapter(this, R.layout.row, R.id.row_title, data);
        setListAdapter(adapter);
        
        SharedPreferences settings = getSharedPreferences(ABD_PREF, 0);
        isMonocleFolder = settings.getBoolean("monocleFolder", false);
        if (!isMonocleFolder) {
        	Log.w(Misc.TAG_W, "No Folder for epubtemp, attempt to creat it");
        	new CreateFolderFilesAsync().execute("anu");
		}else {
			Log.i(Misc.TAG_I, "The epubtemp Folder is presents");
		}
    }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EBOOK_OPEN_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Loading eBook file...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgress(0);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	new EbookLoadAsync().execute(position);
    }
    
	private class RowData {
		protected int mId;
		protected String mTitle;
		protected String mAuthor;

		RowData(int id, String title, String detail) {
			mId = id;
			mTitle = title;
			mAuthor = detail;
		}

		@Override
		public String toString() {
			return mId + " " + mTitle + " " + mAuthor;
		}
	}
	
	private class EbookListAdapter extends ArrayAdapter<RowData> {

		public EbookListAdapter(Context context, int resource,
				int textViewResourceId, List<RowData> objects) {

			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			TextView title = null;
			TextView author = null;
			ImageView i11 = null;
			RowData rowData = getItem(position);
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.row, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			title = holder.gettitle();
			title.setText(rowData.mTitle);
			author = holder.getAuthor();
			author.setText(rowData.mAuthor);

			i11 = holder.getImage();
			i11.setImageResource(R.drawable.ic_epub);
			return convertView;
		}

		private class ViewHolder {
			private View mRow;
			private TextView title = null;
			private TextView author = null;
			private ImageView i11 = null;

			public ViewHolder(View row) {
				mRow = row;
			}

			public TextView gettitle() {
				if (null == title) {
					title = (TextView) mRow.findViewById(R.id.row_title);
				}
				return title;
			}

			public TextView getAuthor() {
				if (null == author) {
					author = (TextView) mRow.findViewById(R.id.row_author);
				}
				return author;
			}

			public ImageView getImage() {
				if (null == i11) {
					i11 = (ImageView) mRow.findViewById(R.id.row_ic);
				}
				return i11;
			}
		}
	}
	
	private class EbookLoadAsync extends AsyncTask<Integer, Integer, String[]> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_EBOOK_OPEN_PROGRESS);
		}
		
		@Override
		protected String[] doInBackground(Integer... params) {
			int pos = params[0];
			String[] toScriptMetadata = new String[2];
			String[] js = new String[2];
			try {
				InputStream epubInputStream = getAssets().open(path[pos]);
				Book book = (new EpubReader()).readEpub(epubInputStream);
				
				List<Resource> allPage = book.getContents();
				MediaType[] mTypeCSS = {MediatypeService.CSS};
				List<Resource> allCSS = book.getResources().getResourcesByMediaTypes(mTypeCSS);
				MediaType[] mType = {MediatypeService.PNG,MediatypeService.GIF,MediatypeService.JPG};
				List<Resource> allRes = book.getResources().getResourcesByMediaTypes(mType);
				
				int tProg = allPage.size()+allCSS.size()+allRes.size();
				int mP = 0;
				String[] toScriptPath = new String[allPage.size()];
				String[] toScriptChapters = new String[allPage.size()];
				
				for (int i = 0; i < allPage.size(); i++) {
					writeFile(allPage.get(i).getData(), "/component/"+allPage.get(i).getHref());
					toScriptPath[i] = allPage.get(i).getHref();
					if (i>=book.getTableOfContents().getTocReferences().size()) {
						toScriptChapters[i] = "title unknown";
					} else {
						toScriptChapters[i] = book.getTableOfContents().getTocReferences().get(i).getTitle();
					}
					mP++;
					publishProgress(mP*100/tProg);
				}
				
				for (int i = 0; i < allCSS.size(); i++) {
					writeFile(allPage.get(i).getData(), "/component/"+allCSS.get(i).getHref());
					mP++;
					publishProgress(mP*100/tProg);
				}
				
				for (int i = 0; i < allRes.size(); i++) {
					Log.i(Misc.TAG_I, allRes.get(i).getHref());
					writeFile(allRes.get(i).getData(), "/component/"+allRes.get(i).getHref());
					mP++;
					publishProgress(mP*100/tProg);
				}
				
				toScriptMetadata[0] = book.getMetadata().getAuthors().toString();
				toScriptMetadata[1] = book.getMetadata().getFirstTitle();
				js = MonocleScriptInjector.getJavascript(toScriptPath,toScriptChapters,toScriptMetadata);
				
			} catch (IOException e) {
				Log.e(Misc.TAG_E, e.getMessage());
			}
			return js;
		}
		
//		private void logTableOfContents(List<TOCReference> tocReferences, int depth) {
//			if (tocReferences == null) {
//				return;
//			}
//			for (TOCReference tocReference : tocReferences) {
//				StringBuilder tocString = new StringBuilder();
//				for (int i = 0; i < depth; i++) {
//					tocString.append("\t");
//				}
//				tocString.append(tocReference.getTitle());
//				Log.i("epublib", tocString.toString());
//
//				logTableOfContents(tocReference.getChildren(), depth + 1);
//			}
//		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d(Misc.TAG_D, String.valueOf(values[0]));
			mProgressDialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			dismissDialog(DIALOG_EBOOK_OPEN_PROGRESS);
			Intent intent = new Intent(AksesBukuDigitalActivity.this, EpubReaderView.class);
			intent.putExtra("jsonData", result);
			startActivity(intent);
		}
	}
	
	private class CreateFolderFilesAsync extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... path) {
			File dir = getDir("epubtemp", MODE_WORLD_READABLE);
			File compoDir = new File(dir.getPath()+"/component");
			compoDir.mkdir();
			return "Making Temp Folder is Done";
		}
		
		@Override
		protected void onPostExecute(String result) {
			isMonocleFolder = true;
			Log.v(Misc.TAG_V, result);
		}
	}
	
//	private byte[] readFile(InputStream is) throws IOException {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		byte[] b = new byte[1024];
//		int bytesRead;
//		try {
//			while ((bytesRead = is.read(b)) != -1) {
//				bos.write(b, 0, bytesRead);
//			}
//			return bos.toByteArray();
//		} finally {
//			is.close();
//		}
//	}
	
	private boolean writeFile(byte[] data, String path) throws IOException,FileNotFoundException {
		if (path.indexOf("/") != -1) {
			String dir = path.substring(0, path.lastIndexOf("/"));
			File f = new File(getDir("epubtemp", MODE_WORLD_READABLE).getPath()+dir);
			if(!f.exists())
				f.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(getDir("epubtemp", MODE_WORLD_READABLE).getPath()+path);
		try {
			fos.write(data);
			return true;
		} finally {
			fos.close();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SharedPreferences settings = getSharedPreferences(ABD_PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("monocleFolder", isMonocleFolder);
		editor.commit();
	}
}