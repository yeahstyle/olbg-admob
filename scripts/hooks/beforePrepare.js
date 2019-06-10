(function() {
  // properties

  const configPreferences = require("../npm/processConfigXml.js");

  // entry
  module.exports = run;

  // builds before platform config
  function run(context) {
  	console.log("beforePrepare...");
    const preferences = configPreferences.read(context);
    const platforms = context.opts.cordova.platforms;
  }
})();
