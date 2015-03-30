package com.teddytab.studio;

import java.util.ArrayList;
import java.util.List;

import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Book;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageEvent;
import com.teddytab.common.model.PageObject;

public class BookBuilder {

	public Book book;

	public BookBuilder(Book book) {
		this.book = book;
	}

	public void reset() {
		this.book = new Book();
		this.book.title = "New Book";
		Page page = new Page();
		page.id = "Page1";
		this.addPage(page);
	}

	public boolean isWide() {
		return true;
	}

	public void addTag(String tag) {
		List<String> tags = new ArrayList<String>();
		Utils.fillList(tags, book.tags);
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
		book.tags = tags.toArray(new String[0]);
	}

	public void deleteTag(String tag) {
		List<String> tags = new ArrayList<String>();
		Utils.fillList(tags, book.tags);
		tags.remove(tag);
		book.tags = tags.toArray(new String[0]);
	}

	public Page getPage(int pageNumber) {
		if (book == null || book.pages == null || pageNumber >= book.pages.length) {
			return null;
		}
		return book.pages[pageNumber];
	}

	public int addPage(Page page) {
		List<Page> pages = new ArrayList<Page>();
		Utils.fillList(pages, book.pages);
		pages.add(page);
		book.pages = pages.toArray(new Page[0]);
		return pages.size() - 1;
	}

	public void copyPage(int pageNumber) {
		Page page = book.pages[pageNumber];
		List<Page> pages = new ArrayList<Page>();
		Utils.fillList(pages, book.pages);
		Page newPage = Utils.fromJson(Utils.toJson(page), Page.class);
		newPage.id = String.format("%s_copy%d", newPage.id.replaceAll("_copy[0-9]+", ""),
				(int) (Math.random() * 100.0f));
		pages.add(pages.indexOf(page) + 1, newPage);
		book.pages = pages.toArray(new Page[0]);
	}

	public void deletePage(Page page) {
		List<Page> pages = new ArrayList<Page>();
		Utils.fillList(pages, book.pages);
		pages.remove(page);
		book.pages = pages.toArray(new Page[0]);
	}

	public void movePage(Page pageToMove, Page droppedOnPage) {
		List<Page> pages = new ArrayList<Page>();
		Utils.fillList(pages, book.pages);
		pages.remove(pageToMove);
		int index = droppedOnPage == null ? pages.size() : pages.indexOf(droppedOnPage);
		pages.add(index == -1 ? 0 : index, pageToMove);
		book.pages = pages.toArray(new Page[0]);
	}

	// ---------------------------- Current Page Ops ----------------------------

	public void addPageTag(int pageNumber, String tag) {
		List<String> tags = new ArrayList<String>();
		Utils.fillList(tags, book.pages[pageNumber].tags);
		if (!tags.contains(tag)) {
			tags.add(tag);
		}
		book.pages[pageNumber].tags = tags.toArray(new String[0]);
	}

	public void deletePageTag(int pageNumber, String tag) {
		List<String> tags = new ArrayList<String>();
		Utils.fillList(tags, book.pages[pageNumber].tags);
		tags.remove(tag);
		book.pages[pageNumber].tags = tags.toArray(new String[0]);
	}

	// ---------------------------- Object Ops ----------------------------
	public PageObject getObject(int pageNumber, String objectId) {
		Page page = getPage(pageNumber);
		if (page == null || page.objects == null || objectId == null) {
			return null;
		}
		for (PageObject object : page.objects) {
			if (objectId.equals(object.id)) {
				return object;
			}
		}
		return null;
	}

	public void addObject(int pageNumber, PageObject object) {
		List<PageObject> objects = new ArrayList<PageObject>();
		Utils.fillList(objects, book.pages[pageNumber].objects);
		objects.add(object);
		book.pages[pageNumber].objects = objects.toArray(new PageObject[0]);
	}

	public void moveObject(int pageNumber, Object from, Object to) {
		List<PageObject> objects = new ArrayList<PageObject>();
		Utils.fillList(objects, book.pages[pageNumber].objects);
		objects.remove(from);
		int toIndex = objects.indexOf(to);
		objects.add(toIndex >= 0 ? toIndex : to == null ? objects.size() : 0, (PageObject) from);
		book.pages[pageNumber].objects = objects.toArray(new PageObject[0]);
	}

	public void removeObject(int pageNumber, PageObject object) {
		List<PageObject> objects = new ArrayList<PageObject>();
		Utils.fillList(objects, book.pages[pageNumber].objects);
		objects.remove(objects.indexOf(object));
		book.pages[pageNumber].objects = objects.toArray(new PageObject[0]);
	}

	public void updateLocation(int pageNumber, PageObject obj, int x, int y) {
		List<PageObject> objects = new ArrayList<PageObject>();
		Utils.fillList(objects, book.pages[pageNumber].objects);
		int index = objects.indexOf(obj);
		objects.remove(obj);
		obj.setRelativeX(Integer.toString(x));
		obj.setRelativeY(Integer.toString(y));
		objects.add(index, obj);
		book.pages[pageNumber].objects = objects.toArray(new PageObject[0]);
	}
	// ---------------------------- End Object Ops ----------------------------

	// ---------------------------- Event Ops ----------------------------

	public int addEvent(int pageNumber, PageEvent event) {
		List<PageEvent> events = new ArrayList<PageEvent>();
		Utils.fillList(events, book.pages[pageNumber].events);
		events.add(event);
		book.pages[pageNumber].events = events.toArray(new PageEvent[0]);
		return events.size() - 1;
	}

	public void removeEvent(int pageNumber, PageEvent event) {
		List<PageEvent> events = new ArrayList<PageEvent>();
		Utils.fillList(events, book.pages[pageNumber].events);
		events.remove(events.indexOf(event));
		book.pages[pageNumber].events = events.toArray(new PageEvent[0]);
	}

	public void copyEvent(int pageNumber, PageEvent event) {
		List<PageEvent> events = new ArrayList<PageEvent>();
		Utils.fillList(events, book.pages[pageNumber].events);
		PageEvent newEvent = Utils.fromJson(Utils.toJson(event), PageEvent.class);
		events.add(events.indexOf(event) + 1, newEvent);
		book.pages[pageNumber].events = events.toArray(new PageEvent[0]);
	}

	public void addEventAnimation(int pageNumber, int eventIndex, String animationId) {
		List<String> animationIds = new ArrayList<String>();
		Utils.fillList(animationIds, book.pages[pageNumber].events[eventIndex].animationids);
		animationIds.add(animationId);
		book.pages[pageNumber].events[eventIndex].animationids =
				animationIds.toArray(new String[0]);
	}

	public void removeEventAnimation(int pageNumber, int eventIndex, String animationId) {
		List<String> animationIds = new ArrayList<String>();
		Utils.fillList(animationIds, book.pages[pageNumber].events[eventIndex].animationids);
		animationIds.remove(animationId);
		book.pages[pageNumber].events[eventIndex].animationids =
				animationIds.toArray(new String[0]);
	}

	// ---------------------------- End Event Ops ----------------------------

	// ---------------------------- Animation Ops ----------------------------

	public int addObjectAnimation(int pageNumber, String objectId, Animation animation) {
		PageObject object = getObject(pageNumber, objectId);
		List<Animation> animations = new ArrayList<Animation>();
		Utils.fillList(animations, object.animation);
		animations.add(animation);
		object.animation = animations.toArray(new Animation[0]);
		return animations.size() - 1;
	}

	public void removeObjectAnimation(int pageNumber, String objectId, int animationIndex) {
		PageObject object = getObject(pageNumber, objectId);
		List<Animation> animations = new ArrayList<Animation>();
		Utils.fillList(animations, object.animation);
		animations.remove(animationIndex);
		object.animation = animations.toArray(new Animation[0]);
	}

	public void addAnimationImage(int pageNumber, String objectId, int animationIndex,
			String imageUrl) {
		List<String> imageUrls = new ArrayList<String>();
		PageObject object = getObject(pageNumber, objectId);
		Utils.fillList(imageUrls, object.animation[animationIndex].animationImages);
		imageUrls.add(imageUrl);
		object.animation[animationIndex].animationImages = imageUrls.toArray(new String[0]);
	}

	public void removeAnimationImage(int pageNumber, String objectId, int animationIndex,
			String imageUrl) {
		List<String> imageUrls = new ArrayList<String>();
		PageObject object = getObject(pageNumber, objectId);
		Utils.fillList(imageUrls, object.animation[animationIndex].animationImages);
		imageUrls.remove(imageUrl);
		object.animation[animationIndex].animationImages = imageUrls.toArray(new String[0]);
	}

	// ---------------------------- End Animation Ops ----------------------------

	// ---------------------------- End Current Page Ops ----------------------------

}
