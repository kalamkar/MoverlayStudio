package com.teddytab.studio.toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.teddytab.common.editor.BookLoadFragment;
import com.teddytab.common.editor.BookLoadFragment.OnBookLoadListener;
import com.teddytab.common.editor.Config;
import com.teddytab.common.editor.MediaSearchActivity;
import com.teddytab.common.model.Book;
import com.teddytab.common.model.Page;
import com.teddytab.common.net.StringResponseHttpRequest;
import com.teddytab.common.net.StringResponseHttpRequest.ResponseListener;
import com.teddytab.studio.App;
import com.teddytab.studio.BookActivity;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.UploadBookActivity;
import com.teddytab.studio.Utils;

public class BookToolbar extends Fragment implements OnClickListener {
	private static final String TAG = "BookToolbar";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.toolbar_book, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.play).setOnClickListener(this);
		view.findViewById(R.id.search).setOnClickListener(this);
		view.findViewById(R.id.upload).setOnClickListener(this);
		view.findViewById(R.id.clear).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.play:
			new BookLoadFragment().setBook(app.book.book).setOnBookLoadListener(
					new OnBookLoadListener() {
						@Override
						public void onBookLoad() {
							startActivity(new Intent(getActivity(), BookActivity.class));
						}
			}).show(getFragmentManager(), null);
			return;
		case R.id.search:
			Intent intent = new Intent(getActivity(), MediaSearchActivity.class);
			intent.setType("application/json");
			startActivityForResult(intent, App.BOOK_SEARCH_ACTIVITY);
			return;
		case R.id.upload:
			Log.v(TAG, Config.GSON_PRETTY.toJson(app.book.book));
			intent = new Intent(getActivity(), UploadBookActivity.class);
			intent.putExtra(UploadBookActivity.DATA,
					Utils.getZippedData(Config.GSON_PRETTY.toJson(app.book.book).getBytes()));
			intent.putExtra(UploadBookActivity.CONTENT_TYPE, "application/json");
			intent.putExtra(UploadBookActivity.TAGS, String.format("book,%s,%s",
					app.book.book.title, Utils.join(app.book.book.tags, ",")));
			intent.putExtra(UploadBookActivity.ZIPPED, true);
			startActivity(intent);
			return;
		case R.id.clear:
			Utils.showDelete(getActivity(), new DialogInterface.OnClickListener() {
				@Override
			    public void onClick(DialogInterface dialog, int which) {
			    	app.book.reset();
			    	((MainActivity) getActivity()).initialize();
			    }
			});
			return;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode != Activity.RESULT_OK || intent == null) {
			return;
		}
		switch (requestCode) {
		case App.BOOK_SEARCH_ACTIVITY:
			ResponseListener listener = new ResponseListener() {
				@Override
				public void onHttpResponse(final String response, String[] extraData) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.replace_or_merge_searched_book);
					builder.setPositiveButton(R.string.merge,
						new DialogInterface.OnClickListener() {
					    	@Override
					    	public void onClick(DialogInterface dialog, int which) {
					    		Book book = Config.GSON.fromJson(response, Book.class);
					    		if (book != null && book.pages != null) {
					    			for (Page page : book.pages) {
					    				app.book.addPage(page);
					    			}
					    		}
								((MainActivity) getActivity()).initialize();
					    	}
						});
					builder.setNegativeButton(R.string.replace,
						new DialogInterface.OnClickListener() {
					    	@Override
					    	public void onClick(DialogInterface dialog, int which) {
					    		app.book.book = Config.GSON.fromJson(response, Book.class);
								((MainActivity) getActivity()).initialize();
					    	}
						});
					builder.show();
				}
			};
			new StringResponseHttpRequest(getActivity(),
					intent.getStringExtra(MediaSearchActivity.URL), listener).execute();
		}
	}
}
