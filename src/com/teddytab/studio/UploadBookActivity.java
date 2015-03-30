/*******************************************************************************
 * Copyright 2013 TeddyTab llc. All rights reserved.
 ******************************************************************************/
package com.teddytab.studio;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.teddytab.common.editor.AuthenticatedActivity;
import com.teddytab.common.editor.Config;
import com.teddytab.common.editor.Utils;
import com.teddytab.common.editor.model.JsonResponse;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Book;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageObject;
import com.teddytab.common.net.CustomCacheHttpClient;

public class UploadBookActivity extends AuthenticatedActivity implements OnClickListener {
	private static final String TAG = "UploadBookActivity";
	
	public static final String DATA = "DATA";
	public static final String CONTENT_TYPE = "CONTENT_TYPE";
	public static final String TAGS = "TAGS";
	public static final String ZIPPED = "ZIPPED";
	
	protected String bookData;
	protected String contentType;
	private Set<String> localUrls;

	protected boolean signupIfNeeded = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_book);
		
		if (getIntent().getBooleanExtra(ZIPPED, false)) {
			bookData = new String(Utils.getUnzippedData(getIntent().getByteArrayExtra(DATA)));
		} else {
			bookData = new String(getIntent().getByteArrayExtra(DATA));
		}
		localUrls = getLocalUrls(Utils.fromJson(bookData, Book.class));
		
		contentType = getIntent().getStringExtra(CONTENT_TYPE);
		Utils.setText(this, R.id.contentType, contentType);
		Utils.setText(this, R.id.size, bookData != null ? Integer.toString(bookData.length()) : "");
		Utils.setText(this, R.id.tags, getIntent().getStringExtra(TAGS));

		findViewById(R.id.upload).setOnClickListener(this);
		updateAuthToken();
	}

	@Override
	protected void onOrganizationsUpdated() {
		if (orgNames.size() == 0 && signupIfNeeded) {
			signupIfNeeded = false;
			signupPerson();
			return;
		}
		((Spinner) findViewById(R.id.organization)).setAdapter(
				new ArrayAdapter<String>(UploadBookActivity.this,
						android.R.layout.simple_dropdown_item_1line, orgNames));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.upload:
			Utils.setText(UploadBookActivity.this, R.id.response, null);
			findViewById(R.id.progress).setVisibility(View.VISIBLE);
			findViewById(R.id.upload).setVisibility(View.INVISIBLE);

			String tags = Utils.getText(this, R.id.tags, null);
			String orgId = organizations
					.get(((Spinner) findViewById(R.id.organization))
							.getSelectedItem());
			for (String localUrl : localUrls) {
				try {
					new DownloadTask(localUrl, tags, orgId).execute();
				} catch (Throwable e) {
					Log.w(TAG, e);
				}
			}
			if (localUrls.isEmpty()) {
				new UploadTask(null, tags, bookData.getBytes(), contentType,
						orgId == null ? "" : orgId).execute();
			}
		}
	}

	private Set<String> getLocalUrls(Book book) {
		Set<String> localUrls = new HashSet<String>();
		if (book == null || book.pages == null) {
			return localUrls;
		}
		for (Page page : book.pages) {
			if (page.objects == null) {
				continue;
			}
			for (PageObject obj : page.objects) {
				if (obj.image != null && isLocalUrl(obj.image.url)) {
					localUrls.add(obj.image.url);
				}
				if (obj.animation == null) {
					continue;
				}
				for (Animation anim : obj.animation) {
					if (anim != null && isLocalUrl(anim.audio)) {
						localUrls.add(anim.audio);
					}
					if (anim.animationImages != null) {
						for (String imageUrl : anim.animationImages) {
							if (isLocalUrl(imageUrl)) {
								localUrls.add(imageUrl);
							}
						}
					}
				}
			}
		}
		return localUrls;
	}
	
	private static boolean isLocalUrl(String url) {
		if (url != null && !url.toLowerCase().startsWith("http")) {
			return true;
		}
		return false;
	}
	
	private String getContentType(String url) {
		if (url == null) {
			return null;
		}
		String type = getContentResolver().getType(Uri.parse(url));
		if (type == null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
		    int index = url.lastIndexOf('.') + 1;
		    String ext = url.substring(index).toLowerCase();
		    type = mime.getMimeTypeFromExtension(ext);
		}
		return type;
	}
	
	private class DownloadTask extends AsyncTask<Void, Void, ByteArrayOutputStream> {
		private final String url;
		private final String tags;
		private final String orgId;
		
		private DownloadTask(String url, String tags, String orgId) {
			this.url = url;
			this.tags = tags;
			this.orgId = orgId;
		}
		
		@Override
		protected ByteArrayOutputStream doInBackground(Void... params) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				URL parsedUrl = new URL(url);
				URLConnection connection = parsedUrl.openConnection();
				CustomCacheHttpClient.copy(
						new BufferedInputStream(connection.getInputStream()), output);
			} catch (Throwable t) {
				Log.w(TAG, t);
			}
			return output;
		}
		
		@Override
		protected void onPostExecute(ByteArrayOutputStream output) {
			String mimeType = getContentType(url);
			new UploadTask(url, tags, output.toByteArray(), mimeType, orgId == null ? "" : orgId)
				.execute();
		}
	}
	
	public class UploadTask extends AsyncTask<Void, Void, Pair<Integer, String>> {
		private final String localUrl;
		private final String tags;
		private final byte[] data;
		private final String contentType;
		private final String orgId;

		public UploadTask(String localUrl, String tags, byte[] data, String contentType,
				String orgId) {
			this.localUrl = localUrl;
			this.tags = tags;
			this.data = data;
			this.contentType = contentType;
			this.orgId = orgId;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Pair<Integer, String> doInBackground(Void... params) {
			try {
				return Utils.uploadFile(Config.ADD_URL, authToken, data, contentType,
						Pair.create("tags", tags), Pair.create("orgId", orgId));
			} catch (IOException e) {
				Log.w(TAG, e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Pair<Integer, String> response) {
			findViewById(R.id.progress).setVisibility(View.INVISIBLE);
			if (response == null) {
				Utils.setText(UploadBookActivity.this, R.id.response, "Unable to upload.");
				Log.w(TAG, "Got null response");
				return;
			}
			Log.d(TAG, String.format("Got response %d %s", response.first, response.second));
			if (response.first != 200) {
				findViewById(R.id.upload).setVisibility(View.VISIBLE);
				Utils.setText(UploadBookActivity.this, R.id.response, response.second);
			} else {
				JsonResponse jsonResponse = Utils.fromJson(response.second, JsonResponse.class);
				if (localUrl != null && jsonResponse.media != null
						&& jsonResponse.media.length > 0 && jsonResponse.media[0].url != null) {
					bookData = bookData.replaceAll(localUrl, jsonResponse.media[0].url);
					localUrls.remove(localUrl);
				}
				
				if (localUrl == null) {
					Utils.setText(UploadBookActivity.this, R.id.response, jsonResponse.code);
				}
			}
			
			
			// After last image upload, upload the book.
			if (localUrls.isEmpty() && localUrl != null) {
				new UploadTask(null, tags, bookData.getBytes(), UploadBookActivity.this.contentType,
						orgId == null ? "" : orgId).execute();
			}
		}
	}
}
