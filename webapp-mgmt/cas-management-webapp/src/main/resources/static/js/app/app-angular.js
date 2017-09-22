var url = new URL(document.location);
var array = url.pathname.split("/");
if (array.length == 3) {
    appContext = "/" + array[1];
} else {
    appContext = "";
}

(function () {
    var app = angular.module('casmgmt', [
        'ui.sortable',
        'ngTable'
    ]);


    app.filter('checkmark', function () {
        return function (input) {
            return input ? '\u2713' : '\u2718';
        };
    })
        .filter('wordCharTrunc', function () {
            return function (str, limit) {
                if (typeof str != 'string') {
                    return '';
                }
                if (!limit || str.length <= limit) {
                    return str;
                }

                var newStr = str.substring(0, limit).replace(/\w+$/, '');
                return (newStr || str.substring(0, limit)) + '...';
            };
        })
        .filter('serviceTableFilter', function () {
            return function (services, fields, regex) {
                if (typeof fields == 'string') {
                    fields = [fields];
                }
                try {
                    regex = regex ? new RegExp(regex, 'i') : false;
                } catch (e) {
                    // TODO: How do we want to tell the user their regex is bad? On error, return list or null?
                    regex = false;
                }
                if (!services || !fields || !regex) {
                    return services;
                }

                var matches = [];
                angular.forEach(services, function (service, i) {
                    angular.forEach(fields, function (field, j) {
                        if (regex.test(service[field]) && matches.indexOf(service) == -1) {
                            matches.push(service);
                        }
                    });
                });
                return matches;
            };
        });

    app.factory('sharedFactoryCtrl', [
        function () {
            var factory = {
                assignedId: null,
                sourceId: null
            };

            factory.httpHeaders = {};
            factory.httpHeaders[$("meta[name='_csrf_header']").attr("content")] = $("meta[name='_csrf']").attr("content");

            factory.maxEvalOrder = 0;

            factory.setItem = function (id) {
                factory.assignedId = id;
                factory.sourceId = null;
            };
            factory.duplicateItem = function (id) {
                factory.assignedId = null;
                factory.sourceId = id;
            };
            factory.clearItem = function () {
                factory.assignedId = null;
                factory.sourceId = null;
            };
            factory.getItem = function () {
                return factory.assignedId;
            };

            factory.forceReload = function () {
                $('#logoutUrlLink')[0].click();
            };

            return factory;
        }
    ]);

// View Swapper
    app.controller('actionsController', [
        '$timeout',
        'sharedFactoryCtrl',
        function ($timeout, sharedFactory) {
            var action = this;

            this.actionPanel = 'manage';

            this.selectAction = function (setAction) {
                action.actionPanel = setAction;
            };

            this.isSelected = function (checkAction) {
                return action.actionPanel === checkAction;
            };

            this.homepage = function () {
                action.selectAction('manage');
                sharedFactory.clearItem();
            };

            this.serviceAdd = function () {
                sharedFactory.clearItem();
                $timeout(function () {
                    action.selectAction('add');
                }, 100);
            };

            this.serviceDuplicate = function (id) {
                sharedFactory.duplicateItem(id);
                $timeout(function () {
                    action.selectAction('add');
                }, 100);
            }

            this.serviceEdit = function (id) {
                sharedFactory.setItem(id);
                $timeout(function () {
                    action.selectAction('edit');
                }, 100);
            };
        }
    ]);

// Services Table: Manage View
    app.controller('ServicesTableController', [
        '$scope',
        '$http',
        '$timeout',
        'sharedFactoryCtrl',
        function ($scope, $http, $timeout, sharedFactory) {
            var serviceData = this,
                httpHeaders = sharedFactory.httpHeaders,
                delayedAlert = function (n, t, d, skipScrollTop) {
                    skipScrollTop = skipScrollTop || false;
                    $timeout(function () {
                        serviceData.alert = {
                            name: n,
                            type: t,
                            data: d
                        };
                    }, 10);
                    if (!skipScrollTop) {
                        $timeout(function () {
                            $('html, body').animate({
                                scrollTop: $('.service-editor').offset().top
                            }, 750);
                        }, 100);
                    }
                };

            this.dataTable = null; // Prevents 'flashing' on load
            this.sortableOptions = {
                axis: 'y',
                items: '> tr',
                handle: '.grabber-icon',
                placeholder: 'tr-placeholder',
                start: function (e, ui) {
                    serviceData.detailRow = -1;
                    ui.item.data('data_changed', false);
                },
                update: function (e, ui) {
                    ui.item.data('data_changed', true);
                },
                stop: function (e, ui) {
                    if (ui.item.data('data_changed')) {
                        var myData = $(this).sortable('serialize', {key: 'id'});

                        $.ajax({
                            type: 'post',
                            url: appContext + '/updateRegisteredServiceEvaluationOrder.html',
                            data: myData,
                            headers: httpHeaders,
                            dataType: 'json',
                            success: function (data, status) {
                                if (data.status != 200)
                                    delayedAlert('notupdated', 'danger', data);
                                else if (angular.isString(data))
                                    sharedFactory.forceReload();
                                else
                                    serviceData.getServices();
                            },
                            error: function (xhr, status) {
                                if (xhr.status == 403)
                                    sharedFactory.forceReload();
                                else
                                    delayedAlert('notupdated', 'danger', xhr.responseJSON);
                            }
                        });
                    }
                }
            };

            this.getServices = function () {
                $http.get(appContext + '/getServices.html')
                    .then(function (response) {
                        if (response.status != 200) {
                            delayedAlert('listfail', 'danger', response.data);
                        }
                        else {
                            if (serviceData.alert && serviceData.alert.type != 'info')
                                serviceData.alert = null;
                            serviceData.dataTable = response.data.services || [];
                            angular.forEach(serviceData.dataTable, function (service) {
                                if (service.evalOrder > sharedFactory.maxEvalOrder) {
                                    sharedFactory.maxEvalOrder = service.evalOrder;
                                }
                            });
                        }
                    });
            };

            this.openModalDelete = function (item) {
                serviceData.modalItem = item;
                $timeout(function () {
                    $('#confirm-delete .btn-default').focus();
                }, 100);
            };
            this.closeModalDelete = function () {
                serviceData.modalItem = null;
            };


            this.deleteService = function (item) {
                var myData = {id: item.assignedId};

                serviceData.closeModalDelete();
                $.ajax({
                    type: 'post',
                    url: appContext + '/deleteRegisteredService.html',
                    data: myData,
                    headers: httpHeaders,
                    success: function (data, status) {
                        if (data.status != 200)
                            delayedAlert('notdeleted', 'danger', data);
                        else if (angular.isString(data))
                            sharedFactory.forceReload();
                        else {
                            serviceData.getServices();
                            delayedAlert('deleted', 'info', item, true);
                        }
                    },
                    error: function (xhr, status) {
                        if (xhr.status == 403)
                            sharedFactory.forceReload();
                        else
                            delayedAlert('notdeleted', 'danger', xhr.responseJSON);
                    }
                });
            };

            this.clearFilter = function () {
                serviceData.serviceTableQuery = "";
            };

            this.toggleDetail = function (rowId) {
                serviceData.detailRow = serviceData.detailRow == rowId ? -1 : rowId;
            };

            $scope.$watch(
                function () {
                    return sharedFactory.assignedId;
                },
                function (newAssignedId, oldAssignedId) {
                    if (oldAssignedId && !newAssignedId)
                        serviceData.getServices();
                }
            );

            this.getServices();
        }
    ]);


    /**
     * Rejected Attributes controller
     */
    app.controller("rejectedAttributesController", rejAttrController);
    // rejAttrController.$inject = ["NgTableParams"];
    // function rejAttrController(NgTableParams) {
    //     var self = this;
    //
    //     var dataList = [];
    //
    //     var originalData = angular.copy(dataList);
    //
    //     self.tableParams = new NgTableParams({}, {
    //         filterDelay: 0,
    //         dataset: angular.copy(dataList),
    //         counts: []
    //     });
    //
    //     self.cancel = cancel;
    //     self.del = del;
    //     self.save = save;
    //
    //     //////////
    //
    //     /**
    //      * Todo: Refactor/cleanup the below code and wire things up
    //      */
    //
    //     function cancel(row, rowForm) {
    //         var originalRow = resetRow(row, rowForm);
    //         angular.extend(row, originalRow);
    //     }
    //
    //     function del(row) {
    //         _.remove(self.tableParams.settings().dataset, function (item) {
    //             return row === item;
    //         });
    //         self.tableParams.reload().then(function (data) {
    //             if (data.length === 0 && self.tableParams.total() > 0) {
    //                 self.tableParams.page(self.tableParams.page() - 1);
    //                 self.tableParams.reload();
    //             }
    //         });
    //     }
    //
    //     function resetRow(row, rowForm) {
    //         row.isEditing = false;
    //         // rowForm.$setPristine();
    //         // self.tableTracker.untrack(row);
    //         return _.findWhere(originalData, function (r) {
    //             return r.id === row.id;
    //         });
    //     }
    //
    //     function save(row, rowForm) {
    //         var originalRow = resetRow(row, rowForm);
    //         angular.extend(originalRow, row);
    //     }
    //
    //     function toggleAddRow(p) {
    //         // $scope.showAdd = true;
    //     }
    //
    //     function addRow(add) {
    //
    //         // add.id = $scope.nextid;
    //         // $scope.nextid += 1;
    //         // $scope.data.push(add);
    //         // $scope.showAdd = false;
    //         // $scope.add = {};
    //
    //     }
    //
    //     function cancelAdd() {
    //
    //     }
    // };
    function rejAttrController($scope, NgTableParams) {
        var self = this;

        self.tmpRowData;

        var dataList = [];

        var originalData = angular.copy(dataList);

        self.tableParams = new NgTableParams({}, {
            filterDelay: 0,
            dataset: angular.copy(dataList),
            counts: []
        });

        self.cancel = cancel;
        self.del = del;
        self.save = save;
        self.editRow = editRow;
        self.addRow = addRow;
        self.cancelAdd = cancelAdd;
        self.toggleAddRow = toggleAddRow;

        function cancel(row, rowForm) {
            // console.log('cancel', row);

            self.tmpRowData = {};

            row.isEditing = false;

            rowForm.$setPristine();

        }

        function del(row) {
            _.remove(self.tableParams.settings().dataset, function (item) {
                return row === item;
            });
            self.tableParams.reload().then(function (data) {
                if (data.length === 0 && self.tableParams.total() > 0) {
                    self.tableParams.page(self.tableParams.page() - 1);
                    self.tableParams.reload();
                }
            });
        }

        function resetRow(row, rowForm){
            // console.log('resetRow', row);
            row.isEditing = false;
            rowForm.$setPristine();
            self.tableTracker.untrack(row);
            return _.findWhere(originalData, function(r){
                return r.id === row.id;
            });
        }


        function save(row, tmpData, rowForm) {
            // console.log('save');
            angular.extend(row, tmpData);

            row.isEditing = false;
            rowForm.$setPristine();

        }

        function toggleAddRow( row ) {
            // console.log('toggleAddRow', row);

            // row.isAdding = true;
            // $scope.showAdd = true;
        }

        function editRow( row, rowForm) {
            // console.log('editRow');
            self.tmpRowData = angular.copy( row );
            row.isEditing = true;
        }

        function addRow(row, rowForm) {
            // console.log('addRow');
            self.tmpRowData = angular.copy( row );

            self.tableParams.settings().dataset.push( self.tmpRowData );
            var syncData = angular.copy(self.tableParams.settings().dataset).map(function(obj) {
                delete obj.$$hashKey;
                delete obj.isAdding;
                obj.value = obj.value;
                delete obj.value;
                return obj;
            });

            $scope.$parent.serviceFormCtrl.serviceData.supportAccess.rejectedAttr = syncData;
            row.name = '';
            row.value = '';
            row.isAdding = false;

            self.tableParams.reload();

        }

        function cancelAdd(row, rowForm) {
            // Clean up?
            // console.log('cancelAdd');
            row.name = '';
            row.value = '';
            row.isAdding = false;
        }

    }

    /**
     * Properties Pane controller
     */
    app.controller("propertiesController", propertiesController);

    function propertiesController($scope, NgTableParams) {
        var self = this;

        self.tmpRowData;

        var dataList = [];

        // console.debug(dataList);

        var originalData = angular.copy(dataList);

        self.tableParams = new NgTableParams({}, {
            filterDelay: 0,
            dataset: angular.copy(dataList),
            counts: []
        });

        self.cancel = cancel;
        self.del = del;
        self.save = save;
        self.editRow = editRow;
        self.addRow = addRow;
        self.cancelAdd = cancelAdd;
        self.toggleAddRow = toggleAddRow;

        function cancel(row, rowForm) {
            // console.log('cancel', row);

            self.tmpRowData = {};

            row.isEditing = false;

            rowForm.$setPristine();

        }

        function del(row) {
            _.remove(self.tableParams.settings().dataset, function (item) {
                return row === item;
            });
            self.tableParams.reload().then(function (data) {
                if (data.length === 0 && self.tableParams.total() > 0) {
                    self.tableParams.page(self.tableParams.page() - 1);
                    self.tableParams.reload();
                }
            });
        }

        function resetRow(row, rowForm){
            // console.log('resetRow', row);
            row.isEditing = false;
            rowForm.$setPristine();
            self.tableTracker.untrack(row);
            return _.findWhere(originalData, function(r){
                return r.id === row.id;
            });
        }


        function save(row, tmpData, rowForm) {
            // console.log('save');
            angular.extend(row, tmpData);

            row.isEditing = false;
            rowForm.$setPristine();

        }

        function toggleAddRow( row ) {
            // console.log('toggleAddRow', row);

            // row.isAdding = true;
            // $scope.showAdd = true;
        }

        function editRow( row, rowForm) {
            // console.log('editRow');
            self.tmpRowData = angular.copy( row );
            row.isEditing = true;
        }

        function addRow(row, rowForm) {
            // console.log('addRow');
            self.tmpRowData = angular.copy( row );

            self.tableParams.settings().dataset.push( self.tmpRowData );
            // var syncData = angular.copy($scope.$parent.serviceFormCtrl.serviceData.properties).map(function(obj) {
            //     delete obj.$$hashKey;
            //     delete obj.isAdding;
            //     obj.value = obj.value;
            //     delete obj.value;
            //     return obj;
            // });

            // $scope.$parent.serviceFormCtrl.serviceData.properties = syncData;
            $scope.$parent.serviceFormCtrl.serviceData.properties.push(angular.copy(row));
            row.name = '';
            row.value = '';
            row.isAdding = false;

            self.tableParams.reload();

        }

        function cancelAdd(row, rowForm) {
            // Clean up?
            // console.log('cancelAdd');
            row.name = '';
            row.value = '';
            row.isAdding = false;
        }

    }


// Service Form: Add/Edit Service View
    app.controller('ServiceFormController', [
        '$scope',
        '$http',
        '$timeout',
        'sharedFactoryCtrl',
        function ($scope, $http, $timeout, sharedFactory) {
            var serviceForm = this,
                httpHeaders = sharedFactory.httpHeaders,
                delayedAlert = function (n, t, d, skipScrollTop) {
                    skipScrollTop = skipScrollTop || false;
                    $timeout(function () {
                        serviceForm.alert = {
                            name: n,
                            type: t,
                            data: d
                        };
                    }, 10);
                    if (!skipScrollTop) {
                        $timeout(function () {
                            $('html, body').animate({
                                scrollTop: $('.service-editor').offset().top
                            }, 750);
                        }, 100);
                    }
                },
                showInstructions = function () {
                    $('.required-missing').removeClass('required-missing');
                    delayedAlert('instructions', 'info', null, true);
                };

            this.serviceData = {};
            this.formData = {};
            this.radioWatchBypass = true;
            this.showOAuthSecret = false;

            this.selectOptions = {
                serviceTypeList: [
                    {name: 'CAS Client', value: 'cas'},
                    {name: 'OAuth2 Client', value: 'oauth'},
                    {name: 'SAML2 Service Provider', value: 'saml'},
                    {name: 'OpenID Connect Client', value: 'oidc'}
                ],
                logoutTypeList: [
                    {name: 'NONE', value: 'none'},
                    {name: 'BACK_CHANNEL', value: 'back'},
                    {name: 'FRONT_CHANNEL', value: 'front'}
                ],
                timeUnitsList: [
                    {name: 'MILLISECONDS', value: 'MILLISECONDS'},
                    {name: 'SECONDS', value: 'SECONDS'},
                    {name: 'MINUTES', value: 'MINUTES'},
                    {name: 'HOURS', value: 'HOURS'},
                    {name: 'DAYS', value: 'DAYS'}
                ],
                mergeStrategyList: [
                    {name: 'DEFAULT', value: 'DEFAULT'},
                    {name: 'ADD', value: 'ADD'},
                    {name: 'MULTIVALUED', value: 'MULTIVALUED'},
                    {name: 'REPLACE', value: 'REPLACE'}
                ],
                selectType: [
                    {name: 'DEFAULT', value: 'DEFAULT'},
                    {name: 'TIME', value: 'TIME'},
                    {name: 'GROUPER', value: 'GROUPER'},
                    {name: 'REMOTE', value: 'REMOTE'}
                ],
                groupField: [
                    {name: 'NAME', value: 'NAME'},
                    {name: 'DISPLAY_NAME', value: 'DISPLAY_NAME'},
                    {name: 'EXTENSION', value: 'EXTENSION'},
                    {name: 'DISPLAY_EXTENSION', value: 'DISPLAY_EXTENSION'}
                ],
                failureMode: [
                    {name: 'NONE', value: 'NONE'},
                    {name: 'OPEN', value: 'OPEN'},
                    {name: 'CLOSED', value: 'CLOSED'},
                    {name: 'PHANTOM', value: 'PHANTOM'}
                ],
                samlRoleList: [
                    {name: 'SPSSODescriptor', value: 'SPSSODescriptor'},
                    {name: 'IDPSSODescriptor', value: 'IDPSSODescriptor'}
                ],
                samlDirectionList: [
                    {name: 'INCLUDE', value: 'INCLUDE'},
                    {name: 'EXCLUDE', value: 'EXCLUDE'}
                ]
            };

            this.isSelected = function (option, selected) {
                if (!angular.isArray(selected)) {
                    return option == selected;
                }

                angular.forEach(selected, function (opt) {
                    if (option == opt) return true;
                });
                return false;
            };

            this.isEmpty = function (thing) {
                if (angular.isArray(thing)) {
                    return thing.length === 0;
                }
                if (angular.isObject(thing)) {
                    return jQuery.isEmptyObject(thing);
                }
                return !thing;
            };


            this.saveForm = function () {
                var formErrors;
                // console.log(serviceForm.serviceData);
                // return;

                serviceDataTransformation('save');
                formErrors = serviceForm.validateForm();

                if (formErrors.length !== 0) {
                    delayedAlert('notvalid', 'danger', formErrors);
                    angular.forEach(formErrors, function (fieldId) {
                        $('#' + fieldId).addClass('required-missing');
                    });
                    return;
                } else $('.required-missing').removeClass('required-missing');

                $.ajax({
                    type: 'post',
                    url: appContext + '/saveService.html',
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    data: JSON.stringify(serviceForm.serviceData),
                    headers: httpHeaders,
                    success: function (data, status) {
                        if (data.status != 200) {
                            delayedAlert('notsaved', 'danger', data);
                            serviceForm.newService();
                        } else if (angular.isString(data)) {
                            sharedFactory.forceReload();
                        } else {

                            var hasIdAssignedAlready = (serviceForm.serviceData.assignedId != undefined
                            && serviceForm.serviceData.assignedId > 0);

                            if (!hasIdAssignedAlready && data.id > 0) {
                                serviceForm.serviceData.assignedId = data.id;
                                sharedFactory.assignedId = data.id;

                                delayedAlert('added', 'info', null);
                            }
                            else {
                                delayedAlert('updated', 'info', null);
                            }
                            $timeout(function () {
                                sharedFactory.clearItem();
                                $('#manageServices').click();
                            }, 200);


                        }
                    },
                    error: function (xhr, status) {
                        if (xhr.status == 403)
                            sharedFactory.forceReload();
                        else
                            delayedAlert('notsaved', 'danger', xhr.responseJSON);
                    }
                });
            };

            this.validateRegex = function (pattern) {
                try {
                    if (pattern == "")
                        return true;
                    var patt = new RegExp(pattern);
                    return true;
                } catch (e) {
                    return false;
                }
            };

            this.validateForm = function () {
                var err = [],
                    data = serviceForm.serviceData;

                // Service Basics
                if (!data.serviceId) err.push('serviceId');
                if (!data.name) err.push('serviceName');
                if (!data.description) err.push('serviceDesc');
                if (!data.type) err.push('serviceType');
                // OAuth Client Options Only
                if (data.type == 'oauth') {
                    if (!data.oauth.clientId) err.push('oauthClientId');
                    if (!data.oauth.clientSecret) err.push('oauthClientSecret');
                }
                // Username Attribute Provider Options
                if (!data.userAttrProvider.value) {
                    if (data.userAttrProvider.type == 'attr') err.push('uapUsernameAttribute');
                    if (data.userAttrProvider.type == 'anon') err.push('uapSaltSetting');
                }
                // Proxy Policy Options
                if (data.proxyPolicy.type == 'REGEX' && !data.proxyPolicy.value) err.push('proxyPolicyRegex');

                if (data.proxyPolicy.type == 'REGEX' && data.proxyPolicy.value != null) {
                    if (!this.validateRegex(data.proxyPolicy.value)) err.push('proxyPolicyRegex');
                }


                // Principle Attribute Repository Options
                if (data.attrRelease.attrOption == 'CACHED') {
                    if (!data.attrRelease.cachedTimeUnit) err.push('cachedTime');
                    if (!data.attrRelease.mergingStrategy) err.push('mergingStrategy');
                }
                if (data.attrRelease.attrFilter != null) {
                    if (!this.validateRegex(data.attrRelease.attrFilter)) err.push('attFilter');
                }
                return err;
            };

            this.newService = function () {
                serviceForm.radioWatchBypass = true;

                serviceForm.showOAuthSecret = false;
                serviceForm.serviceData = new Object({
                    assignedId: 0,
                    evalOrder: sharedFactory.maxEvalOrder + 1,
                    type: serviceForm.selectOptions.serviceTypeList[0].value,
                    logoutType: serviceForm.selectOptions.logoutTypeList[0].value,
                    attrRelease: {
                        attrOption: 'DEFAULT',
                        attrPolicy: {type: 'all'},
                        cachedTimeUnit: serviceForm.selectOptions.timeUnitsList[0].value,
                        mergingStrategy: serviceForm.selectOptions.mergeStrategyList[0].value
                    },
                    supportAccess: {
                        casEnabled: true,
                        ssoEnabled: true,
                        caseInsensitive: true,
                        type: serviceForm.selectOptions.selectType[0].value
                    },
                    publicKey: {algorithm: 'RSA'},
                    userAttrProvider: {type: 'default'},
                    proxyPolicy: {type: 'REFUSE'},
                    multiAuth: {
                        failureMode: serviceForm.selectOptions.failureMode[0].value
                    }
                });
                serviceDataTransformation('load');
                showInstructions();

                $http.get(appContext + '/getService?id=-1')
                    .then(function (response) {
                        if (response.status != 200)
                            delayedAlert('notloaded', 'danger', data);
                        else if (angular.isString(response.data))
                            sharedFactory.forceReload();
                        else
                            serviceForm.formData = response.data.formData;

                    });

                serviceForm.radioWatchBypass = false;
            };

            this.loadService = function (serviceId, duplicate) {
                serviceForm.radioWatchBypass = true;

                $http.get(appContext + '/getService?id=' + serviceId)
                    .then(function (response) {
                        if (response.status != 200) {
                            delayedAlert('notloaded', 'danger', data);
                            serviceForm.newService();
                        }
                        else if (angular.isString(response.data))
                            sharedFactory.forceReload();
                        else {
                            serviceForm.showOAuthSecret = false;
                            if (serviceForm.formData != response.data.formData)
                                serviceForm.formData = response.data.formData;
                            serviceForm.serviceData = response.data.serviceData;
                            if (duplicate) {
                                serviceForm.serviceData.assignedId = 0;
                            }
                            serviceDataTransformation('load');
                            showInstructions();
                        }
                    });

                serviceForm.radioWatchBypass = false;
            };

            // Parse the data for textareas to/from a(n) string/array from/to a(n) array/string
            var textareaArrParse = function (dir, value) {
                var newValue;
                if (dir == 'load') {
                    newValue = value ? value.join("\n") : '';
                }
                else {
                    if (value != undefined) {
                        newValue = value.split("\n");
                        for (var i = newValue.length - 1; i >= 0; i--) {
                            newValue[i] = newValue[i].trim();
                            if (!newValue[i]) newValue.splice(i, 1);
                        }
                    } else {
                        newValue = [];
                    }
                }
                return newValue;
            };

            // Transform the data so it is ready from/to the form to/from the server.
            var serviceDataTransformation = function (dir) {
                // console.log('serviceDataTransformation');
                var data = serviceForm.serviceData;

                // Logic safeties
                serviceForm.formData.availableAttributes = serviceForm.formData.availableAttributes || [];
                data.supportAccess.requiredAttr = data.supportAccess.requiredAttr || {};
                data.supportAccess.requiredAttrStr = data.supportAccess.requiredAttrStr || {};

                if (dir == 'load') {
                    // console.log('load');

                    angular.forEach(serviceForm.formData.availableAttributes, function (item) {
                        data.supportAccess.requiredAttrStr[item] = textareaArrParse(dir, data.supportAccess.requiredAttr[item]);
                    });

                    data.reqHandlersStr = textareaArrParse(dir, data.requiredHandlers);
                    data.userAttrProvider.valueAnon = (data.userAttrProvider.type == 'anon') ? data.userAttrProvider.value : '';
                    data.userAttrProvider.valueAttr = (data.userAttrProvider.type == 'attr') ? data.userAttrProvider.value : '';
                } else {
                    // console.log('else');
                    angular.forEach(serviceForm.formData.availableAttributes, function (item) {
                        data.supportAccess.requiredAttr[item] = textareaArrParse(dir, data.supportAccess.requiredAttrStr[item]);
                    });

                    data.requiredHandlers = textareaArrParse(dir, data.reqHandlersStr);
                    if (data.userAttrProvider.type == 'anon')
                        data.userAttrProvider.value = data.userAttrProvider.valueAnon;
                    else if (data.userAttrProvider.type == 'attr')
                        data.userAttrProvider.value = data.userAttrProvider.valueAttr;
                }

                switch (data.attrRelease.attrPolicy.type) {
                    case 'mapped':
                        if (dir == 'load')
                            data.attrRelease.attrPolicy.mapped = data.attrRelease.attrPolicy.attributes;
                        else
                            data.attrRelease.attrPolicy.attributes = data.attrRelease.attrPolicy.mapped || {};
                        break;
                    case 'allowed':
                        if (dir == 'load')
                            data.attrRelease.attrPolicy.allowed = data.attrRelease.attrPolicy.attributes;
                        else
                            data.attrRelease.attrPolicy.attributes = data.attrRelease.attrPolicy.allowed || [];
                        break;
                    default:
                        data.attrRelease.attrPolicy.value = null;
                        break;
                }

                serviceForm.serviceData = data;

                // serviceForm.serviceData.properties = [{"name": "foo", "value": 10}];

                // console.log(serviceForm.serviceData.properties);
            };

            $scope.$watch(
                function () {
                    return {
                        assignedId: sharedFactory.assignedId,
                        sourceId: sharedFactory.sourceId
                    };
                },
                function (registeredService) {
                    if (serviceForm.alert && serviceForm.alert.type != 'info')
                        serviceForm.alert = null;
                    if (registeredService.assignedId) {
                        serviceForm.loadService(registeredService.assignedId);
                    }
                    else if (registeredService.sourceId) {
                        serviceForm.loadService(registeredService.sourceId, true);
                    }
                    else {
                        serviceForm.newService();
                    }
                },
                true
            );
        }
    ]);

})();



/**********
 The following directives are necessary in order to track dirty state and validity of the rows
 in the table as the user pages within the grid
 ------------------------
 */

(function() {
    angular.module("casmgmt").directive("demoTrackedTable", demoTrackedTable);

    demoTrackedTable.$inject = [];

    function demoTrackedTable() {
        return {
            restrict: "A",
            priority: -1,
            require: "ngForm",
            controller: demoTrackedTableController
        };
    }

    demoTrackedTableController.$inject = ["$scope", "$parse", "$attrs", "$element"];

    function demoTrackedTableController($scope, $parse, $attrs, $element) {
        var self = this;
        var tableForm = $element.controller("form");
        var dirtyCellsByRow = [];
        var invalidCellsByRow = [];

        init();

        ////////

        function init() {
            var setter = $parse($attrs.demoTrackedTable).assign;
            setter($scope, self);
            $scope.$on("$destroy", function() {
                setter(null);
            });

            self.reset = reset;
            self.isCellDirty = isCellDirty;
            self.setCellDirty = setCellDirty;
            self.setCellInvalid = setCellInvalid;
            self.untrack = untrack;
        }

        function getCellsForRow(row, cellsByRow) {
            return _.find(cellsByRow, function(entry) {
                return entry.row === row;
            })
        }

        function isCellDirty(row, cell) {
            var rowCells = getCellsForRow(row, dirtyCellsByRow);
            return rowCells && rowCells.cells.indexOf(cell) !== -1;
        }

        function reset() {
            dirtyCellsByRow = [];
            invalidCellsByRow = [];
            setInvalid(false);
        }

        function setCellDirty(row, cell, isDirty) {
            setCellStatus(row, cell, isDirty, dirtyCellsByRow);
        }

        function setCellInvalid(row, cell, isInvalid) {
            setCellStatus(row, cell, isInvalid, invalidCellsByRow);
            setInvalid(invalidCellsByRow.length > 0);
        }

        function setCellStatus(row, cell, value, cellsByRow) {
            var rowCells = getCellsForRow(row, cellsByRow);
            if (!rowCells && !value) {
                return;
            }

            if (value) {
                if (!rowCells) {
                    rowCells = {
                        row: row,
                        cells: []
                    };
                    cellsByRow.push(rowCells);
                }
                if (rowCells.cells.indexOf(cell) === -1) {
                    rowCells.cells.push(cell);
                }
            } else {
                _.remove(rowCells.cells, function(item) {
                    return cell === item;
                });
                if (rowCells.cells.length === 0) {
                    _.remove(cellsByRow, function(item) {
                        return rowCells === item;
                    });
                }
            }
        }

        function setInvalid(isInvalid) {
            self.$invalid = isInvalid;
            self.$valid = !isInvalid;
        }

        function untrack(row) {
            _.remove(invalidCellsByRow, function(item) {
                return item.row === row;
            });
            _.remove(dirtyCellsByRow, function(item) {
                return item.row === row;
            });
            setInvalid(invalidCellsByRow.length > 0);
        }
    }
})();

(function() {
    angular.module("casmgmt").directive("demoTrackedTableRow", demoTrackedTableRow);

    demoTrackedTableRow.$inject = [];

    function demoTrackedTableRow() {
        return {
            restrict: "A",
            priority: -1,
            require: ["^demoTrackedTable", "ngForm"],
            controller: demoTrackedTableRowController
        };
    }

    demoTrackedTableRowController.$inject = ["$attrs", "$element", "$parse", "$scope"];

    function demoTrackedTableRowController($attrs, $element, $parse, $scope) {
        var self = this;
        var row = $parse($attrs.demoTrackedTableRow)($scope);
        var rowFormCtrl = $element.controller("form");
        var trackedTableCtrl = $element.controller("demoTrackedTable");

        self.isCellDirty = isCellDirty;
        self.setCellDirty = setCellDirty;
        self.setCellInvalid = setCellInvalid;

        function isCellDirty(cell) {
            return trackedTableCtrl.isCellDirty(row, cell);
        }

        function setCellDirty(cell, isDirty) {
            trackedTableCtrl.setCellDirty(row, cell, isDirty)
        }

        function setCellInvalid(cell, isInvalid) {
            trackedTableCtrl.setCellInvalid(row, cell, isInvalid)
        }
    }
})();

(function() {
    angular.module("casmgmt").directive("demoTrackedTableCell", demoTrackedTableCell);

    demoTrackedTableCell.$inject = [];

    function demoTrackedTableCell() {
        return {
            restrict: "A",
            priority: -1,
            scope: true,
            require: ["^demoTrackedTableRow", "ngForm"],
            controller: demoTrackedTableCellController
        };
    }

    demoTrackedTableCellController.$inject = ["$attrs", "$element", "$scope"];

    function demoTrackedTableCellController($attrs, $element, $scope) {
        var self = this;
        var cellFormCtrl = $element.controller("form");
        var cellName = cellFormCtrl.$name;
        var trackedTableRowCtrl = $element.controller("demoTrackedTableRow");

        if (trackedTableRowCtrl.isCellDirty(cellName)) {
            cellFormCtrl.$setDirty();
        } else {
            cellFormCtrl.$setPristine();
        }
        // note: we don't have to force setting validaty as angular will run validations
        // when we page back to a row that contains invalid data

        $scope.$watch(function() {
            return cellFormCtrl.$dirty;
        }, function(newValue, oldValue) {
            if (newValue === oldValue) return;

            trackedTableRowCtrl.setCellDirty(cellName, newValue);
        });

        $scope.$watch(function() {
            return cellFormCtrl.$invalid;
        }, function(newValue, oldValue) {
            if (newValue === oldValue) return;

            trackedTableRowCtrl.setCellInvalid(cellName, newValue);
        });
    }
})();

/**
 * End tracking directives
 */

/*

 (function() {
 "use strict";

 angular.module("casmgmt").factory("ngTableSimpleList", dataFactory);

 dataFactory.$inject = [];

 function dataFactory() {
 return [{"id":1,"name":"Nissim","value":41,},{"id":2,"name":"Mariko","value":10}];
 }
 })();
 */
