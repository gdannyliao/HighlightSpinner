package com.ggdsn.test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import com.ggdsn.highlightspinner.HighlightSpinner;
import com.ggdsn.highlightspinner.HighlightSpinnerBaseAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private HighlightSpinner spinner;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		spinner = (HighlightSpinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter =
			ArrayAdapter.createFromResource(this, R.array.spinners, android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setDropdownWidth(500);

		HighlightSpinner spinner1 = (HighlightSpinner) findViewById(R.id.spinner1);
		spinner1.setItems("sdf", "dsf", "ww");

		HighlightSpinner spinner2 = (HighlightSpinner) findViewById(R.id.spinner2);
		spinner2.setAdapter(new RegionAdapter(this, RegionAdapter.getList()));

		HighlightSpinner spinnerWrapContent = (HighlightSpinner) findViewById(R.id.spinnerWrapContent);
		spinnerWrapContent.setAdapter(new RegionAdapter(this, RegionAdapter.getList()));
	}

	private static class RegionAdapter extends HighlightSpinnerBaseAdapter<Region> {

		private static final String TAG = "fuck";
		private final List<Region> regions;

		public RegionAdapter(Context context, List<Region> regions) {
			super(context);
			this.regions = regions;
		}

		@Override public int getCount() {
			return regions.size();
		}

		@Override public Region getItem(int i) {
			return regions.get(i);
		}

		@Override public List<Region> getItems() {
			return regions;
		}

		@Override public String toDisplayString(Region item) {
			return item.getName();
		}

		public static List<Region> getList() {
			ArrayList<Region> res = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				res.add(new Region(String.valueOf(i + 'A'), i));
			}
			return res;
		}
	}
}
