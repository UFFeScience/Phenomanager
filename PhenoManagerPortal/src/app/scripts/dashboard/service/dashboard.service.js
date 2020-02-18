(function () {
    'use strict';
  
    angular
        .module('pheno-manager.dashboard')
        .service('dashboardService', dashboardService);
  
        dashboardService.$inject = ['$http', 'config'];
  
        function dashboardService($http, config) {
            var baseUrl = config.baseUrl;
  
            return {

                getValidationStatistics: function() {
                    var url = baseUrl + '/v1/dashboard/validation_item_statistics';
                    return $http.get(url);
                },

                getCountAllRunningModels: function() {
                    var url = baseUrl + '/v1/dashboard/running_models';
                    return $http.get(url);
                },

                getCountAllErrorModels: function() {
                    var url = baseUrl + '/v1/dashboard/error_models';
                    return $http.get(url);
                }
            }
        }
})();