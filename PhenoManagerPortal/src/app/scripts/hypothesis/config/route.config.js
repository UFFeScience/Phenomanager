(function() {
    'use strict';

    angular
        .module('pheno-manager.hypothesis')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('hypothesis-details', {
                parent: 'default',
                url: '/hypotheses/:slug',
                templateUrl: '/scripts/hypothesis/view/hypothesis-details.html',
                controller: 'HypothesisDetailsController as vm',
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
