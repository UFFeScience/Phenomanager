(function () {
    'use strict';
  
    angular
        .module('pheno-manager.hypothesis')
        .service('hypothesisService', hypothesisService);
  
    hypothesisService.$inject = ['$http', 'config', 'localStorageService'];

    function hypothesisService($http, config, localStorageService) {
        var baseUrl = config.baseUrl;

        return {
            
            insert: function(data) {
                return $http.post(
                    baseUrl + '/v1/hypotheses',
                    JSON.stringify(data)
                );
            },

            update: function(data) {
                return $http.put(
                    baseUrl + '/v1/hypotheses/' + data.slug,
                    JSON.stringify(data)
                );
            },

            delete: function(slug) {
                return $http.delete(
                    baseUrl + '/v1/hypotheses/' + slug,
                    config
                );
            },
            
            getAll: function(offset, limit, filter) {
                var url = baseUrl + '/v1/hypotheses';

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
                var url = baseUrl + '/v1/hypotheses/' + slug;
                return $http.get(url);
            }
        }
    }

})();