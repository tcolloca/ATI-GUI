package com.itba.atigui.async;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

public class BackgroundTask extends AsyncTask<Void, Void, Void> {

    private Callback callback;

    public interface Callback {
        void onBackground();
        void onUiThread();
    }

    public BackgroundTask(@NonNull Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.onUiThread();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        callback.onBackground();
        return null;
    }
}
