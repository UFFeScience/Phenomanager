(function () {
    'use strict';
  
    angular
        .module('pheno-manager.phenomenon')
        .service('phenomenonService', phenomenonService);
  
    phenomenonService.$inject = ['$http', 'config', 'localStorageService'];

    function phenomenonService($http, config, localStorageService) {
        var baseUrl = config.baseUrl;

        return {
            
            insert: function(data) {
                return $http.post(
                    baseUrl + '/v1/phenomenons',
                    JSON.stringify(data)
                );
            },

            update: function(data) {
                return $http.put(
                    baseUrl + '/v1/phenomenons/' + data.slug,
                    JSON.stringify(data)
                );
            },

            delete: function(slug) {
                return $http.delete(
                    baseUrl + '/v1/phenomenons/' + slug,
                    config
                );
            },
            
            getAll: function(offset, limit, filter) {
                var url = baseUrl + '/v1/phenomenons';

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
                var url = baseUrl + '/v1/phenomenons/' + slug;
                return $http.get(url);
            }
        }
    }

})();