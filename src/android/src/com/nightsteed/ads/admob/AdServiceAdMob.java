package com.nightsteed.ads.admob;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.google.android.gms.ads.MobileAds;
import com.nightsteed.ads.AdBanner;
import com.nightsteed.ads.AdInterstitial;
import com.nightsteed.ads.AdRewardedVideo;
import com.nightsteed.ads.AdService;
import com.unity3d.ads.metadata.MetaData;

import org.json.JSONObject;

public class AdServiceAdMob implements AdService {
    private static final String TAG = "AdServiceAdMob";
    private boolean _initialized = false;
    private boolean _personalizedAdsConsent;
    private String _bannerAdUnit;
    private String _interstitialAdUnit;
    private String _rewardedVideoAdUnit;
    private String _testDeviceId;
    private boolean _isTest;
    private Activity _activity;

    public void configure(Activity activity, JSONObject obj) {
        String appId = obj.optString("appId");
        _testDeviceId = obj.optString("testDeviceId");
        String isTestStr = obj.optString("isTest");
        Log.d(TAG, "isTestStr: " + isTestStr);
        _isTest = obj.optBoolean("isTest", false);

        String banner = obj.optString("banner");
        String interstitial = obj.optString("interstitial");
        String rewardedVideo = obj.optString("rewardedVideo");
        boolean personalizedAdsConsent = obj.optBoolean("personalizedAdsConsent");
        if (appId == null) {
            throw new RuntimeException("Empty App AdUnit");
        }

        Log.d(TAG, "Initializing with appId: " + appId);

        if (!_initialized) {
            MobileAds.initialize(activity, appId);
            _initialized = true;
        }
        _activity = activity;
        _bannerAdUnit = banner;
        _interstitialAdUnit = interstitial;
        _rewardedVideoAdUnit = rewardedVideo;
        _personalizedAdsConsent = personalizedAdsConsent;
    }

    /**
     * Sets the consent of the user. This consent is used for each type of ads
     * @param consentGiven The consent
     */
    public void setConsent(boolean consentGiven) {
        Log.d(TAG, "setConsent: " + consentGiven);
        _personalizedAdsConsent = consentGiven;
        MetaData gdprMetaData = new MetaData(_activity); gdprMetaData.set("gdpr.consent", true); gdprMetaData.commit();
    }

    public AdBanner createBanner(Context ctx) {
        return createBanner(ctx, null, AdBanner.BannerSize.SMART_SIZE);
    }

    public AdBanner createBanner(Context ctx, String adUnit, AdBanner.BannerSize size) {
        if (adUnit == null || adUnit.length() == 0) {
            adUnit = _bannerAdUnit;
        }
        if (adUnit == null || adUnit.length() == 0) {
            throw new RuntimeException("Empty AdUnit");
        }
        return new AdBannerAdMob(ctx, adUnit, size, _personalizedAdsConsent, _testDeviceId, _isTest);
    }

    public AdInterstitial createInterstitial(Context ctx) {
        return createInterstitial(ctx, null);
    }

    public AdInterstitial createInterstitial(Context ctx, String adUnit) {

        if (adUnit == null || adUnit.length() == 0) {
            adUnit = _interstitialAdUnit;
        }
        if (adUnit == null || adUnit.length() == 0) {
            throw new RuntimeException("Empty AdUnit");
        }
        return new AdInterstitialAdMob(ctx, adUnit, _personalizedAdsConsent, _testDeviceId, _isTest);
    }

    public AdRewardedVideo createRewardedVideo(Context ctx) {
        return createRewardedVideo(ctx, null);
    }

    public AdRewardedVideo createRewardedVideo(Context ctx, String adUnit) {
        if (adUnit == null || adUnit.length() == 0) {
            adUnit = _rewardedVideoAdUnit;
        }
        if (adUnit == null || adUnit.length() == 0) {
            throw new RuntimeException("Empty AdUnit");
        }
        Log.d(TAG, "creating AdRewardedAdMob, _personalizedAdsConsent: " + _personalizedAdsConsent);
        return new AdRewardedAdMob(ctx, adUnit, _personalizedAdsConsent, _testDeviceId, _isTest);
    }
}
