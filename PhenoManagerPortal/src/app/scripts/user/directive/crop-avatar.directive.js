(function () {
    'use strict';

    angular
        .module('pheno-manager.user')
        .directive('cropAvatar', cropAvatar);

        cropAvatar.$inject = ['$rootScope', '$filter', 'localStorageService', 'userService', '$state', '$timeout', 'toastr'];

    function cropAvatar($rootScope, $filter, localStorageService, userService, $state, $timeout, toastr) {
        return {
            restric: 'A',
            templateUrl: '/scripts/user/view/templates/crop-avatar.html',
            controller: function ($scope, $state) {
                
                var $image = $('.img-container > img'),
                    $dataX = $('#dataX'),
                    $dataY = $('#dataY'),
                    $dataHeight = $('#dataHeight'),
                    $dataWidth = $('#dataWidth'),
                    $dataRotate = $('#dataRotate'),
                    options = {
                        aspectRatio: 1 / 1,
                        preview: '.img-preview',
                        crop: function(data) {
                            $dataX.val(Math.round(data.x));
                            $dataY.val(Math.round(data.y));
                            $dataHeight.val(Math.round(data.height));
                            $dataWidth.val(Math.round(data.width));
                            $dataRotate.val(Math.round(data.rotate));
                        }
                    };
            
                $image.on({
                    'build.cropper': function(e) {},
                    'built.cropper': function(e) {}
                }).cropper(options);
            
                $(document.body).on('click', '[data-method]', function() {
                    var data = $(this).data(),
                        $target,
                        result;
                
                    if (data.method) {
                        data = $.extend({}, data);
                
                        if (typeof data.target !== 'undefined') {
                            $target = $(data.target);
                    
                            if (typeof data.option === 'undefined') {
                                try {
                                    data.option = JSON.parse($target.val());
                                } catch (e) {
                                    console.log(e.message);
                                }
                            }
                        }
                
                        result = $image.cropper(data.method, data.option);
                
                        if (data.method === 'getDataURL') {
 
                            if (result != null && result !== undefined && result) {

                                $rootScope.loadingAsync++;
                                $rootScope.loggedUser.imageContentText = result;

                                userService
                                    .update($rootScope.loggedUser)
                                    .then(function(resp) {
                                        $rootScope.loggedUser.profileImageFileId = resp.data.profileImageFileId;
                                        toastr.success('Action performed with success.', 'Success!');
                                        $rootScope.loadingAsync--;
                                    })
                                    .catch(function(resp) {
                                        toastr.error('Error while performing action.', 'Unexpected error!');
                                        $rootScope.loadingAsync--;
                                    });
                            }
                            else {
                                toastr.info('Select avatar.', 'Info!');
                            }
                        }
                
                        if ($.isPlainObject(result) && $target) {
                            try {
                                $target.val(JSON.stringify(result));
                            } catch (e) {
                                console.log(e.message);
                            }
                        }
                
                    }
                }).on('keydown', function(e) {
            
                    switch (e.which) {
                        case 37:
                            e.preventDefault();
                            $image.cropper('move', -1, 0);
                            break;
                
                        case 38:
                            e.preventDefault();
                            $image.cropper('move', 0, -1);
                            break;
                
                        case 39:
                            e.preventDefault();
                            $image.cropper('move', 1, 0);
                            break;
                
                        case 40:
                            e.preventDefault();
                            $image.cropper('move', 0, 1);
                            break;
                    }
        
                });
            
                // Import image
                var $inputImage = $('#inputImage'),
                    URL = window.URL || window.webkitURL,
                    blobURL;
            
                if (URL) {
                    $inputImage.change(function() {
                        var files = this.files,
                            file;
                
                        if (files && files.length) {
                            file = files[0];
                    
                            if (file.size > 2097152) {
                                toastr.warning('Image size, exceed the limit of 2mb.', 'Warning!');
                                return
                            }
                    
                            if (/^image\/\w+$/.test(file.type)) {
                                blobURL = URL.createObjectURL(file);
                                
                                $image.one('built.cropper', function () {
                                    URL.revokeObjectURL(blobURL); // Revoke when load complete
                                }).cropper('reset', true).cropper('replace', blobURL);

                                $inputImage.val('');
                            } else {
                                toastr.info('Select avatar.', 'Info!');
                            }
                        }
                    });
                }
            
                // Options
                $('.docs-options :checkbox').on('change', function() {
                    var $this = $(this);
                
                    options[$this.val()] = $this.prop('checked');
                    $image.cropper('destroy').cropper(options);
                });
                
            }
        }
    }
})();
