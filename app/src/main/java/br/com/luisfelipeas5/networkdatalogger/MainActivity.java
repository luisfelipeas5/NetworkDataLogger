package br.com.luisfelipeas5.networkdatalogger;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.databinding.DataBindingUtil;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import br.com.luisfelipeas5.networkdatalogger.adapter.AppDataAdapter;
import br.com.luisfelipeas5.networkdatalogger.databinding.ActivityMainBinding;
import br.com.luisfelipeas5.networkdatalogger.model.AppData;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityMainBinding mBinding;
    private List<AppData> mAppDataList;
    private Toast mCurrentToast;
    private boolean mRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mBinding.recyclerView.setLayoutManager(layoutManager);

        mBinding.swipeRefreshLayout.setOnRefreshListener(this);
        mBinding.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);

        loadNetworkUsage();
    }

    private void loadNetworkUsage() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setRefreshing(true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                setUpAppDataList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setRefreshing(false);
                AppDataAdapter appDataAdapter = new AppDataAdapter(mAppDataList);
                mBinding.recyclerView.setAdapter(appDataAdapter);
            }
        }.execute();
    }

    private void setRefreshing(final boolean refreshing) {
        mRefreshing = refreshing;
        mBinding.swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (refreshing && mRefreshing) {
                    mBinding.swipeRefreshLayout.setRefreshing(true);
                } else {
                    mBinding.swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private void setUpAppDataList() {
        List<ApplicationInfo> installedApplications = getPackageManager().getInstalledApplications(0);
        mAppDataList = new LinkedList<>();
        for (ApplicationInfo applicationInfo : installedApplications) {
            String name = applicationInfo.className;
            if (!TextUtils.isEmpty(name)) {
                long tx = TrafficStats.getUidTxBytes(applicationInfo.uid);
                long rx = TrafficStats.getUidRxBytes(applicationInfo.uid);

                AppData appData = new AppData(name, tx, rx);
                mAppDataList.add(appData);
            }
        }
        Collections.sort(mAppDataList, new Comparator<AppData>() {
            @Override
            public int compare(AppData o1, AppData o2) {
                long transmitted1 = o1.getTransmitted();
                long transmitted2 = o2.getTransmitted();
                if (transmitted1 > transmitted2) {
                    return -1;
                } else if (transmitted1 < transmitted2) {
                    return 1;
                }
                return 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_export:
                exportData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportData() {
        if (mAppDataList == null || mAppDataList.isEmpty()) {
            if (mCurrentToast != null) {
                mCurrentToast.cancel();
            }
            mCurrentToast = Toast.makeText(this, R.string.no_data_to_export, Toast.LENGTH_SHORT);
            mCurrentToast.show();
            return;
        }

        String nameLabel = getString(R.string.name_label) + ": ";
        String transmittedLabel = getString(R.string.transmitted_label) + ": ";
        String receivedLabel = getString(R.string.received_label) + ": ";

        StringBuilder stringBuilder = new StringBuilder();
        for (AppData appData : mAppDataList) {
            stringBuilder
                    .append(nameLabel).append(appData.getName()).append("\n")
                    .append("\t").append(transmittedLabel).append(appData.getTransmitted()).append("\n")
                    .append("\t").append(receivedLabel).append(appData.getReceived()).append("\n")
                    .append("\n");
        }
        String textToShare = stringBuilder.toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        sendIntent.setType("text/plain");
        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(sendIntent);
        }
    }

    @Override
    public void onRefresh() {
        loadNetworkUsage();
    }
}
