(function () {
    'use strict';
  
    angular
        .module('pheno-manager.project')
        .service('projectService', projectService);
  
        projectService.$inject = ['$http', 'config', 'localStorageService'];
  
        function projectService($http, config, localStorageService) {
            var baseUrl = config.baseUrl;
  
            return {

                insert: function(data) {
                    return $http.post(
                        baseUrl + '/v1/projects',
                        JSON.stringify(data)
                    );
                },

                update: function(data) {
                    return $http.put(
                        baseUrl + '/v1/projects/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                delete: function(slug) {
                    return $http.delete(
                        baseUrl + '/v1/projects/' + slug,
                        config
                    );
                },
                
                getAll: function(offset, limit, filter) {
                    var url = baseUrl + '/v1/projects';

                    if (filter) {
                        url += '?filter=[' + filter + ']';
                    }

                    if (offset && !filter) {
                        url += '?offset=' + (offset * limit) + '&sort=[insertDate=desc]';
                    } else if (offset && filter) {
                        url += '&offset=' + (offset * limit) + '&sort=[insertDate=desc]';
                    } else if (!offset && !filter) {
                        url += '?sort=[insertDate=desc]';
                    }

                    return $http.get(url, config);
                },

                getBySlug: function(slug) {
                    var url = baseUrl + '/v1/projects/' + slug;
                    return $http.get(url);
                },

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
                },

                syncSciManager: function(slug) {
                    return $http.post(
                        baseUrl + '/v1/projects/' + slug + '/sync'
                    );
                },
            }
        }
})();