(function() {
    'use strict';

    angular
        .module('pheno-manager.user')
        .controller('UserController', UserController);

        UserController.$inject = ['$scope', 'localStorageService', '$location', '$filter', 'userService', '$rootScope', '$state', 'toastr'];

    function UserController($scope, localStorageService, $location, $filter, userService, $rootScope, $state, toastr) {
        var vm = this;

        vm.changePage = function() {
            vm.loading = true;
            var filter = null;

            if (vm.filterUsers) {
                filter = 'name=like=' + vm.filterUsers + ',email=like=' + vm.filterUsers 
                    + ',email=institutionName=' + vm.filterUsers;
            }

            userService
                .getAll(vm.currentPage - 1, vm.limit, filter)
                .then(function(resp) {
                    for (var i = 0; i < resp.data.records.length; i++) {
                        resp.data.records[i].parsedInsertDate = new Date(resp.data.records[i].insertDate);
                    }
                    vm.users = resp.data.records;
                    vm.totalCount = resp.data.metadata.totalCount;
                    vm.loading = false;
            })
            .catch(function(resp) {
                console.log(resp);
                vm.loading = false;
                toastr.error('Error while performing action.', 'Unexpected error!');
            });
            
        }

        vm.syncWithSciManager = function(userSlug) {
            vm.userSlug = userSlug;
        }

        vm.doSyncSciManager = function(userSlug) {
            $rootScope.loadingAsync++;

            userService
                .syncSciManager(userSlug)
                .then(function(resp) {
                    console.log(resp);
                    toastr.success('Success!', 'Action performed with succes, user data is being synced.');
                    $rootScope.loadingAsync--;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while performing action.', 'Unexpected error!');
                    $rootScope.loadingAsync--;
                });
        }

        vm.doDelete = function(userSlug) {
            $rootScope.loadingAsync++;

            userService
                .delete(userSlug)
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
            }

        vm.deleteUser = function(userSlug) {
            vm.userSlug = userSlug;
        }

        vm.editUser = function(userSlug) {
            vm.userSaveTitle = 'Update user';
            vm.updateUser = true;

            userService
                .getBySlug(userSlug)
                    .then(function(resp) {
                        vm.user = resp.data;
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        toastr.error('Error while performing action.', 'Unexpected error!');
                    });
        }

        vm.insertUser = function() {
            vm.user = {};
            vm.updateUser = false;
            vm.userSaveTitle = 'Create user';
        }

        vm.doSave = function() {
            if (!vm.updateUser) {
                userService
                    .insert(vm.user)
                    .then(function(resp) {
                        vm.data = resp.data;
                        vm.changePage();
                        toastr.success('Action performed with success.', 'Success!');
                    })
                    .catch(function(resp) {
                        console.log(resp);
                        if (resp.status === 400) {
                            toastr.warning('Duplicate user email.', 'Invalid data!');
                        } else {
                            toastr.error('Error while performing action.', 'Unexpected error!');
                        }
                    });
            } else {
                userService
                    .update(vm.user)
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
            vm.loggedUserSlug = localStorageService.getUserSlug();
            
            vm.updateUser = false;
            vm.user = {};
    
            vm.limit = 20;
            vm.totalCount = 0;
            vm.currentPage = 1;
            vm.users = [];
            
            vm.roles = [{
                'value': 'ADMIN',
                'name': 'Admin'
            }, {
                'value': 'USER',
                'name': 'Regular User'
            }];
            
            vm.changePage();
        }
    }

})();
