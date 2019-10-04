(function() {
    'use strict';

    angular
        .module('pheno-manager.team')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('list-teams', {
                parent: 'default',
                url: '/teams',
                templateUrl: '/scripts/team/view/list-teams.html',
                controller: 'TeamController as vm',
                controllerAs: 'vm',
                resolve: {
                    check: ['$state', '$timeout', 'localStorageService', function ($state, $timeout, localStorageService) {
                        if (localStorageService.verifyTokenExpiration()) {
                            $timeout(function() {
                                $state.go('login');
                            });
                        } else if ('ADMIN' !== localStorageService.getRole()) {
                            $state.go('dashboard');
                        }
                    }]
                }
            });
    }

})();
