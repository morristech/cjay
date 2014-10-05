package com.cloudjay.cjay.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by nambv on 03/10/2014.
 */
public class CJayConstant {

    // File path
    public static final String APP_DIRECTORY = "CJay";
    public static final String HIDDEN_APP_DIRECTORY = ".CJay";
    public static final String BACK_UP_DIRECTORY = ".backup";
    public static final String LOG_DIRECTORY = "log";

    public static final int TYPE_IMPORT = 0;
    public static final int TYPE_EXPORT = 1;
    public static final int TYPE_AUDIT = 2;
    public static final int TYPE_REPAIRED = 3;

    // `/sdcard/DCMI/CJay/`
    public static final File APP_DIRECTORY_FILE = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            CJayConstant.APP_DIRECTORY);

}
