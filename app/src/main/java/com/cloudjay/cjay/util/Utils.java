package com.cloudjay.cjay.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import com.cloudjay.cjay.App;
import com.cloudjay.cjay.R;
import com.cloudjay.cjay.model.AuditImage;
import com.cloudjay.cjay.model.AuditItem;
import com.cloudjay.cjay.model.GateImage;
import com.cloudjay.cjay.model.Session;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Utils {

    public static String getAppVersionName(Context ctx) {

        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pInfo.versionName;
    }

    public static void showCrouton(Activity context, int textResId) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, textResId, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    public static void showCrouton(Activity context, String message) {
        Crouton.cancelAllCroutons();
        final Crouton crouton = Crouton.makeText(context, message, Style.ALERT);
        crouton.setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder().setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE).build());
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crouton.hide(crouton);
            }
        });
        crouton.show();
    }

    public static int getRole(Context context) {

        return Integer.valueOf(PreferencesUtil.getPrefsValue(context, PreferencesUtil.PREF_USER_ROLE));
    }

    //Check containerID is valid or not
    public static boolean isContainerIdValid(String containerId) {

        //if (!Logger.isDebuggable()) {
        Logger.Log("isContainerIdValid");

        int crc = ContCheckDigit.getCRC(containerId);
        if (crc == 10) {
            crc = 0;
        }

        char lastChar = containerId.charAt(containerId.length() - 1);
        if (Character.getNumericValue(lastChar) == crc) {
            return true;
        } else {
            return false;
        }
        //}

        //return true;
    }

    public static boolean simpleValid(String containerID) {

        Pattern pattern = Pattern.compile("^([A-Z]+){4,4}+(\\d{7,7}+)$");
        Matcher matcher = pattern.matcher(containerID);
        if (!matcher.matches()) return false;
        return true;
    }

    public static String replaceNullBySpace(String in) {
        return in == null || in.equals("") ? " " : in;
    }

    /**
     * Convert container session json to Session Object.
     * Need to check if container is existed or not. (should use insert or update concept)
     *
     * @param context
     * @param session
     * @return
     */
    public static Session parseSession(Context context, Session session) throws SnappydbException {

        //Check available session
        DB snappyDb = App.getSnappyDB(context);
        Session found = snappyDb.getObject(session.getContainerId(), Session.class);

        // If hasn't -> create
        if (found == null) {
            snappyDb.put(session.getContainerId(), session);
            return session;
        }

        // else -> update
        else {
            snappyDb.del(session.getContainerId());
            snappyDb.put(session.getContainerId(), session);
            return session;
        }


    }

    /**
     * Count total image off session
     *
     * @param session
     * @return
     */
    public static int countTotalImage(Session session) {
        int totalImage = 0;
        List<AuditItem> auditItems = session.getAuditItems();
        for (AuditItem auditItem : auditItems) {
            totalImage = totalImage + auditItem.getAuditImages().size();
        }
        totalImage = totalImage + session.getGateImages().size();
        return totalImage;
    }

    public static int countUploadedImage(Session session) {
        int uploadedImage = 0;
        List<AuditItem> auditItems = session.getAuditItems();
        for (AuditItem auditItem : auditItems) {
            List<AuditImage> auditImages = auditItem.getAuditImages();
            for (AuditImage auditImage : auditImages) {
                if (auditImage.isUploaded()) {
                    uploadedImage = uploadedImage + 1;
                }
            }

        }
        List<GateImage> gateImages = session.getGateImages();
        for (GateImage gateImage : gateImages) {
            if (gateImage.isUploaded()) {
                uploadedImage = uploadedImage + 1;
            }
        }
        return uploadedImage;
    }

    public static String getImageTypeDescription(Context ctx, int imageType) {

        switch (imageType) {
            case CJayConstant.TYPE_IMPORT:
                return ctx.getResources().getString(R.string.image_type_description_import);

            case CJayConstant.TYPE_EXPORT:
                return ctx.getResources().getString(R.string.image_type_description_export);

            case CJayConstant.TYPE_AUDIT:
                return ctx.getResources().getString(R.string.image_type_description_report);

            case CJayConstant.TYPE_REPAIRED:
            default:
                return ctx.getResources().getString(R.string.image_type_description_repaired);
        }
    }
}