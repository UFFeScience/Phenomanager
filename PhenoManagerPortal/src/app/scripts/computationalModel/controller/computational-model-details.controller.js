(function() {
    'use strict';

    angular
        .module('pheno-manager.computational-model')
        .controller('ComputationalModelDetailsController', ComputationalModelDetailsController);

    ComputationalModelDetailsController.$inject = ['$scope', '$q', '$controller', 'Blob', 'FileSaver', '$window', '$stateParams', '$timeout', 'toastr', '$location', 'permissionService', 'computationalModelService', '$rootScope', '$state', '$filter'];

    function ComputationalModelDetailsController($scope, $q, $controller, Blob, FileSaver, $window, $stateParams, $timeout, toastr, $location, permissionService, computationalModelService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'computationalModel'
        }));

        vm.openExecutionOutput = function(executionSlug) {
            vm.updateLogOutput = true;
            vm.getExecution(executionSlug);           
        };

        vm.getExecution = function(executionSlug) {
            computationalModelService
                .getExecutionBySlug(executionSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.execution = resp.data;

                    vm.execution.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.execution.parsedFinishDate = new Date(resp.data.finishDate);
                    vm.execution.parsedStartDate = new Date(resp.data.startDate);

                    if (vm.execution.status !== 'RUNNING') {
                        vm.updateLogOutput = false;
                    
                    } else if (vm.updateLogOutput) {
                        $timeout(function() {
                            if ($location.$$path && $location.$$path.includes('/computational-models/')) {
                                vm.getExecution(executionSlug);
                            }
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.updateLogOutput = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.changeExecutionPage = function() {
            vm.loadingExecution = true;

            computationalModelService
                .getAllExecutions(vm.executionCurrentPage - 1, vm.limit, vm.computationalModel.slug)
                .then(function(resp) {
                    var hasRunningModel = false;
                    var hasRunningExtractor = false;

                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                        resp.data.records[i].parsedFinishDate = new Date(resp.data.records[i].finishDate);
                        resp.data.records[i].parsedStartDate = new Date(resp.data.records[i].startDate);
                    
                        if (resp.data.records[i].status === 'RUNNING' || resp.data.records[i].status === 'SCHEDULED') {
                            hasRunningModel = true;
                        }

                        for (var j = 0; j < resp.data.records[i].extractorExecutions.length; j++) {
                            if (resp.data.records[i].extractorExecutions[j].status === 'RUNNING' || 
                                    resp.data.records[i].extractorExecutions[j].status === 'SCHEDULED') {
                                hasRunningExtractor = true;
                            }
                        }
                    }

                    vm.executions = resp.data.records;
                    vm.totalExecutionCount = resp.data.metadata.totalCount;
                    vm.loadingExecution = false;

                    if (hasRunningModel || hasRunningExtractor || resp.data.records.length === 0) {
                        $timeout(function() {
                            if ($location.$$path && $location.$$path.includes('/computational-models/')) {
                                if (vm.tab === 'executions') {
                                    vm.refreshExecutions();
                                } 
                            }
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExecution = false;
                    toastr.error('Error while loading executions.', 'Unexpected error!');
                });
        };

        vm.refreshExecutions = function() {
            computationalModelService
                .getAllExecutions(vm.executionCurrentPage - 1, vm.limit, vm.computationalModel.slug)
                .then(function(resp) {
                    var hasRunningModel = false;
                    var hasRunningExtractor = false;
                    
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                        resp.data.records[i].parsedFinishDate = new Date(resp.data.records[i].finishDate);
                        resp.data.records[i].parsedStartDate = new Date(resp.data.records[i].startDate);
                    
                        if (resp.data.records[i].status === 'RUNNING' || resp.data.records[i].status === 'SCHEDULED') {
                            hasRunningModel = true;
                        }

                        for (var j = 0; j < resp.data.records[i].extractorExecutions.length; j++) {
                            if (resp.data.records[i].extractorExecutions[j].status === 'RUNNING' || 
                                    resp.data.records[i].extractorExecutions[j].status === 'SCHEDULED') {
                                hasRunningExtractor = true;
                            }
                        }
                    }

                    vm.executions = resp.data.records;
                    vm.totalExecutionCount = resp.data.metadata.totalCount;
               
                    if (hasRunningModel || hasRunningExtractor || resp.data.records.length === 0) {
                        $timeout(function() {
                            if ($location.$$path && $location.$$path.includes('/computational-models/')) {
                                if (vm.tab === 'executions') {
                                    vm.refreshExecutions();
                                } 
                            }
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExecution = false;
                    toastr.error('Error while refreshing model result metadatas.', 'Unexpected error!');
                });
        };

        $scope.setValueFile = function(fileInput) {
            var files = fileInput.files;
            
            vm.errorUploadSize = false;
            vm.loadingUpload = true;

            for (var i = 0, max = files.length; i < max; i++) {
                var file = files[i];
                var extension = file.name.split('.').pop();
                
                if (file.size > 50000000) {
                    vm.errorUploadSize = true;
                    vm.loadingUpload = false;
                    return;
                
                } else {
                    vm.valueFile = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        };

        vm.doUploadValueFile = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.instanceParam.slug);
            formData.append('valueFile', vm.valueFile);

            computationalModelService
                .uploadInstanceParamValueFile(formData, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    vm.changeInstanceParamPage();
                    toastr.success('Success!', 'Action performed with success.');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
            
        };

        vm.downloadInstanceParamValueFile = function(instanceParamSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getInstanceParamValueFile(instanceParamSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'value-file';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);

                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.doDeleteInstanceParam = function(instanceParamSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .deleteInstanceParam(instanceParamSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeInstanceParamPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--; 
                });
        };

        vm.deleteInstanceParam = function(instanceParamSlug) {
            vm.instanceParamSlug = instanceParamSlug;
            vm.valueFile = null;
        };

        vm.editInstanceParam = function(instanceParamSlug) {
            vm.instanceParamSaveTitle = 'Update instance param';
            vm.updateInstanceParam = true;

            computationalModelService
                .getInstanceParamBySlug(instanceParamSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.instanceParam = resp.data;
                    vm.instanceParam.hasValueFile = vm.instanceParam.valueFileId ? true : false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertInstanceParam = function() {
            vm.instanceParam = {
            	hasValueFile: false,
            	conceptualParam: {}
            };
            vm.updateInstanceParam = false;
            vm.instanceParamSaveTitle = 'Create instance param';
        };

        vm.doSaveInstanceParam = function() {
            vm.instanceParam.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            if (!vm.updateInstanceParam) {
                computationalModelService
                    .insertInstanceParam(vm.instanceParam, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.instanceParam = resp.data;

                        if (!vm.valueFile) {
                            vm.changeInstanceParamPage();
                            toastr.success('Action performed with success.', 'Success!');

                        } else {
                            vm.doUploadValueFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                experimentService
                    .updateInstanceParam(vm.instanceParam, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.instanceParam = resp.data;

                        if (!vm.valueFile) {
                            vm.changeInstanceParamPage();
                            toastr.success('Success!', 'Action performed with success.');
                            
                        } else {
                            vm.doUploadValueFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.changeInstanceParamPage = function() {
            vm.loadingInstanceParam = true;
            var filter = null;

            if (vm.filterInstanceParams) {
                filter = 'key=like=' + vm.filterInstanceParams + ',description=like=' + vm.filterInstanceParams;
            }

            computationalModelService
                .getAllInstanceParams(vm.instanceParamCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.instanceParams = resp.data.records;
                    vm.totalInstanceParamCount = resp.data.metadata.totalCount;
                    vm.loadingInstanceParam = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingInstanceParam = false;
                    toastr.error('Error while loading instance params.', 'Unexpected error!');
                });
        };

        $scope.setExecutor = function(fileInput) {
            var files = fileInput.files;
            
            vm.errorUploadSize = false;
            vm.loadingUpload = true;

            for (var i = 0, max = files.length; i < max; i++) {
                var file = files[i];
                var extension = file.name.split('.').pop();
                
                if (file.size > 50000000) {
                    vm.errorUploadSize = true;
                    vm.loadingUpload = false;
                    return;
                
                } else {
                    vm.executorFile = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        };

        vm.doUploadExecutorFile = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.executor.slug);
            formData.append('executorFile', vm.executorFile);

            computationalModelService
                .uploadExecutorFile(formData, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    
                    vm.changeExecutorPage();

                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
            
        };

        vm.downloadExecuctorFile = function(executorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExecutorFile(executorSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'executor';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.downloadExecutorMetadata = function(executionSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExecutionMetadata(executionSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'execution-metadata.txt';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.downloadAbortionMetadata = function(executionSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getAbortionMetadata(executionSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'abortion-metadata.txt';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.downloadExtractorMetadata = function(extractorExecutionSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExtractorMetadata(extractorExecutionSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'extraction-metadata';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.generateResearchObject = function(executionSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getResearchObject(executionSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.instanceParam = resp.data;
                    var data = new Blob([JSON.stringify(resp.data, null, '\t')], { type: 'application/json' });
                    FileSaver.saveAs(data, 'research-object.json');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.doDeleteExecutor = function(executorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .deleteExecutor(executorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExecutorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.deleteExecutor = function(executorSlug) {
            vm.executorSlug = executorSlug;
            vm.executorFile = null;
        };

        vm.editExecutor = function(executorSlug) {
            vm.executorSaveTitle = 'Update executor';
            vm.updateExecutor = true;

            computationalModelService
                .getExecutorBySlug(executorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.executor = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertExecutor = function() {
            vm.executor = {};
            vm.updateExecutor = false;
            vm.executorSaveTitle = 'Create executor';
        };

        vm.doSaveExecutor = function() {
            vm.executor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            if (!vm.updateExecutor) {
                computationalModelService
                    .insertExecutor(vm.executor, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.executor = resp.data;

                        if (!vm.executorFile) {
                            vm.changeExecutorPage();
                            toastr.success('Action performed with success.', 'Success!');

                        } else {
                            vm.doUploadExecutorFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateExecutor(vm.executor, vm.computationalModel.slug)
                    .then(function(resp) {
                        
                        if (!vm.executorFile) {
                            vm.changeExecutorPage();
                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadExecutorFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.doAbortExecutor = function(executionSlug, uploadMetadata) {
            var requestBody = {
                executionSlug: executionSlug,
                executionCommand: 'STOP',
                uploadMetadata: uploadMetadata,
                computationalModelVersion: vm.computationalModel.currentVersion,
                computationalModelSlug: vm.computationalModel.slug
            };

            for (var i = 0; i < vm.executions.length; i++) {
                if (vm.executions[i].slug === executionSlug) {
                    vm.executions[i].hasAbortionRequested = true;
                }
            }

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeExecutionPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.runExecutor = function(executorSlug) {
            vm.executionData = {
                uploadMetadata: false
            };
            vm.executorSlug = executorSlug;
        };

        vm.abortExecution = function(executionSlug) {
            vm.executionData = {
                uploadMetadata: false
            };
            vm.executionSlug = executionSlug;
        };

        vm.runExtractor = function(extractorSlug) {
            vm.executionData = {
                uploadMetadata: false
            };
            vm.extractorSlug = extractorSlug;
        };

        vm.doRunExecutor = function(executorSlug, environmentForExecution, executionExtractors, uploadMetadata) {
            var requestBody = {
                executorSlug: executorSlug,
                executionCommand: 'START',
                uploadMetadata: uploadMetadata,
                computationalModelVersion: vm.computationalModel.currentVersion,
                computationalModelSlug: vm.computationalModel.slug
            };  

            if (environmentForExecution && environmentForExecution.slug) {
                requestBody.environmentSlug = environmentForExecution.slug;
            }

            if (executionExtractors && executionExtractors.length > 0) {
                var extractorSlugs = [];
                for (var i = 0; i < executionExtractors.length; i++) {
                    extractorSlugs.push(executionExtractors[i].slug);
                }
                requestBody.executionExtractorSlugs = extractorSlugs;
            }

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeExecutorPage();

                    $timeout(function() {
                        if ($location.$$path && $location.$$path.includes('/computational-models/')) {
                            if (vm.tab === 'executions') {
                                vm.refreshExecutions();
                            }
                        }
                    }, 2000);
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.changeExecutorPage = function() {
            vm.loadingExecutor = true;
            var filter = null;

            if (vm.filterExecutors) {
                filter = 'tag=like=' + vm.filterExecutors;
            }

            computationalModelService
                .getAllExecutors(vm.executorCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.executors = resp.data.records;
                    vm.totalExecutorCount = resp.data.metadata.totalCount;
                    vm.loadingExecutor = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExecutor = false;
                    toastr.error('Error while loading executors.', 'Unexpected error!');
                });
        };

        $scope.setExtractorFile = function(fileInput) {
            var files = fileInput.files;
            
            vm.errorUploadSize = false;
            vm.loadingUpload = true;

            for (var i = 0, max = files.length; i < max; i++) {
                var file = files[i];
                var extension = file.name.split('.').pop();
                
                if (file.size > 50000000) {
                    vm.errorUploadSize = true;
                    vm.loadingUpload = false;
                    return;
                
                } else {
                    vm.extractorFile = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        };

        vm.doUploadExtractorFile = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.extractor.slug);
            formData.append('extractorFile', vm.extractorFile);

            computationalModelService
                .uploadExtractorFile(formData, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    vm.changeExtractorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.doRunExtractor = function(extractorSlug, environmentForExecution, uploadMetadata) {
            var requestBody = {
                extractorSlug: extractorSlug,
                executionCommand: 'START',
                uploadMetadata: uploadMetadata,
                computationalModelVersion: vm.computationalModel.currentVersion,
                computationalModelSlug: vm.computationalModel.slug
            };

            if (environmentForExecution && environmentForExecution.slug) {
                requestBody.environmentSlug = environmentForExecution.slug;
            }

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeExtractorPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.downloadExtractorFile = function(extractorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExtractorFile(extractorSlug, vm.computationalModel.slug)
                    .then(function(resp) {
                        if (resp.status === 200) {
                            var disposition = resp.headers('Content-Disposition');
                            var contentType = resp.headers('content-type');
                            var fileName;
                            
                            if (disposition !== undefined && !fileName) {
                                var temp = disposition.split('filename=');
                                if (temp.length === 2) {
                                    fileName = temp[1];
                                }
                            }

                            if (!fileName) {
                                fileName = 'extractor';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        };

        vm.doDeleteExtractor = function(extractorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .deleteExtractor(extractorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExtractorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.deleteExtractor = function(extractorSlug) {
            vm.extractorSlug = extractorSlug;
            vm.extractor = null;
        };

        vm.editExtractor = function(extractorSlug) {
            vm.extractorSaveTitle = 'Update extractor';
            vm.updateExtractor = true;

            computationalModelService
                .getExtractorBySlug(extractorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.extractor = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertExtractor = function() {
            vm.extractor = {};
            vm.updateExtractor = false;
            vm.extractorSaveTitle = 'Create extractor';
        };

        vm.doSaveExtractor = function() {
            vm.extractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            if (!vm.updateExtractor) {
                computationalModelService
                    .insertExtractor(vm.extractor, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.extractor = resp.data;    

                        if (!vm.extractorFile) {
                            vm.changeExtractorPage();
                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadExtractorFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateExtractor(vm.extractor, vm.computationalModel.slug)
                    .then(function(resp) {
                        
                        if (!vm.extractor) {
                            vm.changeExtractorPage();
                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadExtractorFile();
                        }
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.doActivateExtractor = function() {
            vm.extractor.active = true;
            vm.extractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            computationalModelService
                .updateExtractor(vm.extractor, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExtractorPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.doDeactivateExtractor = function() {
            vm.extractor.active = false;
            vm.extractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            computationalModelService
                .updateExtractor(vm.extractor, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExtractorPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.changeExtractorPage = function() {
            vm.loadingExtractor = true;
            var filter = null;

            if (vm.filterExtractors) {
                filter = 'tag=like=' + vm.filterExtractors;
            }

            computationalModelService
                .getAllExtractors(vm.extractorCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.extractors = resp.data.records;
                    vm.totalExtractorCount = resp.data.metadata.totalCount;
                    vm.loadingExtractor = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExtractor = false;
                    toastr.error('Error while loading extractors.', 'Unexpected error!');
                });
        };
        
        vm.doSaveComputationalModel = function() {
            computationalModelService
                .update(vm.computationalModel)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.doDeleteEnvironment = function(environmentSlug) {
            computationalModelService
                .deleteEnvironment(environmentSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeEnvironmentPage();
                    toastr.success('Action performed with success.', 'Success!');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.deleteEnvironment = function(environmentSlug) {
            vm.environmentSlug = environmentSlug;
        };

        vm.editEnvironment = function(environmentSlug) {
            vm.environmentSaveTitle = 'Update execution environment';
            vm.updateEnvironment = true;

            vm.showNewVirtualMachine = false;
            vm.type = undefined;
            vm.financialCost = undefined;
            vm.diskSpace = undefined;
            vm.ram = undefined;
            vm.gflops = undefined;
            vm.platform = undefined;
            vm.numberOfCores = undefined;
            
            computationalModelService
                .getEnvironmentBySlug(environmentSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.environment = resp.data;

                    if (!vm.environment.vpnType) {
                        vm.environment.vpnType = 'NONE';
                    }
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertEnvironment = function() {
            vm.environment = {
                virtualMachines: [],
                vpnType : 'NONE'
            };
            
            vm.updateEnvironment = false;
            vm.environmentSaveTitle = 'Create execution environment';
            
            vm.showNewVirtualMachine = false;
            vm.type = undefined;
            vm.financialCost = undefined;
            vm.diskSpace = undefined;
            vm.ram = undefined;
            vm.gflops = undefined;
            vm.platform = undefined;
            vm.numberOfCores = undefined;
        };

        vm.doSaveEnvironment = function() {
            vm.environment.computationalModel = {
                slug: vm.computationalModel.slug
            };

            if (vm.environment.vpnType === 'NONE') {
                delete vm.environment.vpnType;
                delete vm.environment.vpnConfiguration;
            }
            
            if (!vm.updateEnvironment) {
                computationalModelService
                    .insertEnvironment(vm.environment, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.changeEnvironmentPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateEnvironment(vm.environment, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.changeEnvironmentPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.addVirtualMachine = function(type, financialCost, diskSpace, ram, gflops, platform, numberOfCores) {
            if (type && financialCost && diskSpace && ram && gflops && platform && numberOfCores) {

                vm.environment.virtualMachines = vm.environment.virtualMachines || [];
                vm.environment.virtualMachines.push({
                    type: type,
                    financialCost: financialCost,
                    diskSpace: diskSpace,
                    ram: ram,
                    gflops: gflops,
                    platform: platform,
                    numberOfCores: numberOfCores
                });

                vm.showNewVirtualMachine = false;

                vm.type = undefined;
                vm.financialCost = undefined;
                vm.diskSpace = undefined;
                vm.ram = undefined;
                vm.gflops = undefined;
                vm.platform = undefined;
                vm.numberOfCores = undefined;
            } 
        };

        vm.toggleNewVirtualMachine = function(toggle) {
            vm.showNewVirtualMachine = toggle;
        };

        vm.removeVirtualMachine = function(i) {
            if (vm.environment.virtualMachines) {
                vm.environment.virtualMachines.splice(i, 1);
            }
        };

        vm.changeEnvironmentPage = function() {
            vm.loadingEnvironment = true;
            var filter = null;

            if (vm.filterEnvironment) {
                filter = 'tag=like=' + vm.filterEnvironment;
            }

            computationalModelService
                .getAllEnvironments(vm.environmentCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.environments = resp.data.records;
                    vm.totalEnvironmentCount = resp.data.metadata.totalCount;
                    vm.loadingEnvironment = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingEnvironment = false;
                    toastr.error('Error while loading execution environments.', 'Unexpected error!');
                });
        };

        function getComputationalModel() {
            vm.loadingComputationalModel = true;

            computationalModelService
                .getBySlug(vm.computationalModelSlug)
                .then(function(resp) {
                	resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.computationalModel = resp.data;
                    vm.checkWriteAccess(vm.computationalModel);
                    vm.loadingComputationalModel = false;

                    vm.changeExecutionPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingComputationalModel = false;
                    toastr.error('Error while loading computational model.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            $('body').off('hidden.bs.modal', '#output-detail-execution')
            .on('hidden.bs.modal', '#output-detail-execution', function() {
                vm.updateLogOutput = false;
            });

            vm.tab = 'executions';
            vm.computationalModelSaveTitle = 'Update computational model';
            vm.updateComputationalModel = true;

            vm.computationalModelSlug = $stateParams.slug;
            vm.computationalModel = {};
        
            vm.limit = 20;

            vm.totalExecutionCount = 0;
            vm.executionCurrentPage = 1;
            vm.executions = [];
            vm.execution = {};
            vm.updateExecution = false;

            vm.totalExecutorCount = 0;
            vm.executorCurrentPage = 1;
            vm.executors = [];
            vm.executor = {};
            vm.updateExecutor = false;

            vm.totalExtractorCount = 0;
            vm.extractorCurrentPage = 1;
            vm.extractors = [];
            vm.extractor = {};
            vm.updateExtractor = false;

            vm.totalEnvironmentCount = 0;
            vm.environmentCurrentPage = 1;
            vm.environments = [];
            vm.environment = {};
            vm.updateEnvironment = false;

            vm.totalInstanceParamCount = 0;
            vm.instanceParamCurrentPage = 1;
            vm.instanceParams = [];
            vm.instanceParam = {};
            vm.updateInstanceParam = false;

            vm.types = [{
                'value': 'WORKFLOW',
                'name': 'Workflow'
            }, {
                'value': 'EXECUTABLE',
                'name': 'Executable'
            }, {
                'value': 'COMMAND',
                'name': 'Command'
            }, {
                'value': 'WEB_SERVICE',
                'name': 'Web Service'
            }];

            vm.httpVerbs = [{
                'value': 'GET',
                'name': 'GET'
            }, {
                'value': 'POST',
                'name': 'POST'
            }, {
                'value': 'PUT',
                'name': 'PUT'
            }, {
                'value': 'DELETE',
                'name': 'DELETE'
            }];

            vm.webServiceTypes = [{
                'value': 'REST',
                'name': 'REST'
            }, {
                'value': 'SOAP',
                'name': 'SOAP'
            }];

            vm.environmentTypes = [{
                'value': 'SSH',
                'name': 'ssh'
            }, {
                'value': 'CLOUD',
                'name': 'Cloud'
            }, {
                'value': 'CLUSTER',
                'name': 'Cluster'
            }];

            getComputationalModel();
        }
    }

})();