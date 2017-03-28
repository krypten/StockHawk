package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Stock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class DetailActivity extends AppCompatActivity {
	private Stock mStock;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		final Intent intent = getIntent();
		// Stock Object
		if (intent != null) {
			mStock = intent.getParcelableExtra(Stock.PARCELABLE_ID);
		} else if (savedInstanceState != null && savedInstanceState.containsKey(Stock.PARCELABLE_ID)) {
			mStock = savedInstanceState.getParcelable(Stock.PARCELABLE_ID);
		}

		final ActionBar actionBar = getSupportActionBar();
		TextView textView = null;
		if (mStock != null) {
			textView = ((TextView) findViewById(R.id.detail_symbol_textview));
			textView.setText(mStock.getSymbol());
			textView.setContentDescription(getString(R.string.a11y_symbol, mStock.getSymbol()));
			textView = ((TextView) findViewById(R.id.detail_price_textview));
			textView.setText(mStock.getPrice());
			textView.setContentDescription(getString(R.string.a11y_price, mStock.getPrice()));
			textView = ((TextView) findViewById(R.id.detail_change_textview));
			textView.setText(mStock.getChange());
			textView.setContentDescription(getString(R.string.a11y_change, mStock.getChange()));

			findViewById(R.id.detail_header).setBackgroundColor(getResources().getColor(mStock.getStockColor()));
			createHistoryChart((LineChartView) findViewById(R.id.detail_chart), mStock);
		}
		// no title for action bar and also color of action bar showing the stock color.
		if (actionBar != null) {
			actionBar.setTitle("");
			actionBar.setElevation(0);
			actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(mStock.getStockColor())));
		}
	}

	private void createHistoryChart(final LineChartView chartView, final Stock stock) {
		final List<Stock.Quote> history = stock.getHistory();
		if (history != null) {
			final List<AxisValue> axisValuesX = new ArrayList<>();
			final List<PointValue> pointValues = new ArrayList<>();

			int counter = -1;
			do {
				counter++;
				final Stock.Quote quote = history.get(counter);
				final String date = new SimpleDateFormat("yyyy/MM/dd").format(quote.mTimeStamp.getTime());
				final String bidPrice = String.valueOf(quote.mClosingValue);

				// We have to show chart in right order.
				int x = history.size() - 1 - counter;

				// Point for line chart (date, price).
				final PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
				pointValue.setLabel(date);
				pointValues.add(pointValue);

				// Set labels for x-axis (we have to reduce its number to avoid overlapping text).
				if (counter % (history.size() / 3) == 0) {
					AxisValue axisValueX = new AxisValue(x);
					axisValueX.setLabel(date);
					axisValuesX.add(axisValueX);
				}
			} while (counter < (history.size() - 1));

			// Prepare data for chart
			final Line line = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
			List<Line> lines = new ArrayList<>();
			lines.add(line);
			LineChartData lineChartData = new LineChartData();
			lineChartData.setLines(lines);

			// Init x-axis
			final Axis axisX = new Axis(axisValuesX);
			axisX.setHasLines(true);
			axisX.setMaxLabelChars(4);
			lineChartData.setAxisXBottom(axisX);

			// Init y-axis
			final Axis axisY = new Axis();
			axisY.setAutoGenerated(true);
			axisY.setHasLines(true);
			axisY.setMaxLabelChars(4);
			lineChartData.setAxisYLeft(axisY);

			// Update chart with new data.
			chartView.setInteractive(true);
			chartView.setLineChartData(lineChartData);

			// Show chart
			chartView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(Stock.PARCELABLE_ID, mStock);
	}
}