package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;

/**
 * Created by krypten on 3/18/17.
 */
public class StockHawkAppWidgetProvider extends AppWidgetProvider {
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (final int appWidgetId : appWidgetIds) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_list);

			// Set up the collection
			final Intent adapterIntent = new Intent(context, StockHawkAppWidgetIntentService.class);
			adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			views.setRemoteAdapter(R.id.appwidget_stock_list, adapterIntent);

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager,
	                                      final int appWidgetId, final Bundle newOptions) {
		context.startService(new Intent(context, StockHawkAppWidgetIntentService.class));
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);
		if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
			final AppWidgetManager manager = AppWidgetManager.getInstance(context);
			final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, getClass()));
			manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.appwidget_stock_list);
		}
	}
}
