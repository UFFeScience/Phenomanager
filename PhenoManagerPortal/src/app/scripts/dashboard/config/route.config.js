(function() {
    'use strict';

    angular
        .module('pheno-manager.dashboard')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('dashboard', {
                parent: 'default',
                url: '/dashboard',
                templateUrl: '/scripts/dashboard/view/dashboard.html',
                controller: 'DashboardController as vm',
                controllerAs: 'vm',
                resolve: {
                    check: ['$state', '$timeout', 'localStorageService', function ($state, $timeout, localStorageService) {
                        if (!localStorageService.getToken()) {
                            $timeout(function() {
                                $state.go('login');
                            });
                        }
                    }]
                }
            });
        
    }

})();
