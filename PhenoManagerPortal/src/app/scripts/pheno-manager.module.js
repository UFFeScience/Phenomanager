(function() {
    'use strict';
    angular.module('pheno-manager', [
        'ui.router',
        'ngSanitize',
        'pheno-manager.core',
        'pheno-manager.user',
        'pheno-manager.team',
        'pheno-manager.project',
        'pheno-manager.phenomenon',
        'pheno-manager.hypothesis',
        'pheno-manager.experiment',
        'pheno-manager.computational-model',
        'pheno-manager.permission',
        'pheno-manager.dashboard',
        'angular-jwt',
        'ngAnimate',
        'ui.bootstrap',
        'gridshore.c3js.chart',
        'ngFileSaver',
        'toastr',
        'counter'
    ])
    .run(run);

    run.$inject = ['$rootScope', '$state'];

    function run($rootScope, $state) {
        $rootScope.$state = $state;
    }

})();
