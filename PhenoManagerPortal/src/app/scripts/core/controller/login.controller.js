(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .controller('LoginController', LoginController);

    LoginController.$inject = ['$scope', '$location', 'authLoginService', '$window', 'jwtHelper', 'localStorageService', '$rootScope', '$state'];

    function LoginController($scope, $location, authLoginService, $window, jwtHelper, localStorageService, $rootScope, $state) {
        var vm = this;
        
        vm.user = {};
        vm.loading = false;

    	vm.login = function() {
            vm.loading = true;
            vm.error = false;

    		authLoginService
                .auth(vm.user)
    	        .then(function(resp) {
                    localStorageService.setToken(resp.data.token);
                    $state.go('dashboard');
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loading = false;
                    vm.error = true;
    	            vm.message = resp.data.message;
                });
        };
    }

})();