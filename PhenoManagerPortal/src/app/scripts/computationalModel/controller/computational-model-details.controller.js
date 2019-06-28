(function() {
    'use strict';

    angular
        .module('pheno-manager.computational-model')
        .controller('ComputationalModelDetailsController', ComputationalModelDetailsController);

        ComputationalModelDetailsController.$inject = ['$scope', '$q', 'Blob', 'FileSaver', '$window', '$stateParams', '$timeout', 'toastr', '$location', 'localStorageService', 'userService', 'permissionService', 'computationalModelService', '$rootScope', '$state', '$filter'];

    function ComputationalModelDetailsController($scope, $q, Blob, FileSaver, $window, $stateParams, $timeout, toastr, $location, localStorageService, userService, permissionService, computationalModelService, $rootScope, $state, $filter) {
        var vm = this;

        vm.permissionAffectsLoggedUser = function(permission) {
            return (permission.user && permission.user.slug === vm.loggedUserSlug) || 
                    vm.teamContainsUser(permission.team, vm.loggedUserSlug);
        }

        vm.hasReadAccess = function(entity) {
            var hasReadAuthorization = false;

            if (!entity || !entity.permissions) {
                return hasReadAuthorization
            }

            for (var i = 0; i < entity.permissions.length; i++) {
                if ((entity.permissions[i].user && vm.loggedUserSlug === entity.permissions[i].user.slug) || 
                     vm.teamContainsUser(entity.permissions[i].team, vm.loggedUserSlug)) {
                    hasReadAuthorization = true;
                    break;
                }
            }

            return hasReadAuthorization;
        }

        vm.hasAdminAccess = function(entity) {
            var hasAdminAuthorization = false;

            if (!entity || !entity.permissions) {
                return hasAdminAuthorization
            }

            for (var i = 0; i < entity.permissions.length; i++) {
                if (entity.permissions[i].role === 'ADMIN' &&
                    ((entity.permissions[i].user && vm.loggedUserSlug === entity.permissions[i].user.slug) || 
                     vm.teamContainsUser(entity.permissions[i].team, vm.loggedUserSlug))) {
                    hasAdminAuthorization = true;
                    break;
                }
            }

            return hasAdminAuthorization;
        }

        vm.hasWriteAccess = function(entity) {
            var hasWriteAuthorization = false;

            if (!entity || !entity.permissions) {
                return hasWriteAuthorization
            }

            for (var i = 0; i < entity.permissions.length; i++) {
                if ((entity.permissions[i].role === 'ADMIN' || entity.permissions[i].role === 'WRITE') &&
                    ((entity.permissions[i].user && vm.loggedUserSlug === entity.permissions[i].user.slug) || 
                     vm.teamContainsUser(entity.permissions[i].team, vm.loggedUserSlug))) {
                    hasWriteAuthorization = true;
                    break;
                }
            }

            return hasWriteAuthorization;
        }

        vm.checkWriteAccess = function() {
            vm.hasWriteAuthorization = false;

            if (!vm.computationalModel || !vm.computationalModel.permissions) {
                vm.hasWriteAuthorization = false;
            }

            for (var i = 0; i < vm.computationalModel.permissions.length; i++) {
                if ((vm.computationalModel.permissions[i].role === 'ADMIN' ||
                     vm.computationalModel.permissions[i].role === 'WRITE') &&
                    (vm.loggedUserSlug === vm.computationalModel.permissions[i].user.slug || 
                     vm.teamContainsUser(vm.computationalModel.permissions[i].team, vm.loggedUserSlug))) {
                    
                    vm.hasWriteAuthorization = true;
                    vm.roles = [{
                        'value': 'WRITE',
                        'name': 'Write'
                    }, {
                        'value': 'READ',
                        'name': 'Read'
                    }];

                    if (vm.computationalModel.permissions[i].role === 'ADMIN') {
                        vm.roles = [{
                            'value': 'ADMIN',
                            'name': 'Admin'
                        }, {
                            'value': 'WRITE',
                            'name': 'Write'
                        }, {
                            'value': 'READ',
                            'name': 'Read'
                        }];
                    } 
                    break;
                }
            }
        }

        vm.teamContainsUser = function(team, userSlug) {
            if (!team) {
                return false;
            }

            var containsUser = false;

            for (var j = 0; j < team.teamUsers.length; j++) {
                if (team.teamUsers[j].slug === userSlug) {
                    containsUser = true;
                    break;
                }
            }

            return containsUser;
        }

        vm.doDeletePermission = function(permissionSlug) {
            permissionService
                .delete(permissionSlug)
                .then(function(resp) {
                    vm.changePermissionPage();
                    toastr.success('Action performed with success.', 'Success!');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.deletePermission = function(permissionSlug) {
            vm.permissionSlug = permissionSlug;
        }

        vm.insertPermission = function() {
            vm.updatePermission = false;
            vm.permissionSaveTitle = 'Create permission';

            vm.permission = {
                isUserPermission: true,
                user: {},
                team: {}
            };
        }

        vm.editPermission = function(permissionSlug) {
            vm.permissionSaveTitle = 'Update permission';
            vm.updatePermission = true;

            permissionService
                .getBySlug(permissionSlug)
                .then(function(resp) {
                    vm.permission = resp.data;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doSavePermission = function() {
            vm.permission.computationalModel = {
                slug: vm.computationalModel.slug
            };

            if (vm.permission.isUserPermission) {
                delete vm.permission.team;
            
            } else {
                delete vm.permission.user;
            }

            if (!vm.updatePermission) {
                permissionService
                    .insert(vm.permission)
                    .then(function(resp) {
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    }); 
            } else {
                permissionService
                    .update(vm.permission)
                    .then(function(resp) {
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    }); 
            }
        }

        vm.openModelResultMetadataOutput = function(modelResultMetadataSlug) {
            vm.updateLogOutput = true;
            vm.getModelResultExecution(modelResultMetadataSlug);           
        }

        vm.getModelResultExecution = function(modelResultMetadataSlug) {
            computationalModelService
                .getModelResultMetadataBySlug(modelResultMetadataSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.modelResultMetadata = resp.data;

                    vm.modelResultMetadata.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.modelResultMetadata.parsedExecutionFinishDate = new Date(resp.data.executionFinishDate);
                    vm.modelResultMetadata.parsedExecutionStartDate = new Date(resp.data.executionStartDate);

                    if (vm.modelResultMetadata.executionStatus !== 'RUNNING') {
                        vm.updateLogOutput = false;
                    
                    } else if (vm.updateLogOutput) {
                        $timeout(function() {
                            vm.getModelResultExecution(modelResultMetadataSlug);
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    vm.updateLogOutput = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.changeModelResultMetadataPage = function() {
            vm.loadingModelResultMetadata = true;

            computationalModelService
                .getAllModelResultMetadatas(vm.modelResultMetadataCurrentPage - 1, vm.limit, vm.computationalModel.slug)
                .then(function(resp) {
                    var hasRunningModel = false;

                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                        resp.data.records[i].parsedExecutionFinishDate = new Date(resp.data.records[i].executionFinishDate);
                        resp.data.records[i].parsedExecutionStartDate = new Date(resp.data.records[i].executionStartDate);
                    
                        if (resp.data.records[i].executionStatus === 'RUNNING') {
                            hasRunningModel = true;
                        }

                        if (resp.data.records[i].modelExecutor.slug === vm.activeModelExecutor.slug) {		
                            vm.activeModelExecutor.executionStatus = resp.data.records[i].modelExecutor.executionStatus;		
                        }
                    }
                    vm.modelResultMetadatas = resp.data.records;
                    vm.totalModelResultMetadataCount = resp.data.metadata.totalCount;
                    vm.loadingModelResultMetadata = false;

                    if (hasRunningModel) {
                        $timeout(function() {
                            vm.refreshModelResults();
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    vm.loadingModelResultMetadata = false;
                    toastr.error('Error while loading model result metadatas.', 'Unexpected error!');
                });
        }

        vm.refreshModelResults = function() {
            computationalModelService
                .getAllModelResultMetadatas(vm.modelResultMetadataCurrentPage - 1, vm.limit, vm.computationalModel.slug)
                .then(function(resp) {
                    var hasRunningModel = false;
                    
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                        resp.data.records[i].parsedExecutionFinishDate = new Date(resp.data.records[i].executionFinishDate);
                        resp.data.records[i].parsedExecutionStartDate = new Date(resp.data.records[i].executionStartDate);
                    
                        if (resp.data.records[i].executionStatus === 'RUNNING') {
                            hasRunningModel = true;
                        }

                        if (resp.data.records[i].modelExecutor.slug === vm.activeModelExecutor.slug) {		
                            vm.activeModelExecutor.executionStatus = resp.data.records[i].modelExecutor.executionStatus;		
                        }
                    }
                    vm.modelResultMetadatas = resp.data.records;
                    vm.totalModelResultMetadataCount = resp.data.metadata.totalCount;
               
                    if (hasRunningModel) {
                        $timeout(function() {
                            vm.refreshModelResults();
                        }, 2000);
                    }
                })
                .catch(function(resp) {
                    vm.loadingModelResultMetadata = false;
                    toastr.error('Error while refreshing model result metadatas.', 'Unexpected error!');
                });
        }



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
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

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
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--; 
                });
        }

        vm.deleteInstanceParam = function(instanceParamSlug) {
            vm.instanceParamSlug = instanceParamSlug;
            vm.valueFile = null;
        }

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
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertInstanceParam = function() {
            vm.instanceParam = {
            	hasValueFile: false,
            	conceptualParam: {}
            };
            vm.updateInstanceParam = false;
            vm.instanceParamSaveTitle = 'Create instance param';
        }

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
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

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
                    vm.loadingInstanceParam = false;
                    toastr.error('Error while loading instance params.', 'Unexpected error!');
                });
        }

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
                    vm.executor = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        };

        vm.doUploadModelExecutorExecutor = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.modelExecutor.slug);
            formData.append('executor', vm.executor);

            computationalModelService
                .uploadModelExecutorExecutor(formData, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    
                    vm.changeModelExecutorPage();
                    getActiveModelExecutor();

                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
            
        };

        vm.downloadModelExecutorExecuctor = function(modelExecutorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getModelExecutorExecutor(modelExecutorSlug, vm.computationalModel.slug)
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
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

        vm.downloadExecutorOutput = function(modelResultMetadataSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExecutionOutput(modelResultMetadataSlug, vm.computationalModel.slug)
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
                                fileName = 'execution-output.txt';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

        vm.downloadAbortOutput = function(modelResultMetadataSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getAbortOutput(modelResultMetadataSlug, vm.computationalModel.slug)
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
                                fileName = 'abort-output.txt';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

        vm.downloadExtractorOutput = function(extractorMetadataSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getExtractorOutput(extractorMetadataSlug, vm.computationalModel.slug)
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
                                fileName = 'extraction-output';
                            }

                            var data = new Blob([resp.data], { type: contentType });
                            FileSaver.saveAs(data, fileName);
                            $rootScope.loadingAsync--;
                        } 
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

        vm.generateResearchObject = function(modelResultMetadataSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getResearchObject(modelResultMetadataSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.instanceParam = resp.data;
                    var data = new Blob([JSON.stringify(resp.data, null, '\t')], { type: 'application/json' });
                    FileSaver.saveAs(data, 'research-object.json');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.doDeleteModelExecutor = function(modelExecutorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .deleteModelExecutor(modelExecutorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeModelExecutorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deleteModelExecutor = function(modelExecutorSlug) {
            vm.modelExecutorSlug = modelExecutorSlug;
            vm.executor = null;
        }

        vm.editModelExecutor = function(modelExecutorSlug) {
            vm.modelExecutorSaveTitle = 'Update executor';
            vm.updateModelExecutor = true;

            computationalModelService
                .getModelExecutorBySlug(modelExecutorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.modelExecutor = resp.data;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertModelExecutor = function() {
            vm.modelExecutor = {};
            vm.updateModelExecutor = false;
            vm.modelExecutorSaveTitle = 'Create executor';
        }

        vm.doActivateModelExecutor = function() {
            vm.modelExecutor.active = true;
            vm.modelExecutor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            computationalModelService
                .updateModelExecutor(vm.modelExecutor, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeModelExecutorPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doSaveModelExecutor = function() {
            vm.modelExecutor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            if (!vm.updateModelExecutor) {
                computationalModelService
                    .insertModelExecutor(vm.modelExecutor, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.modelExecutor = resp.data;

                        if (!vm.executor) {
                            vm.changeModelExecutorPage();
                            getActiveModelExecutor();

                            toastr.success('Action performed with success.', 'Success!');

                        } else {
                            vm.doUploadModelExecutorExecutor();
                        }
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateModelExecutor(vm.modelExecutor, vm.computationalModel.slug)
                    .then(function(resp) {
                        
                        if (!vm.executor) {
                            vm.changeModelExecutorPage();
                            getActiveModelExecutor();

                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadModelExecutorExecutor();
                        }
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.doAbortModelExecutor = function(modelExecutorSlug) {
            var requestBody = {
                modelExecutorSlug: modelExecutorSlug,
                executionCommand: 'STOP',
                computationalModelVersion: vm.computationalModel.currentVersion
            };

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeModelExecutorPage();
                    getActiveModelExecutor();
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doRunModelExecutor = function(modelExecutorSlug) {
            var requestBody = {
                modelExecutorSlug: modelExecutorSlug,
                executionCommand: 'START',
                computationalModelVersion: vm.computationalModel.currentVersion
            };

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeModelExecutorPage();
                    getActiveModelExecutor();

                    $timeout(function() {
                        vm.refreshModelResults();
                    }, 2000);
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.changeModelExecutorPage = function() {
            vm.loadingModelExecutor = true;
            var filter = null;

            if (vm.filterModelExecutors) {
                filter = 'tag=like=' + vm.filterModelExecutors;
            }

            computationalModelService
                .getAllModelExecutors(vm.modelExecutorCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.modelExecutors = resp.data.records;
                    vm.totalModelExecutorCount = resp.data.metadata.totalCount;
                    vm.loadingModelExecutor = false;
                })
                .catch(function(resp) {
                    vm.loadingModelExecutor = false;
                    toastr.error('Error while loading model executors.', 'Unexpected error!');
                });
        }

        $scope.setExtractor = function(fileInput) {
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
                    vm.extractor = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        }

        vm.doUploadModelMetadataExtractor = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.modelMetadataExtractor.slug);
            formData.append('extractor', vm.extractor);

            computationalModelService
                .uploadModelMetadataExtractorExtractor(formData, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    vm.changeModelMetadataExtractorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
            
        }

        vm.doRunModelMetadataExtractor = function(modelMetadataExtractorSlug) {
            var requestBody = {
                modelExecutorSlug: modelMetadataExtractorSlug,
                executionCommand: 'START',
                computationalModelVersion: vm.computationalModel.currentVersion
            };

            computationalModelService
                .runModel(requestBody)
                .then(function(resp) {
                    vm.changeModelMetadataExtractorPage();
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.downloadModelMetadataExtractorExtractor = function(modelMetadataExtractorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .getModelMetadataExtractorExtractor(modelMetadataExtractorSlug, vm.computationalModel.slug)
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
                        toastr.error('Error while performing action.', 'Unexpected error!');
                        $rootScope.loadingAsync--;
                    });
        }

        vm.doDeleteModelMetadataExtractor = function(modelMetadataExtractorSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .deleteModelMetadataExtractor(modelMetadataExtractorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeModelMetadataExtractorPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deleteModelMetadataExtractor = function(changeModelMetadataExtractorPage) {
            vm.changeModelMetadataExtractorPage = changeModelMetadataExtractorPage;
            vm.extractor = null;
        }

        vm.editModelMetadataExtractor = function(modelMetadataExtractorSlug) {
            vm.modelMetadataExtractorSaveTitle = 'Update metadata extractor';
            vm.updateModelMetadataExtractor = true;

            computationalModelService
                .getModelMetadataExtractorBySlug(modelMetadataExtractorSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.modelMetadataExtractor = resp.data;
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertModelMetadataExtractor = function() {
            vm.modelMetadataExtractor = {};
            vm.updateModelMetadataExtractor = false;
            vm.modelMetadataExtractorSaveTitle = 'Create metadata extractor';
        }

        vm.doSaveModelMetadataExtractor = function() {
            vm.modelMetadataExtractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            if (!vm.updateModelMetadataExtractor) {
                computationalModelService
                    .insertModelMetadataExtractor(vm.modelMetadataExtractor, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.modelMetadataExtractor = resp.data;    

                        if (!vm.extractor) {
                            vm.changeModelMetadataExtractorPage();
                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadModelMetadataExtractor();
                        }
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateModelMetadataExtractor(vm.modelMetadataExtractor, vm.computationalModel.slug)
                    .then(function(resp) {
                        
                        if (!vm.extractor) {
                            vm.changeModelMetadataExtractorPage();
                            toastr.success('Action performed with success.', 'Success!');
                        } else {
                            vm.doUploadModelMetadataExtractor();
                        }
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.doActivateModelMetadataExtractor = function() {
            vm.modelMetadataExtractor.active = true;
            vm.modelMetadataExtractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            computationalModelService
                .updateModelMetadataExtractor(vm.modelMetadataExtractor, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeModelMetadataExtractorPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doDeactivateModelMetadataExtractor = function() {
            vm.modelMetadataExtractor.active = false;
            vm.modelMetadataExtractor.computationalModel = {
                slug: vm.computationalModel.slug
            };
            
            computationalModelService
                .updateModelMetadataExtractor(vm.modelMetadataExtractor, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeModelMetadataExtractorPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.changeModelMetadataExtractorPage = function() {
            vm.loadingModelMetadataExtractor = true;
            var filter = null;

            if (vm.filterModelMetadataExtractors) {
                filter = 'tag=like=' + vm.filterModelMetadataExtractors;
            }

            computationalModelService
                .getAllModelMetadataExtractors(vm.modelMetadataExtractorCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.modelMetadataExtractors = resp.data.records;
                    vm.totalModelMetadataExtractorCount = resp.data.metadata.totalCount;
                    vm.loadingModelMetadataExtractor = false;
                })
                .catch(function(resp) {
                    vm.loadingModelMetadataExtractor = false;
                    toastr.error('Error while loading model metadata extractors.', 'Unexpected error!');
                });
        }

        vm.changePermissionPage = function() {
            vm.loadingPermission = true;

            permissionService
                .getAll(vm.permissionCurrentPage - 1, vm.limit, 'computationalModel.slug=' + vm.computationalModel.slug)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.permissions = resp.data.records;
                    vm.totalPermissionCount = resp.data.metadata.totalCount;
                    vm.loadingPermission = false;
                })
                .catch(function(resp) {
                    vm.loadingPermission = false;
                    toastr.error('Error while loading permissions.', 'Unexpected error!');
                });
        }

        vm.doSaveComputationalModel = function() {
            computationalModelService
                .update(vm.computationalModel)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doDeleteExecutionEnvironment = function(executionEnvironmentSlug) {
            computationalModelService
                .deleteExecutionEnvironment(executionEnvironmentSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExecutionEnvironmentPage();
                    toastr.success('Action performed with success.', 'Success!');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.deleteExecutionEnvironment = function(executionEnvironmentSlug) {
            vm.executionEnvironmentSlug = executionEnvironmentSlug;
        }

        vm.editExecutionEnvironment = function(executionEnvironmentSlug) {
            vm.executionEnvironmentSaveTitle = 'Update execution environment';
            vm.updateExecutionEnvironment = true;

            vm.showNewVirtualMachineConfig = false;
            vm.type = undefined;
            vm.financialCost = undefined;
            vm.diskSpace = undefined;
            vm.ram = undefined;
            vm.gflops = undefined;
            vm.platform = undefined;
            vm.numberOfCores = undefined;
            
            computationalModelService
                .getExecutionEnvironmentBySlug(executionEnvironmentSlug, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.executionEnvironment = resp.data;

                    if (!vm.executionEnvironment.vpnType) {
                        vm.executionEnvironment.vpnType = 'NONE';
                    }
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertExecutionEnvironment = function() {
            vm.executionEnvironment = {
                virtualMachines: [],
                vpnType : 'NONE'
            };
            
            vm.updateExecutionEnvironment = false;
            vm.executionEnvironmentSaveTitle = 'Create execution environment';
            
            vm.showNewVirtualMachineConfig = false;
            vm.type = undefined;
            vm.financialCost = undefined;
            vm.diskSpace = undefined;
            vm.ram = undefined;
            vm.gflops = undefined;
            vm.platform = undefined;
            vm.numberOfCores = undefined;
        }

        vm.doSaveExecutionEnvironment = function() {
            vm.executionEnvironment.computationalModel = {
                slug: vm.computationalModel.slug
            };

            if (vm.executionEnvironment.vpnType === 'NONE') {
                delete vm.executionEnvironment.vpnType;
                delete vm.executionEnvironment.vpnConfiguration;
            }
            
            if (!vm.updateExecutionEnvironment) {
                computationalModelService
                    .insertExecutionEnvironment(vm.executionEnvironment, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.changeExecutionEnvironmentPage();
                        getActiveExecutionEnvironment();

                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .updateExecutionEnvironment(vm.executionEnvironment, vm.computationalModel.slug)
                    .then(function(resp) {
                        vm.changeExecutionEnvironmentPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.doActivateExecutionEnvironment = function() {
            vm.executionEnvironment.active = true;

            computationalModelService
                .updateExecutionEnvironment(vm.executionEnvironment, vm.computationalModel.slug)
                .then(function(resp) {
                    vm.changeExecutionEnvironmentPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.addVirtualMachineConfig = function(type, financialCost, diskSpace, ram, gflops, platform, numberOfCores) {
            if (type && financialCost && diskSpace && ram && gflops && platform && numberOfCores) {

                vm.executionEnvironment.virtualMachines = vm.executionEnvironment.virtualMachines || [];
                vm.executionEnvironment.virtualMachines.push({
                    type: type,
                    financialCost: financialCost,
                    diskSpace: diskSpace,
                    ram: ram,
                    gflops: gflops,
                    platform: platform,
                    numberOfCores: numberOfCores
                });

                vm.showNewVirtualMachineConfig = false;

                vm.type = undefined;
                vm.financialCost = undefined;
                vm.diskSpace = undefined;
                vm.ram = undefined;
                vm.gflops = undefined;
                vm.platform = undefined;
                vm.numberOfCores = undefined;
            } 
        }

        vm.toggleNewVirtualMachineConfig = function(toggle) {
            vm.showNewVirtualMachineConfig = toggle;
        }

        vm.removeVirtualMachineConfig = function(i) {
            if (vm.executionEnvironment.virtualMachines) {
                vm.executionEnvironment.virtualMachines.splice(i, 1);
            }
        }

        vm.changeExecutionEnvironmentPage = function() {
            vm.loadingExecutionEnvironment = true;
            var filter = null;

            if (vm.filterExecutionEnvironment) {
                filter = 'tag=like=' + vm.filterExecutionEnvironment;
            }

            computationalModelService
                .getAllExecutionEnvironments(vm.executionEnvironmentCurrentPage - 1, vm.limit, vm.computationalModel.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.executionEnvironments = resp.data.records;
                    vm.totalExecutionEnvironmentCount = resp.data.metadata.totalCount;
                    vm.loadingExecutionEnvironment = false;
                })
                .catch(function(resp) {
                    vm.loadingExecutionEnvironment = false;
                    toastr.error('Error while loading execution environments.', 'Unexpected error!');
                });
        }

        function getActiveModelExecutor() {
            vm.loadingActiveExecutor = true;

            computationalModelService
                .getAllModelExecutors(vm.modelExecutorCurrentPage - 1, vm.limit, vm.computationalModelSlug, 'active=true')
                .then(function(resp) {
                	if (resp.data && resp.data.records.length >= 1) {
                		vm.activeModelExecutor = resp.data.records[0];
                	}
                    vm.loadingActiveExecutor = false;
                })
                .catch(function(resp) {
                    vm.loadingActiveExecutor = false;
                    toastr.error('Error while loading active model executor.', 'Unexpected error!');
                });
        }

        function getActiveExecutionEnvironment() {
            vm.loadingActiveExecutionEnvironment = true;

            computationalModelService
                .getAllExecutionEnvironments(vm.modelExecutorCurrentPage - 1, vm.limit, vm.computationalModelSlug, 'active=true')
                .then(function(resp) {
                	if (resp.data && resp.data.records.length >= 1) {
                		vm.activeExecutionEnvironment = resp.data.records[0];
                	}
                    vm.loadingActiveExecutionEnvironment = false;
                })
                .catch(function(resp) {
                    vm.loadingActiveExecutionEnvironment = false;
                    toastr.error('Error while loading active execution environment.', 'Unexpected error!');
                });
        }

        function getComputationalModel() {
            vm.loadingComputationalModel = true;

            computationalModelService
                .getBySlug(vm.computationalModelSlug)
                .then(function(resp) {
                	resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.computationalModel = resp.data;
                    vm.checkWriteAccess();
                    vm.loadingComputationalModel = false;

                    getActiveModelExecutor();
                    getActiveExecutionEnvironment();
                    vm.changeModelResultMetadataPage();
                })
                .catch(function(resp) {
                    vm.loadingComputationalModel = false;
                    toastr.error('Error while loading computational model.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            $('body').off('hidden.bs.modal', '#output-detail-model-result-metadata')
            .on('hidden.bs.modal', '#output-detail-model-result-metadata', function() {
                vm.updateLogOutput = false;
            });

            vm.computationalModelSaveTitle = 'Update computational model';
            vm.updateComputationalModel = true;

            vm.loggedUserSlug = localStorageService.getUserSlug();
            vm.computationalModelSlug = $stateParams.slug;
            vm.computationalModel = {};
        
            vm.limit = 20;

            vm.totalModelResultMetadataCount = 0;
            vm.modelResultMetadataCurrentPage = 1;
            vm.modelResultMetadatas = [];
            vm.modelResultMetadata = {};
            vm.updateModelResultMetadata = false;

            vm.totalModelExecutorCount = 0;
            vm.modelExecutorCurrentPage = 1;
            vm.modelExecutors = [];
            vm.modelExecutor = {};
            vm.updateModelExecutor = false;

            vm.totalModelMetadataExtractorCount = 0;
            vm.modelMetadataextractorCurrentPage = 1;
            vm.modelMetadataExtractors = [];
            vm.modelMetadataExtractors = {};
            vm.updateModelMetadataExtractor = false;

            vm.totalExecutionEnvironmentCount = 0;
            vm.executionEnvironmentCurrentPage = 1;
            vm.executionEnvironments = [];
            vm.executionEnvironment = {};
            vm.updateExecutionEnvironment = false;

            vm.totalInstanceParamCount = 0;
            vm.instanceParamCurrentPage = 1;
            vm.instanceParams = [];
            vm.instanceParam = {};
            vm.updateInstanceParam = false;

            vm.totalPermissionCount = 0;
            vm.permissionCurrentPage = 1;
            vm.permissions = [];
            vm.permission = {};

            vm.hasWriteAuthorization = false;

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
                'value': 'HTTP',
                'name': 'Http'
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

            vm.httpProtocolTypes = [{
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
