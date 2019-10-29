(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('headerTop', headerTop);

    headerTop.$inject = ['$rootScope', '$filter', 'localStorageService', 'userService', '$state', '$timeout', 'toastr'];

    function headerTop($rootScope, $filter, localStorageService, userService, $state, $timeout, toastr) {
        return {
            restric: 'A',
            templateUrl: '/scripts/core/view/templates/header.html',
            controller: function ($scope, $state) {
                
                var FIVE_MINUTES = 5 * 60 * 1000;
                var TEN_SECONDS =  10 * 1000;

                $scope.username = localStorageService.getUserName();
                $scope.loggedUserSlug = localStorageService.getUserSlug(); 
                $rootScope.loadingAsync = 0;
                
            	$scope.logout = function() {
                    localStorageService.deleteToken();
                    $state.go('login');
                };

                $scope.loadLoggedUser = function() {
                    userService
                        .getBySlug($scope.loggedUserSlug)
                            .then(function(resp) {
                                $rootScope.loggedUser = resp.data;
                            })
                            .catch(function(resp) {
                                console.log(resp);
                                toastr.error('Error while performing action.', 'Unexpected error!');
                            });
                };

                $scope.doUpdateUserPassword = function() {
                    $rootScope.loggedUser.password = $rootScope.loggedUser.newPassword;
                    $scope.doUpdateProfile();
                };

                $scope.doUpdateProfile = function() {
                    userService
                        .update($rootScope.loggedUser)
                        .then(function(resp) {
                            toastr.success('Action performed with success.', 'Success!');
                        })
                        .catch(function(resp) {
                            console.log(resp);
                            toastr.error('Error while performing action.', 'Unexpected error!');
                        });
                };

                $scope.loadLoggedUser();

                $('[data-toggle="sidebar"]').bind('click', function() {
                    var $target = $('[data-pages="sidebar"]');
                    var timer;
                    var bodyColor = $('body').css('background-color');
                    
                    $('.page-container').css('background-color', bodyColor);
                    if ($('body').hasClass('sidebar-open')) {
                        $('body').removeClass('sidebar-open');
                        timer = setTimeout(function() {
                            $('.page-sidebar').removeClass('visible');
                        }.bind(this), 400);
                    } else {
                        clearTimeout(timer);
                        $('.page-sidebar').addClass('visible');
                        
                        setTimeout(function() {
                            $('body').addClass('sidebar-open');
                        }.bind(this), 10);

                        setTimeout(function(){
                           $('.page-container').css({'background-color': ''});
                        }, 1000);
                    }
                });
            }
        }
    }
})();
