<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

    <!-- Add/Manage Service Form -->
    <form class="service-editor form-horizontal" autocomplete="off" id="serviceForm" ng-controller="ServiceFormController as serviceFormCtrl" ng-submit="serviceFormCtrl.validateAndSave()" novalidate>
        <div class="services-button-group-top">
            <button href="javascript://" ng-click="serviceFormCtrl.saveForm()" class="btn btn-primary">
                <i class="fa fa-floppy-o"></i>
                <spring:message code="services.form.button.save" />
            </button>
            <a href="javascript://" ng-click="action.homepage()" class="btn btn-default">
                <i class="fa fa-times"></i>
                <spring:message code="services.form.button.cancel" />
            </a>
        </div>

        <h1>
            <i class="fa fa-plus-circle"></i>
            <span ng-if="action.isSelected('edit')"><spring:message code="services.form.header.page.editService" /></span>
            <span ng-if="!action.isSelected('edit')"><spring:message code="services.form.header.page.addService" /></span>
        </h1>

        <div class="alert alert-{{ serviceFormCtrl.alert.type }}" role="alert" ng-show="serviceFormCtrl.alert">
            <p>
                <i class="fa fa-lg fa-{{ serviceFormCtrl.alert.type === 'danger' ? 'exclamation-triangle' : 'info-circle' }}"></i>
                <span ng-if="serviceFormCtrl.alert.name === 'notloaded'">
                    <spring:message code="services.form.alert.loadingFailed" />
                </span>
                <span ng-if="serviceFormCtrl.alert.name === 'notvalid'">
                    <spring:message code="services.form.alert.formHasErrors" />
                </span>
                <span ng-if="serviceFormCtrl.alert.name === 'updated'">
                    <spring:message code="services.form.alert.serviceUpdated" />
                </span>
                <span ng-if="serviceFormCtrl.alert.name === 'added'">
                    <spring:message code="services.form.alert.serviceAdded" />
                </span>
                <span ng-if="serviceFormCtrl.alert.name === 'notsaved'">
                    <spring:message code="services.form.alert.unableToSave" />
                </span>
                <span ng-if="serviceFormCtrl.alert.name === 'instructions'">
                    <spring:message code="services.form.instructions" />
                </span>
            </p>
        </div>

        <div class="row col-sm-12">
            <div class="col-sm-6">
                <div class="row">
                    <div class="col-sm-12">
                        <!-- Service Enabled? -->
                        <div class="form-group">
                            <label class="col-sm-3 important" for="casEnabled">
                                <spring:message code="services.form.label.sas.casEnabled" />
                                <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top"
                                    title="<spring:message code="services.form.tooltip.sas.casEnabled" />"></i>
                            </label>
                            <div class="col-sm-1">
                                <div class="checkbox checkbox-inline">
                                    <input type="checkbox" id="casEnabled" ng-model="serviceFormCtrl.serviceData.supportAccess.casEnabled" />
                                </div>
                            </div>
                            <div class="col-sm-8">
                                <div class="alert alert-danger" role="alert" ng-if="!serviceFormCtrl.serviceData.supportAccess.casEnabled">
                                    <p>
                                        <i class="fa fa-lg fa-info-circle"></i>
                                        <spring:message code="services.form.warning.casDisabled" />
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Service ID -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceId">
                        <spring:message code="services.form.label.serviceId" />
                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.serviceId" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                            <input type="text" class="form-control" id="serviceId" ng-model="serviceFormCtrl.serviceData.serviceId">
                        </div>
                    </div>
                </div>

                <!-- Service Name -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceName">
                        <spring:message code="services.form.label.name" />
                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.name" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                            <input type="text" class="form-control" id="serviceName" ng-model="serviceFormCtrl.serviceData.name">
                        </div>
                    </div>
                </div>

                <!-- Service Description -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceDesc">
                        <spring:message code="services.form.label.description" />
                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.description" />"></i>
                        </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                            <textarea class="form-control" rows="5" id="serviceDesc" ng-model="serviceFormCtrl.serviceData.description"></textarea>
                        </div>
                    </div>
                </div>

                <!-- Service Type -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceType">
                        <spring:message code="services.form.label.type" />
                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.type" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                            <select class="form-control" id="serviceType" ng-model="serviceFormCtrl.serviceData.type">
                                <option ng-repeat="opt in serviceFormCtrl.selectOptions.serviceTypeList" ng-attr-value="{{ opt.value }}"
                                    ng-selected="serviceFormCtrl.isSelected(opt.value, serviceFormCtrl.serviceData.type)">{{ opt.name }}</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- OAuth Options Only -->
                <div class="oauth-only-options-container" ng-show="serviceFormCtrl.serviceData.type=='oauth'">
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="services.form.header.oauthOptions" />
                            </h3>
                        </div>
                            <div class="panel-body">
                                <!-- OAuth Client ID -->
                                <div class="form-group">
                                    <label class="col-sm-4" for="oauthClientId">
                                        <spring:message code="services.form.label.oauthClientId" />
                                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.oauthClientId" />"></i>
                                    </label>
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                            <input type="text" autocomplete="off" class="form-control" id="oauthClientId" ng-model="serviceFormCtrl.serviceData.oauth.clientId" />
                                        </div>
                                    </div>
                                </div>

                                <!-- OAuth Client Secret -->
                                <div class="form-group">
                                    <label class="col-sm-4" for="oauthClientSecret">
                                        <spring:message code="services.form.label.oauthClientSecret" />
                                        <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.oauthClientSecret" />"></i>
                                    </label>
                                    <div class="col-sm-8">
                                        <div class="input-group">
                                            <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                            <input class="form-control" autocomplete="off" id="oauthClientSecret" type="text"
                                                ng-model="serviceFormCtrl.serviceData.oauth.clientSecret" ng-if="serviceFormCtrl.showOAuthSecret" />
                                            <input class="form-control" autocomplete="off" id="oauthClientSecret" type="password"
                                                ng-model="serviceFormCtrl.serviceData.oauth.clientSecret" ng-if="!serviceFormCtrl.showOAuthSecret" />
                                        </div>
                                    </div>
                                </div>

                                <div class="col-md-8 col-md-offset-4">
                                    <!-- OAuth Show/Hide Client Secret -->
                                    <div class="checkbox">
                                        <label for="oauthShowSecret">
                                            <input type="checkbox" id="oauthShowSecret" ng-model="serviceFormCtrl.showOAuthSecret" />
                                            <spring:message code="services.form.label.oauthShowSecret" />
                                            <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.oauthShowSecret" />"></i>
                                        </label>
                                    </div>
                                    <!-- OAuth Bypass Approval Prompt -->
                                    <div class="checkbox">
                                        <label for="oauthBypass">
                                            <input type="checkbox" id="oauthBypass" ng-model="serviceFormCtrl.serviceData.oauth.bypass" />
                                            <spring:message code="services.form.label.oauthBypass" />
                                            <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.oauthBypass" />"></i>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div><%-- end .oauth-only-options-container div --%>

                <div class="row">
                    <div class="col-sm-6">
                        <!-- Service Theme -->
                        <div class="form-group">
                            <label class="col-sm-6" for="serviceTheme">
                                <spring:message code="services.form.label.theme" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.theme" />"></i>
                            </label>
                            <div class="col-sm-6">
                                <input type="text" class="form-control" id="serviceTheme" ng-model="serviceFormCtrl.serviceData.theme" />
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <!-- Assigned ID -->
                        <div class="form-group">
                            <label class="col-sm-5" for="assignedId">
                                <spring:message code="services.form.label.assignedId" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.assignedId" />"></i>
                            </label>
                            <div class="col-sm-7">
                                <input type="text" class="form-control" id="assignedId" ng-model="serviceFormCtrl.serviceData.assignedId" readonly />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Logo URL -->
                <div class="form-group">
                    <label class="col-sm-3" for="logoUrl">
                        <spring:message code="services.form.label.logoUrl" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.logoUrl" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="logoUrl" ng-model="serviceFormCtrl.serviceData.logoUrl">
                    </div>
                </div>

                <!-- Logout URL -->
                <div class="form-group">
                    <label class="col-sm-3" for="logoutUrl">
                        <spring:message code="services.form.label.logoutUrl" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.logoutUrl" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="logoutUrl" ng-model="serviceFormCtrl.serviceData.logoutUrl">
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-6">
                        <!-- Logout Type -->
                        <div class="form-group">
                            <label class="col-sm-6" for="logoutType">
                                <spring:message code="services.form.label.logoutType" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.logoutType" />"></i>
                            </label>
                            <div class="col-sm-6">
                                <select class="form-control" id="logoutType" ng-model="serviceFormCtrl.serviceData.logoutType">
                                    <option ng-repeat="opt in serviceFormCtrl.selectOptions.logoutTypeList" ng-attr-value="{{ opt.value }}"
                                        ng-selected="serviceFormCtrl.isSelected(opt.value, serviceFormCtrl.serviceData.logoutType)">{{ opt.name }}</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <!-- Assigned ID -->
                        <div class="form-group">
                            <label class="col-sm-5" for="evalOrder">
                                <spring:message code="services.form.label.evalOrder" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.evalOrder" />"></i>
                            </label>
                            <div class="col-sm-7">
                                <input type="text" class="form-control" id="assignedId" ng-model="serviceFormCtrl.serviceData.evalOrder" readonly />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Service Required Handlers -->
                <div class="form-group">
                    <label class="col-sm-3" for="reqHandler">
                    <spring:message code="services.form.label.requiredHandlers" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.requiredHandlers" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <textarea class="form-control noresize" id="reqHandler" rows="4" ng-model="serviceFormCtrl.serviceData.reqHandlersStr"></textarea>
                    </div>
                </div>

                <!-- Service Access Strategy -->
                <div class="panel panel-success">
                    <div class="panel-heading">
                        <div class="panel-title"><spring:message code="services.form.header.sas" /></div>
                    </div>
                    <div class="panel-body panel-sas-options">
                        <div class="col-sm-12 form-group">
                            <div class="col-sm-6 checkbox">
                                <label class="col-sm-12" for="ssoEnabled">
                                    <input type="checkbox" id="ssoEnabled" ng-model="serviceFormCtrl.serviceData.supportAccess.ssoEnabled" />
                                    <spring:message code="services.form.label.sas.ssoEnabled" />
                                    <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top"
                                        title="<spring:message code="services.form.tooltip.sas.ssoEnabled" />"></i>
                                </label>
                            </div>
                            <div class="col-sm-6 checkbox">
                                <label class="col-sm-12" for="sasRequireAll">
                                    <input type="checkbox" id="sasRequireAll" ng-model="serviceFormCtrl.serviceData.supportAccess.requireAll" />
                                    <spring:message code="services.form.label.sas.requireAll" />
                                    <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top"
                                        title="<spring:message code="services.form.tooltip.sas.requireAll" />"></i>
                                </label>
                            </div>
                        </div>

                        <div class="col-sm-12">
                            <label for="sasRequiredAttr">
                                <spring:message code="services.form.label.sas.requiredAttr" />
                                <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top"
                                    title="<spring:message code="services.form.tooltip.sas.requiredAttr" />"></i>
                            </label>
                            <div class="panel panel-default">
                                <table id="sasMapTable" class="table table-striped table-hover table-responsive table-condensed">
                                    <thead>
                                        <tr>
<%-- TODO: Do these headers need changing? --%>
                                            <th class="col-md-6"><spring:message code="services.form.label.attrRelease.policies.sourceAttribute" /></th>
                                            <th class="col-md-6"><spring:message code="services.form.label.attrRelease.policies.casAttribute" /></th>
                                        </tr>
                                    </thead>
                                    <tbody>
<%-- TODO: Fix this to the proper variable. --%>
                                        <tr ng-repeat="mappedValue in serviceFormCtrl.formData.availableAttributes">
                                            <td class="va-middle">{{ mappedValue }}</td>
                                            <td><textarea class="form-control noresize" rows="2" ng-model="serviceFormCtrl.serviceData.supportAccess.requiredAttrStr[ mappedValue ]"></textarea></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div><%-- end .panel.panel-default --%>
                        </div><%-- end .col-sm-8.form-group --%>


                        <div class='col-sm-12'>

                            <spring:message code="services.form.label.sas.starttime" />
                            <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top"
                               title="<spring:message code="services.form.tooltip.sas.starttime" />"></i>

                            <div class='input-group date' id='accessStartingDate'>
                                <input type='text' class="form-control"
                                       id='accessStartingDateInput'
                                       ng-model="serviceFormCtrl.serviceData.supportAccess.startingTime" />
                                    <span class="input-group-addon">
                                        <span class="glyphicon glyphicon-time"></span>
                                    </span>
                            </div>
                            <br/>
                            <spring:message code="services.form.label.sas.endtime" />
                            <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float"
                               data-toggle="tooltip" data-placement="top"
                               title="<spring:message code="services.form.tooltip.sas.endtime" />"></i>
                            <div class='input-group date' id='accessEndingDate'>
                                <input type='text' class="form-control"
                                       id='accessEndingDateInput'
                                       ng-model="serviceFormCtrl.serviceData.supportAccess.endingTime" />
                                    <span class="input-group-addon">
                                        <span class="glyphicon glyphicon-time"></span>
                                    </span>
                            </div>

                        </div>
                        <script type="text/javascript">
                            $(function () {
                                $('#accessStartingDate').datetimepicker({
                                    format: "YYYY-MM-DDTHH:mm:ss.SSSZZ",
                                    useCurrent: true
                                });
                                $('#accessEndingDate').datetimepicker({
                                    format: "YYYY-MM-DDTHH:mm:ss.SSSZZ",
                                    useCurrent: true
                                });

                                $( "#accessStartingDateInput" ).blur(function() {
                                    $( "#accessStartingDateInput" ).trigger('input')
                                });
                                $( "#accessEndingDateInput" ).blur(function() {
                                    $( "#accessEndingDateInput" ).trigger('input');
                                });

                            });
                        </script>

                    </div>
                </div><%-- end Service Access Strategy --%>
            </div>

            <div class="col-sm-6">
                <!-- Username Attribute Provider Options -->
                <div class="username-attribute-provider-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="services.form.header.usernameAttProvider" />
                            </h3>
                        </div><%-- end .panel-header div --%>
                        <div class="panel-body">
                            <div class="radio-group">
                                <!-- Username Attribute Provider Radio Button - Default -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapDefault" value="default"
                                        ng-model="serviceFormCtrl.serviceData.userAttrProvider.type"
                                        ng-checked="serviceFormCtrl.serviceData.userAttrProvider.type == 'default'" />
                                    <spring:message code="services.form.label.uap.default" />
                                </label>
                                <!-- Username Attribute Provider Radio Button - Anonymous -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapAnon" value="anon"
                                        ng-model="serviceFormCtrl.serviceData.userAttrProvider.type"
                                        ng-checked="serviceFormCtrl.serviceData.userAttrProvider.type == 'anon'" />
                                    <spring:message code="services.form.label.uap.anon" />
                                </label>
                                <!-- Username Attribute Provider Radio Button - Principle Attribute -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapAtt" value="attr"
                                        ng-model="serviceFormCtrl.serviceData.userAttrProvider.type"
                                        ng-checked="serviceFormCtrl.serviceData.userAttrProvider.type == 'attr'" />
                                    <spring:message code="services.form.label.uap.principleAtt" />
                                </label>
                            </div><%-- end .radio-group div --%>

                            <!-- Username Attribute Provider Default Options -->
                            <div class="well well-sm" ng-show="serviceFormCtrl.serviceData.userAttrProvider.type == 'default'">
                                <spring:message code="management.services.service.noAction" />
                            </div>
                            <!-- Username Attribute Provider Anonymous Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.serviceData.userAttrProvider.type == 'anon'">
                                <label class="col-sm-4" for="uapSaltSetting">
                                    <spring:message code="services.form.label.uap.saltSetting" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.uap.saltSetting" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="uapSaltSetting" ng-model="serviceFormCtrl.serviceData.userAttrProvider.valueAnon">
                                    </div>
                                </div>
                            </div>
                            <!-- Username Attribute Provider Principle Attribute Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.serviceData.userAttrProvider.type == 'attr'">
                                <label class="col-sm-4" for="uapUsernameAttribute">
                                    <spring:message code="services.form.label.uap.usernameAttribute" />
                                    <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.uap.usernameAttribute" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                        <select class="form-control" id="uapUsernameAttribute" ng-model="serviceFormCtrl.serviceData.userAttrProvider.valueAttr">
                                            <option ng-repeat="opt in serviceFormCtrl.formData.availableAttributes" ng-attr-value="{{ opt }}"
                                                ng-selected="serviceFormCtrl.isSelected(opt, serviceFormCtrl.serviceData.userAttrProvider.value)">{{ opt }}</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div><%-- end .panel-body div --%>
                    </div><%-- end .panel div --%>
                </div><%-- end .username-attribute-provider-container div --%>

                <!-- Attribute Release Policy Options -->
                <div class="attribute-release-policy-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title"><spring:message code="services.form.header.attrRelPolicy" /></h3>
                        </div>
                        <div class="panel-body">
                            <div class="checkbox-group">
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" value="1"
                                            ng-model="serviceFormCtrl.serviceData.attrRelease.releasePassword"
                                            ng-checked="serviceFormCtrl.serviceData.attrRelease.releasePassword" />
                                        <spring:message code="services.form.label.attrRelease.releaseCredPassword" />
                                        <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.releaseCredPassword" />"></i>
                                    </label>
                                </div>
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" value="1"
                                            ng-model="serviceFormCtrl.serviceData.attrRelease.releaseTicket"
                                            ng-checked="serviceFormCtrl.serviceData.attrRelease.releaseTicket" />
                                        <spring:message code="services.form.label.attrRelease.releaseProxyTicket" />
                                        <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.releaseProxyTicket" />"></i>
                                    </label>
                                </div>
                            </div><%-- end .checkbox-group div --%>

                            <!-- Attribute Filter -->
                            <div class="form-group">
                                <label class="col-sm-4" for="attFilter">
                                    <spring:message code="services.form.label.attrRelease.attrFilter" />
                                    <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.attrFilter" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <input type="text" class="form-control" id="attFilter" ng-model="serviceFormCtrl.serviceData.attrRelease.attrFilter">
                                    </div>
                                </div>
                            </div>

                            <!-- Principle Attribute Repository Options -->
                            <div class="panel panel-default">
                                <div class="panel-heading"><spring:message code="services.form.header.principleAttRepo" /></div>
                                <div class="panel-body">
                                    <div class="radio-group">
                                        <!-- Principle Repository Radio Button - Default -->
                                        <label class="radio-inline">
                                            <input type="radio" name="prinFilterDefault" id="prinFilterDefault" value="default"
                                                ng-model="serviceFormCtrl.serviceData.attrRelease.attrOption"
                                                ng-checked="serviceFormCtrl.serviceData.attrRelease.attrOption == 'default'" />
                                            <spring:message code="services.form.label.attrRelease.principleAttRepo.default" />
                                        </label>
                                        <!-- Principle Filter Radio Button - Cached -->
                                        <label class="radio-inline">
                                            <input type="radio" name="prinFilterCached" id="prinFilterCached" value="cached"
                                                ng-model="serviceFormCtrl.serviceData.attrRelease.attrOption"
                                                ng-checked="serviceFormCtrl.serviceData.attrRelease.attrOption == 'cached'" />
                                            <spring:message code="services.form.label.attrRelease.principleAttRepo.cached" />
                                        </label>
                                    </div><%-- end .radio-group div --%>

                                    <!-- Principle Attribute Repo Default Options -->
                                    <div class="well well-sm" ng-show="serviceFormCtrl.serviceData.attrRelease.attrOption == 'default'">
                                        <spring:message code="management.services.service.noAction" />
                                    </div>

                                    <!-- Principle Attribute Repo Cached Options -->
                                    <div class="principle-attribute-cached-container" ng-show="serviceFormCtrl.serviceData.attrRelease.attrOption == 'cached'">
                                        <!-- Principle Attribute Repo Cached Time -->
                                        <div class="form-group">
                                            <label class="col-sm-4" for="cachedTime">
                                                <spring:message code="services.form.label.attrRelease.principleAttRepo.cached.timeUnit" />
                                                <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.principleAttRepo.cached.timeUnit" />"></i>
                                            </label>
                                            <div class="col-sm-8">
                                                <div class="input-group">
                                                    <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                                    <select class="form-control" id="cachedTime" ng-model="serviceFormCtrl.serviceData.attrRelease.cachedTimeUnit">
                                                        <option ng-repeat="opt in serviceFormCtrl.selectOptions.timeUnitsList" ng-attr-value="{{ opt.value }}"
                                                            ng-selected="serviceFormCtrl.isSelected(opt.value, serviceFormCtrl.serviceData.attrRelease.cachedTimeUnit)">{{ opt.name }}</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Principle Attribute Repo Cached Expiration -->
                                        <div class="form-group">
                                            <label class="col-sm-4" for="cachedExp">
                                                <spring:message code="services.form.label.attrRelease.principleAttRepo.cached.expiration" />
                                                <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.principleAttRepo.cached.expiration" />"></i>
                                            </label>
                                            <div class="col-sm-8">
<%-- TODO: Shouldn't this be at least greater than 0? --%>
                                                <input type="number" min="0" class="form-control" id="cachedExp" ng-model="serviceFormCtrl.serviceData.attrRelease.cachedExpiration" />
                                            </div>
                                        </div>

                                        <!-- Principle Attribute Repo Merging Strategy -->
                                        <div class="form-group">
                                            <label class="col-sm-4" for="mergingStrategy">
                                                <spring:message code="services.form.label.attrRelease.principleAttRepo.cached.mergeStrategy" />
                                                <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.principleAttRepo.cached.mergeStrategy" />"></i>
                                            </label>
                                            <div class="col-sm-8">
                                                <div class="input-group">
                                                    <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                                    <select class="form-control" id="mergingStrategy" ng-model="serviceFormCtrl.serviceData.attrRelease.mergingStrategy">
                                                        <option ng-repeat="opt in serviceFormCtrl.selectOptions.mergeStrategyList" ng-attr-value="{{ opt.value }}"
                                                            ng-selected="serviceFormCtrl.isSelected(opt.value, serviceFormCtrl.serviceData.attrRelease.mergeStrategy)">{{ opt.name }}</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Attribute Filter Policies Options -->
                            <div class="panel panel-default">
                                <div class="panel-heading"><spring:message code="services.form.header.attrPolicy" /></div>
                                <div class="panel-body">
                                    <div class="radio-group">

                                        <!-- Filter Policies Radio Button - Return All -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnAll" id="policiesRadio" value="all"
                                                ng-model="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type"
                                                ng-checked="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'all'" />
                                            <spring:message code="services.form.label.attrRelease.policies.returnAll" />
                                        </label>

                                        <!-- Filter Policies Radio Button - Return Allowed -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnAllowed" id="policiesReturnAllowed" value="allowed"
                                                ng-model="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type"
                                                ng-checked="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'allowed'"
                                                ng-disabled="serviceFormCtrl.isEmpty( serviceFormCtrl.formData.availableAttributes )" />
                                            <spring:message code="services.form.label.attrRelease.policies.returnAllowed" />
                                        </label>

                                        <!-- Filter Policies Radio Button - Return Allowed -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnMapped" id="policiesReturnMapped" value="mapped"
                                                ng-model="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type"
                                                ng-checked="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'mapped'"
                                                ng-disabled="serviceFormCtrl.isEmpty( serviceFormCtrl.formData.availableAttributes )" />
                                            <spring:message code="services.form.label.attrRelease.policies.returnMapped" />
                                        </label>
                                    </div><%-- end .radio-group div --%>

                                    <!-- Attribute Release Policies Return All Option -->
                                    <div class="well well-sm" ng-show="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'all' || serviceFormCtrl.isEmpty( serviceFormCtrl.formData.availableAttributes )">
                                        <spring:message code="management.services.service.noAction" />
                                    </div>

                                    <!-- Attribute Release Policies Return Allowed Option -->
                                    <div class="form-group" ng-show="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'allowed' && !serviceFormCtrl.isEmpty( serviceFormCtrl.formData.availableAttributes )">
                                        <label class="col-sm-3" for="returnAllowed">
                                            <spring:message code="services.form.label.attrRelease.policies.returnAllowed" />
                                            <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.policies.returnAllowed" />"></i>
                                        </label>
                                        <div class="col-sm-9">
                                            <select multiple class="form-control" id="returnedAllowed" ng-model="serviceFormCtrl.serviceData.attrRelease.attrPolicy.allowed">
                                                <option ng-repeat="opt in serviceFormCtrl.formData.availableAttributes" ng-attr-value="{{ opt }}"
                                                    ng-selected="serviceFormCtrl.isSelected(opt, serviceFormCtrl.serviceData.attrRelease.attrPolicy.allowed)">{{ opt }}</option>
                                            </select>
                                        </div>
                                    </div>

                                    <!-- Attribute Release Policies Return Mapped Option -->
                                    <div class="panel panel-default" ng-show="serviceFormCtrl.serviceData.attrRelease.attrPolicy.type == 'mapped' && !serviceFormCtrl.isEmpty( serviceFormCtrl.formData.availableAttributes )">
                                        <table id="returnMapTable" class="table table-striped table-hover table-responsive table-condensed" >
                                            <thead>
                                                <tr>
                                                    <th class="col-md-4"><spring:message code="services.form.label.attrRelease.policies.sourceAttribute" /></th>
                                                    <th class="col-md-8">
                                                        <spring:message code="services.form.label.attrRelease.policies.casAttribute" />
                                                        <i class="fa fa-lg fa-question-circle form-tooltip-icon no-float" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.attrRelease.policies.returnMapped" />"></i>
                                                    </th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr ng-repeat="mappedValue in serviceFormCtrl.formData.availableAttributes">
                                                    <td class="va-middle">{{ mappedValue }}</td>
                                                    <td><input ng-model="serviceFormCtrl.serviceData.attrRelease.attrPolicy.mapped[ mappedValue ]" type="text" class="form-control input-sm" /></td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div><%-- end .attribute-release-policy-container div --%>

                <!-- Public Key Options -->
                <div class="public-key-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="services.form.header.pubKey" />
                            </h3>
                        </div> <!-- end .panel-header div -->
                        <div class="panel-body">

                            <!-- Public Key Location -->
                            <div class="form-group">
                                <label class="col-sm-4" for="publicKeyLocation">
                                    <spring:message code="services.form.label.pubKey.location" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.pubKey.location" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="publicKeyLocation" ng-model="serviceFormCtrl.serviceData.publicKey.location">
                                </div>
                            </div>
                            <!-- Public Key Algorithm -->
                            <div class="form-group">
                                <label class="col-sm-4" for="publicKeyAlgorithm">
                                    <spring:message code="services.form.label.pubKey.algorithm" /> <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.pubKey.algorithm" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <select class="form-control" id="publicKeyAlgorithm" ng-model="serviceFormCtrl.serviceData.publicKey.algorithm" disabled>
                                        <option value="RSA" ng-selected="true">RSA</option>
                                    </select>
                                </div>
                            </div>
                        </div><%-- end .panel-body div --%>
                    </div><%-- end .panel div --%>
                </div><%-- end .pub-key-container div --%>

                <!-- Proxy Policy Options -->
                <div class="proxy-policy-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="services.form.header.proxyPolicy" />
                            </h3>
                        </div><%-- end .panel-header div --%>
                        <div class="panel-body">
                            <div class="radio-group">

                                <!-- Proxy Policy Radio Button - Refuse -->
                                <label class="radio-inline">
                                    <input type="radio" name="proxyPolicyRadio" id="proxyRefuse" value="refuse" ng-model="serviceFormCtrl.serviceData.proxyPolicy.type" ng-checked="true">
                                    <spring:message code="services.form.label.proxyPolicy.refuse" />
                                </label>

                                <!-- Proxy Policy Radio Button - Regex -->
                                <label class="radio-inline">
                                    <input type="radio" name="proxyPolicyRadio" id="proxyRegex" value="regex" ng-model="serviceFormCtrl.serviceData.proxyPolicy.type" >
                                    <spring:message code="services.form.label.proxyPolicy.regex" />
                                </label>
                            </div><%-- end .radio-group div --%>

                            <!-- Proxy Policy Refuse Options -->
                            <div class="well well-sm" ng-show="serviceFormCtrl.serviceData.proxyPolicy.type === 'refuse'">
                                <spring:message code="management.services.service.noAction" />
                            </div>

                            <!-- Proxy Policy Regex Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.serviceData.proxyPolicy.type === 'regex'">
                                <label class="col-sm-3" for="proxyPolicyRegex">
                                    <spring:message code="services.form.label.proxyPolicy.regex" />
                                    <i class="fa fa-lg fa-question-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="services.form.tooltip.proxyPolicy.regex" />"></i>
                                </label>
                                <div class="col-sm-9">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required" title="<spring:message code="services.form.required" />"><i class="fa fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="proxyPolicyRegex" ng-model="serviceFormCtrl.serviceData.proxyPolicy.value">
                                    </div>
                                </div>
                            </div><%-- end .form-group div --%>
                        </div><%-- end .panel-body div --%>
                    </div><%-- end .panel div --%>
                </div><%-- end .proxy-policy-container div --%>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <div class="form-group services-button-group">
                        <button href="javascript://" ng-click="serviceFormCtrl.saveForm()" class="btn btn-primary">
                            <i class="fa fa-floppy-o"></i>
                            <spring:message code="services.form.button.save" />
                        </button>
                        <a href="javascript://" ng-click="action.homepage()" class="btn btn-default">
                            <i class="fa fa-times"></i>
                            <spring:message code="services.form.button.cancel" />
                        </a>
                    </div><%-- end services-button-group div --%>
                </div>
            </div>
        </div><%-- end .row div --%>
    </form><%-- end #serviceForm form --%>
