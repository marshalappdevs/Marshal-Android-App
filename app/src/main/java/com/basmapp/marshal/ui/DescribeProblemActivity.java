package com.basmapp.marshal.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.GetFilePathFromUri;
import com.basmapp.marshal.util.LocaleUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DescribeProblemActivity extends BaseActivity {
    Toolbar mToolbar;

    private static final int REQUEST_STORAGE = 0;

    private ImageView[] screenshots = new ImageView[3];

    private Uri[] uris = new Uri[3];

    private int PICK_IMAGE_REQUEST;

    private ArrayList<Uri> attachments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_describe_problem);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.mail_subject);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final EditText description = (EditText) findViewById(R.id.describe_problem_description);

        Button next = (Button) findViewById(R.id.describe_problem_help);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (description.getText().length() == 0) {
                    Toast.makeText(DescribeProblemActivity.this, R.string.describe_problem_description, Toast.LENGTH_LONG).show();
                } else if (description.getText().length() >= 10) {

                    Calendar now = Calendar.getInstance();
                    // Create debug info text file and set file name to current date and time
                    String filename = String.format(Locale.getDefault(),
                            "marshal_%02d%02d%04d_%02d%02d%02d.log",
                            now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1,
                            now.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
                    File tempFile = new File(getBaseContext().getExternalCacheDir() + File.separator + filename + ".txt");
                    try {
                        FileWriter writer = new FileWriter(tempFile);
                        writer.write(debugInfo());
                        writer.close();
                        attachments.add(Uri.fromFile(tempFile));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    List<Intent> intentShareList = new ArrayList<>();
                    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("text/plain");
                    List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(shareIntent, 0);

                    for (ResolveInfo resInfo : resolveInfoList) {
                        String packageName = resInfo.activityInfo.packageName;

                        if (packageName.contains("gm") ||
                                packageName.contains("email") ||
                                packageName.contains("fsck.k9") ||
                                packageName.contains("maildroid") ||
                                packageName.contains("hotmail") ||
                                packageName.contains("android.mail") ||
                                packageName.contains("com.baydin.boomerang") ||
                                packageName.contains("yandex.mail") ||
                                packageName.contains("com.google.android.apps.inbox") ||
                                packageName.contains("com.microsoft.office.outlook") ||
                                packageName.contains("com.asus.email")) {

                            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                            emailIntent.setType("plain/text");
                            emailIntent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"marshaldevs@gmail.com"});
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
                            emailIntent.putExtra(Intent.EXTRA_TEXT, description.getText());
                            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
                            intentShareList.add(emailIntent);
                        }
                    }
                    if (intentShareList.isEmpty()) {
                        Toast.makeText(DescribeProblemActivity.this, R.string.no_supported_mail_apps, Toast.LENGTH_LONG).show();
//                        finish();
                    } else {
                        Intent chooserIntent = Intent.createChooser(intentShareList.remove(0), getResources().getText(R.string.send_to));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentShareList.toArray(new Parcelable[intentShareList.size()]));
                        startActivity(chooserIntent);
                        finish();
                    }
                } else {
                    Toast.makeText(DescribeProblemActivity.this, R.string.describe_problem_description_further, Toast.LENGTH_LONG).show();
                }
            }
        });

        for (int i = 0; i < screenshots.length; i++) {
            final int finalI = i;
            screenshots[i] = (ImageView) ((LinearLayout) findViewById(R.id.screenshots)).getChildAt(i);
            screenshots[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PICK_IMAGE_REQUEST = finalI;
                    // Check for storage permission
                    if (ActivityCompat.checkSelfPermission(DescribeProblemActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestStoragePermission();
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    }
                }
            });
            screenshots[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (uris[finalI] != null && attachments.contains(uris[finalI])) {
                        attachments.remove(uris[finalI]);
                        uris[finalI] = null;
                        screenshots[finalI].setImageResource(R.drawable.ic_add_large);
                        screenshots[finalI].setScaleType(ImageView.ScaleType.CENTER);
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.screenshot_removed, Snackbar.LENGTH_SHORT).show();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (requestCode == 0) {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    String filePath = GetFilePathFromUri.getPath(this, data.getData());
                    if (filePath != null) {
                        if (uris[0] != null && attachments.contains(uris[0]))
                            attachments.remove(uris[0]);
                        uris[0] = Uri.fromFile(new File(filePath));
                        attachments.add(uris[0]);
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        screenshots[0].setScaleType(ImageView.ScaleType.CENTER_CROP);
                        screenshots[0].setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == 1) {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    String filePath = GetFilePathFromUri.getPath(this, data.getData());
                    if (filePath != null) {
                        if (uris[1] != null && attachments.contains(uris[1]))
                            attachments.remove(uris[1]);
                        uris[1] = Uri.fromFile(new File(filePath));
                        attachments.add(uris[1]);
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        screenshots[1].setScaleType(ImageView.ScaleType.CENTER_CROP);
                        screenshots[1].setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String filePath = GetFilePathFromUri.getPath(this, data.getData());
                    if (filePath != null) {
                        if (uris[2] != null && attachments.contains(uris[2]))
                            attachments.remove(uris[2]);
                        uris[2] = Uri.fromFile(new File(filePath));
                        attachments.add(uris[2]);
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        screenshots[2].setScaleType(ImageView.ScaleType.CENTER_CROP);
                        screenshots[2].setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(0, MainActivity.dp2px(288), 1.0F);
            int margin = getResources().getDimensionPixelSize(R.dimen.medium_thumbnail_padding);
            layoutParams.setMargins(margin, margin, margin, margin);
            int i1 = 0;
            while (i1 < 3) {
                screenshots[i1].setLayoutParams(layoutParams);
                i1 += 1;
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(0, MainActivity.dp2px(144), 1.0F);
            int margin = getResources().getDimensionPixelSize(R.dimen.medium_thumbnail_padding);
            layoutParams.setMargins(margin, margin, margin, margin);
            int i1 = 0;
            while (i1 < 3) {
                screenshots[i1].setLayoutParams(layoutParams);
                i1 += 1;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (LocaleUtils.isRtl()) {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit_rtl);
            } else {
                overridePendingTransition(R.anim.activity_close_enter,
                        R.anim.activity_close_exit);
            }
        }
    }

    public String debugInfo() {
        // Get debug info from the device for error report email and save it as string
        long freeBytesInternal = new File(getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        String freeGBInternal = String.format(Locale.getDefault(), "%.2f", freeBytesInternal / Math.pow(2, 30));
        String debugInfo = "--Support Info--";
        debugInfo += "\n Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        debugInfo += "\n Manufacturer: " + Build.MANUFACTURER;
        debugInfo += "\n Model: " + Build.MODEL;
        debugInfo += "\n Locale: " + getBaseContext().getResources().getConfiguration().locale.toString();
        debugInfo += "\n OS: " + Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")";
        debugInfo += "\n Free Space: " + freeBytesInternal + " (" + freeGBInternal + " GB)";
        float density = getResources().getDisplayMetrics().density;
        String densityName = null;
        if (density == 4.0) densityName = "xxxhdpi";
        if (density == 3.0) densityName = "xxhdpi";
        if (density == 2.0) densityName = "xhdpi";
        if (density == 1.5) densityName = "hdpi";
        if (density == 1.0) densityName = "mdpi";
        if (density == 0.75) densityName = "ldpi";
        debugInfo += "\n Screen Density: " + density + " (" + densityName + ")";
        debugInfo += "\n Target: " + BuildConfig.BUILD_TYPE;
        return debugInfo;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(DescribeProblemActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {
            // Storage permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
