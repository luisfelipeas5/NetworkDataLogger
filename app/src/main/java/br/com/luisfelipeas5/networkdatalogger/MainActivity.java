package br.com.luisfelipeas5.networkdatalogger;

import android.Manifest;
import android.app.AlertDialog;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showDialogPermissionExplanation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    queryNetworkStats();
                }
                break;
        }

    }

    private void queryNetworkStats() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean readPhoneStateNotGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED;
            if (readPhoneStateNotGranted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PERMISSIONS);
            } else {
                NetworkStatsManager service = getSystemService(NetworkStatsManager.class);

                Calendar fromCalendar = Calendar.getInstance(Locale.getDefault());
                fromCalendar.set(2017, 0, 1);
                long from = fromCalendar.getTimeInMillis();

                Calendar toCalendar = Calendar.getInstance(Locale.getDefault());
                toCalendar.set(2017, 5, 1);
                long to = toCalendar.getTimeInMillis();

                try {
                    NetworkStats.Bucket bucket = service.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, from, to);
                    long rxBytes = bucket.getRxBytes();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showDialogPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.package_usage_stats_espectial_permission);
        builder.setPositiveButton(R.string.oh_nice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            }
        });
        builder.setNeutralButton(R.string.not_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                queryNetworkStats();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                queryNetworkStats();
            }
        });
        builder.show();
    }
}
