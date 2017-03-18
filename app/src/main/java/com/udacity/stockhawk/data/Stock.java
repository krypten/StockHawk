package com.udacity.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

/**
 * Created by krypten on 3/17/17.
 */
public class Stock implements Parcelable {
	public static final String PARCELABLE_ID = "Stock";
	private String mSymbol;
	private String mPrice;
	private String mChange;
	private String mHistory;

	private boolean mIsProfit;

	private Stock(final Parcel in) {
		mSymbol = in.readString();
		mHistory = in.readString();
		mPrice = in.readString();
		mChange = in.readString();
		mIsProfit = in.readInt() > 0;
	}

	public Stock(final String symbol, final String history, final String price, final String change, final boolean isProfit) {
		mSymbol = symbol;
		mHistory = history;
		mPrice = price;
		mChange = change;
		mIsProfit = isProfit;
		Timber.d("Symbol clicked: %s", symbol);
		Timber.d("price clicked: %s", price);
		Timber.d("change clicked: %s", change);
		Timber.d("isProfit clicked: %s", isProfit);
		Timber.d("history clicked: %s", history);
	}

	public boolean isProfit() {
		return mIsProfit;
	}

	public String getChange() {
		return mChange;
	}

	public String getPrice() {
		return mPrice;
	}

	public String getSymbol() {
		return mSymbol;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel parcel, final int flags) {
		parcel.writeString(mSymbol);
		parcel.writeString(mHistory);
		parcel.writeString(mPrice);
		parcel.writeString(mChange);
		parcel.writeInt(mIsProfit ? 1 : 0);
	}

	public List<Quote> getHistory() {
		return convertHistoryToList(mHistory);
	}

	private List<Quote> convertHistoryToList(final String mHistory) {
		List<Quote> quotes = null;
		if (mHistory != null) {
			quotes = new ArrayList<>();
			final String[] values = mHistory.split("\n");
			for (final String value : values) {
				final String[] row = value.split(",");
				quotes.add(new Quote(row[0], row[1]));
			}
		}
		return quotes;
	}

	// After implementing the `Parcelable` interface, we need to create the
	// `Parcelable.Creator<Stock> CREATOR` constant for our class;
	public static final Parcelable.Creator<Stock> CREATOR = new Parcelable.Creator<Stock>() {
		// This simply calls our new constructor (typically private) and
		// passes along the unmarshalled `Parcel`, and then returns the new object!
		@Override
		public Stock createFromParcel(final Parcel in) {
			return new Stock(in);
		}

		// We just need to copy this and change the type to match our class.
		@Override
		public Stock[] newArray(final int size) {
			return new Stock[size];
		}
	};

	public class Quote {
		public Calendar mTimeStamp;
		public float mClosingValue;

		public Quote(final String time, final String closingValue) {
			mTimeStamp = Calendar.getInstance();
			mTimeStamp.setTimeInMillis(Long.valueOf(time));
			mClosingValue = Float.parseFloat(closingValue.trim());
		}
	}
}
