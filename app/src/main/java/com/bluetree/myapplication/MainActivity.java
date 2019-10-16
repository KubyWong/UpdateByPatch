package com.bluetree.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(BuildConfig.VERSION_NAME);

        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isAllowPermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (!isAllowPermission) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},200);
                    return;
                }

                final String oldApkPath = getApplication().getApplicationInfo().sourceDir;
                final String patchPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "patch";
                final String newApkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "newApkPath.apk";

                if (!new File(oldApkPath).exists() || !new File(patchPath).exists()) {
                    Log.e(TAG, "没有补丁文件");
                    return;
                }

                new AsyncTask<Void, Void, File>() {
                    @Override
                    protected File doInBackground(Void... voids) {
                        createNewFile(newApkPath);
                        increaseUpdate(oldApkPath, patchPath, newApkPath);

                        return new File(newApkPath);
                    }

                    @Override
                    protected void onPostExecute(File file) {
                        super.onPostExecute(file);
                        if(file == null) return;
                        installApk(file);
                    }
                }.execute();

            }
        });
    }

    /**
     * 创建一个文件
     * @param path
     * @return
     */
    private File createNewFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 安装应用
     * @param file
     */
    private void installApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //兼容7.0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        } else {
            // 声明需要的临时权限
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 第二个参数，即第一步中配置的authorities
            String packageName = getApplication().getPackageName();
            Uri contentUri = FileProvider.getUriForFile(MainActivity.this, packageName + ".fileprovider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 合成新的apk安装包
     * @param oldApk 旧版本
     * @param patch 补丁
     * @param newApk 新版本
     * @return
     */
    public native String increaseUpdate(String oldApk,String patch,String newApk);
}
