(function() {
  // properties

  const path = require("path");
  const xmlHelper = require("../lib/xmlHelper.js");

  // entry
  module.exports = {
    read: read
  };

  // read NightSteed config from config.xml
  function read(context) {
    const projectRoot = getProjectRoot(context);
    const configXml = getConfigXml(projectRoot);
    const nightSteedXml = getNightSteedXml(configXml);
    const nightSteedPreferences = getNightSteedPreferences(
      context,
      configXml,
      nightSteedXml
    );

    console.log("Read preferences: " + JSON.stringify(nightSteedPreferences));
 
    validateNightSteedPreferences(nightSteedPreferences);

    return nightSteedPreferences;
  }

  // read config.xml
  function getConfigXml(projectRoot) {
    const pathToConfigXml = path.join(projectRoot, "config.xml");
    const configXml = xmlHelper.readXmlAsJson(pathToConfigXml);

    if (configXml == null) {
      throw new Error(
        "A config.xml is not found in project's root directory."
      );
    }

    return configXml;
  }

  // read <nightsteed-config> within config.xml
  function getNightSteedXml(configXml) {
    const nightSteedConfig = configXml.widget["nightsteed-config"];

    if (nightSteedConfig == null || nightSteedConfig.length === 0) {
      throw new Error(
        "<nightsteed-config> tag is not set in the config.xml."
      );
    }

    return nightSteedConfig[0];
  }

  // read <nightsteed-config> properties within config.xml
  function getNightSteedPreferences(context, configXml, nightSteedXml) {
    return {
      projectRoot: getProjectRoot(context),
      projectName: getProjectName(configXml),
      appId: getConfigValue(nightSteedXml, "app-id"),
      testDeviceId: getConfigValue(nightSteedXml, "device-id"),
      isTest: getConfigValue(nightSteedXml, "is-test"),
    };
  }

  // read project root from cordova context
  function getProjectRoot(context) {
    return context.opts.projectRoot || null;
  }

  // read project name from config.xml
  function getProjectName(configXml) {
    let output = null;
    if (configXml.widget.hasOwnProperty("name")) {
      const name = configXml.widget.name[0];
      if (typeof name === "string") {
        output = configXml.widget.name[0];
      } else {
        output = configXml.widget.name[0]._;
      }
    }

    return output;
  }

  // read NightSteed value from <nightsteed-config>
  function getConfigValue(nightSteedXml, key) {
    return nightSteedXml.hasOwnProperty(key) ? nightSteedXml[key][0].$.value : null;
  }

  // validate <nightsteed-config> properties within config.xml
  function validateNightSteedPreferences(preferences) {
    if (preferences.projectRoot === null) {
      throw new Error(
        'Invalid "root" in your config.xml.'
      );
    }
    if (preferences.projectName === null) {
      throw new Error(
        'Invalid "name" in your config.xml.'
      );
    }
    if (preferences.appId === null) {
      throw new Error(
        'Invalid "appId" in <nightsteed-config> in your config.xml.'
      );
    }

    if (preferences.testDeviceId === null) {
      console.log('"testDeviceId" is not defined in <nightsteed-config> in your config.xml.');
    }

    if (preferences.isTest === null) {
      console.log('"isTest" is not defined in <nightsteed-config> in your config.xml. Assuming isTest = false');
    }
  }
})();
