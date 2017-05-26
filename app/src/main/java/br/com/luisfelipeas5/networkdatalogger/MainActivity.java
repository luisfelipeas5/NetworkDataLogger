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
import br.com.luisfelipeas5.networkdatalogger.comparators.ReceivedComparator;
import br.com.luisfelipeas5.networkdatalogger.comparators.TransmittedComparator;
import br.com.luisfelipeas5.networkdatalogger.databinding.ActivityMainBinding;
import br.com.luisfelipeas5.networkdatalogger.model.AppData;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ActivityMainBinding mBinding;
    private Toast mCurrentToast;
    private boolean mRefreshing;

    private List<AppData> mAppDataList; //a list of AppData objects that are in the RecyclerView
    private OrderBy mOrderBy = OrderBy.TRANSMITTED; //the order that tha AppData list is sorted

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

    /**
     * Prepare the data to list in a asynchronous task. Even if this work were heavy, the task won't freeze the screen
     */
    private void loadNetworkUsage() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setRefreshing(true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                loadAppDataList();
                sortAppDataList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setRefreshing(false);
                inflateViews();
            }
        }.execute();
    }

    private void inflateViews() {
        long transmittedTotalData = TrafficStats.getTotalTxBytes();
        mBinding.txtTotalTransmittedData.setText(getString(R.string.total_transmitted_data, transmittedTotalData));
        long receivedTotalData = TrafficStats.getTotalRxBytes();
        mBinding.txtTotalReceivedData.setText(getString(R.string.total_received_data, receivedTotalData));
        //Put in the data in the list
        AppDataAdapter appDataAdapter = new AppDataAdapter(mAppDataList);
        mBinding.recyclerView.setAdapter(appDataAdapter);
    }

    /**
     * Indicates the screen state
     * @param refreshing, if true, the data is been prepared to be shown, otherwise the data is already ready
     */
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

    /**
     * Setup a list of AppData objects by TrafficStats
     */
    private void loadAppDataList() {
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
    }

    /**
     *  Sort AppDataList by transmitted or received data by app
     */
    private void sortAppDataList() {
        Comparator<AppData> comparator;
        if (mOrderBy == OrderBy.TRANSMITTED) {
            comparator = new TransmittedComparator();
        } else {
            comparator = new ReceivedComparator();
        }

        Collections.sort(mAppDataList, comparator);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity, menu);

        MenuItem receivedOrderMenuItem = menu.findItem(R.id.action_received_order);
        receivedOrderMenuItem.setVisible(mOrderBy != OrderBy.RECEIVED);

        MenuItem transmittedOrderMenuItem = menu.findItem(R.id.action_transmitted_order);
        transmittedOrderMenuItem.setVisible(mOrderBy != OrderBy.TRANSMITTED);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_export:
                exportData();
                return true;

            case R.id.action_received_order:
                setOrderBy(OrderBy.RECEIVED);
                return true;

            case R.id.action_transmitted_order:
                setOrderBy(OrderBy.TRANSMITTED);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOrderBy(OrderBy orderBy) {
        mOrderBy = orderBy;
        sortAppDataList();
        inflateViews();
        invalidateOptionsMenu();
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

        StringBuilder textShareBuilder = new StringBuilder();

        textShareBuilder.append(getString(R.string.total_transmitted_data, TrafficStats.getTotalTxBytes())).append("\n")
                .append(getString(R.string.total_received_data, TrafficStats.getTotalRxBytes())).append("\n")
                .append("\n");

        String nameLabel = getString(R.string.name_label) + ": ";
        String transmittedLabel = getString(R.string.transmitted_label) + ": ";
        String receivedLabel = getString(R.string.received_label) + ": ";
        for (AppData appData : mAppDataList) {
            textShareBuilder
                    .append(nameLabel).append(appData.getName()).append("\n")
                    .append("\t").append(transmittedLabel).append(appData.getTransmitted()).append("\n")
                    .append("\t").append(receivedLabel).append(appData.getReceived()).append("\n")
                    .append("\n");
        }

        String textToShare = textShareBuilder.toString();

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

    private enum OrderBy {
        TRANSMITTED, RECEIVED
    }
}
