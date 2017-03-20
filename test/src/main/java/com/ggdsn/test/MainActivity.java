package com.ggdsn.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import com.ggdsn.highlightspinner.HighlightSpinner;

public class MainActivity extends AppCompatActivity {

	private HighlightSpinner spinner;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		spinner = (HighlightSpinner) findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter =
			ArrayAdapter.createFromResource(this, R.array.spinners, android.R.layout.simple_spinner_dropdown_item);
		//spinner.setAdapter(adapter);
		spinner.setItems("sdf","dsf","ww");
	}
}
