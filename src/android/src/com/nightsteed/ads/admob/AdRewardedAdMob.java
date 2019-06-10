package com.nightsteed.ads.admob;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.nightsteed.ads.AbstractAdRewardedVideo;
import com.nightsteed.ads.AdRewardedVideo;

public class AdRewardedAdMob extends AbstractAdRewardedVideo {
    private final String TAG = "AdRewardedAdMob";
    private final int    ERR_REWARD_FAILED = 1000;
    private RewardedVideoAd _rewardedVideo;
    private String adUnit;
    private boolean loading = false;
    private boolean adsConsent;
    private boolean rewardEarned;
    private boolean isTest;
    private String testDeviceId;

    AdRewardedAdMob(Context ctx, String adUnit, boolean personalizedAdsConsent, String testDeviceId, boolean isTest) {
        Log.d(TAG, "AdRewardedAdMob ctor...");
        adsConsent = personalizedAdsConsent;
        rewardEarned = false;
        this.isTest = isTest;
        this.testDeviceId = testDeviceId;

        _rewardedVideo = MobileAds.getRewardedVideoAdInstance(ctx);
        this.adUnit = adUnit;
        _rewardedVideo.setRewardedVideoAdListener(new RewardedVideoAdListener() {

            public void onRewardedVideoAdLoaded() {
                Log.d(TAG, "onRewardedVideoAdLoaded...");
                loading = false;
                rewardEarned = false;
                notifyOnLoaded();
            }

            public void onRewardedVideoAdOpened() {
                Log.d(TAG, "onRewardedVideoAdOpened...");
                notifyOnShown();
            }

            public void onRewardedVideoStarted() {
                Log.d(TAG, "onRewardedVideoStarted...");
            }

            public void onRewardedVideoAdClosed() {
                Log.d(TAG, "onRewardedVideoAdClosed...");
                if (!rewardEarned) {
                    AdRewardedVideo.Error error = new AdRewardedVideo.Error();
                    error.code = ERR_REWARD_FAILED;
                    error.message = "Rewarded video dismissed";
                    notifyOnRewardCompleted(null, error);
                }
                notifyOnDismissed();
            }

            public void onRewarded(RewardItem rewardItem) {
                Log.d(TAG, "onRewarded...");
                rewardEarned = true;
                AdRewardedVideo.Reward result = new AdRewardedVideo.Reward();
                result.amount = Math.max(rewardItem.getAmount(), 1);
                result.currency = rewardItem.getType();
                result.itmKey = rewardItem.getType();
                notifyOnRewardCompleted(result, null);
            }

            public void onRewardedVideoAdLeftApplication() {
                Log.d(TAG, "onRewardedVideoAdLeftApplication...");
                notifyOnClicked();
            }

            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                Log.d(TAG, "onRewardedVideoAdFailedToLoad...errorCode: " + errorCode);
                loading = false;
                Error error = new Error();
                error.code = errorCode;
                error.message = "Error with code: " + errorCode;
                notifyOnFailed(error);
            }

            public void onRewardedVideoCompleted() {
                Log.d(TAG, "onRewardedVideoCompleted...");

            }
        });

        Log.d(TAG, "AdRewardedAdMob ctor...END");
    }

    @Override
    public void loadAd() {
        Log.d(TAG, "loadAd...");
        if (!loading) {
            loading = true;
            AdRequest adRequest = AdMobUtils.getAdRequest(adsConsent, isTest, testDeviceId);
            Log.d(TAG, "loadAd, request...adUnit: " + adUnit);
            _rewardedVideo.loadAd(adUnit, adRequest);
            Log.d(TAG, "loadAd, request...END...");
        } else {
            Log.d(TAG, "loadAd...still loading...");
        }
    }

    @Override
    public void show() {
        Log.d(TAG, "show...");
        if (_rewardedVideo.isLoaded()) {
            _rewardedVideo.show();
        } else {
            if (!loading) {
                loadAd();
            }
            AdRewardedVideo.Error error = new AdRewardedVideo.Error();
            error.message = "Rewarded video not ready yet";
            notifyOnRewardCompleted(null, error);
        }
    }

    @Override
    public void destroy() {
        Log.d(TAG, "destroy...");

    }
}
