package com.teddytab.studio.toolbar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.teddytab.common.model.Animation;
import com.teddytab.studio.App;
import com.teddytab.studio.MainActivity;
import com.teddytab.studio.R;
import com.teddytab.studio.Utils;

public class AnimationsToolbar extends Fragment implements OnClickListener {

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
		return inflater.inflate(R.layout.toolbar_animations, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.more).setOnClickListener(this);
		view.findViewById(R.id.extra).setVisibility(View.GONE);

		view.findViewById(R.id.move).setOnClickListener(this);
		view.findViewById(R.id.zoom).setOnClickListener(this);
		view.findViewById(R.id.playSound).setOnClickListener(this);
		view.findViewById(R.id.showApp).setOnClickListener(this);
		view.findViewById(R.id.openUrl).setOnClickListener(this);
		view.findViewById(R.id.changePage).setOnClickListener(this);
		view.findViewById(R.id.stop).setOnClickListener(this);
		view.findViewById(R.id.show).setOnClickListener(this);
		view.findViewById(R.id.hide).setOnClickListener(this);
		view.findViewById(R.id.bounce).setOnClickListener(this);
		view.findViewById(R.id.rotate).setOnClickListener(this);
		view.findViewById(R.id.consume).setOnClickListener(this);
		view.findViewById(R.id.eat).setOnClickListener(this);
		view.findViewById(R.id.counter).setOnClickListener(this);
		view.findViewById(R.id.closeBook).setOnClickListener(this);
		view.findViewById(R.id.change).setOnClickListener(this);
		view.findViewById(R.id.flip).setOnClickListener(this);
		view.findViewById(R.id.hop).setOnClickListener(this);
		view.findViewById(R.id.fly).setOnClickListener(this);
		view.findViewById(R.id.sharePage).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.more) {
			View extra = getView().findViewById(R.id.extra);
			extra.setVisibility(extra.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
			return;
		}
		if (app.selectedObject == null) {
			return;
		}
		String animType = null;
		switch(view.getId()) {
		case R.id.move:
			animType = Animation.Type.move.name();
			break;
		case R.id.zoom:
			animType = Animation.Type.zoom.name();
			break;
		case R.id.playSound:
			animType = Animation.Type.playSound.name();
			break;
		case R.id.showApp:
			animType = Animation.Type.showApp.name();
			break;
		case R.id.openUrl:
			animType = Animation.Type.openUrl.name();
			break;
		case R.id.changePage:
			animType = Animation.Type.changePage.name();
			break;
		case R.id.stop:
			animType = Animation.Type.stop.name();
			break;
		case R.id.show:
			animType = Animation.Type.show.name();
			break;
		case R.id.hide:
			animType = Animation.Type.hide.name();
			break;
		case R.id.bounce:
			animType = Animation.Type.bounce.name();
			break;
		case R.id.rotate:
			animType = Animation.Type.rotate.name();
			break;
		case R.id.consume:
			animType = Animation.Type.consume.name();
			break;
		case R.id.eat:
			animType = Animation.Type.eat.name();
			break;
		case R.id.counter:
			animType = Animation.Type.counter.name();
			break;
		case R.id.closeBook:
			animType = Animation.Type.closeBook.name();
			break;
		case R.id.change:
			animType = Animation.Type.change.name();
			break;
		case R.id.flip:
			animType = Animation.Type.flip.name();
			break;
		case R.id.hop:
			animType = Animation.Type.hop.name();
			break;
		case R.id.fly:
			animType = Animation.Type.fly.name();
			break;
		case R.id.sharePage:
			animType = Animation.Type.sharePage.name();
			break;
		}
		String animPrefix = app.selectedObject + animType.substring(0, 1).toUpperCase()
				+ animType.substring(1).toLowerCase();
		Animation animation = new Animation();
		animation.id = animPrefix + Utils.getNextIdSuffix(animPrefix,
				app.book.getObject(app.selectedPage, app.selectedObject).animation);
		animation.type = animType;
		animation.reset = "NO";
		int index = app.book.addObjectAnimation(app.selectedPage, app.selectedObject, animation);
		((MainActivity) getActivity()).setSelectedAnimation(
				app.selectedPage, app.selectedObject, index);
	}

}
