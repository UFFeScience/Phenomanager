(function() {
    'use strict';

    angular
        .module('pheno-manager.permission')
        .controller('PermissionController', PermissionController);

    PermissionController.$inject = ['$scope', 'vm', 'entityName', '$rootScope', 'permissionService', 'localStorageService', 'toastr'];

    function PermissionController($scope, vm, entityName, $rootScope, permissionService, localStorageService, toastr) {
        var vm = vm || this;

        vm.entityName = entityName;
        vm.loggedUserSlug = localStorageService.getUserSlug();
        vm.totalPermissionCount = 0;
        vm.permissionCurrentPage = 1;
        vm.permissions = [];
        vm.permission = {};

        vm.hasWriteAuthorization = false;
        
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

        vm.checkWriteAccess = function(entity) {
            vm.hasWriteAuthorization = false;

            if (!entity || !entity.permissions) {
                vm.hasWriteAuthorization = false;
            }

            for (var i = 0; i < entity.permissions.length; i++) {
                if ((entity.permissions[i].role === 'ADMIN' ||
                     entity.permissions[i].role === 'WRITE') &&
                    (entity.permissions[i].user && vm.loggedUserSlug === entity.permissions[i].user.slug || 
                     vm.teamContainsUser(entity.permissions[i].team, vm.loggedUserSlug))) {
                    
                    vm.hasWriteAuthorization = true;
                    vm.roles = [{
                        'value': 'WRITE',
                        'name': 'Write'
                    }, {
                        'value': 'READ',
                        'name': 'Read'
                    }];

                    if (entity.permissions[i].role === 'ADMIN') {
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
            vm.permission[vm.entityName] = {
                slug: vm[vm.entityName].slug
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

        vm.changePermissionPage = function() {
            vm.loadingPermission = true;

            permissionService
                .getAll(vm.permissionCurrentPage - 1, vm.limit, vm.entityName + '.slug=' + vm[vm.entityName].slug)
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
    }
})();
