(function() {
    'use strict';

    angular
        .module('pheno-manager.experiment')
        .controller('ExperimentDetailsController', ExperimentDetailsController);

    ExperimentDetailsController.$inject = ['$scope', '$q', '$controller', 'Blob', 'FileSaver', '$stateParams', '$timeout', 'toastr', '$location', 'experimentService', 'computationalModelService', '$rootScope', '$state', '$filter'];

    function ExperimentDetailsController($scope, $q, $controller, Blob, FileSaver, $stateParams, $timeout, toastr, $location, experimentService, computationalModelService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'experiment'
        }));

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
        };

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
        };

        vm.deleteComputationalModel = function(computationalModelSlug) {
            vm.computationalModelSlug = computationalModelSlug;
        };

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
        };

        vm.insertComputationalModel = function() {
            vm.computationalModel = {};
            vm.updateComputationalModel = false;
            vm.computationalModelSaveTitle = 'Create computational model';
        };

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
        };

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
        };

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
        };

        vm.deletePhase = function(phaseSlug) {
            vm.phaseSlug = phaseSlug;
        };

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
        };

        vm.insertPhase = function() {
            vm.phase = {};
            vm.updatePhase = false;
            vm.phaseSaveTitle = 'Create phase';
        };

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
        };

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
        };

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
        };

        vm.deleteConceptualParam = function(conceptualParamSlug) {
            vm.conceptualParamSlug = conceptualParamSlug;
        };

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
        };

        vm.insertConceptualParam = function() {
            vm.conceptualParam = {};
            vm.updateConceptualParam = false;
            vm.conceptualParamSaveTitle = 'Create conceptual param';
        };

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
        };

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
        };

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
        };

        vm.deleteValidationItem = function(validationItemSlug) {
            vm.validationItemSlug = validationItemSlug;
        };

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
        };

        vm.insertValidationItem = function() {
            vm.validationItem = {};
            vm.updateValidationItem = false;
            vm.validationItemSaveTitle = 'Create validation item';
        };

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
        };

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
        };

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
        };

        function getExperiment() {
            vm.loadingExperiment = true;

            experimentService
                .getBySlug(vm.experimentSlug)
                .then(function(resp) {
                	resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.experiment = resp.data;
                    vm.checkWriteAccess(vm.experiment);
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
