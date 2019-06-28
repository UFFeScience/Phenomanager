(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('loading', loading);

    loading.$inject = ['$rootScope'];

    function loading($rootScope) {
        return {
            restric: 'AEC',
            templateUrl: '/scripts/core/view/templates/loading.html'
        }
    }
})();
