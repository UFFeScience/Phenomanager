(function () {
    'use strict';
  
    angular
        .module('pheno-manager.experiment')
        .service('experimentService', experimentService);
  
        experimentService.$inject = ['$http', 'config', 'localStorageService'];
  
        function experimentService($http, config, localStorageService) {
            var baseUrl = config.baseUrl;
  
            return {
                
                insert: function(data) {
                    return $http.post(
                        baseUrl + '/v1/experiments',
                        JSON.stringify(data)
                    );
                },

                update: function(data) {
                    return $http.put(
                        baseUrl + '/v1/experiments/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                delete: function(slug) {
                    return $http.delete(
                        baseUrl + '/v1/experiments/' + slug,
                        config
                    );
                },
                
                getAll: function(offset, limit, filter) {
                    var url = baseUrl + '/v1/experiments';

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
                    var url = baseUrl + '/v1/experiments/' + slug;
                    return $http.get(url);
                },

                insertPhase: function(data, experimentSlug) {
                    return $http.post(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/phases',
                        JSON.stringify(data)
                    );
                },

                updatePhase: function(data, experimentSlug) {
                    return $http.put(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/phases/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                deletePhase: function(slug, experimentSlug) {
                    return $http.delete(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/phases/' + slug,
                        config
                    );
                },
                
                getAllPhases: function(offset, limit, experimentSlug, filter) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/phases';

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

                getPhaseBySlug: function(slug, experimentSlug) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/phases/' + slug;
                    return $http.get(url);
                },

                insertConceptualParam: function(data, experimentSlug) {
                    return $http.post(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/conceptual_params',
                        JSON.stringify(data)
                    );
                },

                updateConceptualParam: function(data, experimentSlug) {
                    return $http.put(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/conceptual_params/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                deleteConceptualParam: function(slug, experimentSlug) {
                    return $http.delete(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/conceptual_params/' + slug,
                        config
                    );
                },
                
                getAllConceptualParams: function(offset, limit, experimentSlug, filter) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/conceptual_params';

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

                getConceptualParamBySlug: function(slug, experimentSlug) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/conceptual_params/' + slug;
                    return $http.get(url);
                },

                insertValidationItem: function(data, experimentSlug) {
                    return $http.post(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items',
                        JSON.stringify(data)
                    );
                },

                updateValidationItem: function(data, experimentSlug) {
                    return $http.put(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadValidationEvidence: function(data, experimentSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items/' + data.get('slug') + '/validation_evidence',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteValidationItem: function(slug, experimentSlug) {
                    return $http.delete(
                        baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items/' + slug,
                        config
                    );
                },
                
                getAllValidationItems: function(offset, limit, experimentSlug, filter) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items';

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

                getValidationItemBySlug: function(slug, experimentSlug) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items/' + slug;
                    return $http.get(url);
                },

                getValidationEvidence: function(slug, experimentSlug) {
                    var url = baseUrl + '/v1/experiments/' + experimentSlug + '/validation_items/' + slug + '/validation_evidence';
                    return $http.get(url, { responseType: 'blob' });
                }
            }
        }
})();