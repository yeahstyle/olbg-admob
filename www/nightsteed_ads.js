!function() {
    !window.NightSteed && window.cordova && "undefined" != typeof require && cordova.require("nightsteed-ads-admob.NightSteedUtils");
    var e = window.NightSteed;
    e.define("Ad", function(i) {
        "use strict";
        i.BannerLayout = {
            TOP_CENTER: "TOP_CENTER",
            BOTTOM_CENTER: "BOTTOM_CENTER",
            CUSTOM: "CUSTOM"
        }, i.BannerSize = {
            SMART: "SMART",
            BANNER: "BANNER",
            MEDIUM_RECT: "MEDIUM_RECT",
            LEADERBOARD: "LEADERBOARD"
        }, i.Banner = function(i, t) {
            this.id = i, this.serviceName = t, this.signal = new e.Signal, this.width = 0, this.height = 0, this.ready = !1
        }, i.Banner.prototype = {
            show: function() {
                e.exec(this.serviceName, "showBanner", [this.id])
            },
            hide: function() {
                e.exec(this.serviceName, "hideBanner", [this.id])
            },
            setLayout: function(i) {
                e.exec(this.serviceName, "setBannerLayout", [this.id, i])
            },
            setPosition: function(i, t) {
                e.exec(this.serviceName, "setBannerPosition", [this.id, i, t])
            },
            load: function() {
                e.exec(this.serviceName, "loadBanner", [this.id])
            },
            isReady: function() {
                return this.ready
            },
            on: function(e, i) {
                this.signal.on(e, i)
            }
        }, i.Interstitial = function(i, t) {
            this.id = i, this.serviceName = t, this.signal = new e.Signal, this.ready = !1
        }, i.Interstitial.prototype = {
            show: function() {
                this.ready = !1, e.exec(this.serviceName, "showInterstitial", [this.id])
            },
            load: function() {
                e.exec(this.serviceName, "loadInterstitial", [this.id])
            },
            isReady: function() {
                return this.ready
            },
            on: function(e, i) {
                this.signal.on(e, i)
            }
        }

        , i.RewardedVideo = function(i, t) {
            this.id = i, this.serviceName = t, this.signal = new e.Signal, this.ready = !1
        }, i.RewardedVideo.prototype = {
            show: function() {
                this.ready = !1, e.exec(this.serviceName, "showRewardedVideo", [this.id])
            },
            load: function() {
                console.log("JS Plugin calling loadRewardedVideo...");
                e.exec(this.serviceName, "loadRewardedVideo", [this.id])
            },
            isReady: function() {
                return this.ready
            },
            on: function(e, i) {
                this.signal.on(e, i)
            }
        };
        var t = 0;
        i.AdService = function(e) {
            console.log("AdService...service " + e);
            this.serviceName = e, this.activeAds = {}
        };

        var n = {};
        i.AdService.prototype = n, 
        n.listenerHandler = function(e) {
            var i = e[0],
                t = e[1],
                n = e.slice(2),
                o = this.activeAds[t];
            o && ("load" === i ? o.ready = !0 : "fail" === i && (o.ready = !1), "load" === i && n.length > 0 && (o.width = n[0], o.height = n[1], n = n.slice(2)), o.signal.emit(i, null, n))
        }, n.init = function() {
            this.initialized || (e.exec(this.serviceName, "setBannerListener", [], this.listenerHandler.bind(this)),
                                 e.exec(this.serviceName, "setInterstitialListener", [], this.listenerHandler.bind(this)),
                                 e.exec(this.serviceName, "setRewardedVideoListener", [], this.listenerHandler.bind(this)),
                                 this.initialized = !0)
        }, n.configure = function(i) {
            console.log("plugin, configure...");
            var t = e.getPlatform();
            t === e.PlatformType.AMAZON && i[e.PlatformType.ANDROID] && (t = e.PlatformType.ANDROID), i[t] && (i = i[t]), e.exec(this.serviceName, "configure", [i])
        }, n.setConsent = function(i) {
            var t = e.getPlatform();
            t === e.PlatformType.AMAZON && i[e.PlatformType.ANDROID] && (t = e.PlatformType.ANDROID), i[t] && (i = i[t]), e.exec(this.serviceName, "setConsent", [i])
        }, n.getConsent = function() {
//            this.consentGiven = i;
            
        }, n.createBanner = function(n, o) {
            this.init();
            var s = t++;
            e.exec(this.serviceName, "createBanner", [s, n, o]);
            var r = new i.Banner(s, this.serviceName);
            return this.activeAds[s] = r, r
        }, n.releaseBanner = function(i) {
            e.exec(this.serviceName, "releaseBanner", [i.id]), delete this.activeAds[id]
        }, n.createInterstitial = function(n) {
            this.init();
            var o = t++;
            e.exec(this.serviceName, "createInterstitial", [o, n]);
            var s = new i.Interstitial(o, this.serviceName);
            return this.activeAds[o] = s, s
        }, n.releaseInterstitial = function(i) {
            e.exec(this.serviceName, "releaseInterstitial", [i.id]), delete this.activeAds[i.id]
        }, n.createRewardedVideo = function(n) {
            this.init();
            var o = t++;
            e.exec(this.serviceName, "createRewardedVideo", [o, n]);
            var s = new i.RewardedVideo(o, this.serviceName);
            return this.activeAds[o] = s, s
        }, n.releaseRewardedVideo = function(i) {
            e.exec(this.serviceName, "releaseRewardedVideo", [i.id]), delete this.activeAds[i.id]
        }, i.serviceName = "LDAdService", 
        i.activeAds = {}

        for (var o in n) {
            n.hasOwnProperty(o) && (i[o] = n[o]);
        }
        return i
    })
}(),
function() {
    NightSteed.define("NightSteed.Ad", function(e) {
        console.log("NightSteed.Ad defined..." + JSON.stringify(e));
        return e.AdMob = new NightSteed.Ad.AdService("LDAdMobPlugin"), e
    })
}(),
function() {
    NightSteed.define("NightSteed.Ad", function(e) {
        return e.MoPub = new NightSteed.Ad.AdService("LDMoPubPlugin"), e
    })
}(),
function() {
    NightSteed.define("NightSteed.Ad", function(e) {
        return e.Chartboost = new NightSteed.Ad.AdService("LDChartboostPlugin"), e
    })
}(),
function() {
    NightSteed.define("NightSteed.Ad", function(e) {
        return e.Heyzap = new NightSteed.Ad.AdService("LDHeyzapPlugin"), e.Heyzap.showDebug = function() {
            NightSteed.exec(this.serviceName, "showDebug", [])
        }, e
    })
}();