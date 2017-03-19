package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

	private PrefUtils() {
	}

	public static Set<String> getStocks(final Context context) {
		final String stocksKey = context.getString(R.string.pref_stocks_key);
		final String initializedKey = context.getString(R.string.pref_stocks_initialized_key);
		final String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);

		final HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		final boolean initialized = prefs.getBoolean(initializedKey, false);
		if (!initialized) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(initializedKey, true);
			editor.putStringSet(stocksKey, defaultStocks);
			editor.apply();
			return defaultStocks;
		}
		return prefs.getStringSet(stocksKey, new HashSet<String>());
	}

	private static void editStockPref(final Context context, final String symbol, final Boolean add) {
		final String key = context.getString(R.string.pref_stocks_key);
		final Set<String> stocks = getStocks(context);

		if (add) {
			stocks.add(symbol);
		} else {
			stocks.remove(symbol);
		}

		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putStringSet(key, stocks);
		editor.apply();
	}

	public static void addStock(final Context context, final String symbol) {
		editStockPref(context, symbol, true);
	}

	public static void removeStock(final Context context, final String symbol) {
		editStockPref(context, symbol, false);
	}

	public static String getDisplayMode(final Context context) {
		final String key = context.getString(R.string.pref_display_mode_key);
		final String defaultValue = context.getString(R.string.pref_display_mode_default);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(key, defaultValue);
	}

	public static void toggleDisplayMode(Context context) {
		String key = context.getString(R.string.pref_display_mode_key);
		String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
		String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

		String displayMode = getDisplayMode(context);
		if (displayMode.equals(absoluteKey)) {
			editor.putString(key, percentageKey);
		} else {
			editor.putString(key, absoluteKey);
		}
		editor.apply();
	}
}
