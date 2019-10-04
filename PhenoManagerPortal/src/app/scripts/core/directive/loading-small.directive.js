(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('loadingSmall', loadingSmall);

    loadingSmall.$inject = ['$rootScope'];

    function loadingSmall($rootScope) {
        return {
            restric: 'AEC',
            templateUrl: '/scripts/core/view/templates/loading-small.html'
        }
    }
})();
