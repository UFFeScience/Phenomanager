(function() {
    'use strict';

    angular
        .module('pheno-manager.project')
        .controller('ProjectDetailsController', ProjectDetailsController);

        ProjectDetailsController.$inject = ['$scope', '$stateParams', '$q', '$timeout', 'toastr','$location', 'phenomenonService', 'permissionService', 'userService', 'localStorageService', 'projectService', '$rootScope', '$state', '$filter'];

    function ProjectDetailsController($scope, $stateParams, $q, $timeout, toastr, $location, phenomenonService, permissionService, userService, localStorageService, projectService, $rootScope, $state, $filter) {
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

            if (!vm.project || !vm.project.permissions) {
                vm.hasWriteAuthorization = false;
            }

            for (var i = 0; i < vm.project.permissions.length; i++) {
                if ((vm.project.permissions[i].role === 'ADMIN' ||
                     vm.project.permissions[i].role === 'WRITE') &&
                    (vm.loggedUserSlug === vm.project.permissions[i].user.slug || 
                     vm.teamContainsUser(vm.project.permissions[i].team, vm.loggedUserSlug))) {
                    
                    vm.hasWriteAuthorization = true;
                    vm.roles = [{
                        'value': 'WRITE',
                        'name': 'Write'
                    }, {
                        'value': 'READ',
                        'name': 'Read'
                    }];

                    if (vm.project.permissions[i].role === 'ADMIN') {
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
            vm.permission.project = {
                slug: vm.project.slug
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

        vm.changePermissionPage = function() {
            vm.loadingPermission = true;

            permissionService
                .getAll(vm.permissionCurrentPage - 1, vm.limit, 'project.slug=' + vm.project.slug)
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
                    vm.checkWriteAccess();
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

            vm.loggedUserSlug = localStorageService.getUserSlug();
            vm.projectSlug = $stateParams.slug;
            vm.project = {};
        
            vm.limit = 20;

            vm.totalPhenomenonCount = 0;
            vm.phenomenonCurrentPage = 1;
            vm.phenomenons = [];
            vm.updatePhenomenon = false;
            vm.phenomenon = {};

            vm.totalPermissionCount = 0;
            vm.permissionCurrentPage = 1;
            vm.permissions = [];
            vm.permission = {};

            vm.hasWriteAuthorization = false;

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
