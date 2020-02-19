(function() {
    'use strict';

    angular
        .module('pheno-manager.core')
        .service('localStorageService', localStorageService);

    localStorageService.$inject = ['jwtHelper'];

    function localStorageService(jwtHelper) {
        return {

            getUserName: function() {
                if (localStorage) {
                    var token = this.getToken();

                    if (!token) {
                        return undefined;
                    }

                    return jwtHelper.decodeToken(token).name;
                }
                else {
                    return undefined;
                }
            },

            getUserSlug: function() {
                if (localStorage) {
                    var token = this.getToken();

                    if (!token) {
                        return undefined;
                    }

                    return jwtHelper.decodeToken(token).userSlug;
                }
                else {
                    return undefined;
                }
            },

            getRole: function() {
                if (localStorage) {
                    var token = this.getToken();

                    if (!token) {
                        return undefined;
                    }

                    return jwtHelper.decodeToken(token).role;
                }
                else {
                    return undefined;
                }
            },

            getUserEmail: function() {
                if (localStorage) {
                    var token = this.getToken();

                    if (!token) {
                        return undefined;
                    }

                    return jwtHelper.decodeToken(token).email;
                }
                else {
                    return undefined;
                }
            },

            getToken: function() {
                if (localStorage) {
                    var token = localStorage.getItem('token');

                    try {
                        jwtHelper.decodeToken(token);
                        return token;
                    }
                    catch (err) {
                        return undefined;
                    }
                }
                else {
                    return undefined;
                }
            },

            verifyTokenExpiration: function() {
                if (localStorage) {
                    var token = localStorage.getItem('token');

                    try {
                        return jwtHelper.isTokenExpired(token);
                    }
                    catch (err) {
                        return true;
                    }
                }
                else {
                    return true;
                }
            },

            deleteToken: function() {
                if (localStorage) {
                    delete localStorage.token;
                }
            },

            setToken: function(token) {
                if (localStorage) {
                    localStorage.token = token;
                }
            }
        }
    }

})();