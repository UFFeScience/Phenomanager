module.exports = function (config) {
    config.set({

        basePath: '',

        frameworks: ['jasmine'],
        //frameworks: ['mocha', 'chai', 'sinon', 'chai-sinon'],

        files: [
            './bower_components/jquery/dist/jquery.js',
            './bower_components/angular/angular.js',
            './bower_components/angular-mocks/angular-mocks.js',
            './bower_components/angular-route/angular-route.js',
            './bower_components/bootstrap/dist/js/bootstrap.js',
            './bower_components/angular-intro.js/build/angular-intro.min.js',

            './src/app/**/*.module.js',
            './src/app/**/config.*.js',
            './src/app/**/*.service.js',
            './src/app/**/*.controller.js',

            './src/app/**/*.spec.js'
        ],

        plugins: [
            'karma-phantomjs-launcher',
            'karma-jasmine',
            //'karma-mocha',
            //'karma-chai',
            //'karma-sinon',
            'karma-junit-reporter',
            'karma-html-reporter',
            'karma-coverage',
            'karma-spec-reporter'
        ],

        exclude: [
        ],

        preprocessors: {
            'src/app/**/*.js': 'coverage'
        },

        reporters: ['junit', 'html', 'coverage', 'spec'],

        coverageReporter: {
            dir: 'test_out/coverage/',
            reporters: [
                {type: 'html', dir: 'test_out/coverage/html'},
                {type: 'cobertura', dir: 'test_out/coverage/cobertura', file: 'coverage.xml'},
                {type: 'text', dir: 'test_out/coverage/text', file: 'coverage.txt'},
                {type: 'text-summary'},
                {type: 'lcov', dir: 'test_out/coverage/lcov'}
            ]
        },

        junitReporter: {
            outputFile: 'test_out/unit.xml',
            suite: 'unit'
        },

        htmlReporter: {
            outputDir: 'test_out/karma_html'
        },

        specReporter: {
            maxLogLines: 5
        },

        colors: true,

        logLevel: config.LOG_DISABLE,

        autoWatch: false,

        browsers: ['PhantomJS'],

        singleRun: false
    });
};