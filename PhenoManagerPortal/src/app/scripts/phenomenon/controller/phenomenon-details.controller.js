(function() {
    'use strict';

    angular
        .module('pheno-manager.phenomenon')
        .controller('PhenomenonDetailsController', PhenomenonDetailsController);

    PhenomenonDetailsController.$inject = ['$scope', '$controller', '$stateParams', '$q', '$timeout', 'toastr', 'FileSaver', 'Blob', '$location', 'phenomenonService', 'hypothesisService', 'userService', '$rootScope', '$state', '$filter'];

    function PhenomenonDetailsController($scope, $controller, $stateParams, $q, $timeout, toastr, FileSaver, Blob, $location, phenomenonService, hypothesisService, userService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'phenomenon'
        }));

        vm.doDeleteHypothesis = function(hypothesisSlug) {
            $rootScope.loadingAsync++;

            hypothesisService
                .delete(hypothesisSlug)
                .then(function(resp) {
                    vm.changeHypothesisPage();
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

        vm.editHypothesis = function(hypothesisSlug) {
            vm.hypothesisSaveTitle = 'Update hypothesis';
            vm.updateHypothesis = true;

            hypothesisService
                .getBySlug(hypothesisSlug)
                .then(function(resp) {
                    vm.hypothesis = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertBranchHypothesis = function(parentHypothesis) {
            vm.childHypothesis = {};
            vm.parentHypothesis = angular.copy(parentHypothesis);
        };

        vm.doSaveChildHypothesis = function() {
            vm.childHypothesis.phenomenon = {
                slug: vm.phenomenon.slug
            };

            if (vm.parentHypothesis) {
                vm.childHypothesis.parentHypothesis = {
                    slug: vm.parentHypothesis.slug
                }; 
            }
            
            hypothesisService
                .insert(vm.childHypothesis)
                .then(function(resp) {
                    vm.changeHypothesisPage();
                    toastr.success('Action performed with success.', 'Success!');

                    hypothesisService
                        .update(vm.parentHypothesis)
                        .then(function(resp) {
                            vm.changeHypothesisPage();
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
        };

        vm.insertHypothesis = function() {
            vm.hypothesis = {};
            vm.updateHypothesis = false;
            vm.hypothesisSaveTitle = 'Create hypothesis';
        };

        vm.doSaveHypothesis = function() {
            vm.hypothesis.phenomenon = {
                slug: vm.phenomenon.slug
            };
            
            if (!vm.updateHypothesis) {
                hypothesisService
                    .insert(vm.hypothesis)
                    .then(function(resp) {
                        vm.changeHypothesisPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                hypothesisService
                    .update(vm.hypothesis)
                    .then(function(resp) {
                        vm.changeHypothesisPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        vm.changeHypothesisPage = function() {
            vm.loadingHypothesis = true;
            var filter = 'phenomenon.slug=' + vm.phenomenon.slug;

            if (vm.filterHypotheses) {
                filter += ';name=like=' + vm.filterHypotheses + ',description=like=' + vm.filterHypotheses;
            }
            
            hypothesisService
                .getAll(vm.hypothesisCurrentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.hypotheses = resp.data.records;
                    vm.totalHypothesisCount = resp.data.metadata.totalCount;
                    vm.loadingHypothesis = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingHypothesis = false;
                    toastr.error('Error while loading hypotheses.', 'Unexpected error!');
                });
        };

        vm.doSavePhenomenon = function() {
            phenomenonService
                .update(vm.phenomenon)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        function getPhenomenon() {
            vm.loadingPhenomenon = true;

            phenomenonService
                .getBySlug(vm.phenomenonSlug)
                .then(function(resp) {
                    resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.phenomenon = resp.data;
                    vm.checkWriteAccess(vm.phenomenon);
                    vm.loadingPhenomenon = false;

                    vm.changeHypothesisPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingPhenomenon = false;
                    toastr.error('Error while loading phenomenon.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            vm.phenomenonSaveTitle = 'Update phenomenon';

            vm.phenomenonSlug = $stateParams.slug;
            vm.phenomenon = {};
        
            vm.limit = 20;

            vm.totalHypothesisCount = 0;
            vm.hypothesisCurrentPage = 1;
            vm.hypotheses = [];
            vm.updateHypothesis = false;
            vm.hypothesis = {};

            vm.childHypothesis = {};
            vm.updateChildHypothesis = false;
            vm.childHypothesisSaveTitle = 'Create child hypothesis';

            vm.researchDomains = [{
                'value': 'MATH',
                'name': 'Math'
            }, {
                'value': 'PHYSICS',
                'name': 'Phisics'
            }, {
                'value': 'BIOLOGY',
                'name': 'Biology'
            }, {
                'value': 'CHEMISTRY',
                'name': 'Chemistry'
            }, {
                'value': 'BIO_CHEMISTRY',
                'name': 'Biochemistry'
            }, {
                'value': 'ASTRONOMY',
                'name': 'Astronomy'
            }, {
                'value': 'COMPUTER_SCIENCE',
                'name': 'Computer Science'
            }, {
                'value': 'LINGUISTICS',
                'name': 'Linguistics'
            }, {
                'value': 'OTHER',
                'name': 'Other'
            }];

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

            getPhenomenon();
        }
    }

})();