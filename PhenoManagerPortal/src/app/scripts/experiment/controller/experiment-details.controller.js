(function() {
    'use strict';

    angular
        .module('pheno-manager.experiment')
        .controller('ExperimentDetailsController', ExperimentDetailsController);

        ExperimentDetailsController.$inject = ['$scope', '$q', 'Blob', 'FileSaver', '$stateParams', '$timeout', 'toastr', '$location', 'localStorageService', 'userService', 'experimentService', 'permissionService', 'computationalModelService', '$rootScope', '$state', '$filter'];

    function ExperimentDetailsController($scope, $q, Blob, FileSaver, $stateParams, $timeout, toastr, $location, localStorageService, userService, experimentService, permissionService, computationalModelService, $rootScope, $state, $filter) {
        var vm = this;

        $scope.setEvidence = function(fileInput) {
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
                    vm.validationItem.validationEvidence = file;
                    vm.loadingUpload = false;
                    $scope.$apply();
                }
            }
        };

        vm.doValidateItem = function() {
            $rootScope.loadingAsync++;
            vm.loadingUpload = true;

            var formData = new FormData();
            formData.append('slug', vm.validationItem.slug);
            formData.append('validated', true);
            formData.append('validationEvidence', vm.validationItem.validationEvidence);

            experimentService
                .uploadValidationEvidence(formData, vm.experiment.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    vm.changeValidationItemPage();
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

        vm.doUnvalidateItem = function() {
            vm.loadingUpload = true;
            vm.validationItem.validated = false;
            vm.validationItem.validationEvidence = null;
            vm.validationItem.validationEvidenceFile = null;

            experimentService
                .updateValidationItem(vm.validationItem, vm.experiment.slug)
                .then(function(resp) {
                    vm.loadingUpload = false;
                    vm.changeValidationItemPage();
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingUpload = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
            
        };

        vm.downloadValidationEvidence = function(validationItemSlug) {
            $rootScope.loadingAsync++;

            experimentService
                .getValidationEvidence(validationItemSlug, vm.experiment.slug)
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
                                fileName = 'validation-evidence';
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
        }

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

            if (!vm.experiment || !vm.experiment.permissions) {
                vm.hasWriteAuthorization = false;
            }

            for (var i = 0; i < vm.experiment.permissions.length; i++) {
                if ((vm.experiment.permissions[i].role === 'ADMIN' ||
                     vm.experiment.permissions[i].role === 'WRITE') &&
                    (vm.loggedUserSlug === vm.experiment.permissions[i].user.slug || 
                     vm.teamContainsUser(vm.experiment.permissions[i].team, vm.loggedUserSlug))) {
                    
                    vm.hasWriteAuthorization = true;
                    vm.roles = [{
                        'value': 'WRITE',
                        'name': 'Write'
                    }, {
                        'value': 'READ',
                        'name': 'Read'
                    }];

                    if (vm.experiment.permissions[i].role === 'ADMIN') {
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
            if (!team || !team.teamUsers) {
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
                    console.log(resp);
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
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.doSavePermission = function() {
            vm.permission.experiment = {
                slug: vm.experiment.slug
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
                        vm.changePermissionPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    }); 
            } else {
                permissionService
                    .update(vm.permission)
                    .then(function(resp) {
                        vm.changePermissionPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    }); 
            }
        }

        vm.doDeleteComputationalModel = function(computationalModelSlug) {
            $rootScope.loadingAsync++;

            computationalModelService
                .delete(computationalModelSlug)
                .then(function(resp) {
                    vm.changeComputationalModelPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deleteComputationalModel = function(computationalModelSlug) {
            vm.computationalModelSlug = computationalModelSlug;
        }

        vm.editComputationalModel = function(computationalModelSlug) {
            vm.computationalModelSaveTitle = 'Update computational model';
            vm.updateComputationalModel = true;

            computationalModelService
                .getBySlug(computationalModelSlug)
                .then(function(resp) {
                    vm.computationalModel = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertComputationalModel = function() {
            vm.computationalModel = {};
            vm.updateComputationalModel = false;
            vm.computationalModelSaveTitle = 'Create computational model';
        }

        vm.doSaveComputationalModel = function() {
            vm.computationalModel.experiment = {
                slug: vm.experiment.slug
            };
            
            if (!vm.updateComputationalModel) {
                computationalModelService
                    .insert(vm.computationalModel)
                    .then(function(resp) {
                        vm.changeComputationalModelPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                computationalModelService
                    .update(vm.computationalModel)
                    .then(function(resp) {
                        vm.changeComputationalModelPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.changeComputationalModelPage = function() {
            vm.loadingComputationalModel = true;
            var filter = 'experiment.slug=' + vm.experiment.slug;

            if (vm.filterComputationalModels) {
                filter += ';name=like=' + vm.filterComputationalModels + ',description=like=' + vm.filterComputationalModels;
            }

            computationalModelService
                .getAll(vm.computationalModelCurrentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.computationalModels = resp.data.records;
                    vm.totalComputationalModelCount = resp.data.metadata.totalCount;
                    vm.loadingComputationalModel = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingComputationalModel = false;
                    toastr.error('Error while loading computational models.', 'Unexpected error!');
                });
        }

        vm.doDeletePhase = function(phaseSlug) {
            experimentService
                .deletePhase(phaseSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.changePhasePage();
                    toastr.success('Action performed with success.', 'Success!');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.deletePhase = function(phaseSlug) {
            vm.phaseSlug = phaseSlug;
        }

        vm.editPhase = function(phaseSlug) {
            vm.phaseSaveTitle = 'Update phase';
            vm.updatePhase = true;

            experimentService
                .getPhaseBySlug(phaseSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.phase = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertPhase = function() {
            vm.phase = {};
            vm.updatePhase = false;
            vm.phaseSaveTitle = 'Create phase';
        }

        vm.doSavePhase = function() {
            vm.phase.experiment = {
                slug: vm.experiment.slug
            };
            
            if (!vm.updatePhase) {
                experimentService
                    .insertPhase(vm.phase, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changePhasePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                experimentService
                    .updatePhase(vm.phase, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changePhasePage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.changePhasePage = function() {
            vm.loadingPhase = true;
            var filter = null;

            if (vm.filterPhases) {
                filter = 'name=like=' + vm.filterPhases;
            }

            experimentService
                .getAllPhases(vm.phaseCurrentPage - 1, vm.limit, vm.experiment.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.phases = resp.data.records;
                    vm.totalPhaseCount = resp.data.metadata.totalCount;
                    vm.loadingPhase = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingPhase = false;
                    toastr.error('Error while loading phases.', 'Unexpected error!');
                });
        }

        vm.doDeleteConceptualParam = function(conceptualParamSlug) {
            $rootScope.loadingAsync++;

            experimentService
                .deleteConceptualParam(conceptualParamSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.changeConceptualParamPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deleteConceptualParam = function(conceptualParamSlug) {
            vm.conceptualParamSlug = conceptualParamSlug;
        }

        vm.editConceptualParam = function(conceptualParamSlug) {
            vm.conceptualParamSaveTitle = 'Update conceptual param';
            vm.updateConceptualParam = true;

            experimentService
                .getConceptualParamBySlug(conceptualParamSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.conceptualParam = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertConceptualParam = function() {
            vm.conceptualParam = {};
            vm.updateConceptualParam = false;
            vm.conceptualParamSaveTitle = 'Create conceptual param';
        }

        vm.doSaveConceptualParam = function() {
            vm.conceptualParam.experiment = {
                slug: vm.experiment.slug
            };
            
            if (!vm.updateConceptualParam) {
                experimentService
                    .insertConceptualParam(vm.conceptualParam, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changeConceptualParamPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                experimentService
                    .updateConceptualParam(vm.conceptualParam, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changeConceptualParamPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.changeConceptualParamPage = function() {
            vm.loadingConceptualParam = true;
            var filter = null;

            if (vm.filterConceptualParams) {
                filter = 'key=like=' + vm.filterConceptualParams + ',description=like=' + vm.filterConceptualParams;
            }

            experimentService
                .getAllConceptualParams(vm.conceptualParamCurrentPage - 1, vm.limit, vm.experiment.slug, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.conceptualParams = resp.data.records;
                    vm.totalConceptualParamCount = resp.data.metadata.totalCount;
                    vm.loadingConceptualParam = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingConceptualParam = false;
                    toastr.error('Error while loading conceptual params.', 'Unexpected error!');
                });
        }

        vm.doDeleteValidationItem = function(conceptualParamSlug) {
            $rootScope.loadingAsync++;

            experimentService
                .deleteValidationItem(validationItemSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.changeValidationItemPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deleteValidationItem = function(validationItemSlug) {
            vm.validationItemSlug = validationItemSlug;
        }

        vm.editValidationItem = function(validationItemSlug) {
            vm.validationItemSaveTitle = 'Update validation item';
            vm.updateValidationItem = true;

            experimentService
                .getValidationItemBySlug(validationItemSlug, vm.experiment.slug)
                .then(function(resp) {
                    vm.validationItem = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertValidationItem = function() {
            vm.validationItem = {};
            vm.updateValidationItem = false;
            vm.validationItemSaveTitle = 'Create validation item';
        }

        vm.doSaveValidationItem = function() {
            vm.validationItem.experiment = {
                slug: vm.experiment.slug
            };
            
            if (!vm.updateValidationItem) {
                experimentService
                    .insertValidationItem(vm.validationItem, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changeValidationItemPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                experimentService
                    .updateValidationItem(vm.validationItem, vm.experiment.slug)
                    .then(function(resp) {
                        vm.changeValidationItemPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.changeValidationItemPage = function() {
            vm.loadingValidationItem = true;

            experimentService
                .getAllValidationItems(vm.validationItemCurrentPage - 1, vm.limit, vm.experiment.slug)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.validationItems = resp.data.records;
                    vm.totalValidationItemCount = resp.data.metadata.totalCount;
                    vm.loadingValidationItem = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingValidationItem = false;
                    toastr.error('Error while loading validation items.', 'Unexpected error!');
                });
        }

        vm.changePermissionPage = function() {
            vm.loadingPermission = true;

            permissionService
                .getAll(vm.permissionCurrentPage - 1, vm.limit, 'experiment.slug=' + vm.experiment.slug)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.permissions = resp.data.records;
                    vm.totalPermissionCount = resp.data.metadata.totalCount;
                    vm.loadingPermission = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingPermission = false;
                    toastr.error('Error while loading permissions.', 'Unexpected error!');
                });
        }

        vm.doSaveExperiment = function() {
            experimentService
                .update(vm.experiment)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        function getExperiment() {
            vm.loadingExperiment = true;

            experimentService
                .getBySlug(vm.experimentSlug)
                .then(function(resp) {
                	resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.experiment = resp.data;
                    vm.checkWriteAccess();
                    vm.loadingExperiment = false;

                    vm.changeComputationalModelPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExperiment = false;
                    toastr.error('Error while loading experiment.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            vm.experimentSaveTitle = 'Update experiment';
            vm.updateExperiment = true;
            
            vm.loggedUserSlug = localStorageService.getUserSlug();
            vm.experimentSlug = $stateParams.slug;
            vm.experiment = {};
        
            vm.limit = 20;

            vm.totalComputationalModelCount = 0;
            vm.computationalModelCurrentPage = 1;
            vm.computationalModels = [];
            vm.computationalModel = {};
            vm.updateComputationalModel = false;

            vm.totalPhaseCount = 0;
            vm.phaseCurrentPage = 1;
            vm.phases = [];
            vm.phase = {};
            vm.updatePhase = false;

            vm.totalConceptualParamCount = 0;
            vm.conceptualParamCurrentPage = 1;
            vm.conceptualParams = [];
            vm.conceptualParam = {};
            vm.updateConceptualParam = false;

            vm.totalValidationItemCount = 0;
            vm.validationItemCurrentPage = 1;
            vm.validationItems = [];
            vm.validationItem = {};
            vm.updateValidationItem = false;

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

            getExperiment();
        }
    }

})();
