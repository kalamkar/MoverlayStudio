package com.teddytab.studio.sidebar;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.teddytab.common.editor.Config;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageObject;
import com.teddytab.studio.App;
import com.teddytab.studio.DataChangeListener;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class AnimationListFragment extends Fragment implements DataChangeListener {

	private App app;
	private final List<Pair<String, Integer>> objectAnimations = new ArrayList<Pair<String, Integer>>();

	private void updateObjectIdAndAnimationIndex(Page page) {
		objectAnimations.removeAll(objectAnimations);
		if (page.objects == null) {
			return;
		}
		for (PageObject object : page.objects) {
			if (object.animation == null) {
				continue;
			}
			for (int i = 0; i < object.animation.length; i++) {
				if (object.animation[i] == null || object.animation[i].id == null) {
					continue;
				}
				objectAnimations.add(Pair.create(object.id, i));
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return new ListView(app);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		updateObjectIdAndAnimationIndex(app.book.getPage(app.selectedPage));
		((ListView) view).setAdapter(new AnimationListAdapter());
	}

	@Override
	public void notifyDataChange() {
		updateObjectIdAndAnimationIndex(app.book.getPage(app.selectedPage));
		((BaseAdapter) ((ListView) getView()).getAdapter()).notifyDataSetChanged();
	}

	private class AnimationListAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return objectAnimations.size();
		}

		@Override
		public Pair<String, Integer> getItem(int position) {
			return objectAnimations.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
			}
			Utils.setText(view, R.id.position, Integer.toString(position + 1));
			view.findViewById(R.id.menuButtons).setVisibility(View.INVISIBLE);

			Pair<String, Integer> pair = getItem(position);
			Animation animation =
					app.book.getObject(app.selectedPage, pair.first).animation[pair.second];
			((TextView) view.findViewById(R.id.title)).setText(animation == null ? getResources()
					.getString(R.string.add_new_animation) : animation.id);
			((TextView) view.findViewById(R.id.tip)).setText(getTip(animation));

			view.setOnClickListener(this);
			view.findViewById(R.id.edit).setOnClickListener(this);
			view.findViewById(R.id.delete).setOnClickListener(this);
			view.findViewById(R.id.copy).setOnClickListener(this);

			view.findViewById(R.id.edit).setTag(pair);
			view.findViewById(R.id.delete).setTag(pair);
			view.findViewById(R.id.copy).setTag(pair);
			view.setTag(pair);

			return view;
		}

		private String getTip(Animation animation) {
			Animation temp = Config.GSON.fromJson(Config.GSON.toJson(animation), Animation.class);
			temp.id = null;
			temp.reset = "NO".equals(temp.reset) ? null : temp.reset;
			temp.audio = null;
			temp.animationImages = null;
			return animation == null ? "" : Config.GSON.toJson(temp)
					.replaceAll("[}{\\[\\]\"]", "").replaceAll(",", " ");
		}

		@Override
		public void onClick(View view) {
			@SuppressWarnings("unchecked")
			final Pair<String, Integer> pair = (Pair<String, Integer>) view.getTag();
			switch (view.getId()) {
			case R.id.copy:
				((MainActivity) getActivity()).copy(
						app.book.getObject(app.selectedPage, pair.first).animation[pair.second]);
				return;
			case R.id.delete:
				Utils.showDelete(getActivity(), new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	app.book.removeObjectAnimation(app.selectedPage, pair.first, pair.second);
				    	notifyDataChange();
				    }
				});
				return;
			case R.id.edit:
				((MainActivity) getActivity()).setSelectedAnimation(
						app.selectedPage, pair.first, pair.second);
			default:
				View menu = view.findViewById(R.id.menuButtons);
				if (menu != null) {
					menu.setVisibility(
							menu.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
				}
			}
		}
	}
}
