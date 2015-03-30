package com.teddytab.studio;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.teddytab.common.editor.Config;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Book;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageEvent;
import com.teddytab.common.model.PageObject;
import com.teddytab.studio.sidebar.AnimationListFragment;
import com.teddytab.studio.sidebar.AnimationPropertiesFragment;
import com.teddytab.studio.sidebar.BookPropertiesFragment;
import com.teddytab.studio.sidebar.EventListFragment;
import com.teddytab.studio.sidebar.EventPropertiesFragment;
import com.teddytab.studio.sidebar.ObjectListFragment;
import com.teddytab.studio.sidebar.ObjectPropertiesFragment;
import com.teddytab.studio.sidebar.PageListFragment;
import com.teddytab.studio.sidebar.PagePropertiesFragment;
import com.teddytab.studio.toolbar.AnimationsToolbar;
import com.teddytab.studio.toolbar.BookToolbar;
import com.teddytab.studio.toolbar.EventsToolbar;
import com.teddytab.studio.toolbar.ObjectsToolbar;
import com.teddytab.studio.toolbar.PagesToolbar;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnPageChangeListener {
	private static final String TAG = "MainActivity";

	private static final String BOOK_PREF = "BOOK";

	private App app;
	private Fragment currentFragment;

	private ViewPager pager;
	private DrawerHandler drawerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (App) getApplication();

		app.book.reset();
		try {
			app.book.book = Config.GSON.fromJson(
					getSharedPreferences(getPackageName(), 0).getString(BOOK_PREF,
							Config.GSON.toJson(app.book.book)), Book.class);
		} catch (JsonSyntaxException ex) {
			Log.w(TAG, getSharedPreferences(getPackageName(), 0).getString(BOOK_PREF, ""), ex);
			Toast.makeText(this, R.string.could_not_load_book, Toast.LENGTH_LONG).show();
		}

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
		pager.setOnPageChangeListener(this);

		drawerHandler = new DrawerHandler(findViewById(R.id.drawer), findViewById(R.id.handle));
		findViewById(android.R.id.content).setOnDragListener(drawerHandler);

		findViewById(R.id.cut).setOnClickListener(this);
		findViewById(R.id.copy).setOnClickListener(this);
		findViewById(R.id.paste).setOnClickListener(this);

		findViewById(R.id.title).setOnClickListener(this);
		findViewById(R.id.book_properties).setOnClickListener(this);
		findViewById(R.id.page_list).setOnClickListener(this);
		findViewById(R.id.object_list).setOnClickListener(this);
		findViewById(R.id.animation_list).setOnClickListener(this);
		findViewById(R.id.event_list).setOnClickListener(this);
		showTabs();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		app.book.book = Config.GSON.fromJson(savedInstanceState.getString(BOOK_PREF), Book.class);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BOOK_PREF, Config.GSON.toJson(app.book.book));
	}

	@Override
	protected void onStop() {
		getSharedPreferences(getPackageName(), 0).edit()
				.putString(BOOK_PREF, Config.GSON.toJson(app.book.book)).commit();
		super.onStop();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.paste:
			Object pastedObject = app.paste();
			if (pastedObject == null) {
				return;
			}
			if (pastedObject instanceof Page) {
				app.book.addPage(Utils.getCopy((Page) pastedObject, app.book.book.pages));
			} else if (pastedObject instanceof PageObject) {
				app.book.addObject(app.selectedPage, Utils.getCopy((PageObject) pastedObject,
						app.book.getPage(app.selectedPage).objects));
			} else if (pastedObject instanceof Animation && app.selectedObject != null) {
				app.book.addObjectAnimation(app.selectedPage, app.selectedObject,
						Utils.getCopy((Animation) pastedObject, app.book
								.getObject(app.selectedPage, app.selectedObject).animation));
			} else if (pastedObject instanceof PageEvent) {
				PageEvent event =
						Config.GSON.fromJson(Config.GSON.toJson(pastedObject), PageEvent.class);
				app.book.addEvent(app.selectedPage, event);
			}
			updatePage();
			updateSidebar();
			return;
		case R.id.copy:
			if (app.selectedAnimation >= 0) {
				app.copy(app.book.getObject(app.selectedPage, app.selectedObject)
						.animation[app.selectedAnimation]);
			} else if (app.selectedEvent >= 0) {
				app.copy(app.book.getPage(app.selectedPage).events[app.selectedEvent]);
			} else if (app.selectedObject != null) {
				app.copy(app.book.getObject(app.selectedPage, app.selectedObject));
			} else if (app.selectedPage >= 0) {
				app.copy(app.book.getPage(app.selectedPage));
			}
			return;
		case R.id.cut:
			Utils.showDelete(this, new DialogInterface.OnClickListener() {
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
		    		if (app.selectedAnimation >= 0) {
		    			app.book.removeObjectAnimation(app.selectedPage, app.selectedObject,
		    					app.selectedAnimation);
		    		} else if (app.selectedEvent >= 0) {
		    			app.book.removeEvent(app.selectedPage,
		    					app.book.getPage(app.selectedPage).events[app.selectedEvent]);
		    		} else if (app.selectedObject != null) {
		    			app.book.removeObject(app.selectedPage,
		    					app.book.getObject(app.selectedPage, app.selectedObject));
		    			updatePage();
		    		} else if (app.selectedPage >= 0) {
		    			app.book.deletePage(app.book.getPage(app.selectedPage));
		    			app.selectedPage = app.selectedPage > 0 ? app.selectedPage - 1 : 0;
		    			updatePage();
		    		}
	    			showTabs();
	    		}
			});
			return;
		}

		if (currentFragment != null) {
			showTabs();
			return;
		}
		switch (view.getId()) {
		case R.id.book_properties:
			showDetails(new BookPropertiesFragment(),
					getResources().getString(R.string.book), R.color.YellowBack, new BookToolbar());
			break;
		case R.id.page_list:
			showDetailsWithPaste(new PageListFragment(), getResources().getString(R.string.pages),
					R.color.GreenBack, Page.class, new PagesToolbar());
			break;
		case R.id.object_list:
			showDetailsWithPaste(new ObjectListFragment(),
					getResources().getString(R.string.objects), R.color.BlueBack,
					PageObject.class, new ObjectsToolbar());
			break;
		case R.id.animation_list:
			showDetailsWithPaste(new AnimationListFragment(),
					getResources().getString(R.string.animations), R.color.PurpleBack,
					Animation.class, new AnimationsToolbar());
			break;
		case R.id.event_list:
			showDetailsWithPaste(new EventListFragment(),
					getResources().getString(R.string.events), R.color.RedBack, PageEvent.class,
					new EventsToolbar());
			break;
		}
	}

	public void copy(Object object) {
		app.copy(object);
		showPasteButton();
	}

	public void updatePage() {
		pager.getAdapter().notifyDataSetChanged();
	}

	public void updateSidebar() {
		if (currentFragment != null) {
			((DataChangeListener) currentFragment).notifyDataChange();
		}
	}

	public void initialize() {
		app.selectedPage = 0;
		app.selectedObject = null;
		app.selectedEvent = -1;
		app.selectedAnimation = -1;
		copy(null);
		pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
		updateSidebar();
	}

	public void setSelectedPage(int pageNumber) {
		app.selectedPage = pageNumber;
		app.selectedObject = null;
		app.selectedEvent = -1;
		app.selectedAnimation = -1;
		pager.setCurrentItem(pageNumber);
		showDetailsWithCopy(new PagePropertiesFragment(),
				app.book.getPage(pageNumber).id, R.color.GreenBack, new ObjectsToolbar());
	}

	public void setSelectedObject(int pageNumber, String objectId) {
		app.selectedPage = pageNumber;
		app.selectedObject = objectId;
		app.selectedEvent = -1;
		app.selectedAnimation = -1;
		PageObject object = app.book.getObject(pageNumber, objectId);
		if (object == null) {
			return;
		}
		showDetailsWithCopy(new ObjectPropertiesFragment(), object.id, R.color.BlueBack,
				new AnimationsToolbar());
		if (object.getRelativeX() < 50) {
			drawerHandler.moveDrawerRight();
		} else {
			drawerHandler.moveDrawerLeft();
		}
	}

	public void setSelectedEvent(int pageNumber, int eventIndex) {
		app.selectedPage = pageNumber;
		app.selectedObject = null;
		app.selectedEvent = eventIndex;
		app.selectedAnimation = -1;
		showDetailsWithCopy(new EventPropertiesFragment(),
				app.book.book.pages[pageNumber].events[eventIndex].toString(), R.color.RedBack,
				null);
	}

	public void setSelectedAnimation(int pageNumber, String objectId, int animationIndex) {
		app.selectedPage = pageNumber;
		app.selectedObject = objectId;
		app.selectedEvent = -1;
		app.selectedAnimation = animationIndex;
		showDetailsWithCopy(new AnimationPropertiesFragment(),
				app.book.getObject(pageNumber, objectId).animation[animationIndex].id,
				R.color.PurpleBack, null);
	}

	private void showTabs() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (currentFragment != null) {
			transaction.remove(currentFragment);
		}
		currentFragment = null;
		findViewById(R.id.title).setVisibility(View.GONE);
		findViewById(R.id.details).setVisibility(View.GONE);
		findViewById(R.id.book_properties).setVisibility(View.VISIBLE);
		findViewById(R.id.page_list).setVisibility(View.VISIBLE);
		findViewById(R.id.object_list).setVisibility(View.VISIBLE);
		findViewById(R.id.event_list).setVisibility(View.VISIBLE);
		findViewById(R.id.animation_list).setVisibility(View.VISIBLE);
		transaction.replace(R.id.toolbar, new BookToolbar(), "toolbar").commit();
	}

	private void showDetailsWithCopy(Fragment fragment, String title, int colorId,
			Fragment toolbar) {
		showDetails(fragment, title, colorId, toolbar);
		findViewById(R.id.copy).setVisibility(View.VISIBLE);
		findViewById(R.id.cut).setVisibility(View.VISIBLE);
	}

	@SuppressWarnings("rawtypes")
	private void showDetailsWithPaste(Fragment fragment, String title, int colorId, Class cls,
			Fragment toolbar) {
		showDetails(fragment, title, colorId, toolbar);
		if (cls.isInstance(app.paste())) {
			showPasteButton();
		}
	}

	private void showPasteButton() {
		Object object = app.paste();
		if (object == null) {
			return;
		}
		Button pasteButton = (Button) findViewById(R.id.paste);
		String id = object instanceof PageEvent ? object.toString() : Utils.getId(object);
		if (id == null) {
			return;
		}
		pasteButton.setText(id.length() > 10 ? id.substring(0, 10) : id);
		pasteButton.setVisibility(View.VISIBLE);
	}

	private void showDetails(Fragment fragment, String title, int colorId, Fragment toolbar) {
		currentFragment = fragment;
		findViewById(R.id.title).setVisibility(View.VISIBLE);
		findViewById(R.id.details).setVisibility(View.VISIBLE);
		findViewById(R.id.book_properties).setVisibility(View.GONE);
		findViewById(R.id.page_list).setVisibility(View.GONE);
		findViewById(R.id.object_list).setVisibility(View.GONE);
		findViewById(R.id.event_list).setVisibility(View.GONE);
		findViewById(R.id.animation_list).setVisibility(View.GONE);
		findViewById(R.id.cut).setVisibility(View.GONE);
		findViewById(R.id.copy).setVisibility(View.GONE);
		findViewById(R.id.paste).setVisibility(View.GONE);
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.details, fragment);
		if (toolbar != null) {
			transaction.replace(R.id.toolbar, toolbar, "toolbar");
		} else if (getFragmentManager().findFragmentByTag("toolbar") != null) {
			transaction.remove(getFragmentManager().findFragmentByTag("toolbar"));
		}
		transaction.commit();
		((TextView) findViewById(R.id.titleText)).setText(title);
		findViewById(R.id.title).setBackgroundColor(getResources().getColor(colorId));
	}

	public class PagerAdapter extends FragmentStatePagerAdapter {
		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
	    public android.support.v4.app.Fragment getItem(int pageNumber) {
	        PageFragment page = new PageFragment();
	        Bundle args = new Bundle();
	        args.putInt(PageFragment.PAGE_NUMBER, pageNumber);
	        page.setArguments(args);
	        return page;
	    }

	    @Override
	    public int getCount() {
	        return app.book.book != null && app.book.book.pages != null
	        		? app.book.book.pages.length: 0;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	        return app.book.getPage(position).id;
	    }

	    @Override
	    public int getItemPosition(Object fragment) {
	    	int pageNumber = POSITION_UNCHANGED;
	    	if (fragment == null) {
	    		return POSITION_NONE;
	    	}
	    	PageFragment page = (PageFragment) fragment;
	    	pageNumber = page.getArguments().getInt(PageFragment.PAGE_NUMBER, POSITION_NONE);
	    	if (pageNumber == app.selectedPage) {
	    		page.notifyDataChange();
	    	}
	    	return pageNumber;
	    }
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (app.selectedPage != position) {
			app.selectedObject = null;
			app.selectedEvent = -1;
			app.selectedAnimation = -1;
		}
		app.selectedPage = position;
		showTabs();
	}
}
