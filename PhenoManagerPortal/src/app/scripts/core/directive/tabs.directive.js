(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('tabs', tabs);

    tabs.$inject = ['$http', 'config', '$rootScope', '$filter', '$location', 'localStorageService', 'toastr'];

    function tabs($http, config, $rootScope, $filter, $location, localStorageService, toastr) {
        return {
            restric: 'AEC',
            scope: true,
            controller: function ($scope, $element, $state) {
               
                window.SelectFx && $('select[data-init-plugin="cs-select"]').each(function() {
                    var el = $(this).get(0);
                    $(el).wrap('<div class="cs-wrapper"></div>');
                    new SelectFx(el);
                });

                var drop = $($element);
                drop.addClass("hidden-sm-down");
                var content = '<select class="cs-select cs-skin-slide full-width" data-init-plugin="cs-select">'
               
                for (var i = 1; i <= drop.children("li").length; i++){
                    var li = drop.children('li:nth-child(' + i + ')');
                    var selected = '';
                    if (li.children('a').hasClass("active")) {
                        selected= 'selected';
                    }
                    var tabRef = li.children('a').attr('href');
                    if (tabRef === '#' || '') {
                        tabRef = li.children('a').attr('data-target')
                    }
                    content += '<option value="' + tabRef + '" ' + selected + '>';
                    content += li.children('a').text();
                    content += '</option>';
                }
               
                content += '</select>'
                drop.after(content);
                var select = drop.next()[0];
               
                $(select).on('change', function (e) {
                    var optionSelected = $('option:selected', this);
                    var valueSelected = this.value;
                    var tabLink = drop.find('a[data-target="' + valueSelected + '"]');
                    if (tabLink.length == 0) {
                        tabLink = drop.find('a[data-target="' + valueSelected + '"]')
                    }
                    tabLink.tab('show')
                })

                $(select).wrap('<div class="nav-tab-dropdown cs-wrapper full-width hidden-md-up"></div>');
                new SelectFx(select);
            }
        }
    }

})();