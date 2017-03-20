package com.ggdsn.highlightspinner;

import android.content.Context;
import android.widget.ListAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiaoXingyu on 20/03/2017.
 */

class HighlightSpinnerAdapterWrapper extends HighlightSpinnerBaseAdapter {

	private final ListAdapter adapter;

	public HighlightSpinnerAdapterWrapper(Context context, ListAdapter adapter) {
		super(context);
		this.adapter = adapter;
	}

	@Override public Object getItem(int position) {
		return adapter.getItem(position);
	}

	@Override public int getCount() {
		return adapter.getCount();
	}

	@Override public List getItems() {
		List<Object> items = new ArrayList<>();
		for (int i = 0; i < getCount(); i++) {
			items.add(getItem(i));
		}
		return items;
	}
}
