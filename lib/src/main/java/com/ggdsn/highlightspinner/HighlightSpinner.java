package com.ggdsn.highlightspinner;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import com.ggdsn.lib.R;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LiaoXingyu on 20/03/2017.
 */
public class HighlightSpinner extends AppCompatTextView {
	public static final int MATCH_SPINNER = -3;
	private OnNothingSelectedListener onNothingSelectedListener;
	private OnItemSelectedListener onItemSelectedListener;
	private HighlightSpinnerBaseAdapter adapter;
	private PopupWindow popupWindow;
	private ListView listView;
	private Drawable arrowDrawable;
	private boolean hideArrow;
	private boolean nothingSelected;
	private int popupWindowMaxHeight;
	private int popupWindowHeight;
	private int selectedIndex;
	private int backgroundColor;
	private int arrowColor;
	private int arrowColorDisabled;
	private int textColor;
	private int numberOfItems;
	private int popupWindowWidthType;
	private int popupWindowWidthPx;

	private OnDisplayChangeListener onDisplayChangeListener;

	public HighlightSpinner(Context context) {
		super(context);
		init(context, null);
	}

	public HighlightSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public HighlightSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HighlightSpinner);
		int defaultColor = getTextColors().getDefaultColor();
		boolean rtl = Utils.isRtl(context);

		try {
			backgroundColor = ta.getColor(R.styleable.HighlightSpinner_highlight_spinner_background_color, Color.WHITE);
			textColor = ta.getColor(R.styleable.HighlightSpinner_highlight_spinner_text_color, defaultColor);
			arrowColor = ta.getColor(R.styleable.HighlightSpinner_highlight_spinner_arrow_tint, textColor);
			hideArrow = ta.getBoolean(R.styleable.HighlightSpinner_highlight_spinner_hide_arrow, false);
			popupWindowMaxHeight =
				ta.getDimensionPixelSize(R.styleable.HighlightSpinner_highlight_spinner_dropdown_max_height, 0);
			popupWindowHeight = ta.getLayoutDimension(R.styleable.HighlightSpinner_highlight_spinner_dropdown_height,
				WindowManager.LayoutParams.WRAP_CONTENT);
			popupWindowWidthType =
				ta.getLayoutDimension(R.styleable.HighlightSpinner_highlight_spinner_dropdown_width, MATCH_SPINNER);
			arrowColorDisabled = Utils.lighter(arrowColor, 0.8f);
		} finally {
			ta.recycle();
		}

		Resources resources = getResources();
		int left, right, bottom, top;
		left = right = bottom = top = resources.getDimensionPixelSize(R.dimen.highlight_spinner__padding_top);
		if (rtl) {
			right = resources.getDimensionPixelSize(R.dimen.highlight_spinner__padding_left);
		} else {
			left = resources.getDimensionPixelSize(R.dimen.highlight_spinner__padding_left);
		}

		setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
		setClickable(true);
		setPadding(left, top, right, bottom);
		setBackgroundResource(R.drawable.highlight_spinner_selector);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && rtl) {
			setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
			setTextDirection(View.TEXT_DIRECTION_RTL);
		}

		if (!hideArrow) {
			arrowDrawable = Utils.getDrawable(context, R.drawable.highlight_spinner_arrow).mutate();
			arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN);
			if (rtl) {
				setCompoundDrawablesWithIntrinsicBounds(arrowDrawable, null, null, null);
			} else {
				setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);
			}
		}

		listView = new ListView(context);
		listView.setId(getId());
		listView.setDivider(null);
		listView.setItemsCanFocus(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedIndex = position;
				nothingSelected = false;
				Object item = adapter.getItem(position);
				adapter.notifyItemSelected(position);
				@SuppressWarnings("unchecked") String toDisplayString = adapter.toDisplayString(item);
				setText(toDisplayString);
				collapse();
				if (onItemSelectedListener != null) {
					//noinspection unchecked
					onItemSelectedListener.onItemSelected(HighlightSpinner.this, position, id, item);
				}
			}
		});

		popupWindow = new PopupWindow(context);
		popupWindow.setContentView(listView);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setFocusable(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			popupWindow.setElevation(16);
			popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.highlight_spinner_drawable));
		} else {
			popupWindow.setBackgroundDrawable(
				Utils.getDrawable(context, R.drawable.highlight_spinner_drop_down_shadow));
		}

		if (backgroundColor != Color.WHITE) { // default color is white
			setBackgroundColor(backgroundColor);
		}
		if (textColor != defaultColor) {
			setTextColor(textColor);
		}

		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			@Override public void onDismiss() {
				if (nothingSelected && onNothingSelectedListener != null) {
					onNothingSelectedListener.onNothingSelected(HighlightSpinner.this);
				}
				if (!hideArrow) {
					animateArrow(false);
				}
				if (onDisplayChangeListener != null) {
					onDisplayChangeListener.onDisplayChanged(false);
				}
			}
		});
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (popupWindowWidthType == MATCH_SPINNER) {
			popupWindowWidthPx = getMeasuredWidth();
		}
		popupWindow.setWidth(calculatePopupWindowWidth());
		popupWindow.setHeight(calculatePopupWindowHeight());
	}

	@Override public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (isEnabled() && isClickable()) {
				if (!popupWindow.isShowing()) {
					expand();
				} else {
					collapse();
				}
			}
		}
		return super.onTouchEvent(event);
	}

	@Override public void setBackgroundColor(int color) {
		// FIXME: 20/03/2017 不需要背景和列表项背景一起变化
		backgroundColor = color;
		Drawable background = getBackground();
		if (background instanceof StateListDrawable) { // pre-L
			try {
				Method getStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
				if (!getStateDrawable.isAccessible()) getStateDrawable.setAccessible(true);
				int[] colors = { Utils.darker(color, 0.85f), color };
				for (int i = 0; i < colors.length; i++) {
					ColorDrawable drawable = (ColorDrawable) getStateDrawable.invoke(background, i);
					drawable.setColor(colors[i]);
				}
			} catch (Exception e) {
				Log.e("MaterialSpinner", "Error setting background color", e);
			}
		} else if (background != null) { // 21+ (RippleDrawable)
			background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
		}
		popupWindow.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
	}

	@Override public void setTextColor(int color) {
		// FIXME: 20/03/2017 所有元素的背景色都会改变，是否需要？
		textColor = color;
		super.setTextColor(color);
	}

	@Override public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("state", super.onSaveInstanceState());
		bundle.putInt("selected_index", selectedIndex);
		if (popupWindow != null) {
			bundle.putBoolean("is_popup_showing", popupWindow.isShowing());
			collapse();
		} else {
			bundle.putBoolean("is_popup_showing", false);
		}
		return bundle;
	}

	@Override public void onRestoreInstanceState(Parcelable savedState) {
		if (savedState instanceof Bundle) {
			Bundle bundle = (Bundle) savedState;
			selectedIndex = bundle.getInt("selected_index");
			if (adapter != null) {
				setText(adapter.getItem(selectedIndex).toString());
				adapter.notifyItemSelected(selectedIndex);
			}
			if (bundle.getBoolean("is_popup_showing")) {
				if (popupWindow != null) {
					// Post the show request into the looper to avoid bad token exception
					post(new Runnable() {

						@Override public void run() {
							expand();
						}
					});
				}
			}
			savedState = bundle.getParcelable("state");
		}
		super.onRestoreInstanceState(savedState);
	}

	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (arrowDrawable != null) {
			arrowDrawable.setColorFilter(enabled ? arrowColor : arrowColorDisabled, PorterDuff.Mode.SRC_IN);
		}
	}

	/**
	 * @return the selected item position
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * Set the default spinner item using its index
	 *
	 * @param position the item's position
	 */
	public void setSelectedIndex(int position) {
		if (adapter != null) {
			if (position >= 0 && position <= adapter.getCount()) {
				adapter.notifyItemSelected(position);
				selectedIndex = position;
				setText(adapter.toDisplayString(adapter.getItem(position)));
			} else {
				throw new IllegalArgumentException("Position must be lower than adapter count!");
			}
		}
	}

	/**
	 * Register a callback to be invoked when an item in the dropdown is selected.
	 *
	 * @param onItemSelectedListener The callback that will run
	 */
	public void setOnItemSelectedListener(@Nullable OnItemSelectedListener onItemSelectedListener) {
		this.onItemSelectedListener = onItemSelectedListener;
	}

	/**
	 * Register a callback to be invoked when the {@link PopupWindow} is shown but the user didn't select an item.
	 *
	 * @param onNothingSelectedListener the callback that will run
	 */
	public void setOnNothingSelectedListener(@Nullable OnNothingSelectedListener onNothingSelectedListener) {
		this.onNothingSelectedListener = onNothingSelectedListener;
	}

	/**
	 * Set the dropdown items
	 *
	 * @param items A list of items
	 * @param <T> The item type
	 */
	public <T> void setItems(@NonNull List<T> items) {
		numberOfItems = items.size();
		adapter = new HighlightSpinnerAdapter<>(getContext(), items).setTextColor(textColor);
		setAdapterInternal(adapter);
	}

	/**
	 * Set the dropdown items
	 *
	 * @param items A list of items
	 * @param <T> The item type
	 */
	public <T> void setItems(@NonNull T... items) {
		setItems(Arrays.asList(items));
	}

	/**
	 * Get the list of items in the adapter
	 *
	 * @param <T> The item type
	 * @return A list of items or {@code null} if no items are set.
	 */
	public <T> List<T> getItems() {
		if (adapter == null) {
			return null;
		}
		//noinspection unchecked
		return adapter.getItems();
	}

	/**
	 * Set a custom adapter for the dropdown items
	 *
	 * @param adapter The list adapter
	 */
	public void setAdapter(@NonNull ListAdapter adapter) {
		this.adapter = new HighlightSpinnerAdapterWrapper(getContext(), adapter);
		setAdapterInternal(this.adapter);
	}

	public <T> void setAdapter(HighlightSpinnerBaseAdapter<T> adapter) {
		// FIXME: 20/03/2017 列表一片空白
		this.adapter = adapter;
		setAdapterInternal(adapter);
	}

	private <T> void setAdapterInternal(@NonNull HighlightSpinnerBaseAdapter<T> adapter) {
		listView.setAdapter(adapter);
		if (selectedIndex >= numberOfItems) {
			selectedIndex = 0;
		}
		setText(adapter.toDisplayString(adapter.getItem(selectedIndex)));
	}

	/**
	 * Show the dropdown menu
	 */
	public void expand() {
		if (!hideArrow) {
			animateArrow(true);
		}
		nothingSelected = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			popupWindow.setOverlapAnchor(false);
			popupWindow.showAsDropDown(this);
		} else {
			int[] location = new int[2];
			getLocationOnScreen(location);
			int x = location[0];
			int y = getHeight() + location[1];
			popupWindow.showAtLocation(this, Gravity.TOP | Gravity.START, x, y);
		}
		if (onDisplayChangeListener != null) {
			onDisplayChangeListener.onDisplayChanged(true);
		}
	}

	/**
	 * Closes the dropdown menu
	 */
	public void collapse() {
		if (!hideArrow) {
			animateArrow(false);
		}
		popupWindow.dismiss();
	}

	/**
	 * Set the tint color for the dropdown arrow
	 *
	 * @param color the color value
	 */
	public void setArrowColor(@ColorInt int color) {
		arrowColor = color;
		arrowColorDisabled = Utils.lighter(arrowColor, 0.8f);
		if (arrowDrawable != null) {
			arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN);
		}
	}

	private void animateArrow(boolean shouldRotateUp) {
		int start = shouldRotateUp ? 0 : 10000;
		int end = shouldRotateUp ? 10000 : 0;
		ObjectAnimator animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
		animator.start();
	}

	/**
	 * Set the maximum height of the dropdown menu.
	 *
	 * @param height the height in pixels
	 */
	public void setDropdownMaxHeight(int height) {
		popupWindowMaxHeight = height;
		popupWindow.setHeight(calculatePopupWindowHeight());
	}

	/**
	 * Set the height of the dropdown menu
	 *
	 * @param height the height in pixels
	 */
	public void setDropdownHeight(int height) {
		popupWindowHeight = height;
		popupWindow.setHeight(calculatePopupWindowHeight());
	}

	public void setDropdownWidth(int width) {
		popupWindowWidthType = width;
		popupWindow.setWidth(calculatePopupWindowWidth());
	}

	private int calculatePopupWindowWidth() {
		if (adapter == null) {
			return popupWindowWidthPx != 0 ? popupWindowWidthPx : WindowManager.LayoutParams.WRAP_CONTENT;
		}
		if (popupWindowWidthType == WindowManager.LayoutParams.MATCH_PARENT) {
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			return size.x;
		} else if (popupWindowWidthType == WindowManager.LayoutParams.WRAP_CONTENT) {
			// FIXME: 05/05/2017 warp content会根据spinner的宽度设置，而不是popup内容的宽度
			return WindowManager.LayoutParams.WRAP_CONTENT;
		} else {
			return popupWindowWidthPx;
		}
	}

	private int calculatePopupWindowHeight() {
		if (adapter == null) {
			return WindowManager.LayoutParams.WRAP_CONTENT;
		}
		float listViewHeight = adapter.getCount() * getResources().getDimension(R.dimen.highlight_spinner__item_height);
		if (popupWindowMaxHeight > 0 && listViewHeight > popupWindowMaxHeight) {
			return popupWindowMaxHeight;
		} else if (popupWindowHeight != WindowManager.LayoutParams.MATCH_PARENT
			&& popupWindowHeight != WindowManager.LayoutParams.WRAP_CONTENT
			&& popupWindowHeight <= listViewHeight) {
			return popupWindowHeight;
		}
		return WindowManager.LayoutParams.WRAP_CONTENT;
	}

	/**
	 * Get the {@link PopupWindow}.
	 *
	 * @return The {@link PopupWindow} that is displayed when the view has been clicked.
	 */
	public PopupWindow getPopupWindow() {
		return popupWindow;
	}

	public void setOnDisplayChangeListener(OnDisplayChangeListener listener) {
		onDisplayChangeListener = listener;
	}

	/**
	 * a callback to be invoked when PopupWindow displayed or dismissed.
	 */
	public interface OnDisplayChangeListener {
		void onDisplayChanged(boolean displayed);
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this view has been selected.
	 *
	 * @param <T> Adapter item type
	 */
	public interface OnItemSelectedListener<T> {

		/**
		 * <p>Callback method to be invoked when an item in this view has been selected. This callback is invoked only
		 * when
		 * the newly selected position is different from the previously selected position or if there was no selected
		 * item.</p>
		 *
		 * @param view The {@link HighlightSpinner} view
		 * @param position The position of the view in the adapter
		 * @param id The row id of the item that is selected
		 * @param item The selected item
		 */
		void onItemSelected(HighlightSpinner view, int position, long id, T item);
	}

	/**
	 * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
	 */
	public interface OnNothingSelectedListener {

		/**
		 * Callback method to be invoked when the {@link PopupWindow} is dismissed and no item was selected.
		 *
		 * @param spinner the {@link HighlightSpinner}
		 */
		void onNothingSelected(HighlightSpinner spinner);
	}
}
