package com.udacity.stockhawk.sync;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

import timber.log.Timber;

@SuppressLint("NewApi")
public class QuoteJobService extends JobService {
	@Override
	public boolean onStartJob(final JobParameters jobParameters) {
		Timber.d("Intent handled");
		Intent nowIntent = new Intent(getApplicationContext(), QuoteIntentService.class);
		getApplicationContext().startService(nowIntent);
		return true;
	}

	@Override
	public boolean onStopJob(final JobParameters jobParameters) {
		return false;
	}


}
