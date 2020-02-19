(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .value('config', baseUrl());

    function baseUrl() {

        var hosts = {
            'localhost': 'http://localhost:9500/PhenoManagerApi'
        }

        var host = window.location.hostname;
        
        return {
            baseUrl: hosts[host]
        }
    }

})();