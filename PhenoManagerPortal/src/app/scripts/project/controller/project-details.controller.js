(function() {
    'use strict';

    angular
        .module('pheno-manager.project')
        .controller('ProjectDetailsController', ProjectDetailsController);

        ProjectDetailsController.$inject = ['$scope', '$controller', '$stateParams', '$q', '$timeout', 'toastr','$location', 'phenomenonService', 'permissionService', 'userService', 'localStorageService', 'projectService', '$rootScope', '$state', '$filter'];

    function ProjectDetailsController($scope, $controller, $stateParams, $q, $timeout, toastr, $location, phenomenonService, permissionService, userService, localStorageService, projectService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'project'
        }));

        vm.doDeletePhenomenon = function(phenomenonSlug) {
            $rootScope.loadingAsync++;

            phenomenonService
                .delete(phenomenonSlug)
                .then(function(resp) {
                    vm.changePhenomenonPage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.deletePhenomenon = function(phenomenonSlug) {
            vm.phenomenonSlug = phenomenonSlug;
        }

        vm.editPhenomenon = function(phenomenonSlug) {
            vm.phenomenonSaveTitle = 'Update phenomenon';
            vm.updatePhenomenon = true;

            phenomenonService
                .getBySlug(phenomenonSlug)
                .then(function(resp) {
                    vm.phenomenon = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        vm.insertPhenomenon = function() {
            vm.phenomenon = {};
            vm.updatePhenomenon = false;
            vm.phenomenonSaveTitle = 'Create phenomenon';
        }

        vm.doSavePhenomenon = function() {
            vm.phenomenon.project = {
                slug: vm.project.slug
            };

            if (!vm.updatePhenomenon) {
                phenomenonService
                    .insert(vm.phenomenon)
                    .then(function(resp) {
                        vm.changePhenomenonPage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                phenomenonService
                    .update(vm.phenomenon)
                    .then(function(resp) {
                        vm.changePhenomenonPage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        vm.changePhenomenonPage = function() {
            vm.loadingPhenomenon = true;
            var filter = 'project.slug=' + vm.project.slug;

            if (vm.filterPhenomenons) {
                filter += ';name=like=' + vm.filterPhenomenons + ',description=like=' + vm.filterPhenomenons;
            }

            phenomenonService
                .getAll(vm.phenomenonCurrentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.phenomenons = resp.data.records;
                    vm.totalPhenomenonCount = resp.data.metadata.totalCount;
                    vm.loadingPhenomenon = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingPhenomenon = false;
                    toastr.error('Error while loading phenomenons.', 'Unexpected error!');
                });
        }

        vm.doSyncSciManager = function(projectSlug) {
            $rootScope.loadingAsync++;

            projectService
                .syncSciManager(projectSlug)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with succes, project data is being synced.');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.doSave = function() {
            projectService
                .update(vm.project)
                .then(function(resp) {
                    toastr.success('Success!', 'Action performed with success.');
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        }

        function getProject() {
            vm.loadingProject = true;

            projectService
                .getBySlug(vm.projectSlug)
                .then(function(resp) {
                    resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.project = resp.data;
                    vm.checkWriteAccess(vm.project);
                    vm.loadingProject = false;

                    vm.changePhenomenonPage();
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loadingProject = false;
                    toastr.error('Error while loading project.', 'Unexpected error!');
                });
        }

        init();
        
        function init() {
            vm.projectSaveTitle = 'Update project';

            vm.projectSlug = $stateParams.slug;
            vm.project = {};
        
            vm.limit = 20;

            vm.totalPhenomenonCount = 0;
            vm.phenomenonCurrentPage = 1;
            vm.phenomenons = [];
            vm.updatePhenomenon = false;
            vm.phenomenon = {};

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

            getProject();
        }
    }

})();
