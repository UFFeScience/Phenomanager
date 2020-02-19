(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('loadOverlay', loadOverlay);

    loadOverlay.$inject = ['$rootScope'];

    function loadOverlay($rootScope) {
        return {
            restric: 'E',
            templateUrl: '/scripts/core/view/templates/load-overlay.html'
        }
    }

})();