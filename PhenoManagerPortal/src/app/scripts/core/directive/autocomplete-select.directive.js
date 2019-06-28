(function () {
    'use strict';

    angular
        .module('pheno-manager.core')
        .directive('autocompleteSelect', autocompleteSelect);

    autocompleteSelect.$inject = ['$http', 'config', '$rootScope', '$filter',  '$timeout', 'localStorageService'];

    function autocompleteSelect($http, config, $rootScope, $filter, $timeout, localStorageService) {
        return {
            restric: 'AEC',
            scope: {
                placeholder: '@',
                minimumInputLength: '@',
                apiEndpoint: '@',
                apiFilters: '@',
                dropdownParent: '@',
                labelSelectText: '@',
                arrayValue: '=',
                data: '='
            },
            link: function ($scope, $element, $state) {
               
                $timeout(function() {
                    startSelect2();

                    function startSelect2() {
                        if ($scope.apiEndpoint) {
                            var baseUrl = config.baseUrl;

                            $($element).select2({
                                placeholder: $scope.placeholder || 'Select', 
                                minimumInputLength: $scope.minimumInputLength || 2,
                                dropdownParent: $scope.dropdownParent ? $($scope.dropdownParent) : undefined,
                                ajax: {
                                    delay: 250,
                                    url: baseUrl + $scope.apiEndpoint,
                                    headers: {
                                        'authorization': 'Bearer ' + localStorageService.getToken()
                                    },
                                    data: function(params) {
                                        var filters = 'filter=[';
                                        
                                        if (!$scope.apiFilters) {
                                            return filters + ']';
                                        }

                                        var filterKeys = $scope.apiFilters.split(',');
                                        for (var i = 0; i < filterKeys.length; i++) {
                                            if (i > 0) {
                                                filters += ','
                                            }
                                            filters += filterKeys[i].trim() + '=like=' + params.term;
                                        }
                                        filters += ']';                                  

                                        return filters;
                                    },
                                    processResults: function (data, params) {
                                        params.page = params.page || 1;
                                        
                                        if (!data || !data.records) {
                                            return {
                                                results: []
                                            };
                                        }
                                        var selectLabel = $scope.labelSelectText || 'text';
                                        var results = [];
                                        for (var i = 0; i < data.records.length; i++) {
                                            results.push({
                                                id: data.records[i].slug,
                                                text: data.records[i][selectLabel]
                                            })
                                        }

                                        return {
                                            results: results,
                                            pagination: {
                                                more: (params.page * 20) < data.records.length
                                            }
                                        };
                                    }
                                }
                            });
                        } else {
                            $($element).select2({ 
                                placeholder: $scope.placeholder || 'Select', 
                                minimumInputLength: $scope.minimumInputLength || 2,
                                dropdownParent: $scope.dropdownParent ? $($scope.dropdownParent) : undefined
                            });
                        }

                        $($element).trigger('change');

                        $($element).on('select2:select', function(e) {
                            if ($scope.arrayValue) {
                                var selectLabel = $scope.labelSelectText || 'text'; 
                                var values = [];

                                for (var i = 0; i < $($element).val().length; i++) {
                                    var valueData = {
                                        slug: $($element).val()[i]
                                    };
                                    valueData[selectLabel] = $($element)[i].innerText;
                                    values.push(valueData);
                                }

                                $scope.data = values;

                                var selectLabel = $scope.labelSelectText || 'text';                                        
                                var optionsHtml = [];

                                for (var i = 0; i < $scope.data.length; i++) {
                                    optionsHtml.push('<option value="');
                                    optionsHtml.push($scope.data[i].slug)
                                    optionsHtml.push('" selected>');
                                    optionsHtml.push($scope.data[i][selectLabel])
                                    optionsHtml.push('</option>');
                                }
                                
                                $($element).empty().append(optionsHtml.join(''));
                                $($element).trigger('change');

                                $scope.data = values;

                                $scope.$apply();

                            } else {
                                if ($scope.data === undefined || $scope.data === null) {
                                    $scope.data = {};
                                }
                                var selectLabel = $scope.labelSelectText || 'text'; 

                                $scope.data.slug = $($element).val();
                                $scope.data[selectLabel] = $($element)[0].innerText;
                                var modelData = $scope.data;

                                var selectLabel = $scope.labelSelectText || 'text';
                                var $option = $('<option selected></option>').val($scope.data.slug)
                                    .text($scope.data[selectLabel]);
                                
                                $($element).empty().append($option);
                                $($element).trigger('change');

                                $scope.data = modelData;

                                $scope.$apply();
                            }
                        });
                    }

                    $scope.$watch('data', function(newValue, oldValue) {
                        $timeout(function() {
                            if (newValue !== oldValue) {

                                if ($scope.arrayValue) {
                                    
                                    if ($scope.data && Array.isArray($scope.data) && $scope.data.length > 0) {
                                        
                                        var modelData = $scope.data;

                                        var selectLabel = $scope.labelSelectText || 'text';                                        
                                        var optionsHtml = [];

                                        for (var i = 0; i < $scope.data.length; i++) {
                                            optionsHtml.push('<option value="');
                                            optionsHtml.push($scope.data[i].slug)
                                            optionsHtml.push('" selected>');
                                            optionsHtml.push($scope.data[i][selectLabel])
                                            optionsHtml.push('</option>');
                                        }
                                        
                                        $($element).empty().append(optionsHtml.join(''));
                                        $($element).trigger('change');

                                        $scope.data = modelData;
                                    
                                    } else {
                                        $($element).empty();
                                        $($element).trigger('change');
                                    }

                                } else {
                                    
                                    if ($scope.data && $scope.data.slug) {
                                        var modelData = $scope.data;

                                        var selectLabel = $scope.labelSelectText || 'text';
                                        var $option = $('<option selected></option>').val($scope.data.slug)
                                            .text($scope.data[selectLabel]);
                                        
                                        $($element).empty().append($option);
                                        $($element).trigger('change');
                                    
                                        $scope.data = modelData;
                                    
                                    } else {
                                        $($element).empty();
                                        $($element).trigger('change');
                                    }
                                }
                            }
                        });
                    });

                    $scope.$watch('apiEndpoint', function(newValue, oldValue) {
                        $timeout(function() {
                            startSelect2();
                        });
                    });
                });
            }
        }
    }
})();