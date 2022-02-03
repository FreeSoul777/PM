package com.mygame.music_alpha;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import static com.mygame.music_alpha.MusicService.IS_LIVE;
import static com.mygame.music_alpha.MusicService.PP;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private static final String TAG = "MusicActivity";
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_2 = 2;

    public static boolean PER_WRITE_EXTERNAL_STORAGE = false;
    public static boolean PER_RECORD_AUDIO = false;
    private ImageView icon_note;

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_LIVE = false;
        setContentView(getLayoutResId());
        permission_3();
        icon_note = findViewById(R.id.icon_note);
        icon_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission_3();
            }
        });
    }

    public void permission_3(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PER_WRITE_EXTERNAL_STORAGE = false;
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showSettingsDialog(getString(R.string.permission_1));
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
        else {
            PER_WRITE_EXTERNAL_STORAGE = true;
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);
            if (fragment == null) {
                fragment = createFragment();
                fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
            }

            permission_4();
        }

    }

    public void permission_4(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            PER_RECORD_AUDIO = false;
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                return;
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_2);
            }
        }
        else {
            PER_RECORD_AUDIO = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PER_WRITE_EXTERNAL_STORAGE = true;
            }
            else{
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                PER_WRITE_EXTERNAL_STORAGE = false;
            }
            Log.e(TAG, "PER_WRITE_EXTERNAL_STORAGE - " + PER_WRITE_EXTERNAL_STORAGE);
            permission_3();
        }
        else if(requestCode == REQUEST_CODE_2) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PER_RECORD_AUDIO = true;
            }
            else{
                PER_RECORD_AUDIO = false;
                showSettingsDialog(getString(R.string.permission_2));
            }
            Log.e(TAG, "PER_RECORD_AUDIO - " + PER_RECORD_AUDIO);
            permission_4();
        }
    }

//    private void permission() {
//        Dexter.withContext(this)
//                .withPermissions(
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        /*Manifest.permission.RECORD_AUDIO*/)
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        if (report.areAllPermissionsGranted()) {
////                            PER_RECORD_AUDIO = true;
//                            PER_WRITE_EXTERNAL_STORAGE = true;
//                            FragmentManager fm = getSupportFragmentManager();
//                            Fragment fragment = fm.findFragmentById(R.id.fragment_container);
//                            if (fragment == null) {
//                                fragment = createFragment();
//                                fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
//                            }
//                        }
//                        if(report.isAnyPermissionPermanentlyDenied()) {
//                            PER_WRITE_EXTERNAL_STORAGE = false;
////                            showSettingsDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        permissionToken.continuePermissionRequest();
//                    }
//
//                }).onSameThread().check();
//    }

    private void showSettingsDialog(String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.need_Permissions));
        builder.setMessage(permission);
        builder.setPositiveButton(getString(R.string.goto_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onStart(){
        super.onStart();
        ApplicationClass.setNameActivity(this);
        Log.i(TAG, "onStart()");

    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume()");
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        IS_LIVE = true;
        Log.i(TAG, "onDestroy(), IS_LIVE - " + IS_LIVE + " , PP - " + PP);
        ApplicationClass.setNameActivity(this);
        if(IS_LIVE && PP) {
            Intent serviceIntent = new Intent(getApplicationContext(), MusicService.class);
            getApplicationContext().stopService(serviceIntent);
        }
    }
}
