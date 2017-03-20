package com.ggdsn.highlightspinner;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.ggdsn.lib.R;
import java.util.List;

/**
 * Created by LiaoXingyu on 20/03/2017.
 */

public abstract class HighlightSpinnerBaseAdapter<T> extends BaseAdapter {

	private final Context context;
	private final ColorStateList textColorStateList;
	private final int colorGray;
	private final int colorGold;
	private final int colorWhite;
	private int selectedIndex;
	private int textColor;

	public HighlightSpinnerBaseAdapter(Context context) {
		this.context = context;
		Resources resources = context.getResources();
		colorGray = resources.getColor(R.color.gray_f9);
		colorGold = resources.getColor(R.color.gold);
		colorWhite = resources.getColor(android.R.color.white);
		textColorStateList =
			new ColorStateList(new int[][] { new int[] { android.R.attr.state_enabled }, new int[] {} },
				new int[] { colorGold, Color.BLACK });
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		final AppCompatTextView textView;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.layout_list_item, parent, false);
			textView = (AppCompatTextView) convertView.findViewById(R.id.tv_tinted_spinner);
			textView.setTextColor(textColorStateList);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				Configuration config = context.getResources().getConfiguration();
				if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
					textView.setTextDirection(View.TEXT_DIRECTION_RTL);
				}
			}
			convertView.setTag(new ViewHolder(textView));
		} else {
			textView = ((ViewHolder) convertView.getTag()).textView;
		}
		T item = getItem(position);
		textView.setText(item.toString());

		if (position == selectedIndex) {
			textView.setEnabled(true);
			// FIXME: 20/03/2017 背景可以使用tint
			textView.setBackgroundColor(colorGray);
		} else {
			textView.setEnabled(false);
			textView.setBackgroundColor(colorWhite);
		}
		return convertView;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void notifyItemSelected(int index) {
		selectedIndex = index;
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public abstract T getItem(int position);

	public abstract List<T> getItems();

	public HighlightSpinnerBaseAdapter<T> setTextColor(int textColor) {
		this.textColor = textColor;
		return this;
	}

	private static class ViewHolder {

		private AppCompatTextView textView;

		private ViewHolder(AppCompatTextView textView) {
			this.textView = textView;
		}
	}
}
