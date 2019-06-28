(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('sidebarMenu', sidebarMenu);

    sidebarMenu.$inject = ['$http', 'config', '$rootScope', '$filter', '$location', 'localStorageService', 'toastr'];

    function sidebarMenu($http, config, $rootScope, $filter, $location, localStorageService, toastr) {
        return {
            restric: 'AEC',
            templateUrl: '/scripts/core/view/templates/sidebar-menu.html',
            scope: true,
            controller: function ($scope, $element, $state) {
                var baseUrl = config.baseUrl;

                $scope.getActiveClass = function(path) {
                    return ($location.path().substr(0, path.length) === path) ? 'active' : '';
                }

                $scope.getActiveSpanClass = function(path) {
                    return ($location.path().substr(0, path.length) === path) ? 'bg-primary-dark' : '';
                }

                $scope.allowAdminAccess = function() {
                    return 'ADMIN' === localStorageService.getRole();
                }
                
                $('.sidebar-menu a').bind('click', function() {
                    if ($(this).parent().children('.sub-menu') === false) {
                        return;
                    }

                    var el = $(this);
                    var parent = $(this).parent().parent();
                    var li = $(this).parent();
                    var sub = $(this).parent().children('.sub-menu');
    
                    if (li.hasClass('open active')) {
                        el.children('.arrow').removeClass("open active");
                        sub.slideUp(200, function() {
                            li.removeClass("open active");
                        });
                    } else {
                        parent.children('li.open').children('.sub-menu').slideUp(200);
                        parent.children('li.open').children('a').children('.arrow').removeClass('open active');
                        parent.children('li.open').removeClass("open active");
                        el.children('.arrow').addClass("open active");

                        sub.slideDown(200, function() {
                            li.addClass("open active");
                        });
                    }
                });
            
                function isVisibleXs() {
                    (!$('#pg-visible-xs').length) && $('body').append('<div id="pg-visible-xs" class="visible-xs" />');
                    return $('#pg-visible-xs').is(':visible');
                }
            
                function isVisibleSm() {
                    (!$('#pg-visible-sm').length) && $('body').append('<div id="pg-visible-sm" class="visible-sm" />');
                    return $('#pg-visible-sm').is(':visible');
                }
                
                $('.page-sidebar').bind('mouseenter mouseleave', function() {
                    var sideBarWidthCondensed = 280 - 70;
                    var _sideBarWidthCondensed = $('body').hasClass("rtl") ? - sideBarWidthCondensed : sideBarWidthCondensed;
    
                    if (isVisibleSm() || isVisibleXs()) {
                        return false
                    }
                    if ($('.close-sidebar').data('clicked')) {
                        return;
                    }
                    if ($('body').hasClass('menu-pin')) {
                        return;
                    }
    
                    $(this).css({
                        'transform': 'translate3d(' + _sideBarWidthCondensed + 'px, 0,0)'
                    });

                    $('body').addClass('sidebar-visible');
                });
                
                $('.page-container').bind('mouseover', function(e) {
                    if (isVisibleSm() || isVisibleXs()) {
                        return false
                    }
                    if (typeof e != 'undefined') {
                        var target = $(e.target);
                        if (target.parent('.page-sidebar').length) {
                            return;
                        }
                    }
                    if ($('body').hasClass('menu-pin'))
                        return;
    
                    if ($('.sidebar-overlay-slide').hasClass('show')) {
                        $('.sidebar-overlay-slide').removeClass('show')
                    }
    
                    $('.page-sidebar').css({
                        'transform': 'translate3d(0, 0, 0)'
                    });

                    $('body').removeClass('sidebar-visible');
                });

                $('[data-toggle-pin="sidebar"]').bind('click', function() {
                    var $target = $('[data-pages="sidebar"]');
                    $('body').toggleClass('menu-pin');
                });

                function toggleMenuPin() {
                    var width = $(window).width();
                    if (width < 1200) {
                        if ($('body').hasClass('menu-pin')) {
                            $('body').removeClass('menu-pin')
                            $('body').addClass('menu-unpinned')
                        }
                    } else {
                        if ($('body').hasClass('menu-unpinned')) {
                            $('body').addClass('menu-pin')
                        }
                    }
                }
                
                $(document).bind('ready', toggleMenuPin);
                $(window).bind('resize', toggleMenuPin);    
            }
        }
    }
})();
