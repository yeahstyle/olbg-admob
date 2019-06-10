(function() {
  // properties

  const configPreferences = require("../npm/processConfigXml.js");
  const androidManifest = require("../android/updateAndroidManifest.js");
  const ANDROID = "android";

  // entry
  module.exports = run;

  // builds after platform config
  function run(context) {
    console.log("afterPrepare...");
    const preferences = configPreferences.read(context);
    const platforms = context.opts.cordova.platforms;
  
    platforms.forEach(platform => {
      if (platform === ANDROID) {
        androidManifest.writePreferences(context, preferences);
      }
    });
  }
})();
