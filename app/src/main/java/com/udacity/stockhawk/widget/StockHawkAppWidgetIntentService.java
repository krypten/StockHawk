package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.Stock;
import com.udacity.stockhawk.ui.DetailActivity;

/**
 * Created by krypten on 3/18/17.
 */
public class StockHawkAppWidgetIntentService extends RemoteViewsService {
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new RemoteViewsFactory() {
			private Cursor mData = null;
			private Context mContext = getApplicationContext();

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
/*
				Set<String> stockPref = PrefUtils.getStocks(mContext);
				Set<String> stockCopy = new HashSet<>();
				stockCopy.addAll(stockPref);

				String[] stockArray = stockPref.toArray(new String[stockPref.size()]);
				*/
				mData = mContext.getContentResolver().query(Contract.Quote.URI,
						Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
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
				final Stock stock = new Stock(
						mData.getString(mData.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)),
						"",
						mData.getString(mData.getColumnIndex(Contract.Quote.COLUMN_PRICE)),
						mData.getString(mData.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)),
						Float.parseFloat(mData.getString(mData.getColumnIndex(Contract.Quote.COLUMN_ABSOLUTE_CHANGE))) > 0);
				views.setTextViewText(R.id.symbol, stock.getSymbol());
				views.setTextViewText(R.id.price, stock.getPrice());
				views.setTextViewText(R.id.change, stock.getChange());
				views.setTextColor(R.id.change, mContext.getResources().getColor(stock.getStockColor()));

				final Intent fillInIntent = new Intent(mContext, DetailActivity.class);
				fillInIntent.putExtra(Stock.PARCELABLE_ID, stock);
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
}
