package com.panjiesw.android.abd;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
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

public class AksesBukuDigitalActivity extends ListActivity {
	
	private static final String[] titles = {"Plain Tales from the Hills","Washington Square"};
	private static final String[] authors = {"Rudyard Kipling","Henry James"};
	private static final String[] path = {"books/kipling-plain-tales-from-the-hills.epub","books/james-washington-square.epub"};
	private static final String LOG_D = "BUKU DIGITAL DEBUG";
	
	private LayoutInflater mInflater;
	private List<RowData> data;
	RowData rd;
	
	private ProgressDialog mProgressDialog;
	public static final int DIALOG_EBOOK_OPEN_PROGRESS = 0;
	
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
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_EBOOK_OPEN_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Loading eBook file...");
			mProgressDialog.setIndeterminate(false);
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
    
	private void logTableOfContents(List<TOCReference> tocReferences, int depth) {
		if (tocReferences == null) {
			return;
		}
		for (TOCReference tocReference : tocReferences) {
			StringBuilder tocString = new StringBuilder();
			for (int i = 0; i < depth; i++) {
				tocString.append("\t");
			}
			tocString.append(tocReference.getTitle());
			Log.i("epublib", tocString.toString());

			logTableOfContents(tocReference.getChildren(), depth + 1);
		}
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
	
	private class EbookLoadAsync extends AsyncTask<Integer, Integer, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_EBOOK_OPEN_PROGRESS);
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			int pos = params[0];
			try {
				publishProgress(10);
				InputStream epubInputStream = getAssets().open(path[pos]);
				publishProgress(30);
				Book book = (new EpubReader()).readEpub(epubInputStream);
				publishProgress(50);
				Log.d(LOG_D, "author(s): " + book.getMetadata().getAuthors());
				publishProgress(60);
				Log.d(LOG_D, "title: " + book.getTitle());
				publishProgress(70);
				logTableOfContents(book.getTableOfContents().getTocReferences(), 0);
				publishProgress(100);
			} catch (IOException e) {
				Log.e(LOG_D, e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			Log.d(LOG_D, String.valueOf(values[0]));
			mProgressDialog.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			dismissDialog(DIALOG_EBOOK_OPEN_PROGRESS);
		}
	}
}