(function() {
    'use strict';

    angular
        .module('pheno-manager.team')
        .controller('TeamController', TeamController);

        TeamController.$inject = ['$scope', '$location', '$filter', 'teamService', '$rootScope', '$state', 'toastr'];

    function TeamController($scope, $location, $filter, teamService, $rootScope, $state, toastr) {
        var vm = this;

        vm.changePage = function() {
            vm.loading = true;
            var filter = null;

            if (vm.filterTeams) {
                filter = 'name=like=' + vm.filterTeams;
            }

            teamService
                .getAll(vm.currentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.teams = resp.data.records;
                    vm.totalCount = resp.data.metadata.totalCount;
                    vm.loading = false;
            })
            .catch(function(resp) {
                console.log(resp);
                vm.loading = false;
                toastr.error('Error while performing action.', 'Unexpected error!');
            });
            
        }

        vm.doDelete = function(teamSlug) {
            teamService
                .delete(teamSlug)
                    .then(function(resp) {
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }

        vm.deleteTeam = function(teamSlug) {
            vm.teamSlug = teamSlug;
        }

        vm.editTeam = function(teamSlug) {
            vm.teamSaveTitle = 'Update team';
            vm.updateTeam = true;

            teamService
                .getBySlug(teamSlug)
                    .then(function(resp) {
                        vm.team = resp.data;
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
        }

        vm.insertTeam = function() {
            vm.team = {
                teamUsers: []
            };
            vm.updateTeam = false;
            vm.teamSaveTitle = 'Create team';
        }

        vm.doSave = function() {
            if (!vm.updateTeam) {
                teamService
                    .insert(vm.team)
                    .then(function(resp) {
                        vm.data = resp.data;
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            } else {
                teamService
                    .update(vm.team)
                    .then(function(resp) {
                        vm.data = resp.data;
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
            }
        }

        init();
        
        function init() {            
            vm.updateTeam = false;
            vm.team = {};
    
            vm.limit = 20;
            vm.totalCount = 0;
            vm.currentPage = 1;
            vm.teams = [];
            
            vm.changePage();
        }
    }

})();
