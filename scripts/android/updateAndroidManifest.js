(function() {
  // properties

  const path = require("path");
  const xmlHelper = require("../lib/xmlHelper.js");

  // entry
  module.exports = {
    writePreferences: writePreferences
  };

  // injects config.xml preferences into AndroidManifest.xml file.
  function writePreferences(context, preferences) {
    // read manifest
    const manifest = getManifest(context);

    // update manifest
    manifest.file = updateNightSteedMetaData(manifest.file, preferences);

    // save manifest
    xmlHelper.writeJsonAsXml(manifest.path, manifest.file);
  }

  // get AndroidManifest.xml information
  function getManifest(context) {
    let pathToManifest;
    let manifest;

    try {
      // cordova platform add android@6.0.0
      pathToManifest = path.join(
        context.opts.projectRoot,
        "platforms",
        "android",
        "AndroidManifest.xml"
      );
      manifest = xmlHelper.readXmlAsJson(pathToManifest);
    } catch (e) {
      try {
        // cordova platform add android@7.0.0
        pathToManifest = path.join(
          context.opts.projectRoot,
          "platforms",
          "android",
          "app",
          "src",
          "main",
          "AndroidManifest.xml"
        );
        manifest = xmlHelper.readXmlAsJson(pathToManifest);
      } catch (e) {
        throw new Error(`Cannot read AndroidManfiest.xml ${e}`);
      }
    }
    const mainActivityIndex = getMainLaunchActivityIndex(
      manifest.manifest.application[0].activity
    );
    const targetSdk =
      manifest.manifest["uses-sdk"][0].$["android:targetSdkVersion"];

    return {
      file: manifest,
      path: pathToManifest,
      mainActivityIndex: mainActivityIndex,
      targetSdk: targetSdk
    };
  }

  // adds to <application>:
  //    <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="<<app id>>" />
  function updateNightSteedMetaData(manifest, preferences) {
    let metadatas = manifest.manifest.application[0]["meta-data"] || [];
    const metadata = [];

    // remove old
    metadatas = removeBasedOnAndroidName(metadatas, "com.google.android.gms.ads.APPLICATION_ID");
    metadata.push({
        $: {
          "android:name": "com.google.android.gms.ads.APPLICATION_ID",
          "android:value": preferences.appId
        }
      });

    manifest.manifest.application[0]["meta-data"] = metadatas.concat(metadata);

    return manifest;
  }

  // generate the array dictionary for <data> component for the App Link intent filter
  function getAppLinkIntentFilterDictionary(linkDomain, androidPrefix) {
    const scheme = "https";
    const output = {
      $: {
        "android:host": linkDomain,
        "android:scheme": scheme
      }
    };

    if (androidPrefix) {
      output.$["android:pathPrefix"] = androidPrefix;
    }

    return output;
  }

  // remove previous NightSteed related <meta-data> and <receiver> based on android:name
  function removeBasedOnAndroidName(items, androidName) {
    const without = [];
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (item.hasOwnProperty("$") && item.$.hasOwnProperty("android:name")) {
        const key = item.$["android:name"];
        if (key === androidName) {
          continue;
        }
        without.push(item);
      } else {
        without.push(item);
      }
    }
    return without;
  }

  // get the main <activity> because NightSteed Intent Filters must be in the main Launch Activity
  function getMainLaunchActivityIndex(activities) {
    let launchActivityIndex = -1;

    for (let i = 0; i < activities.length; i++) {
      const activity = activities[i];
      if (isLaunchActivity(activity)) {
        launchActivityIndex = i;
        break;
      }
    }

    return launchActivityIndex;
  }

  // determine if <activity> is the main activity
  function isLaunchActivity(activity) {
    const intentFilters = activity["intent-filter"];
    let isLauncher = false;

    if (intentFilters == null || intentFilters.length === 0) {
      return false;
    }

    isLauncher = intentFilters.some(intentFilter => {
      const action = intentFilter.action;
      const category = intentFilter.category;

      if (
        action == null ||
        action.length !== 1 ||
        category == null ||
        category.length !== 1
      ) {
        return false;
      }

      const isMainAction =
        action[0].$["android:name"] === "android.intent.action.MAIN";
      const isLauncherCategory =
        category[0].$["android:name"] === "android.intent.category.LAUNCHER";

      return isMainAction && isLauncherCategory;
    });

    return isLauncher;
  }
})();
