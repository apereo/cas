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


    <h1><i class="fa fa-plus-circle"></i> <spring:message code="addServiceView" /></h1>
    <!-- Add/Manage Service Form -->
    <form class="form-horizontal" id="serviceForm" ng-controller="ServiceFormController as serviceFormCtrl" ng-submit="serviceFormCtrl.addService(service)" novalidate>
        <div class="row">
            <div class="col-sm-6">

                <!-- Service Form Instructions -->
                <div class="alert alert-info" role="alert">
                    <p><i class="fa fa-lg fa-question-circle"></i> <spring:message code="management.services.manage.instructions" /></p>
                    <p><i class="fa fa-lg fa-exclamation-triangle"></i> <spring:message code="management.services.manage.requiredText" /></p>
                </div> <!-- end .alert div -->

                <!-- Service Name -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceName" >
                        <spring:message code="management.services.manage.label.name" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                            <input type="text" class="form-control" id="serviceName" ng-model="serviceFormCtrl.service.name">
                        </div>
                    </div>
                </div>

                <!-- Service Description -->
                <div class="form-group">
                    <label class="col-sm-3" for="serviceDesc">
                        <spring:message code="management.services.manage.label.description" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                        </label>
                    <div class="col-sm-9">
                        <div class="input-group">
                            <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                            <textarea class="form-control" rows="5" id="serviceDesc" ng-model="serviceFormCtrl.service.description"></textarea>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-6">
                        <!-- Service Type -->
                        <div class="form-group">
                            <label class="col-sm-6" for="serviceType">
                                <spring:message code="management.services.manage.label.type" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-6">
                                <select class="form-control" id="serviceType" ng-model="serviceFormCtrl.service.type">
                                    <option ng-option="cas" value="cas" selected>CAS Client</option>
                                    <option ng-option="oauth" value="oauth">OAuth Client</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <!-- Service Theme -->
                        <div class="form-group">
                            <label class="col-sm-4" for="serviceTheme">
                                <spring:message code="management.services.manage.label.theme" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control" id="serviceTheme" ng-model="serviceFormCtrl.service.theme">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- OAuth Options Only -->
                <div class="oauth-only-options-container" ng-show="serviceFormCtrl.service.type=='oauth'">
                    <div class="panel panel-warning">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="management.services.manage.header.oauthOptions" />
                            </h3>
                        </div>
                            <div class="panel-body">
                                <!-- OAuth Client Secret -->
                                <div class="form-group">
                                    <label class="col-sm-4" for="oauthClientSecret">
                                        <spring:message code="management.services.manage.label.oauthClientSecret" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                    </label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="oauthClientSecret" ng-model="serviceFormCtrl.service.oauthClientSecret">
                                    </div>
                                </div>

                                <!-- OAuth Client ID -->
                                <div class="form-group">
                                    <label class="col-sm-4" for="oauthClientId">
                                        <spring:message code="management.services.manage.label.oauthClientId" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                    </label>
                                    <div class="col-sm-8">
                                        <input type="text" class="form-control" id="oauthClientId" ng-model="serviceFormCtrl.service.oauthClientId">
                                    </div>
                                </div>
                                <div class="col-md-8 col-md-offset-4">

                                    <!-- OAuth Bypass Approval Prompt -->
                                    <div class="checkbox">
                                        <label for="oauthBypass">
                                            <input type="checkbox" id="oauthBypass" ng-model="serviceFormCtrl.service.oauthBypass"> <spring:message code="management.services.manage.label.oauthBypass" />&nbsp;<i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                </div> <!-- end .oauth-only-options-container div -->

                <div class="row">

                    <!-- Service ID -->
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-6" for="serviceId">
                                <spring:message code="management.services.manage.label.serviceId" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-6">
                                <input type="text" class="form-control" id="serviceId" ng-model="serviceFormCtrl.service.serviceId">
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">

                        <!-- Evaulation Order -->
                        <div class="form-group">
                            <label class="col-sm-6" for="evalOrder">
                                <spring:message code="management.services.manage.label.evalOrder" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-6">
                                <input type="text" class="form-control" id="evalOrder" ng-model="serviceFormCtrl.service.order">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Service Required Handlers -->
                <div class="form-group">
                    <label class="col-sm-4" for="serviceReqHandlers">
                        <spring:message code="management.services.manage.label.requiredHandlers" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                    </label>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <input type="text" class="form-control">
                            <span class="input-group-btn">
                                <button class="btn btn-default" type="button"><i class="fa fa-plus-circle"></i></button>
                            </span>
                        </div>
                        <select multiple class="form-control form-padding" id="serviceReqHandlers" ng-model="serviceFormCtrl.service.handlers">
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </div>
                </div>

                <!-- Logout URL -->
                <div class="form-group">
                    <label class="col-sm-3" for="logoutUrl">
                        <spring:message code="management.services.manage.label.logoutUrl" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="logoutUrl" ng-model="serviceFormCtrl.service.logoutUrl">
                    </div>
                </div>

                <!-- Logo URL -->
                <div class="form-group">
                    <label class="col-sm-3" for="logoUrl">
                        <spring:message code="management.services.manage.label.logoUrl" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                    </label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="logoUrl" ng-model="serviceFormCtrl.service.logoUrl">
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-6">
                        <!-- Logout Type -->
                        <div class="form-group">
                            <label class="col-sm-4" for="logoutType">
                                <spring:message code="management.services.manage.label.logoutType" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-8">
                                <select class="form-control" id="logoutType" ng-model="service.logoutType" ng-options="type.value as type.name for type in logoutTypes"></select>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <!-- Assigned ID -->
                        <div class="form-group">
                            <label class="col-sm-3" for="assignedId">
                                <spring:message code="management.services.manage.label.assignedId" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                            </label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control" id="assignedId" ng-model="serviceFormCtrl.serviceForm.assignedId" readonly>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-sm-6">
                <!-- Username Attribute Provider Options -->
                <div class="username-attribute-provider-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="management.services.manage.header.usernameAttributeProvider" />
                            </h3>
                        </div> <!-- end .panel-header div -->
                        <div class="panel-body">
                            <div class="radio-group">
                                <!-- Username Attribute Provider Radio Button - Default -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapDefault" value="default" ng-model="serviceFormCtrl.uapRadio.type" ng-checked="true">
                                    <spring:message code="management.services.manage.label.uap.default" />
                                </label>
                                <!-- Username Attribute Provider Radio Button - Anonymous -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapAnon" value="anonymous" ng-model="serviceFormCtrl.uapRadio.type" >
                                    <spring:message code="management.services.manage.label.uap.anon" />
                                </label>
                                <!-- Username Attribute Provider Radio Button - Principle Attribute -->
                                <label class="radio-inline">
                                    <input type="radio" name="uapRadio" id="uapAtt" value="attribute" ng-model="serviceFormCtrl.uapRadio.type" >
                                    <spring:message code="management.services.manage.label.uap.principleAtt" />
                                </label>
                            </div> <!-- end .radio-group div -->

                            <!-- Username Attribute Provider Default Options -->
                            <div class="well well-sm" ng-show="serviceFormCtrl.uapRadio.type === 'default'">
                                <spring:message code="management.services.service.noAction" />
                            </div>
                            <!-- Username Attribute Provider Anonymous Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.uapRadio.type === 'anonymous'">
                                <label class="col-sm-4" for="uapSaltSetting">
                                    <spring:message code="management.services.manage.label.uap.saltSetting" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="uapSaltSetting" ng-model="serviceFormCtrl.service.uapSaltSetting">
                                    </div>
                                </div>
                            </div>
                            <!-- Username Attribute Provider Principle Attribute Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.uapRadio.type === 'attribute'">
                                <label class="col-sm-4" for="uapUsernameAttribute">
                                    <spring:message code="management.services.manage.label.uap.usernameAttribute" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="uapUsernameAttribute" ng-model="serviceFormCtrl.service.uapUsernameAttribute">
                                </div>
                            </div>
                        </div> <!-- end .panel-body div -->
                    </div> <!-- end .panel div -->
                </div> <!-- end .username-attribute-provider-container div -->

                <!-- Public Key Options -->
                <div class="public-key-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="management.services.manage.header.publicKey" />
                            </h3>
                        </div> <!-- end .panel-header div -->
                        <div class="panel-body">

                            <!-- Public Key Location -->
                            <div class="form-group">
                                <label class="col-sm-4" for="publicKeyLocation">
                                    <spring:message code="management.services.manage.label.publicKey.location" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="publicKeyLocation" ng-model="serviceFormCtrl.service.publicKeyLocation">
                                    </div>
                                </div>
                            </div>
                            <!-- Public Key Algorithm -->
                            <div class="form-group">
                                <label class="col-sm-4" for="publicKeyAlgorithm">
                                    <spring:message code="management.services.manage.label.publicKey.algorithm" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-8">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="publicKeyAlgorithm" ng-model="serviceFormCtrl.service.publicKeyAlgorithm">
                                        <div class="input-group-addon">(.*)</div>
                                    </div>
                                </div>
                            </div>
                        </div> <!-- end .panel-body div -->
                    </div> <!-- end .panel div -->
                </div> <!-- end .public-key-container div -->

                <!-- Proxy Policy Options -->
                <div class="proxy-policy-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title">
                                <spring:message code="management.services.manage.header.proxyPolicy" />
                            </h3>
                        </div> <!-- end .panel-header div -->
                        <div class="panel-body">
                            <div class="radio-group">

                                <!-- Proxy Policy Radio Button - Refuse -->
                                <label class="radio-inline">
                                    <input type="radio" name="proxyPolicyRadio" id="proxyRefuse" value="refuse" ng-model="serviceFormCtrl.proxyPolicyRadio.type" ng-checked="true">
                                    <spring:message code="management.services.manage.label.proxyPolicy.refuse" />
                                </label>

                                <!-- Proxy Policy Radio Button - Regex -->
                                <label class="radio-inline">
                                    <input type="radio" name="proxyPolicyRadio" id="proxyRegex" value="regex" ng-model="serviceFormCtrl.proxyPolicyRadio.type" >
                                    <spring:message code="management.services.manage.label.proxyPolicy.regex" />
                                </label>
                            </div> <!-- end .radio-group div -->

                            <!-- Proxy Policy Refuse Options -->
                            <div class="well well-sm" ng-show="serviceFormCtrl.proxyPolicyRadio.type === 'refuse'">
                                <spring:message code="management.services.service.noAction" />
                            </div>

                            <!-- Proxy Policy Regex Options -->
                            <div class="form-group" ng-show="serviceFormCtrl.proxyPolicyRadio.type === 'regex'">
                                <label class="col-sm-3" for="uapSaltSetting">
                                    <spring:message code="management.services.manage.label.proxyPolicy.regex" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-9">
                                    <div class="input-group">
                                        <div class="input-group-addon input-group-required"><i class="fa fa-lg fa-exclamation-triangle"></i></div>
                                        <input type="text" class="form-control" id="uapSaltSetting" ng-model="serviceFormCtrl.service.proxyPolicyRegex">
                                        <div class="input-group-addon">(.*)</div>
                                    </div>
                                </div>
                            </div> <!-- end .form-group div -->
                        </div> <!-- end .panel-body div -->
                    </div> <!-- end .panel div -->
                </div> <!-- end .proxy-policy-container div -->

                <div class="attribute-release-policy-container">
                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title"><spring:message code="management.services.manage.header.attributeReleasePolicy" /></h3>
                        </div>
                        <div class="panel-body">
                            <div class="checkbox">
                                <label>
                                    <input type="checkbox" value="relPassword">
                                    <spring:message code="management.services.manage.label.attRelease.releaseCredPassword" />
                                </label>
                            </div>
                            <div class="checkbox">
                                <label>
                                    <input type="checkbox" value="releasePgt">
                                    <spring:message code="management.services.manage.label.attRelease.releaseProxyTicket" />
                                </label>
                            </div>

                            <h4><spring:message code="management.services.manage.label.attRelease.attFilter" /></h4>

                            <!-- Attribute Filter Options -->
                            <div class="radio-group">

                                <!-- Attribute Filter Radio Button - Regex Filter -->
                                <label class="radio-inline">
                                    <input type="radio" name="attFilterRegex" id="attFilterRegex" value="regex" ng-model="serviceFormCtrl.attFilterRadio.type" ng-checked="true">
                                    <spring:message code="management.services.manage.label.attRelease.regexFilter" />
                                </label>

                                <!-- Attribute Filter Radio Button - Principle Attribute Repository -->
                                <label class="radio-inline">
                                    <input type="radio" name="attFilterPrincipleRepo" id="attFilterPrincipleRepo" value="prinRepo" ng-model="serviceFormCtrl.attFilterRadio.type" >
                                    <spring:message code="management.services.manage.label.attRelease.principleAttRepo" />
                                </label>

                                <!-- Attribute Filter Radio Button - Policies -->
                                <label class="radio-inline">
                                    <input type="radio" name="attFilterPolicies" id="attFilterPolicies" value="policies" ng-model="serviceFormCtrl.attFilterRadio.type" >
                                    <spring:message code="management.services.manage.label.attRelease.policies" />
                                </label>
                            </div> <!-- end .radio-group div -->

                            <!-- Attribute Filter Regex Option -->
                            <div class="form-group" ng-show="serviceFormCtrl.attFilterRadio.type === 'regex'">
                                <label class="col-sm-3" for="attFilterRegex">
                                    <spring:message code="management.services.manage.label.attRelease.regexFilter" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                </label>
                                <div class="col-sm-9">
                                    <div class="input-group">
                                        <input type="text" class="form-control" id="attFilterRegex" ng-model="serviceFormCtrl.service.attFilterRadio">
                                        <div class="input-group-addon">(.*)</div>
                                    </div>
                                </div>
                            </div>

                            <!-- Attribute Filter Principle Repository Options -->
                            <div class="panel panel-default" ng-show="serviceFormCtrl.attFilterRadio.type === 'prinRepo'">
                                <div class="panel-body">
                                    <div class="radio-group">

                                        <!-- Principle Repository Radio Button - Default -->
                                        <label class="radio-inline">
                                            <input type="radio" name="prinFilterDefault" id="prinFilterDefault" value="default" ng-model="serviceFormCtrl.prinFilterRadio.type" ng-checked="true">
                                            <spring:message code="management.services.manage.label.attRelease.principleAttRepo.default" />
                                        </label>

                                        <!-- Principle Filter Radio Button - Cached -->
                                        <label class="radio-inline">
                                            <input type="radio" name="prinFilterCached" id="prinFilterCached" value="cached" ng-model="serviceFormCtrl.prinFilterRadio.type" >
                                            <spring:message code="management.services.manage.label.attRelease.principleAttRepo.cached" />
                                        </label>
                                    </div> <!-- end .radio-group div -->

                                    <!-- Principle Attribute Repo Default Options -->
                                    <div class="well well-sm" ng-show="serviceFormCtrl.prinFilterRadio.type === 'default'">
                                        <spring:message code="management.services.service.noAction" />
                                    </div>

                                    <!-- Principle Attribute Repo Cached Time -->
                                    <div class="form-group" ng-show="serviceFormCtrl.prinFilterRadio.type === 'cached'">
                                        <label class="col-sm-4" for="cachedTime">
                                            <spring:message code="management.services.manage.label.attRelease.principleAttRepo.cached.timeUnit" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                        </label>
                                        <div class="col-sm-8">
                                            <div class="input-group">
                                                <input type="text" class="form-control" id="uapSaltSetting" ng-model="serviceFormCtrl.service.cachedTime">
                                                <div class="input-group-addon">MM:SS:HH:DD</div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Principle Attribute Repo Cached Expiration -->
                                    <div class="form-group">
                                        <label class="col-sm-4" for="cachedExp">
                                            <spring:message code="management.services.manage.label.attRelease.principleAttRepo.cached.expiration" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                        </label>
                                        <div class="col-sm-8">
                                            <input type="text" class="form-control" id="cachedExp" ng-model="serviceFormCtrl.service.cachedExp">
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Attribute Filter Policies Option -->
                            <div class="panel panel-default" ng-show="serviceFormCtrl.attFilterRadio.type === 'policies'">
                                <div class="panel-body">
                                    <div class="radio-group">

                                        <!-- Filter Policies Radio Button - Return All -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnAll" id="policiesRadio" value="returnAll" ng-model="serviceFormCtrl.policiesRadio.type" ng-checked="true">
                                            <spring:message code="management.services.manage.label.attRelease.policies.returnAll" />
                                        </label>

                                        <!-- Filter Policies Radio Button - Return Allowed -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnAllowed" id="policiesReturnAllowed" value="returnAllowed" ng-model="serviceFormCtrl.policiesRadio.type" >
                                            <spring:message code="management.services.manage.label.attRelease.policies.returnAllowed" />
                                        </label>

                                        <!-- Filter Policies Radio Button - Return Allowed -->
                                        <label class="radio-inline">
                                            <input type="radio" name="policiesReturnMapped" id="policiesReturnMapped" value="returnMapped" ng-model="serviceFormCtrl.policiesRadio.type" >
                                            <spring:message code="management.services.manage.label.attRelease.policies.returnMapped" />
                                        </label>
                                    </div> <!-- end .radio-group div -->

                                    <!-- Attribute Release Policies Return All Option -->
                                    <div class="well well-sm" ng-show="serviceFormCtrl.policiesRadio.type === 'returnAll'">
                                        <spring:message code="management.services.service.noAction" />
                                    </div>

                                    <!-- Attribute Release Policies Return Allowed Option -->
                                    <div class="form-group" ng-show="serviceFormCtrl.policiesRadio.type === 'returnAllowed'">
                                        <label class="col-sm-3" for="returnAllowed">
                                            <spring:message code="management.services.manage.label.attRelease.policies.returnAllowed" /> <i class="fa fa-lg fa-info-circle form-tooltip-icon" data-toggle="tooltip" data-placement="top" title="<spring:message code="management.services.tooltip.name" />"></i>
                                        </label>
                                        <div class="col-sm-9">
                                            <select multiple class="form-control" id="returnedAllowedMultiselect" ng-model="serviceFormCtrl.service.returnedAllowed">
                                                <option>Option 1</option>
                                                <option>Option 2</option>
                                                <option>Option 3</option>
                                                <option>Option 4</option>
                                                <option>Option 5</option>
                                            </select>
                                        </div>
                                    </div>

                                    <!-- Attribute Release Policies Return Mapped Option -->
                                    <div class="panel panel-default" ng-show="serviceFormCtrl.policiesRadio.type === 'returnMapped'">
                                        <table id="returnMapTable" class="table table-striped table-hover table-responsive table-condensed">
                                            <thead>
                                                <tr>
                                                    <th class="col-md-4">Attribute</th>
                                                    <th class="col-md-8">Remapped</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td>Attribute 1</td>
                                                    <td><input class="form-control input-sm"></td>
                                                </tr>
                                                <tr>
                                                    <td>Attribute 2</td>
                                                    <td><input class="form-control input-sm"></td>
                                                </tr>
                                                <tr>
                                                    <td>Attribute 3</td>
                                                    <td><input class="form-control input-sm"></td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div> <!-- end .attribute-release-policy-container div -->
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <div class="form-group services-button-group">
                        <button type="submit" class="btn btn-primary">
                            <i class="fa fa-floppy-o"></i> <spring:message code="management.services.manage.button.save" />
                        </button>&nbsp;&nbsp;&nbsp;
                        <a href ng-click="action.isSelected('manage')" class="btn btn-default"><i class="fa fa-times"></i> <spring:message code="management.services.manage.button.cancel" /></a>
                    </div> <!-- end services-button-group div -->
                </div>
            </div>
        </div> <!-- end .row div -->
    </form> <!-- end #serviceForm form -->