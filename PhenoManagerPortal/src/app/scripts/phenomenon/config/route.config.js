(function() {
    'use strict';

    angular
        .module('pheno-manager.phenomenon')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('phenomenon-details', {
                parent: 'default',
                url: '/phenomenons/:slug',
                templateUrl: '/scripts/phenomenon/view/phenomenon-details.html',
                controller: 'PhenomenonDetailsController as vm',
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
