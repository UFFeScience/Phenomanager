(function() {
    'use strict';

    angular
        .module('pheno-manager.project')
        .controller('ProjectController', ProjectController);

    ProjectController.$inject = ['$scope', '$q', '$controller', '$timeout', 'toastr','$location', 'projectService', '$rootScope', '$state', '$filter'];

    function ProjectController($scope, $q, $controller, $timeout, toastr, $location, projectService, $rootScope, $state, $filter) {
        var vm = this;

        angular.extend(this, $controller('PermissionController', {
            $scope: $scope,
            vm: vm,
            entityName: 'project'
        }));

        vm.changePage = function() {
            vm.loading = true;
            var filter = null;

            if (vm.filterProjects) {
                filter = 'name=like=' + vm.filterProjects + ',description=like=' + vm.filterProjects;
            }

            projectService
                .getAll(vm.currentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.projects = resp.data.records;
                    vm.totalCount = resp.data.metadata.totalCount;
                    vm.loading = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    vm.loading = false;
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.syncWithSciManager = function(projectSlug) {
            vm.projectSlug = projectSlug;
        };

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
        };

        vm.doDelete = function(projectSlug) {
            $rootScope.loadingAsync++;

            projectService
                .delete(projectSlug)
                .then(function(resp) {
                    vm.changePage();
                    toastr.success('Action performed with success.', 'Success!');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        };

        vm.deleteProject = function(projectSlug) {
            vm.projectSlug = projectSlug;
        };

        vm.editProject = function(projectSlug) {
            vm.projectSaveTitle = 'Update project';
            vm.updateProject = true;

            projectService
                .getBySlug(projectSlug)
                .then(function(resp) {
                    vm.project = resp.data;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                });
        };

        vm.insertProject = function() {
            vm.project = {};
            vm.updateProject = false;
            vm.projectSaveTitle = 'Create project';
        };

        vm.doSave = function() {
            if (!vm.updateProject) {
                projectService
                    .insert(vm.project)
                    .then(function(resp) {
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                projectService
                    .update(vm.project)
                    .then(function(resp) {
                        vm.changePage();
                        toastr.success('Success!', 'Action performed with success.');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        };

        init();
        
        function init() {            
            vm.limit = 20;
            vm.totalCount = 0;
            vm.currentPage = 1;
            vm.projects = [];
            vm.updateProject = false;
            vm.project = {};

            vm.changePage();
        };
    }

})();