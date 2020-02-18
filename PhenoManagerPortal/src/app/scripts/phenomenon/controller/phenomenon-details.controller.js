(function() {
    'use strict';

    angular
        .module('pheno-manager.phenomenon')
        .controller('PhenomenonDetailsController', PhenomenonDetailsController);

        PhenomenonDetailsController.$inject = ['$scope', '$stateParams', '$q', '$timeout', 'toastr', 'FileSaver', 'Blob', '$location', 'localStorageService', 'permissionService', 'phenomenonService', 'hypothesisService', 'userService', '$rootScope', '$state', '$filter'];

    function PhenomenonDetailsController($scope, $stateParams, $q, $timeout, toastr, FileSaver, Blob, $location, localStorageService, permissionService, phenomenonService, hypothesisService, userService, $rootScope, $state, $filter) {
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

            if (!vm.phenomenon || !vm.phenomenon.permissions) {
                vm.hasWriteAuthorization = false;
            }

            for (var i = 0; i < vm.phenomenon.permissions.length; i++) {
                if ((vm.phenomenon.permissions[i].role === 'ADMIN' ||
                     vm.phenomenon.permissions[i].role === 'WRITE') &&
                    ((vm.phenomenon.permissions[i].user && vm.loggedUserSlug === vm.phenomenon.permissions[i].user.slug) || 
                     vm.teamContainsUser(vm.phenomenon.permissions[i].team, vm.loggedUserSlug))) {
                    
                    vm.hasWriteAuthorization = true;
                    vm.roles = [{
                        'value': 'WRITE',
                        'name': 'Write'
                    }, {
                        'value': 'READ',
                        'name': 'Read'
                    }];

                    if (vm.phenomenon.permissions[i].role === 'ADMIN') {
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
            vm.permission.phenomenon = {
                slug: vm.phenomenon.slug
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
        }

        vm.deleteHypothesis = function(hypothesisSlug) {
            vm.hypothesisSlug = hypothesisSlug;
        }

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
        }

        vm.insertBranchHypothesis = function(parentHypothesis) {
            vm.childHypothesis = {};
            vm.parentHypothesis = angular.copy(parentHypothesis);
        }

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
        }

        vm.insertHypothesis = function() {
            vm.hypothesis = {};
            vm.updateHypothesis = false;
            vm.hypothesisSaveTitle = 'Create hypothesis';
        }

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
        }

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
        }

        vm.changePermissionPage = function() {
            vm.loadingPermission = true;

            permissionService
                .getAll(vm.permissionCurrentPage - 1, vm.limit, 'phenomenon.slug=' + vm.phenomenon.slug)
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
        }

        function getPhenomenon() {
            vm.loadingPhenomenon = true;

            phenomenonService
                .getBySlug(vm.phenomenonSlug)
                .then(function(resp) {
                    resp.data.parsedInsertDate = new Date(resp.data.insertDate);
                    vm.phenomenon = resp.data;
                    vm.checkWriteAccess();
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

            vm.loggedUserSlug = localStorageService.getUserSlug();
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
