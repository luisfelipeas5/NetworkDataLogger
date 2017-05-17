package br.com.luisfelipeas5.networkdatalogger;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logNetworkUsage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logNetworkUsage();
                }
                break;
        }

    }

    private void logNetworkUsage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean readPhoneStateGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED;
            boolean writeExternalGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            boolean readExternalGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            if (!readPhoneStateGranted || !writeExternalGranted || !readExternalGranted) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSIONS);
            } else {
                File filesDir = getFilesDir();

                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault());
                File file = new File(filesDir, "report" + simpleDateFormatter.format(date) + ".txt");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    List<ApplicationInfo> installedApplications = getPackageManager().getInstalledApplications(0);
                    for(ApplicationInfo app : installedApplications){
                        String name = app.className;
                        long tx = TrafficStats.getUidTxBytes(app.uid);
                        long rx = TrafficStats.getUidRxBytes(app.uid);

                        String output = name + ": total transmitted = " + tx + "; total received = " +
                                rx;
                        fileOutputStream.write(output.getBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
