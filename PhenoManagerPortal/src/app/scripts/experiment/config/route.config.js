(function() {
    'use strict';

    angular
        .module('pheno-manager.experiment')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('experiment-details', {
                parent: 'default',
                url: '/experiments/:slug',
                templateUrl: '/scripts/experiment/view/experiment-details.html',
                controller: 'ExperimentDetailsController as vm',
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