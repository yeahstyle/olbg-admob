package com.nightsteed.ads.admob;

import android.os.Bundle;
import android.util.Log;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;


public class AdMobUtils {
    private static final String TAG = "AdMobUtils";

    public static AdRequest getAdRequest(boolean adsConsent, boolean isTest, String testDeviceId) {
        AdRequest.Builder req = new AdRequest.Builder();
        if (!adsConsent) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            req = req.addNetworkExtrasBundle(AdMobAdapter.class, extras);
        }

        if (isTest && testDeviceId != null) {
            Log.d(TAG, "getAdRequest TESTING...testDeviceId: " + testDeviceId);
            req = req.addTestDevice(testDeviceId);
        } else {
            Log.d(TAG, "getAdRequest NOT TESTING...isTest: " + isTest + ", testDeviceId: " + testDeviceId);
        }

        return req.build();
    }

}
