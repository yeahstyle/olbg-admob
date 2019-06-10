package com.nightsteed.ads.admob;

import android.content.Context;
import android.os.Bundle;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nightsteed.ads.AbstractAdInterstitial;

public class AdInterstitialAdMob extends AbstractAdInterstitial {
    private InterstitialAd _interstitial;
    private boolean adsConsent;
    private boolean isTest;
    private String testDeviceId;

    AdInterstitialAdMob(Context ctx, String adUnit, boolean personalizedAdsConsent, String testDeviceId, boolean isTest) {
        adsConsent = personalizedAdsConsent;
        this.isTest = isTest;
        this.testDeviceId = testDeviceId;

        _interstitial = new InterstitialAd(ctx);
        _interstitial.setAdUnitId(adUnit);
        _interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                notifyOnDismissed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Error error = new Error();
                error.code = errorCode;
                error.message = "Error with code: " + errorCode;
                notifyOnFailed(error);
            }

            @Override
            public void onAdLeftApplication() {
                notifyOnClicked();
            }

            @Override
            public void onAdOpened() {
                notifyOnShown();
            }

            @Override
            public void onAdLoaded() {
                notifyOnLoaded();
            }
        });

    }

    @Override
    public void loadAd() {
        AdRequest adRequest = AdMobUtils.getAdRequest(adsConsent, isTest, testDeviceId);
        // .addTestDevice("A8CA27EE6F83C9D384A8523CDE61C70C")

        _interstitial.loadAd(adRequest);
    }

    @Override
    public void show() {
        if (_interstitial.isLoaded()) {
            _interstitial.show();
        } else {
            loadAd();
        }

    }

    @Override
    public void destroy() {

    }

}
