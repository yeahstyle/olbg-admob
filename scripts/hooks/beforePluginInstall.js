#!/usr/bin/env node

// Adapted from:
// https://github.com/AllJoyn-Cordova/cordova-plugin-alljoyn/blob/master/scripts/beforePluginInstall.js

var path = require('path');
var exec = require('child_process').exec;

// XXX FUTURE TBD auto-detect:
var package_name = 'xml2js';

module.exports = function (context) {
    var Q = context.requireCordovaModule('q');
    var deferral = new Q.defer();

    console.log('installing external dependencies via npm');

    // exec(   'npm install cordova-custom-config xml2js lodash shelljs colors elementtree lodash plist shelljs tostr xcode',
    exec(   'npm install xml2js q lodash shelljs colors elementtree lodash plist shelljs tostr xcode',
    // exec(   'npm install cordova-custom-config',
            function (error, stdout, stderr) {
                if (error !== null) {
                    // XXX TODO SIGNAL FAILURE HERE.
                    console.log('npm install of external dependencies (npm install xml2js q lodash shelljs colors elementtree lodash plist shelljs tostr xcode) failed: ' + error);
                    deferral.resolve();
                } else {
                    console.log('npm install of external dependencies (cordova-custom-config) ok');
                    deferral.resolve();
                }
            }
    );

    return deferral.promise;
};
