! function() {
    var t = window.NightSteed || (window.NightSteed = {});
    window.cordova && "undefined" != typeof module && (module.exports = t), t.version = "1.0", t.extend = function(t, o) {
        var e = t.prototype,
            n = function() {};
        n.prototype = o.prototype, t.prototype = new n, t.superclass = o.prototype, t.prototype.constructor = t, o.prototype.constructor === Object.prototype.constructor && (o.prototype.constructor = o);
        for (var r in e) e.hasOwnProperty(r) && (t.prototype[r] = e[r])
    }, t.exec = function(t, o, e, n, r) {
        window.cordova ? window.cordova.exec(n, r, t, o, e) : console.error("window.cordova not found")
    }, t.define = function(t, o, e) {
        for (var n = "NightSteed." == t.substring(0, 7) ? t.substr(7) : t, r = window.NightSteed, i = n.split("."), s = r, a = 0; a < i.length; a++) {
            var p = i[a];
            if (console.log(s[p] ? "Updated namespace: - " + t : "Created namespace: " + t), s = s[p] = a == i.length - 1 ? o(s[p] || {}) : {}, !s) throw "Unable to create class " + t
        }
        return arguments.length < 2 && (e = !0), e && "undefined" != typeof module && (module.exports = s), !0
    }, t.Signal = function() {
        this.signals = {}
    }, t.Signal.prototype = {
        on: function(t, o) {
            if (!t || !o) throw new Error("Can't create signal " + (t || ""));
            var e = this.signals[t];
            e || (e = [], this.signals[t] = e), e.push(o)
        },
        emit: function(t, o, e) {
            var n = this.signals[t];
            if (n)
                for (var r = 0; r < n.length; ++r) {
                    var i = n[r];
                    o && (i = i[o]), i && i.apply(null, e || [])
                }
        },
        remove: function(t, o) {
            var e = this.signals[t];
            if (e)
                if (o)
                    for (var n = 0; n < e.lenght; ++n) e[n] === o && (e.splice(n, 1), --n);
                else e.lenght = 0
        },
        expose: function() {
            return this.on.bind(this)
        }
    }, t.PlatformType = {
        ANDROID: "android",
        IOS: "ios",
        AMAZON: "amazon",
        WINDOWS_PHONE: "wp",
        BLACKBERRY: "blackberry",
        OTHER: "other"
    };
    var o;
    t.getPlatform = function() {
        if (o) return o;
        var e = navigator.userAgent;
        return o = navigator.isCocoonJS ? /ios/gi.test(e) ? t.PlatformType.IOS : t.PlatformType.ANDROID : /(iPad|iPhone|iPod)/g.test(e) ? t.PlatformType.IOS : /Kindle/i.test(e) || /Silk/i.test(e) || /KFTT/i.test(e) || /KFOT/i.test(e) || /KFJWA/i.test(e) || /KFJWI/i.test(e) || /KFSOWI/i.test(e) || /KFTHWA/i.test(e) || /KFTHWI/i.test(e) || /KFAPWA/i.test(e) || /KFAPWI/i.test(e) ? t.PlatformType.AMAZON : /Android/i.test(e) ? t.PlatformType.ANDROID : /BlackBerry/i.test(navigator.userAgent) ? t.PlatformType.BLACKBERRY : /IEMobile/i.test(navigator.userAgent) ? t.PlatformType.WINDOWS_PHONE : t.PlatformType.OTHER
    }, console.log("Created namespace: NightSteed")
}();