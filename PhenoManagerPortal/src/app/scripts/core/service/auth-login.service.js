(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .service('authLoginService', authLoginService);

    authLoginService.$inject = ['$http', 'config'];

    function authLoginService($http, config) {
        var baseUrl = config.baseUrl;

        return {
            auth: function(user) {
                return $http.post(
                    baseUrl + '/login',
                    JSON.stringify({ email: user.email, password: user.password })
                );
            }
        }
    }

})();