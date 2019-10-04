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

                getAllModelResultMetadatas: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_result_metadatas';

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

                getModelResultMetadataBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_result_metadatas/' + slug;
                    return $http.get(url);
                },

                getAllModelExecutors: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors';

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

                insertModelExecutor: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors',
                        JSON.stringify(data)
                    );
                },

                updateModelExecutor: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadModelExecutorExecutor: function(data, computationalModelSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors/' + data.get('slug') + '/executor',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteModelExecutor: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors/' + slug,
                        config
                    );
                },

                getModelExecutorBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors/' + slug;
                    return $http.get(url);
                },

                getModelExecutorExecutor: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_executors/' + slug + '/executor';
                    return $http.get(url, { responseType: 'blob' });
                },

                insertModelMetadataExtractor: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors',
                        JSON.stringify(data)
                    );
                },

                updateModelMetadataExtractor: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                uploadModelMetadataExtractorExtractor: function(data, computationalModelSlug) {
                    return $http({
                        method: 'POST',
                        url: baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors/' + data.get('slug') + '/extractor',
                        transformRequest: angular.identity,
                        headers: {
                            'Content-Type': 'form-upload'
                        },
                        data: data
                    });
                },

                deleteModelMetadataExtractor: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors/' + slug,
                        config
                    );
                },

                getModelMetadataExtractorBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors/' + slug;
                    return $http.get(url);
                },

                getModelMetadataExtractorExtractor: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors/' + slug + '/extractor';
                    return $http.get(url, { responseType: 'blob' });
                },

                getAllModelMetadataExtractors: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_metadata_extractors';

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

                getAllExecutionEnvironments: function(offset, limit, computationalModelSlug, filter) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/execution_environments';

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

                insertExecutionEnvironment: function(data, computationalModelSlug) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/execution_environments',
                        JSON.stringify(data)
                    );
                },

                updateExecutionEnvironment: function(data, computationalModelSlug) {
                    return $http.put(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/execution_environments/' + data.slug,
                        JSON.stringify(data)
                    );
                },

                deleteExecutionEnvironment: function(slug, computationalModelSlug) {
                    return $http.delete(
                        baseUrl + '/v1/computational_models/' + computationalModelSlug + '/execution_environments/' + slug,
                        config
                    );
                },

                getExecutionEnvironmentBySlug: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/execution_environments/' + slug;
                    return $http.get(url);
                },

                runModel: function(requestBody) {
                    return $http.post(
                        baseUrl + '/v1/computational_models/' + requestBody.computationalModelSlug + '/run',
                        JSON.stringify(requestBody)
                    );
                },

                getExtractorOutput: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractor_metadatas/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getExecutionOutput: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_result_metadatas/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getAbortOutput: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_result_metadatas/' + slug + '/abort_metadata';
                    return $http.get(url, { responseType: 'blob' });
                },

                getResearchObject: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/model_result_metadatas/' + slug + '/research_object';
                    return $http.get(url);
                },

                downloadExtractorOutput: function(slug, computationalModelSlug) {
                    var url = baseUrl + '/v1/computational_models/' + computationalModelSlug + '/extractor_metadatas/' + slug + '/execution_metadata';
                    return $http.get(url, { responseType: 'blob' });
                }
            }
        }
})();