package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.data.Stock;
import com.udacity.stockhawk.ui.DetailActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by krypten on 3/18/17.
 */
public class StockHawkAppWidgetIntentService extends RemoteViewsService {
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new RemoteViewsFactory() {
			private Cursor mData = null;

			@Override
			public void onCreate() {
				// Nothing to do
			}

			@Override
			public void onDataSetChanged() {
				if (mData != null) {
					mData.close();
				}
				// This method is called by the app hosting the widget (e.g., the launcher)
				// However, our ContentProvider is not exported so it doesn't have access to the
				// mData. Therefore we need to clear (and finally restore) the calling identity so
				// that calls use our process and permission
				final long identityToken = Binder.clearCallingIdentity();

				Set<String> stockPref = PrefUtils.getStocks(getApplicationContext());
				Set<String> stockCopy = new HashSet<>();
				stockCopy.addAll(stockPref);
				String[] stockArray = stockPref.toArray(new String[stockPref.size()]);
				mData = getContentResolver().query(Contract.Quote.makeUriForStock(stockArray[0]),
						(String[]) Contract.Quote.QUOTE_COLUMNS.toArray(),
						null,
						null,
						null);

				Binder.restoreCallingIdentity(identityToken);
			}

			@Override
			public void onDestroy() {
				if (mData != null) {
					mData.close();
					mData = null;
				}
			}

			@Override
			public int getCount() {
				return mData == null ? 0 : mData.getCount();
			}

			@Override
			public RemoteViews getViewAt(int position) {
				if (mData == null || !mData.moveToPosition(position)) {
					return null;
				}
				RemoteViews views = new RemoteViews(getPackageName(),
						R.layout.list_item_quote);
				final String symbol = mData.getString(Contract.Quote.POSITION_SYMBOL);
				final String price = mData.getString(Contract.Quote.POSITION_PRICE);
				final String change = mData.getString(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
				views.setTextViewText(R.id.symbol, symbol);
				views.setTextViewText(R.id.price, price);
				views.setTextViewText(R.id.change, change);

				final Intent fillInIntent = new Intent(getApplicationContext(), DetailActivity.class);
				fillInIntent.putExtra(Stock.PARCELABLE_ID, new Stock(symbol, "", price, change, Float.parseFloat(change) > 0));
				views.setOnClickFillInIntent(R.id.list_item, fillInIntent);
				return views;
			}

			@Override
			public RemoteViews getLoadingView() {
				return new RemoteViews(getPackageName(), R.layout.list_item_quote);
			}

			@Override
			public int getViewTypeCount() {
				return 1;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public boolean hasStableIds() {
				return true;
			}
		};
	}

	;
}
