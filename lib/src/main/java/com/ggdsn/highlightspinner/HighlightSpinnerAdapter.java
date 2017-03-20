package com.ggdsn.highlightspinner;

import android.content.Context;
import java.util.List;

/**
 * Created by LiaoXingyu on 20/03/2017.
 */

public class HighlightSpinnerAdapter<T> extends HighlightSpinnerBaseAdapter<T> {

	private final List<T> items;

	public HighlightSpinnerAdapter(Context context, List<T> items) {
		super(context);
		this.items = items;
	}

	@Override public T getItem(int position) {
		return items.get(position);
	}

	@Override public int getCount() {
		return items.size();
	}

	@Override public List<T> getItems() {
		return items;
	}
}
