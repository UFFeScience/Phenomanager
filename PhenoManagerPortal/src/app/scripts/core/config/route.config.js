(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {
        $stateProvider
            .state('root', {
                url: '/',
                redirectTo: 'dashboard'
            });
        
        $stateProvider
            .state('default', {
                url: '',
                abstract: true,
                templateUrl: '/scripts/core/view/default.html',
                resolve: {
                    check: ['$state', '$timeout', 'localStorageService', function($state, $timeout, localStorageService) {
                        if (localStorageService.verifyTokenExpiration()) {
                            $timeout(function() {
                                $state.go('login');
                            });
                        }
                    }]
                }
            });

        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: '/scripts/core/view/login.html',
                controller: 'LoginController as vm',
                controllerAs: 'vm',
                resolve: {
                    check: ['$state', '$timeout', 'localStorageService', function($state, $timeout, localStorageService) {
                        if (!localStorageService.verifyTokenExpiration()) {
                            $timeout(function() {
                                $state.go('dashboard');
                            });
                        }
                    }]
                }
            });
    }

})();