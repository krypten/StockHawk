package com.udacity.stockhawk.sync;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {
	public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";

	private static final int ONE_OFF_ID = 2;
	private static final int PERIOD = 300000;
	private static final int INITIAL_BACKOFF = 10000;
	private static final int PERIODIC_ID = 1;
	private static final int YEARS_OF_HISTORY = 2;

	private QuoteSyncJob() {
	}

	public static void getQuotes(final Context context) {
		Timber.d("Running sync job");

		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

		try {
			final Set<String> stockPref = PrefUtils.getStocks(context);
			final Set<String> stockCopy = new HashSet<>();
			stockCopy.addAll(stockPref);
			Timber.d(stockCopy.toString());

			final String[] stockArray = stockPref.toArray(new String[stockPref.size()]);
			if (stockArray.length == 0) {
				return;
			}

			final Map<String, Stock> quotes = YahooFinance.get(stockArray);
			Timber.d(quotes.toString());

			final ArrayList<ContentValues> quoteCVs = new ArrayList<>();
			final Iterator<String> iterator = stockCopy.iterator();
			while (iterator.hasNext()) {
				final String symbol = iterator.next();
				final Stock stock = quotes.get(symbol);
				final StockQuote quote = stock.getQuote();
				if (stock != null && quote != null && quote.getPrice() != null) {
					float price = quote.getPrice().floatValue();
					float change = quote.getChange().floatValue();
					float percentChange = quote.getChangeInPercent().floatValue();

					// WARNING! Don't request historical data for a stock that doesn't exist!
					// The request will hang forever X_x
					final List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

					StringBuilder historyBuilder = new StringBuilder();
					for (HistoricalQuote it : history) {
						historyBuilder.append(it.getDate().getTimeInMillis());
						historyBuilder.append(", ");
						historyBuilder.append(it.getClose());
						historyBuilder.append("\n");
					}

					ContentValues quoteCV = new ContentValues();
					quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
					quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
					quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
					quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
					quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
					quoteCVs.add(quoteCV);
				}
			}

			context.getContentResolver()
					.bulkInsert(
							Contract.Quote.URI,
							quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

			final Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
			context.sendBroadcast(dataUpdatedIntent);
		} catch (IOException exception) {
			Timber.e(exception, "Error fetching stock quotes");
		}
	}

	@SuppressLint("NewApi")
	private static void schedulePeriodic(final Context context) {
		Timber.d("Scheduling a periodic task");
		JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));
		builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.setPeriodic(PERIOD)
				.setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

		JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
		scheduler.schedule(builder.build());
	}

	public static synchronized void initialize(final Context context) {
		schedulePeriodic(context);
		syncImmediately(context);
	}

	@SuppressLint("NewApi")
	public static synchronized void syncImmediately(final Context context) {
		final ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
			final Intent nowIntent = new Intent(context, QuoteIntentService.class);
			context.startService(nowIntent);
		} else {
			final JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));
			builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
					.setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

			final JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
			scheduler.schedule(builder.build());
		}
	}
}
