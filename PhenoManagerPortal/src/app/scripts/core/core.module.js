(function() {
    'use strict';
    angular.module('pheno-manager.core', [
        'ngRoute',
        'pascalprecht.translate'
    ])
    .config(config);

    config.$inject = ['$httpProvider', '$translateProvider', '$locationProvider'];

    function config($httpProvider, $translateProvider, $locationProvider) {
        $httpProvider.interceptors.push('authInterceptor');
        $httpProvider.defaults.useXDomain = true;
        delete $httpProvider.defaults.headers.common['X-Requested-With'];

        $translateProvider.useStaticFilesLoader({
            prefix: '/translate/locale-',
            suffix: '.json'
        });
        $translateProvider.preferredLanguage('pt');

        $locationProvider.hashPrefix('');
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });
    }

})();