(function() {
    'use strict';

    angular
        .module('pheno-manager.computational-model')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('computational-model-details', {
                parent: 'default',
                url: '/computational-models/:slug',
                templateUrl: '/scripts/computationalModel/view/computational-model-details.html',
                controller: 'ComputationalModelDetailsController as vm',
                controllerAs: 'vm',
                resolve: {
                    check: ['$state', '$timeout', 'localStorageService', function ($state, $timeout, localStorageService) {
                        if (localStorageService.verifyTokenExpiration()) {
                            $timeout(function() {
                                $state.go('login');
                            });
                        } 
                    }]
                }
            });

    }

})();