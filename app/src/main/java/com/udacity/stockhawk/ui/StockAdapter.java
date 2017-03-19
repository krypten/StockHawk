package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {
	private final Context context;
	private final DecimalFormat dollarFormatWithPlus;
	private final DecimalFormat dollarFormat;
	private final DecimalFormat percentageFormat;
	private Cursor cursor;
	private final StockAdapterOnClickHandler clickHandler;

	StockAdapter(final Context context, final StockAdapterOnClickHandler clickHandler) {
		this.context = context;
		this.clickHandler = clickHandler;

		dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
		dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
		dollarFormatWithPlus.setPositivePrefix("+$");
		percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
		percentageFormat.setMaximumFractionDigits(2);
		percentageFormat.setMinimumFractionDigits(2);
		percentageFormat.setPositivePrefix("+");
	}

	void setCursor(final Cursor cursor) {
		this.cursor = cursor;
		notifyDataSetChanged();
	}

	String getSymbolAtPosition(final int position) {
		cursor.moveToPosition(position);
		return cursor.getString(Contract.Quote.POSITION_SYMBOL);
	}

	String getPriceAtPosition(final int position) {
		cursor.moveToPosition(position);
		return dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE));
	}

	String getChangeAtPosition(final int position) {
		cursor.moveToPosition(position);
		return dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE));
	}

	boolean isProfitAtPosition(final int position) {
		cursor.moveToPosition(position);
		return (cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE) > 0);
	}

	String getHistoryAtPosition(final int position) {
		cursor.moveToPosition(position);
		return (cursor.getString(Contract.Quote.POSITION_HISTORY));
	}

	@Override
	public StockViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
		return new StockViewHolder(item);
	}

	@Override
	public void onBindViewHolder(final StockViewHolder holder, final int position) {
		cursor.moveToPosition(position);

		holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
		holder.symbol.setContentDescription(context.getString(R.string.a11y_symbol, holder.symbol.getText()));
		holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));
		holder.symbol.setContentDescription(context.getString(R.string.a11y_price, holder.price.getText()));

		float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
		float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

		if (rawAbsoluteChange > 0) {
			holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
		} else {
			holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
		}

		final String change = dollarFormatWithPlus.format(rawAbsoluteChange);
		final String percentage = percentageFormat.format(percentageChange / 100);
		if (PrefUtils.getDisplayMode(context)
				.equals(context.getString(R.string.pref_display_mode_absolute_key))) {
			holder.change.setText(change);
		} else {
			holder.change.setText(percentage);
		}
		holder.change.setContentDescription(context.getString(R.string.a11y_change, holder.change.getText()));
	}

	@Override
	public int getItemCount() {
		int count = 0;
		if (cursor != null) {
			count = cursor.getCount();
		}
		return count;
	}


	interface StockAdapterOnClickHandler {
		void onClick(int position);
	}

	class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		@BindView(R.id.symbol)
		TextView symbol;

		@BindView(R.id.price)
		TextView price;

		@BindView(R.id.change)
		TextView change;

		StockViewHolder(final View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(final View v) {
			clickHandler.onClick(getAdapterPosition());
		}
	}
}