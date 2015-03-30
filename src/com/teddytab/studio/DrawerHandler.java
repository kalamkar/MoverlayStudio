package com.teddytab.studio;

import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class DrawerHandler implements OnDragListener, OnTouchListener {
	private static final String TAG = "DrawerHandler";
	
	private final View handle;
	private final View drawer;
	
	public DrawerHandler(View drawer, View handle) {
		this.drawer = drawer;
		this.handle = handle;
		handle.setOnTouchListener(this);
		moveDrawerRight();
	}
	
	public void closeDrawerLeft() {
		Log.i(TAG, "closeDrawerLeft");
		RelativeLayout.LayoutParams handleParams = getHandlerParams();
		drawer.setVisibility(View.GONE);
		handleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		handle.setLayoutParams(handleParams);
		handle.invalidate();
	}

	public void closeDrawerRight() {
		Log.i(TAG, "closeDrawerRight");
		RelativeLayout.LayoutParams handleParams = getHandlerParams();
		drawer.setVisibility(View.GONE);
		handleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		handle.setLayoutParams(handleParams);
		handle.invalidate();
	}

	public void moveDrawerLeft() {
		Log.i(TAG, "moveDrawerLeft");
		RelativeLayout.LayoutParams drawerParams = getDrawerParams();
		RelativeLayout.LayoutParams handleParams = getHandlerParams();
		
		drawer.setVisibility(View.VISIBLE);
		drawerParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		handleParams.addRule(RelativeLayout.RIGHT_OF, drawer.getId());
		drawer.setLayoutParams(drawerParams);
		handle.setLayoutParams(handleParams);
		drawer.invalidate();
		handle.invalidate();
	}

	public void moveDrawerRight() {
		Log.i(TAG, "moveDrawerRight");
		RelativeLayout.LayoutParams drawerParams = getDrawerParams();
		RelativeLayout.LayoutParams handleParams = getHandlerParams();
		
		drawer.setVisibility(View.VISIBLE);
		drawerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		handleParams.addRule(RelativeLayout.LEFT_OF, drawer.getId());
		drawer.setLayoutParams(drawerParams);
		handle.setLayoutParams(handleParams);
		drawer.invalidate();
		handle.invalidate();
	}

	@Override
	public boolean onDrag(View receiverView, DragEvent event) {
		int screenWidth = receiverView.getWidth();
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			return true;
		case DragEvent.ACTION_DRAG_ENTERED:
			return true;
		case DragEvent.ACTION_DROP:
			if (event.getX() < (screenWidth * 0.1)) {
				closeDrawerLeft();
			} else if (event.getX() > (screenWidth - (screenWidth * 0.1))) {
				closeDrawerRight();
			} else if (event.getX() < screenWidth / 2) {
				moveDrawerLeft();
			} else if (event.getX() > screenWidth / 2) {
				moveDrawerRight();
			}
			return true;
		case DragEvent.ACTION_DRAG_ENDED:
			return true;
		}
		return true;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		handle.startDrag(null, new View.DragShadowBuilder(), null, 0);
		return true;
	}
	
	private RelativeLayout.LayoutParams getDrawerParams() {
		float drawerWidth = drawer.getContext().getResources().getDimension(R.dimen.drawer_width);
		RelativeLayout.LayoutParams params
				= new RelativeLayout.LayoutParams((int) drawerWidth, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		return params;
	}
	
	private RelativeLayout.LayoutParams getHandlerParams() {
		float drawerHeight = drawer.getContext().getResources().getDimension(R.dimen.handle_height);
		RelativeLayout.LayoutParams params
				= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, (int) drawerHeight);
		return params;
	}
}
