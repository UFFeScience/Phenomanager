(function() {
    'use strict';

    angular
        .module('pheno-manager.project')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('list-projects', {
                parent: 'default',
                url: '/projects',
                templateUrl: '/scripts/project/view/list-projects.html',
                controller: 'ProjectController as vm',
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

        $stateProvider
            .state('project-details', {
                parent: 'default',
                url: '/projects/:slug',
                templateUrl: '/scripts/project/view/project-details.html',
                controller: 'ProjectDetailsController as vm',
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
