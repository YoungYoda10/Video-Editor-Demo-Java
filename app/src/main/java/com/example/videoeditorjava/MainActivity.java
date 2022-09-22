package com.example.videoeditorjava;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ly.img.android.pesdk.VideoEditorSettingsList;
import ly.img.android.pesdk.backend.model.EditorSDKResult;
import ly.img.android.pesdk.backend.model.state.LoadSettings;
import ly.img.android.pesdk.ui.activity.VideoEditorBuilder;

public class MainActivity extends AppCompatActivity {

    public static final int EDITOR_REQUEST_CODE = 0x42;
    public static final int SAME_REQUEST_CODE = 0x69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getVideoFromGallery();
    }

    private void getVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, SAME_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showMessage("No Gallery app installed");
        }
    }



//    private void captureVideoUsingCamera() {
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        try {
//            startActivityForResult(takeVideoIntent, SAME_REQUEST_CODE);
//        } catch (ActivityNotFoundException e) {
//            showMessage("No Camera app installed");
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case EDITOR_REQUEST_CODE:
                if (intent != null) {
                    EditorSDKResult result = new EditorSDKResult(intent);
                    switch (result.getResultStatus()) {
                        case CANCELED:
                            showMessage("Editor cancelled");
                            break;
                        case EXPORT_DONE:
                            showMessage("Result saved at " + result.getResultUri());
                            break;
                    }
                } else {
                    showMessage("Invalid Intent");
                }
                break;
            case SAME_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (intent != null) {
                        if (intent.getData() != null) {
                            showEditor(intent.getData());
                        } else {
                            showMessage("Invalid Uri");
                        }
                    } else {
                        showMessage("Invalid Intent");
                    }
                }
                break;
        }
    }

    private void showEditor(Uri uri) {
        // In this example, we do not need access to the Uri(s) after the editor is closed
        // so we pass false in the constructor
        VideoEditorSettingsList settingsList = new VideoEditorSettingsList(false);
        settingsList.getSettingsModel(LoadSettings.class).setSource(uri);
        // Start the video editor using VideoEditorBuilder
        // The result will be obtained in onActivityResult() corresponding to EDITOR_REQUEST_CODE
        new VideoEditorBuilder(this).setSettingsList(settingsList).startActivityForResult(this, EDITOR_REQUEST_CODE);
        // Release the SettingsList once done
        settingsList.release();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private Uri resourceUri(int resourceId) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(getResources().getResourceEntryName(resourceId)).appendPath(getResources().getResourceEntryName(resourceId)).build();
    }
}