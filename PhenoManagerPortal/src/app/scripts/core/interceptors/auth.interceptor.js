(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .service('authInterceptor', authInterceptor);

    authInterceptor.$inject = ['$window', '$q', '$timeout', 'localStorageService', '$injector'];

    function authInterceptor($window, $q, $timeout, localStorageService, $injector) {
        return {
            request: function(config) {
                config.headers = config.headers || {};
                
                if (localStorageService.getToken()) {
                    config.headers['x-access-token'] = localStorageService.getToken();
                    config.headers['Authorization'] = 'Bearer ' + localStorageService.getToken();
                }

                if (!config.headers['Content-Type']) {
                    config.headers['Content-Type'] = 'application/json';
                
                } else if (config.headers['Content-Type'] === 'form-upload') {
                    delete config.headers['Content-Type']
                } 

                return config;
            },

            responseError: function(rejection) {
                if (rejection !== null && rejection.status === 403 || rejection.status === 401) {
                    localStorageService.deleteToken();
                    
                    $timeout(function () {
                        $injector.get('$state').go('login');
                    });
                }

                return $q.reject(rejection);
            }
        }
    }

})();