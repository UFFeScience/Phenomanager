(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .controller('RootController', RootController);

    RootController.$inject = ['$rootScope'];

    function RootController($rootScope,) {
        $rootScope.isLoading = false;
    }

})();