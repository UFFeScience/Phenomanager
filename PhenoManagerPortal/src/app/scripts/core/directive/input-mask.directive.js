(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('inputMask', inputMask);

        inputMask.$inject = ['$http', '$timeout'];

    function inputMask($http, $timeout) {
        return {
            restric: 'AEC',
            scope: {
                mask: '@',
                money: '='
            },
            link: function ($scope, $element, $attrs) {
               
                $timeout(function() {
                    var options = {
                        onKeyPress: function(val, e, field, options) {
                            applyMask();
                        }
                    }
                    
                    if ($scope.money) {
                        options.reverse = true;
                    }

                    $($element).mask($scope.mask, options);
            
                    function applyMask() {
                        $($element).mask($scope.mask, options);
                    }
                });
            }
        }
    }

})();