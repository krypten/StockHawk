package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
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
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by krypten on 3/18/17.
 */
public class StockHawkAppWidgetProvider extends AppWidgetProvider {
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onUpdate(final Context context, final AppWidgetManager manager, int[] appWidgetIds) {
		for (final int appWidgetId : appWidgetIds) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_list);

			// Create an Intent to launch MainActivity
			final Intent intent = new Intent(context, MainActivity.class);
			final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.widget_stock_list, pendingIntent);

			// Set up the collection
			views.setRemoteAdapter(R.id.widget_stock_list,
					new Intent(context, StockHawkAppWidgetIntentService.class));


			PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
					.addNextIntentWithParentStack(new Intent(context, MainActivity.class))
					.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setPendingIntentTemplate(R.id.widget_stock_list, clickPendingIntentTemplate);

			// Tell the AppWidgetManager to perform an update on the current app widget
			manager.updateAppWidget(appWidgetId, views);
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
			manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_stock_list);
		}
	}
}
