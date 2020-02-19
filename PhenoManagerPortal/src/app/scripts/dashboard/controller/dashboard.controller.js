(function() {
    'use strict';

    angular
        .module('pheno-manager.dashboard')
        .controller('DashboardController', DashboardController);

        DashboardController.$inject = ['$scope', '$location', 'toastr', 'projectService', 'dashboardService', 'computationalModelService', 'hypothesisService', '$state', '$filter', '$timeout'];

    function DashboardController($scope, $location, toastr, projectService, dashboardService, computationalModelService, hypothesisService, $state, $filter, $timeout) {
        var vm = this;

        init();
        
        function init() {
            vm.totalRunningCounter = 0;
            vm.totalRunning = 0;

            vm.totalErrorCounter = 0;
            vm.totalError = 0;

            vm.successValidationCounter = 0;
            vm.successValidation = 0;

            vm.totalValidationCounter = 0;
            vm.totalValidation = 0;

            vm.projects = [];
            vm.computationalModels = [];
            vm.hypotheses = [];
        
            loading();
            getProjects();
            getComputationalModels();
            getHypotheses();
            getCardsStatistics();
        }

        function loading() {
            vm.loadingTotalRunning = true;
            vm.loadingTotalError = true;
            vm.loadingValidation = true;
            vm.loadingProjects = true;
            vm.loadingComputationalModels = true;
            vm.loadingHypotheses = true;
        }

        function getHypotheses() {
            hypothesisService
                .getAll(0, 10)
                .then(function(resp) {
                    vm.loadingHypotheses = false;
                    vm.hypotheses = resp.data.records;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for hypotheses.', 'Unexpected error!');
                });
        }

        function getComputationalModels() {
            computationalModelService
                .getAll(0, 10)
                .then(function(resp) {
                    vm.loadingComputationalModels = false;
                    vm.computationalModels = resp.data.records;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for computational models.', 'Unexpected error!');
                });
        }

        function getProjects() {
            projectService
                .getAll(0, 10)
                .then(function(resp) {
                    vm.loadingProjects = false;
                    vm.projects = resp.data.records;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for projects.', 'Unexpected error!');
                });
        }

        function getCardsStatistics() {
            dashboardService
                .getValidationStatistics()
                .then(function(resp) {
                    vm.successValidation = resp.data.itemsValidated;
                    vm.totalValidation = resp.data.totalItems;
                    vm.loadingValidation = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for validation statistics.', 'Unexpected error!');
                });

            dashboardService
                .getCountAllRunningModels()
                .then(function(resp) {
                    vm.totalRunning = resp.data.totalRunningModels;
                    vm.loadingTotalRunning = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for running models count.', 'Unexpected error!');
                });

            dashboardService
                .getCountAllErrorModels()
                .then(function(resp) {
                    vm.totalError = resp.data.totalErrorModels;
                    vm.loadingTotalError = false;
                })
                .catch(function(resp) {
                    console.log(resp);
                    toastr.error('Error while searching for models with error count.', 'Unexpected error!');
                });
        }
    }

})();