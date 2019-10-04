(function() {
    'use strict';

    angular
        .module('pheno-manager.user')
        .config(route);

    route.$inject = ['$stateProvider'];

    function route($stateProvider) {

        $stateProvider
            .state('list-users', {
                parent: 'default',
                url: '/users',
                templateUrl: '/scripts/user/view/list-users.html',
                controller: 'UserController as vm',
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
