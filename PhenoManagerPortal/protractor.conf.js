var SpecReporter = require('jasmine-spec-reporter');
var specReporter = new SpecReporter({
    displayStacktrace: true,
    displayFailuresSummary: true,
    displaySuccessfulSpec: true,
    displayFailedSpec: true,
    displayPendingSpec: true,
    displaySpecDuration: true,
    displaySuiteNumber: true,
    colors: {
        success: 'green',
        failure: 'red',
        pending: 'cyan'
    },
    prefixes: {
        success: '[OK]   ',
        failure: '[FAIL] ',
        pending: '[PEND] '
    },
    customProcessors: []
});

var HtmlReporter = require('protractor-html-screenshot-reporter');
var htmlReporter = new HtmlReporter({
    baseDirectory: './screenshots'
});

exports.config = {

    allScriptsTimeout: 11000,

    specs: [
        'test/e2e/*.js'
    ],

    rootElement: 'body',

    capabilities: {
        'browserName': 'firefox'
    },

    baseUrl: 'http://localhost:5000',

    framework: 'jasmine2',

    jasmineNodeOpts: {
        defaultTimeoutInterval: 30000,
        showColors: true
    },

    onPrepare: function () {
        jasmine.getEnv().addReporter(specReporter);
        jasmine.getEnv().addReporter(htmlReporter);
    }
};