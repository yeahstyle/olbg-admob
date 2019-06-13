package com.nightsteed.ads.cordova;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;



import com.google.android.gms.ads.AdView;
import com.nightsteed.ads.AdBanner;
import com.nightsteed.ads.AdBanner.BannerSize;
import com.nightsteed.ads.AdInterstitial;
import com.nightsteed.ads.AdRewardedVideo;
import com.nightsteed.ads.AdService;

import com.nightsteed.ads.admob.AdServiceAdMob;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;

public class AdServicePlugin extends CordovaPlugin implements
    AdBanner.BannerListener, AdInterstitial.InterstitialListener
    , AdRewardedVideo.RewardedVideoListener {

    private final String TAG = "AdServicePlugin";
    private final String IDX_CONSENT_GIVEN = "personalizedAdsConsent";

    private SharedPreferences  mSharedPref;
    private SharedPreferences.Editor mEditor;

    protected AdService _service;
    protected HashMap<String, BannerData> _banners = new HashMap<String, BannerData>();
    protected HashMap<String, AdInterstitial> _interstitials = new HashMap<String, AdInterstitial>();
    protected HashMap<String, AdRewardedVideo> _rewardedVideos = new HashMap<String, AdRewardedVideo>();
    protected CallbackContext _bannerListener;
    protected CallbackContext _interstitialListener;
    protected CallbackContext _rewardedVideoListener;
    // private static final boolean CORDOVA_MIN_4 = Integer.valueOf(CordovaWebView.CORDOVA_VERSION.split("\\.")[0]) >= 4;
    private static final boolean CORDOVA_MIN_4 = true;
    private ViewGroup parentView;

    protected void pluginInitialize() {
        _service = new AdServiceAdMob();
        String PREFS_NAME = preferences.getString("NativeStorageSharedPreferencesName", "NativeStorage");
        mSharedPref = cordova.getActivity().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
    }

    private enum BannerLayout {
        TOP_CENTER,
        BOTTOM_CENTER,
        CUSTOM
    }

    private class BannerData {
        public AdBanner banner;
        public BannerLayout layout = BannerLayout.TOP_CENTER;
        public double x;
        public double y;
    }

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        //Run everything in the Main Thread
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Method method = AdServicePlugin.this.getClass().getMethod(action, CordovaArgs.class, CallbackContext.class);
                    method.invoke(AdServicePlugin.this, args, callbackContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    @Override
    public void onDestroy() {
        for (String key : _banners.keySet()) {
            BannerData banner = _banners.get(key);
            banner.banner.destroy();
        }

        for (String key : _interstitials.keySet()) {
            AdInterstitial interstitial = _interstitials.get(key);
            interstitial.destroy();
        }

        for (String key : _rewardedVideos.keySet()) {
            AdRewardedVideo rewardedVideo = _rewardedVideos.get(key);
            rewardedVideo.destroy();
        }
    }

    public void configure(CordovaArgs args, CallbackContext ctx) {
        JSONObject obj = args.optJSONObject(0);
        if (obj == null) {
            return;
        }

        try {
            Boolean bool = mSharedPref.getBoolean(IDX_CONSENT_GIVEN, false);
            obj.put(IDX_CONSENT_GIVEN, bool);

            _service.configure(cordova.getActivity(), obj);

            // ctx.success(String.valueOf(bool));
            ctx.success(String.valueOf(bool));
        } catch(JSONException e) {
            ctx.success(String.valueOf(false));
        }
    }

    public void setConsent(CordovaArgs args, CallbackContext ctx) {
        Boolean consentGiven = args.optBoolean(0);
        if (consentGiven == null) {
            return;
        }

        mEditor.putBoolean(IDX_CONSENT_GIVEN, consentGiven);
        boolean success = mEditor.commit();
        if (success) {
            ctx.success(String.valueOf(consentGiven));
        }
        else {
            ctx.error("Write failed");
        }

        _service.setConsent(consentGiven);
        ctx.sendPluginResult(new PluginResult(Status.OK));
    }

    public void adMob_configure(CordovaArgs args, CallbackContext ctx) {
        JSONObject obj = args.optJSONObject(0);
        if (obj == null) {
            return;
        }

        _service.configure(cordova.getActivity(), obj);

        this.webView.getPluginManager().postMessage("AdMob consent", obj.optBoolean("personalizedAdsConsent"));
    }


    //Bridge methods
    public void setBannerListener(CordovaArgs args, CallbackContext ctx) {
        _bannerListener = ctx;
    }

    public void setInterstitialListener(CordovaArgs args, CallbackContext ctx) {
        _interstitialListener = ctx;
    }

    public void setRewardedVideoListener(CordovaArgs args, CallbackContext ctx) {
        _rewardedVideoListener = ctx;
    }

    public void createBanner(CordovaArgs args, CallbackContext ctx) {

        String bannerId = getId(args);

        String adUnit = args.isNull(1) ? null : args.optString(1);
        String strSize = args.isNull(2) ? null : args.optString(2);

        BannerSize size = BannerSize.SMART_SIZE;
        if (strSize != null) {
            if ("BANNER".equals(strSize)) {
                size = BannerSize.BANNER_SIZE;

            } else if ("MEDIUM_RECT".equals(strSize)) {
                size = BannerSize.MEDIUM_RECT_SIZE;

            } else if ("LEADERBOARD".equals(strSize)) {
                size = BannerSize.LEADERBOARD_SIZE;

            }
        }

        AdBanner banner = _service.createBanner(this.cordova.getActivity(), adUnit, size);
        banner.setListener(this);
        BannerData data = new BannerData();
        data.banner = banner;
        _banners.put(bannerId, data);
        ctx.sendPluginResult(new PluginResult(Status.OK));
    }

    public void createInterstitial(CordovaArgs args, CallbackContext ctx) {
        String interstitialId = getId(args);
        String adUnit = args.isNull(1) ? null : args.optString(1);
        AdInterstitial interstitial = _service.createInterstitial(this.cordova.getActivity(), adUnit);
        interstitial.setListener(this);
        _interstitials.put(interstitialId, interstitial);
        ctx.sendPluginResult(new PluginResult(Status.OK));
    }

    public void createRewardedVideo(CordovaArgs args, CallbackContext ctx) {
        Log.d(TAG, "createRewardedVideo...0: " + args.optString(0) + ", 1: " + args.optString(1));
        
        String rewardedVideoId = getId(args);
        String adUnit = args.isNull(1) ? null : args.optString(1);
        Log.d(TAG, "createRewardedVideo...adUnit: " + adUnit);
        AdRewardedVideo rewardedVideo = _service.createRewardedVideo(this.cordova.getActivity(), adUnit);
        rewardedVideo.setListener(this);
        _rewardedVideos.put(rewardedVideoId, rewardedVideo);
        ctx.sendPluginResult(new PluginResult(Status.OK));
    }

    public void releaseBanner(CordovaArgs args, CallbackContext ctx) {
        String bannerId = getId(args);
        BannerData data = _banners.get(bannerId);
        if (data != null) {
            data.banner.setListener(null);
            if (data.banner.getView().getParent() != null) {
                ViewGroup vg = getViewGroup();
                vg.removeView(data.banner.getView());
            }
            _banners.remove(bannerId);
        }
    }

    public void releaseInterstitial(CordovaArgs args, CallbackContext ctx) {
        String interstitialId = getId(args);
        AdInterstitial interstitial = _interstitials.get(interstitialId);
        if (interstitial != null) {
            interstitial.setListener(null);
            interstitial.destroy();
            _interstitials.remove(interstitialId);
        }
    }

    public void releaseRewardedVideo(CordovaArgs args, CallbackContext ctx) {
        String rewardedVideoId = getId(args);
        AdRewardedVideo rewardedVideo = _rewardedVideos.get(rewardedVideoId);
        if (rewardedVideo != null) {
            rewardedVideo.setListener(null);
            rewardedVideo.destroy();
            _rewardedVideos.remove(rewardedVideoId);
        }
    }

    public void showBanner(CordovaArgs args, CallbackContext ctx) {
        BannerData data = _banners.get(getId(args));
        if (data != null && data.banner.getView() != null) {
            AdBanner banner = data.banner;
            banner.getView().setVisibility(View.VISIBLE);

            if (banner.getView().getParent() == null) {
                ViewGroup vg = getViewGroup();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                vg.addView(banner.getView(), params);
            }
            layoutBanner(data);
        }
    }

    public void hideBanner(CordovaArgs args, CallbackContext ctx) {
        BannerData data = _banners.get(getId(args));
        if (data != null && data.banner.getView() != null) {
            data.banner.getView().setVisibility(View.GONE);
        }
    }

    public void setBannerPosition(CordovaArgs args, CallbackContext ctx) {
        BannerData data = _banners.get(getId(args));
        if (data != null) {
            data.x = args.optDouble(1);
            data.y = args.optDouble(2);
            layoutBanner(data);
        }
    }

    public void setBannerLayout(CordovaArgs args, CallbackContext ctx) {
        BannerData data = _banners.get(getId(args));
        String value = args.optString(1);
        if (data != null) {
            if ("TOP_CENTER".equals(value)) {
                data.layout = BannerLayout.TOP_CENTER;
            } else if ("BOTTOM_CENTER".equals(value)) {
                data.layout = BannerLayout.BOTTOM_CENTER;
            } else {
                data.layout = BannerLayout.CUSTOM;
            }
            layoutBanner(data);
        }
    }

    public void loadBanner(CordovaArgs args, CallbackContext ctx) {
        BannerData data = _banners.get(getId(args));
        if (data != null) {
            data.banner.loadAd();
        }
    }

    public void showInterstitial(CordovaArgs args, CallbackContext ctx) {
        AdInterstitial interstitial = _interstitials.get(getId(args));
        if (interstitial != null) {
            interstitial.show();
        }
    }

    public void loadInterstitial(CordovaArgs args, CallbackContext ctx) {
        AdInterstitial interstitial = _interstitials.get(getId(args));
        if (interstitial != null) {
            interstitial.loadAd();
        }
    }

    public void showRewardedVideo(CordovaArgs args, CallbackContext ctx) {
        AdRewardedVideo rewardedVideo = _rewardedVideos.get(getId(args));
        if (rewardedVideo != null) {
            rewardedVideo.show();
        }
    }

    public void loadRewardedVideo(CordovaArgs args, CallbackContext ctx) {
        AdRewardedVideo rewardedVideo = _rewardedVideos.get(getId(args));
        if (rewardedVideo != null) {
            rewardedVideo.loadAd();
        }
    }

    //Ad Listeners
    @Override
    public void onLoaded(AdBanner banner) {
        String bannerId = findBannerId(banner);
        layoutBanner(_banners.get(bannerId));
        callListeners(_bannerListener, "load", bannerId, banner.getWidth(), banner.getHeight());
    }

    @Override
    public void onFailed(AdBanner banner, AdBanner.Error error) {
        callListeners(_bannerListener, "fail", errorToJSON((int) error.code, error.message));
    }

    @Override
    public void onClicked(AdBanner banner) {
        callListeners(_bannerListener, "click", findBannerId(banner));
    }

    @Override
    public void onExpanded(AdBanner banner) {
        callListeners(_bannerListener, "show", findBannerId(banner));
    }

    @Override
    public void onCollapsed(AdBanner banner) {
        callListeners(_bannerListener, "dismiss", findBannerId(banner));
    }

    @Override
    public void onLoaded(AdInterstitial interstitial) {
        callListeners(_interstitialListener, "load", findInterstitialId(interstitial));
    }

    @Override
    public void onFailed(AdInterstitial interstitial, AdInterstitial.Error error) {
        callListeners(_interstitialListener, "fail", findInterstitialId(interstitial), errorToJSON((int) error.code, error.message));
    }

    @Override
    public void onClicked(AdInterstitial interstitial) {
        callListeners(_interstitialListener, "click", findInterstitialId(interstitial));
    }

    @Override
    public void onShown(AdInterstitial interstitial) {
        callListeners(_interstitialListener, "show", findInterstitialId(interstitial));
    }

    @Override
    public void onDismissed(AdInterstitial interstitial) {
        callListeners(_interstitialListener, "dismiss", findInterstitialId(interstitial));
    }

    @Override
    public void onLoaded(AdRewardedVideo rewardedVideo) {
        callListeners(_rewardedVideoListener, "load", findRewardedVideoId(rewardedVideo));
    }

    @Override
    public void onFailed(AdRewardedVideo rewardedVideo, AdRewardedVideo.Error error) {
        callListeners(_rewardedVideoListener, "fail", findRewardedVideoId(rewardedVideo), errorToJSON((int) error.code, error.message));
    }

    @Override
    public void onClicked(AdRewardedVideo rewardedVideo) {
        callListeners(_rewardedVideoListener, "click", findRewardedVideoId(rewardedVideo));
    }

    @Override
    public void onShown(AdRewardedVideo rewardedVideo) {
        callListeners(_rewardedVideoListener, "show", findRewardedVideoId(rewardedVideo));
    }

    @Override
    public void onDismissed(AdRewardedVideo rewardedVideo) {
        callListeners(_rewardedVideoListener, "dismiss", findRewardedVideoId(rewardedVideo));
    }

    @Override
    public void onRewardCompleted(AdRewardedVideo rewardedVideo, AdRewardedVideo.Reward reward, AdRewardedVideo.Error error) {
        callListeners(_rewardedVideoListener, "reward", findRewardedVideoId(rewardedVideo), reward, error);
    }

    //Utility methods
    protected void callListeners(CallbackContext ctx, Object... args) {
        JSONArray array = new JSONArray();
        for (Object obj : args) {
            if (obj instanceof AdRewardedVideo.Reward) {
                Log.d(TAG, "This is reward");
                Object rewardObj = getRewardJson((AdRewardedVideo.Reward)obj);
                array.put(rewardObj);
            } else if (obj instanceof AdRewardedVideo.Error) {
                Log.d(TAG, "This is reward error");
                AdRewardedVideo.Error rewardError = (AdRewardedVideo.Error)obj;
                array.put(rewardError.code);
                array.put(rewardError.message);
            } else {
                array.put(obj);
            }
        }
        PluginResult pluginResult = new PluginResult(Status.OK, array);
        pluginResult.setKeepCallback(true);
        Log.d(TAG, "callListeners, isNull: " + array.toString());
        ctx.sendPluginResult(pluginResult);
    }

    Object getRewardJson(AdRewardedVideo.Reward reward) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("amount", reward.amount);
            obj.put("currency", reward.currency);
            obj.put("itemKey", reward.itmKey);
        } catch(JSONException e) {
            return null;
        }
        
        return obj;
    }

    protected void layoutBanner(BannerData data) {
// -----------------------------
        AdView adView = (AdView)(data.banner.getView());
        if (CORDOVA_MIN_4) {
            ViewGroup wvParentView = (ViewGroup)getWebView().getParent();
            if (parentView == null) {
                parentView = new LinearLayout(webView.getContext());
            }
            if (wvParentView != null && wvParentView != parentView) {
                wvParentView.removeView(getWebView());
                ((LinearLayout) parentView).setOrientation(LinearLayout.VERTICAL);
                parentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
                getWebView().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
                parentView.addView(getWebView());
                cordova.getActivity().setContentView(parentView);
        }

        } else {
            parentView = (ViewGroup) ((ViewGroup) webView).getParent();
        }

        if (data.layout == BannerLayout.CUSTOM) {
            // layoutParams.leftMargin = (int) data.x;
            // layoutParams.topMargin = (int) data.y;
        } else if (data.layout == BannerLayout.TOP_CENTER) {
            // layoutParams.leftMargin = (int) (vg.getWidth() * 0.5 - layoutParams.width * 0.5);
            // layoutParams.topMargin = 0;
            parentView.addView(adView, 0);
        } else {
            parentView.addView(adView);
            // layoutParams.leftMargin = (int) (vg.getWidth() * 0.5 - layoutParams.width * 0.5);
            // layoutParams.topMargin = vg.getHeight() - layoutParams.height;
        }
        // if (bannerAtTop) {
        //     parentView.addView(adView, 0);
        // } else {
        //     parentView.addView(adView);
        // }




        parentView.bringToFront();
        parentView.requestLayout();

        adView.setVisibility( View.VISIBLE );


// -----------------------------

        // if (data.banner == null || data.banner.getView().getParent() == null) {
        //     return;
        // }

        // ViewGroup vg = getViewGroup();
        // FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) data.banner.getView().getLayoutParams();
        // layoutParams.width = data.banner.getWidth();
        // layoutParams.height = data.banner.getHeight();

        // if (data.layout == BannerLayout.CUSTOM) {
        //     layoutParams.leftMargin = (int) data.x;
        //     layoutParams.topMargin = (int) data.y;
        // } else if (data.layout == BannerLayout.TOP_CENTER) {
        //     layoutParams.leftMargin = (int) (vg.getWidth() * 0.5 - layoutParams.width * 0.5);
        //     layoutParams.topMargin = 0;
        // } else {
        //     layoutParams.leftMargin = (int) (vg.getWidth() * 0.5 - layoutParams.width * 0.5);
        //     layoutParams.topMargin = vg.getHeight() - layoutParams.height;
        // }
        // vg.requestLayout();
    }

    protected ViewGroup getViewGroup() {
        return (ViewGroup) this.cordova.getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
    }

    protected JSONObject errorToJSON(int code, String message) {
        JSONObject result = new JSONObject();
        try {
            result.put("code", code);
            result.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected String findBannerId(AdBanner banner) {
        for (String key : _banners.keySet()) {
            BannerData data = _banners.get(key);
            if (data.banner == banner) {
                return key;
            }
        }
        return "";
    }

    protected String findInterstitialId(AdInterstitial interstitial) {
        for (String key : _interstitials.keySet()) {
            if (_interstitials.get(key) == interstitial) {
                return key;
            }
        }
        return "";
    }

    protected String findRewardedVideoId(AdRewardedVideo rewardedVideo) {
        for (String key : _rewardedVideos.keySet()) {
            if (_rewardedVideos.get(key) == rewardedVideo) {
                return key;
            }
        }
        return "";
    }

    protected String getId(CordovaArgs args) {
        return args.optString(0);
    }


    private View getWebView() {
        try {
            return (View) webView.getClass().getMethod("getView").invoke(webView);
        } catch (Exception e) {
            return (View) webView;
        }
    }

};
