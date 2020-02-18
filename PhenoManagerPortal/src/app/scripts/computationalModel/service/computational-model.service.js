(function () {
    'use strict';
  
    angular
        .module('pheno-manager.computational-model')
        .service('computationalModelService', computationalModelService);
  
        computationalModelService.$inject = ['$http', 'config', 'localStorageService'];
  
        function computationalModelService($http, config, localStorageService) {
            var baseUrl = config.baseUrl;
  
            return {
                
                insert: function(data) {
                    return $http.post(
                        baseUrl + '/v1/computational_models',
                        JSON.stringify(data)
                    );
                },

                update: function(data) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                delete: function(slug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + slug,
                        config
                    );
                },
                
                getAll: function(offset, limit, filter) {
                    var url = baseUrl + '/v1/computational_models';

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
                    var url = baseUrl + '/v1/computational_models/' + slug;
                    return $http.get(url);
                },

                getAllExecutions: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executions';

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

                getExecutionBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executions/' + slug;
                    return $http.get(url);
                },

                getAllExecutors: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors';

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

                insertExecutor: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors',
                        JSON.stringify(data)
                    );
                },

                updateExecutor: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadExecutorFile: function(data, computationalModelSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors/' + data.get('slug') + '/executor_file',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteExecutor: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors/' + slug,
                        config
                    );
                },

                getExecutorBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors/' + slug;
                    return $http.get(url);
                },

                getExecutorFile: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executors/' + slug + '/executor_file';
                    return $http.get(url, { responseType: 'blob' });
                },

                insertExtractor: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors',
                        JSON.stringify(data)
                    );
                },

                updateExtractor: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadExtractorFile: function(data, computationalModelSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors/' + data.get('slug') + '/extractor_file',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteExtractor: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors/' + slug,
                        config
                    );
                },

                getExtractorBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors/' + slug;
                    return $http.get(url);
                },

                getExtractorFile: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors/' + slug + '/extractor_file';
                    return $http.get(url, { responseType: 'blob' });
                },

                getAllExtractors: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractors';

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

                insertInstanceParam: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params',
                        JSON.stringify(data)
                    );
                },

                updateInstanceParam: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadInstanceParamValueFile: function(data, computationalModelSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params/' + data.get('slug') + '/value_file',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteInstanceParam: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params/' + slug,
                        config
                    );
                },

                getInstanceParamBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params/' + slug;
                    return $http.get(url);
                },

                getInstanceParamValueFile: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params/' + slug + '/value_file';
                    return $http.get(url, { responseType: 'blob' });
                },

                getAllInstanceParams: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/instance_params';

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

                getAllEnvironments: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/environments';

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

                insertEnvironment: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/environments',
                        JSON.stringify(data)
                    );
                },

                updateEnvironment: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/environments/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                deleteEnvironment: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/environments/' + slug,
                        config
                    );
                },

                getEnvironmentBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/environments/' + slug;
                    return $http.get(url);
                },

                runModel: function(requestBody) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + requestBody.computationalModelSlug + '/run',
                        JSON.stringify(requestBody)
                    );
                },

                getExtractorMetadata: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractor_executions/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getExecutionMetadata: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executions/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getAbortionMetadata: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executions/' + slug + '/abortion_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getResearchObject: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/executions/' + slug + '/research_object';
                    return $http.get(url);
                },

                downloadExtractorOutput: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractor_executions/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                }
            }
        }
})();