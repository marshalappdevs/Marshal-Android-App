package com.basmapp.marshal.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.basmapp.marshal.ApplicationMarshal;
import com.basmapp.marshal.BaseActivity;
import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.util.GetFilePathFromUri;
import com.basmapp.marshal.util.LocaleUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DescribeProblemActivity extends BaseActivity {
    private static final int REQUEST_STORAGE = 0;
    private ImageView[] screenshots = new ImageView[3];
    private Uri[] uris = new Uri[3];
    private int PICK_IMAGE_REQUEST;
    private ArrayList<Uri> attachments = new ArrayList<>();
    private EditText mDescription;
    private CheckBox mLogsCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_describe_problem);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.mail_subject);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDescription = (EditText) findViewById(R.id.describe_problem_description);
        mLogsCheckBox = (CheckBox) findViewById(R.id.describe_problem_include_logs);

//        Button faq = (Button) findViewById(R.id.describe_problem_help);
//        faq.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(DescribeProblemActivity.this, WebViewActivity.class);
//                intent.putExtra(Constants.EXTRA_WEB_VIEW_TITLE, getString(R.string.faq));
//                intent.putExtra(Constants.EXTRA_WEB_VIEW_URL, "file:///android_asset/frequently_asked_questions.html");
//                startActivity(intent);
//            }
//        });

        for (int i = 0; i < screenshots.length; i++) {
            screenshots[i] = (ImageView) ((LinearLayout) findViewById(R.id.screenshots)).getChildAt(i);
            screenshots[i].setOnClickListener(new screenshotClickListener(i));
            screenshots[i].setOnLongClickListener(new screenshotClickListener(i));
        }

        if (savedInstanceState != null) {
            Parcelable[] savedScreenshots = savedInstanceState.getParcelableArray("screenshots");
            if (savedScreenshots == null) {
                throw new AssertionError();
            }
            int i = 0;
            while (i < savedScreenshots.length) {
                if (savedScreenshots[i] != null) {
                    attachScreenshot(i, (Uri) savedScreenshots[i]);
                }
                i += 1;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_us, menu);
        final MenuItem next = menu.findItem(R.id.next);
        next.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(next);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                sendEmail();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendEmail() {
        int length = mDescription.getText().toString().trim().getBytes().length;
        if (length > 10) {
            if (mLogsCheckBox.isChecked()) {
                // Create debug info text file and set file name to current date and time
                Calendar now = Calendar.getInstance();
                String filename = String.format(Locale.getDefault(),
                        "marshal_%02d%02d%04d_%02d%02d%02d.log",
                        now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1,
                        now.get(Calendar.YEAR), now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
                File tempFile = new File(getBaseContext().getExternalCacheDir()
                        + File.separator + filename + ".txt");
                try {
                    FileWriter writer = new FileWriter(tempFile);
                    writer.write(debugInfo());
                    writer.close();
                    attachments.add(Uri.fromFile(tempFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    emailIntent.putExtra(Intent.EXTRA_TEXT, mDescription.getText());
                    emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
                    intentShareList.add(emailIntent);
                }
            }
            if (intentShareList.isEmpty()) {
                Toast.makeText(DescribeProblemActivity.this, R.string.no_supported_mail_apps, Toast.LENGTH_LONG).show();
//                        finish();
            } else {
                Intent chooserIntent = Intent.createChooser(intentShareList.remove(0), getResources().getText(R.string.contact_support_via));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentShareList.toArray(new Parcelable[intentShareList.size()]));
                startActivity(chooserIntent);
                finish();
            }
        } else if (length == 0) {
            Toast makeText = Toast.makeText(DescribeProblemActivity.this,
                    R.string.describe_problem_description, Toast.LENGTH_LONG);
            makeText.setGravity(Gravity.CENTER, 0, 0);
            makeText.show();
        } else {
            Toast makeText = Toast.makeText(DescribeProblemActivity.this,
                    R.string.describe_problem_description_further, Toast.LENGTH_LONG);
            makeText.setGravity(Gravity.CENTER, 0, 0);
            makeText.show();
        }
    }


    public class screenshotClickListener implements View.OnClickListener, View.OnLongClickListener {

        int position;

        screenshotClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            PICK_IMAGE_REQUEST = position;
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

        @Override
        public boolean onLongClick(View v) {
            if (uris[position] != null && attachments.contains(uris[position])) {
                attachments.remove(uris[position]);
                uris[position] = null;
                screenshots[position].setImageResource(R.drawable.ic_add_large);
                screenshots[position].setScaleType(ImageView.ScaleType.CENTER);
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.screenshot_removed, Snackbar.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        }
    }

    private void attachScreenshot(int position, Uri uri) {
        String filePath = GetFilePathFromUri.getPath(this, uri);
        if (filePath != null) {
            if (uris[position] != null && attachments.contains(uris[position]))
                attachments.remove(uris[position]);
            uris[position] = Uri.fromFile(new File(filePath));
            attachments.add(uris[position]);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            screenshots[position].setScaleType(ImageView.ScaleType.CENTER_CROP);
            screenshots[position].setImageBitmap(bitmap);
        } else {
            Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
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
                    attachScreenshot(requestCode, data.getData());
                } else if (requestCode == 1) {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    attachScreenshot(requestCode, data.getData());
                } else {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    attachScreenshot(requestCode, data.getData());
                }
            }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray("screenshots", uris);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (LocaleUtils.isRtl(getResources())) {
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
        String debugInfo = "--Support Info--";
        debugInfo += "\n Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        debugInfo += "\n Manufacturer: " + Build.MANUFACTURER;
        debugInfo += "\n Model: " + Build.MODEL + " (" + Build.DEVICE + ")";
        debugInfo += "\n Locale: " + Locale.getDefault().toString();
        debugInfo += "\n OS: " + Build.VERSION.RELEASE + " (" + android.os.Build.VERSION.SDK_INT + ")";
        debugInfo += "\n Rooted: " + (ApplicationMarshal.isRooted() ? "true" : "false");
        File[] filesDirs = ContextCompat.getExternalFilesDirs(this, null);
        if (filesDirs[0] != null) {
            long freeBytesInternal = new StatFs(filesDirs[0].getPath()).getAvailableBytes();
            debugInfo += "\n Free Space Built-In: " + freeBytesInternal + " (" + Formatter.formatFileSize(this, freeBytesInternal) + ")";
        } else {
            debugInfo += "\n Free Space Built-In: Unavailable";
        }
        if (filesDirs[1] != null) {
            long freeBytesExternal = new StatFs(filesDirs[1].getPath()).getAvailableBytes();
            debugInfo += "\n Free Space Removable: " + freeBytesExternal + " (" + Formatter.formatFileSize(this, freeBytesExternal) + ")";
        } else {
            debugInfo += "\n Free Space Removable: Unavailable";
        }
        float density = getResources().getDisplayMetrics().density;
        String densityName = "unknown";
        if (density == 4.0) densityName = "xxxhdpi";
        if (density == 3.0) densityName = "xxhdpi";
        if (density == 2.0) densityName = "xhdpi";
        if (density == 1.5) densityName = "hdpi";
        if (density == 1.0) densityName = "mdpi";
        if (density == 0.75) densityName = "ldpi";
        debugInfo += "\n Screen Resolution: " + getResources().getDisplayMetrics().heightPixels + "x" +
                getResources().getDisplayMetrics().widthPixels + " (" + densityName + ")";
        debugInfo += "\n Target: " + BuildConfig.BUILD_TYPE;
        debugInfo += "\n Distribution: " + (verifyInstallerId() ? "play" : "apk");
        return debugInfo;
    }

    boolean verifyInstallerId() {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        // The package name of the app that has installed your app
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
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
