package com.teddytab.studio.toolbar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.teddytab.common.model.Page;
import com.teddytab.studio.App;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class PagesToolbar extends Fragment implements OnClickListener {

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
		return inflater.inflate(R.layout.toolbar_pages, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.add_page).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.add_page:
			Page page = new Page();
			page.id = "Page" + Utils.getNextIdSuffix("Page", app.book.book.pages);
			int pageNumber = app.book.addPage(page);
			((MainActivity) getActivity()).updatePage();
			((MainActivity) getActivity()).setSelectedPage(pageNumber);
			return;
		}
	}

}
