package com.anubhav.notedown.utils;

import android.os.Environment;

import java.io.File;

public class Global {

    public static final String defaultAppPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "TrackX/";

    public static final String PRE_URL_MSG = "http://api.msg91.com/api/";
    public static final String CUSTOMFONTNAME = "font/josefin_regular.ttf";
    public static final String CUSTOMFONTNAMEBOLD = "font/josefin_bold.ttf";
    public static final boolean ISTESTING = true;

    // default app config
    public static String DEVELOPER_ID = "8256627302674614518";
    public static String PRE_URL_MAIN = "https://www.trackxadmin.com/";
    public static String PRE_URL = PRE_URL_MAIN + "api/authController/";

    public static final int CONTAINER_ANIMATION_DURATION = 600;
    public static final int UNKNOWN_PACKAGE_INTENT_REQUEST_CODE = 7665;

//    public static Permissions.Options permissionsOptions = new Permissions.Options()
//            .setRationaleDialogTitle("Info")
//            .setSettingsDialogTitle("Warning")
//            .setSettingsDialogTheme(R.style.CustomDialogTheme)
//            .setSettingsDialogView(R.layout.dialog_required_permission, R.id.dialog_title, R.id.dialog_desc, R.id.bt_confirm, R.id.bt_decline);
//    public static String[] permissionsRequired = new String[]{
//            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };

}