(function() {
    'use strict';

    angular
        .module('pheno-manager.hypothesis')
        .controller('HypothesisDetailsController', HypothesisDetailsController);

    HypothesisDetailsController.$inject = ['$scope', '$q', '$controller', '$stateParams', '$timeout', 'toastr', '$location', 'experimentService', 'permissionService', 'hypothesisService', '$rootScope', '$state', '$filter'];

    function HypothesisDetailsController($scope, $q, $controller, $stateParams, $timeout, toastr, $location, experimentService, permissionService, hypothesisService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'hypothesis'
        }));

        vm.doDeleteHypothesis = function(hypothesisSlug) {
            $rootScope.loadingAsync++;

            hypothesisService
                .delete(hypothesisSlug)
                .then(function(resp) {
                    vm.changeHypothesisChildrenPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.deleteHypothesis = function(hypothesisSlug) {
            vm.hypothesisSlug = hypothesisSlug;
        };

        vm.doDeleteExperiment = function(experimentSlug) {
            $rootScope.loadingAsync++;

            experimentService
                .delete(experimentSlug)
                .then(function(resp) {
                    vm.changeExperimentPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.deleteExperiment = function(experimentSlug) {
            vm.experimentSlug = experimentSlug;
        };

        vm.editExperiment = function(experimentSlug) {
            vm.experimentSaveTitle = 'Update experiment';
            vm.updateExperiment = true;

            experimentService
                .getBySlug(experimentSlug)
                .then(function(resp) {
                    vm.experiment = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertExperiment = function() {
            vm.experiment = {};
            vm.updateExperiment = false;
            vm.experimentSaveTitle = 'Create experiment';
        };

        vm.doSaveExperiment = function() {
            vm.experiment.hypothesis = {
                slug: vm.hypothesis.slug
            };
            
            if (!vm.updateExperiment) {
                experimentService
                    .insert(vm.experiment)
                    .then(function(resp) {
                        vm.changeExperimentPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                experimentService
                    .update(vm.experiment)
                    .then(function(resp) {
                        vm.changeExperimentPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.editChildHypothesis = function(hypothesisSlug) {
            vm.childHypothesisSaveTitle = 'Update child hypothesis';
            vm.updateChildHypothesis = true;

            hypothesisService
                .getBySlug(hypothesisSlug)
                .then(function(resp) {
                    vm.childHypothesis = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertBranchHypothesis = function(parentHypothesis) {
            vm.childHypothesis = {};
            vm.parentHypothesis = angular.copy(parentHypothesis);
            vm.updateChildHypothesis = false;
            vm.childHypothesisSaveTitle = 'Create child hypothesis';
        };

        vm.doSaveChildHypothesis = function() {
            vm.childHypothesis.phenomenon = {
                slug: vm.hypothesis.phenomenon.slug
            };

            if (vm.parentHypothesis) {
                vm.childHypothesis.parentHypothesis = {
                    slug: vm.parentHypothesis.slug
                }; 
            }
            
            if (!vm.updateChildHypothesis) {
                hypothesisService
                    .insert(vm.childHypothesis)
                    .then(function(resp) {
                        vm.changeHypothesisChildrenPage();
                        toastr.success('Action performed with success.', 'Success!');

                        hypothesisService
                            .update(vm.parentHypothesis)
                            .then(function(resp) {
                                vm.hypothesis = resp.data;
                            })
                            .catch(function(resp) {
                                console.log(resp);
                                toastr.error('Error while updating parent hypothesis state.', 'Unexpected error!');
                            });

                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                hypothesisService
                    .update(vm.childHypothesis)
                    .then(function(resp) {
                        vm.changeHypothesisChildrenPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.changeExperimentPage = function() {
            vm.loadingExperiment = true;
            var filter = 'hypothesis.slug=' + vm.hypothesis.slug;

            if (vm.filterExperiments) {
                filter += ';name=like=' + vm.filterExperiments + ',description=like=' + vm.filterExperiments;
            }

            experimentService
                .getAll(vm.experimentCurrentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.experiments = resp.data.records;
                    vm.totalExperimentCount = resp.data.metadata.totalCount;
                    vm.loadingExperiment = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingExperiment = false;
                    toastr.error('Error while loading experiments.', 'Unexpected error!');
                });
        };

        vm.changeHypothesisChildrenPage = function() {
            vm.loadingHypothesisChildren = true;
            var filter = 'parentHypothesis.slug=' + vm.hypothesis.slug;

            if (vm.filterHypotheses) {
                filter += ';name=like=' + vm.filterHypotheses + ',description=like=' + vm.filterHypotheses;
            }

            hypothesisService
                .getAll(vm.hypothesisChildrenCurrentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.hypothesesChildren = resp.data.records;
                    vm.totalHypothesisChildrenCount = resp.data.metadata.totalCount;
                    vm.loadingHypothesisChildren = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingHypothesisChildren = false;
                    toastr.error('Error while loading children hypotheses.', 'Unexpected error!');
                });
        };

        vm.doSaveHypothesis = function() {
            hypothesisService
                .update(vm.hypothesis)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        function getHypothesis() {
            vm.loadingHypothesis = true;

            hypothesisService
                .getBySlug(vm.hypothesisSlug)
                .then(function(resp) {
                	resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.hypothesis = resp.data;
                    vm.checkWriteAccess(vm.hypothesis);
                    vm.loadingHypothesis = false;

                    vm.changeExperimentPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingHypothesis = false;
                    toastr.error('Error while loading hypothesis.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            vm.hypothesisSaveTitle = 'Update hypothesis';
            vm.updateHypothesis = true;

            vm.hypothesisSlug = $stateParams.slug;
            vm.hypothesis = {};
        
            vm.limit = 20;

            vm.totalExperimentCount = 0;
            vm.experimentCurrentPage = 1;
            vm.experiments = [];
            vm.updateExperiment = false;
            vm.experiment = {};

            vm.totalHypothesisChildrenCount = 0;
            vm.hypothesisChildrenCurrentPage = 1;
            vm.hypothesesChildren = [];
            vm.childHypothesisSaveTitle = 'Update hypothesis';
            vm.updateChildHypothesis = false;
            vm.childHypothesis = {};

            vm.states = [{
                'value': 'FORMULATED',
                'name': 'Formulated'
            }, {
                'value': 'VALIDATED',
                'name': 'Validated'
            }, {
                'value': 'CONFIRMED',
                'name': 'Confirmed'
            }, {
                'value': 'IMPROVED',
                'name': 'Improved'
            }, {
                'value': 'REFUTED',
                'name': 'Refuted'
            }];

            getHypothesis();
        }
    }

})();