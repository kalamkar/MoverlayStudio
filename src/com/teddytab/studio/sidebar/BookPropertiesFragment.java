package com.teddytab.studio.sidebar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.teddytab.common.model.Book;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class BookPropertiesFragment extends Fragment implements OnClickListener, DataChangeListener,
		OnCheckedChangeListener {

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
		return inflater.inflate(R.layout.fragment_book_properties, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((ListView) view.findViewById(R.id.tags)).setAdapter(new TagsListAdapter());
		notifyDataChange();

		view.findViewById(R.id.add).setOnClickListener(this);

		Utils.setText(view, R.id.title, app.book.book.title);

		TextView title = (TextView) view.findViewById(R.id.title);
		title.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable text) {
				app.book.book.title = text.toString();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		((CheckBox) view.findViewById(R.id.firstPageIsAnchor)).setOnCheckedChangeListener(this);
		((CheckBox) view.findViewById(R.id.animatedAnchor)).setOnCheckedChangeListener(this);

		RadioGroup orientation = (RadioGroup) view.findViewById(R.id.orientation);
		orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.landscape) {
					app.book.book.orientation = "landscape";
				} else if (checkedId == R.id.portrait) {
					app.book.book.orientation = "portrait";
				} else {
					app.book.book.orientation = null;
				}
			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add:
			String tag = Utils.getText(getView(), R.id.tag, null);
			if (tag != null) {
				app.book.addTag(tag);
				Utils.setText(getView(), R.id.tag, "");
				notifyDataChange();
			}
			break;
		case R.id.delete:
			app.book.deleteTag((String) view.getTag());
			notifyDataChange();
		}
	}

	@Override
	public void notifyDataChange() {
		((BaseAdapter) ((ListView) getView().findViewById(R.id.tags)).getAdapter())
				.notifyDataSetChanged();
		Utils.setText(getView(), R.id.title, app.book.book.title);
		((CheckBox) getView().findViewById(R.id.firstPageIsAnchor)).setChecked(
				Utils.arrayContains(Book.TAG_FIRSTPAGEISANCHOR, app.book.book.tags));
		((CheckBox) getView().findViewById(R.id.animatedAnchor)).setChecked(
				Utils.arrayContains(Book.TAG_ANIMATEDANCHOR, app.book.book.tags));
		if ("landscape".equalsIgnoreCase(app.book.book.orientation)) {
			((RadioButton) getView().findViewById(R.id.landscape)).setChecked(true);
		} else if ("portrait".equalsIgnoreCase(app.book.book.orientation)) {
			((RadioButton) getView().findViewById(R.id.portrait)).setChecked(true);
		} else {
			((RadioButton) getView().findViewById(R.id.flexible)).setChecked(true);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		String tag = null;
		switch(view.getId()) {
		case R.id.firstPageIsAnchor:
			tag = Book.TAG_FIRSTPAGEISANCHOR;
			break;
		case R.id.animatedAnchor:
			tag = Book.TAG_ANIMATEDANCHOR;
			break;
		}
		if (isChecked) {
			app.book.addTag(tag);
		} else {
			app.book.deleteTag(tag);
		}
		notifyDataChange();
	}

	private class TagsListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return app == null || app.book.book.tags == null ? 0 : app.book.book.tags.length;
		}

		@Override
		public String getItem(int position) {
			return app.book.book.tags[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
				view.findViewById(R.id.edit).setVisibility(View.GONE);
				view.findViewById(R.id.copy).setVisibility(View.GONE);
				view.findViewById(R.id.delete).setOnClickListener(BookPropertiesFragment.this);
				Utils.setText(view, R.id.tip, null);
			} else {
				view = convertView;
			}
			String tag = getItem(position);
			Utils.setText(view, R.id.title, tag);
			view.findViewById(R.id.delete).setTag(tag);
			return view;
		}
	}
}
