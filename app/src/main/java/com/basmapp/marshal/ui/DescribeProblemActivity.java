package com.basmapp.marshal.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.basmapp.marshal.BuildConfig;
import com.basmapp.marshal.R;
import com.basmapp.marshal.ui.utils.LocaleUtils;
import com.basmapp.marshal.ui.utils.ThemeUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DescribeProblemActivity extends AppCompatActivity {
    Toolbar mToolbar;

    private ImageView mScreenshotOne;
    private ImageView mScreenshotTwo;
    private ImageView mScreenshotThree;

    private int PICK_IMAGE_REQUEST_ONE = 1;
    private int PICK_IMAGE_REQUEST_TWO = 2;
    private int PICK_IMAGE_REQUEST_THREE = 3;

    private ArrayList<Uri> attachments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_describe_problem);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.mail_subject);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
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
                        Toast.makeText(DescribeProblemActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
                        finish();
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

        mScreenshotOne = (ImageView) findViewById(R.id.screenshot_one);
        mScreenshotOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_action)), PICK_IMAGE_REQUEST_ONE);
            }
        });

        mScreenshotTwo = (ImageView) findViewById(R.id.screenshot_two);
        mScreenshotTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_action)), PICK_IMAGE_REQUEST_TWO);
            }
        });

        mScreenshotThree = (ImageView) findViewById(R.id.screenshot_three);
        mScreenshotThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_action)), PICK_IMAGE_REQUEST_THREE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_ONE || requestCode == PICK_IMAGE_REQUEST_TWO || requestCode == PICK_IMAGE_REQUEST_THREE) {
            if (resultCode == RESULT_OK) {
                if (requestCode == PICK_IMAGE_REQUEST_ONE) {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    if (getRealPathFromURI(uri) != null) {
                        try {
                            attachments.add(Uri.fromFile(new File(getRealPathFromURI(uri))));
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            mScreenshotOne.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == PICK_IMAGE_REQUEST_TWO) {
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    Uri uri = data.getData();
                    if (getRealPathFromURI(uri) != null) {
                        try {
                            attachments.add(Uri.fromFile(new File(getRealPathFromURI(uri))));
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            mScreenshotTwo.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Uri uri = data.getData();
                    if (getRealPathFromURI(uri) != null) {
                        try {
                            attachments.add(Uri.fromFile(new File(getRealPathFromURI(uri))));
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            mScreenshotThree.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(DescribeProblemActivity.this, getString(R.string.error_load_image), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, contentUri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
//        String fileName = Uri.parse(cursor.getString(column_index)).getLastPathSegment();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public String debugInfo() {
        // Get debug info from the device for error report email and save it as string
        long freeBytesInternal = new File(getFilesDir().getAbsoluteFile().toString()).getFreeSpace();
        String freeGBInternal = String.format(Locale.getDefault(), "%.2f", freeBytesInternal / Math.pow(2, 30));
        String debugInfo="--Support Info--";
        debugInfo += "\n Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        debugInfo += "\n Manufacturer: " + Build.MANUFACTURER;
        debugInfo += "\n Model: " + Build.MODEL;
        debugInfo += "\n Locale: " + getBaseContext().getResources().getConfiguration().locale.toString();
        debugInfo += "\n OS: " + Build.VERSION.RELEASE + " ("+android.os.Build.VERSION.SDK_INT+")";
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
}
