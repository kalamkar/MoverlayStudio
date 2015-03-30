package com.teddytab.studio;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.teddytab.common.editor.Config;
import com.teddytab.common.model.Animation;
import com.teddytab.common.model.Page;
import com.teddytab.common.model.PageObject;

public class Utils {
	private static final String TAG = "Utils";

	public static String toJson(Object obj) {
		return Config.GSON.toJson(obj);
	}

	public static <T>T fromJson(String json, Class<T> classOfT) {
		return Config.GSON.fromJson(json, classOfT);
	}

	public static String join(String[] array, String separator) {
		if (array == null) {
			return "";
		}
		StringBuilder response = new StringBuilder();
		for (String element : array) {
			response.append(element).append(separator);
		}
		return response.toString().replaceAll(separator + "$", "");
	}

	public static boolean arrayContains(String item, String[] array) {
		if (array == null) {
			return false;
		}
		for (String element : array) {
			if (element != null && element.equals(item)) {
				return true;
			}
		}
		return false;
	}

	public static <T> void fillList(List<T> list, T[] array) {
		list.removeAll(list);
		if (array == null) {
			return;
		}
		for (T t : array) {
			if (t != null) {
				list.add(t);
			}
		}
	}

	public static String getText(Activity activity, int id, String defaultValue) {
		String text = ((TextView) activity.findViewById(id)).getText().toString();
		if (text == null || "".equals(text)) {
			text = defaultValue;
		}
		return text;
	}

	public static String getText(View view, int id, String defaultValue) {
		String text = ((TextView) view.findViewById(id)).getText().toString();
		if (text == null || "".equals(text)) {
			text = defaultValue;
		}
		return text;
	}

	public static void setText(View view, int id, String value) {
		((TextView) view.findViewById(id)).setText(value);
	}

	public static void setText(Activity activity, int id, String value) {
		((TextView) activity.findViewById(id)).setText(value);
	}

	public static String getId(Object object) {
		try {
			return (String) object.getClass().getDeclaredField("id").get(object);
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		} catch (NoSuchFieldException e) {
		} catch (NullPointerException e) {
		}
		return null;
	}

	public static String getNextIdSuffix(String prefix, Object[] objects) {
		int size = 1;
		if (objects == null) {
			return Integer.toString(size);
		}
		for (Object object : objects) {
			String id = null;
			try {
				id = (String) object.getClass().getDeclaredField("id").get(object);
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (NoSuchFieldException e) {
			} catch (NullPointerException e) {
			}
			if (id != null && id.startsWith(prefix)) {
				size++;
			}
		}
		return Integer.toString(size);
	}

	public static String getPrefix(String id) {
		if (id == null) {
			return "";
		}
		return id.replaceAll("[0-9]*$", "");
	}

	public static Page getCopy(Page pastedObject, Object[] existing) {
		Page page = Config.GSON.fromJson(Config.GSON.toJson(pastedObject), Page.class);
		String prefix = getPrefix(page.id);
		page.id = prefix + getNextIdSuffix(prefix, existing);
		return page;
	}

	public static PageObject getCopy(PageObject pastedObject, Object[] existing) {
		PageObject object =
				Config.GSON.fromJson(Config.GSON.toJson(pastedObject), PageObject.class);
		String prefix = Utils.getPrefix(object.id);
		object.id = prefix + Utils.getNextIdSuffix(prefix, existing);
		if (object.animation != null) {
			for (Animation anim : object.animation) {
				String animPrefix = String.format("%s%s", object.id, anim.type);
				anim.id = animPrefix + getNextIdSuffix(animPrefix , object.animation);
			}
		}
		return object;
	}

	public static Animation getCopy(Animation pastedObject, Object[] existing) {
		Animation animation =
				Config.GSON.fromJson(Config.GSON.toJson(pastedObject), Animation.class);
		String prefix = getPrefix(animation.id);
		animation.id = prefix + Utils.getNextIdSuffix(prefix, existing);
		return animation;
	}

	public static Pair<Integer, Integer> getWidthHeightForRatio(float ratio,
			int originalWidth, int originalHeight) {
		if (ratio == 0) {
			return Pair.create(originalWidth, originalHeight);
		}
		int width = originalWidth;
		int height = originalHeight;
		float heightBasedWidth = originalHeight * ratio;
		float widthBasedHeight = originalWidth * (1/ ratio);
		if (heightBasedWidth < originalWidth) {
			width = (int) heightBasedWidth;
			height = originalHeight;
		} else {
			width = originalWidth;
			height = (int) widthBasedHeight;
		}
		return Pair.create(width, height);
	}

	public static byte[] getZippedData(byte[] unzippedData) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(baos));
		try {
			ZipEntry entry = new ZipEntry("data");
			zos.putNextEntry(entry);
			zos.write(unzippedData);
			zos.closeEntry();
			zos.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not zip the data.", e);
			return null;
		}
		return baos.toByteArray();
	}

	public static void showDelete(Context context,
			DialogInterface.OnClickListener positiveListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.are_you_sure);
		builder.setPositiveButton(R.string.delete, positiveListener);
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
		    		@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				dialog.cancel();
		    			}
				});
		builder.show();
	}
}
