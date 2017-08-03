import { Component, OnInit, ViewChild } from '@angular/core';
import {FormData} from "../../domain/service-view-bean";
import {Messages} from "../messages";
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {Location} from "@angular/common";
import {FormService} from "./form.service";
import {Data} from "./data";
import {AlertComponent} from "../alert/alert.component";
import {AbstractRegisteredService, RegexRegisteredService} from "../../domain/registered-service";
import {DenyAllAttributeReleasePolicy} from "../../domain/attribute-release";
import {DefaultPrincipalAttributesRepository} from "../../domain/attribute-repo";
import {DefaultRegisteredServiceAccessStrategy} from "../../domain/access-strategy";
import {RegisteredServicePublicKeyImpl} from "../../domain/public-key";
import {DefaultRegisteredServiceUsernameProvider} from "../../domain/attribute-provider";
import {RefuseRegisteredServiceProxyPolicy} from "../../domain/proxy-policy,ts";
import {DefaultRegisteredServiceMultifactorPolicy} from "../../domain/multifactor";

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {
  radioWatchBypass: boolean = true;
  showOAuthSecret: boolean = false;
  active: String;

  id: String;
  dup: boolean;
  path: String;

  allowedDomains: String[];
  isAdmin: boolean;

  @ViewChild('alert')
  alert: AlertComponent;


  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private fservice: FormService,
              public data: Data,
              private location: Location) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: AbstractRegisteredService}) => {
        if (!data.resp) {
          this.newService()
        } else {
          this.loadService(data.resp,false);
        }
      });

    this.route.url.subscribe((url: UrlSegment[]) => {
      this.path = url[0].path;
      this.route.params.subscribe((params) => {
        this.id = params['id'];
        this.dup = params['duplicate']
        this.goto('basics');
      });
    });

  }

  goto(tab: String) {
    let route: any[] = [this.path,this.id];
    if (this.dup) {
      route.push({duplicate:this.dup});
    }
    route.push({outlets: {form: [tab]}});
    this.router.navigate(route,{skipLocationChange: true} );
    this.active = tab;
  }

  newService() {
    this.data.service = new RegexRegisteredService();
    this.radioWatchBypass = true;

    this.showOAuthSecret = false;
    this.data.service.id = -1,
    this.data.service.evaluationOrder = -1,
    //this.service.type = this.data.selectOptions.serviceTypeList[0].value;
    this.data.service.logoutType = "BACK_CHANNEL";
    this.data.service.attributeReleasePolicy = new DenyAllAttributeReleasePolicy();
    this.data.service.attributeReleasePolicy.principalAttributesRepository = new DefaultPrincipalAttributesRepository();
    //this.service.attributeReleasePolicy.attrOption = 'DEFAULT';
    //this.service.attributeReleasePolicy.attrPolicy = {};
    //this.service.attributeReleasePolicy.attrPolicy.type = 'all';
    //this.service.attributeReleasePolicy.cachedTimeUnit = this.data.selectOptions.timeUnitsList[0].value;
    //this.service.attributeReleasePolicy.mergingStrategy = this.data.selectOptions.mergeStrategyList[0].value;

    this.data.service.accessStrategy = new DefaultRegisteredServiceAccessStrategy();
    this.data.service.accessStrategy.enabled = true;
    this.data.service.accessStrategy.ssoEnabled = true;
    this.data.service.accessStrategy.caseInsensitive = true;
    //this.service.accessStrategy.type = this.data.selectOptions.selectType[0].value;
    this.data.service.publicKey = new RegisteredServicePublicKeyImpl();//new PublicKey();
    this.data.service.usernameAttributeProvider =  new DefaultRegisteredServiceUsernameProvider();//UsernameAttributeProvider();
    this.data.service.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();//new ProxyPolicy();
    //this.service.proxyPolicy.type = 'REFUSE';
    this.data.service.multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
    this.data.service.multifactorPolicy.failureMode = this.data.selectOptions.failureMode[1].value;

    //this.showInstructions();


    this.fservice.formData().then(resp => {
      this.data.formData = resp;
      this.radioWatchBypass = false;
    });

  };

  loadService(form: AbstractRegisteredService, duplicate) {
    this.data.service = form;
    this.radioWatchBypass = true;
    this.showOAuthSecret = false;
    /*
    if (this.formData != form.formData) {
      this.formData = form.formData;
    }
    this.serviceData = form.serviceData;
    if (duplicate) {
      this.serviceData.assignedId = "-1";
    }
    */
    this.fservice.formData().then(resp => {
      this.data.formData = resp;
      this.radioWatchBypass = false;
    });
    //this.showInstructions();

    this.data.service = form;

  };

  textareaArrParse(dir, value) {
    var newValue;
    if(dir == 'load') {
      newValue = value ? value.join("\n") : '';
    }
    else {
      if (value != undefined) {
        newValue = value.split("\n");
        for (var i = newValue.length-1; i >= 0; i--) {
          newValue[i] = newValue[i].trim();
          if (!newValue[i]) newValue.splice(i, 1);
        }
      } else {
        newValue = [];
      }
    }
    return newValue;
  };

  saveForm() {
    let formErrors;
    // console.log(serviceForm.serviceData);
    // return;
    this.clearErrors();
    formErrors = this.validateForm();
    if (formErrors.length > 0) {
      this.alert.show(this.messages.services_form_alert_formHasErrors, 'danger');
      formErrors.forEach((fieldId) => {
        document.getElementById(fieldId).classList.add('required-missing');
      });
      this.alert.show(this.messages.services_form_alert_formHasErrors,'danger');
      return;
    }

    this.fservice.saveService(this.data.service)
      .then(resp => this.handleSave(resp))
      .catch(e => this.handleNotSaved(e));

  };

  clearErrors() {
    let missing = document.getElementsByClassName('required-missing');
    let i = 0;
    let j = missing.length;
    for(i = 0; i < j; i++) {
      missing.item(0).classList.remove('required-missing');
    }
  }

  handleSave(id: number) {
    //let hasIdAssignedAlready = (this.data.service.id != undefined &&
    //Number.parseInt(this.data.service.id.toString()) > 0);
    /*
    if (!hasIdAssignedAlready && id != null && id != 0) {
      this.data.service.id = id;
      this.alert.show(this.messages.services_form_alert_serviceAdded,'info');
    }
    else {
      this.alert.show(this.messages.services_form_alert_serviceUpdated,'info');
    }
    */
    this.data.service.id = id;
    this.location.back();
  }

  handleNotSaved(e: any) {
    this.alert.show(this.messages.services_form_alert_unableToSave,'danger');
  }

  validateRegex(pattern) {
    try {
      if (pattern == "")
        return false;
      let patt = new RegExp(pattern);
      return true;
    } catch (e) {
      console.log("Failed regex");
      return false;
    }
  }

  validateForm() {
    let err = [],
    data = this.data.service;

    // Service Basics
    if (!data.serviceId) {
      err.push('serviceId');
    }

    if (!this.validateRegex(data.serviceId)) {
      err.push('serviceId');
    }

    if (!data.name) {
      err.push('serviceName');
    }

    if (!data.description) {
      err.push('serviceDesc');
    }

    /*
    if (!data.type) {
      err.push('serviceType');
    }
    */

    // OAuth Client Options Only
    /*
    if (data.type == 'oauth') {
      if (!data.oauth.clientId) {
        err.push('oauthClientId');
      }
      if (!data.oauth.clientSecret){
        err.push('oauthClientSecret');
      }
    }
    */

    // Username Attribute Provider Options
    /*
    if (!data.usernameAttributeProvider) {
      if (data.userAttrProvider.type == 'attr'){
        err.push('uapUsernameAttribute');
      }
      if (data.userAttrProvider.type == 'anon'){
        err.push('uapSaltSetting');
      }
    }
    */
    // Proxy Policy Options
    /*
    if (data.proxyPolicy.type == 'REGEX' && !data.proxyPolicy.value) {
      err.push('proxyPolicyRegex');
    }

    if (data.proxyPolicy.type == 'REGEX' && data.proxyPolicy.value != null) {
      if (!this.validateRegex(data.proxyPolicy.value)) {
        err.push('proxyPolicyRegex');
      }
    }
    */


    // Principle Attribute Repository Options
    /*
    if (data.attributeReleasePolicy.attrOption == 'CACHED') {
      if (!data.attributeReleasePolicy.cachedTimeUnit){
        err.push('cachedTime');
      }
      if (!data.attributeReleasePolicy.mergingStrategy){
        err.push('mergingStrategy');
      }
    }
    if (data.attributeReleasePolicy.attrFilter != null) {
      if (!this.validateRegex(data.attributeReleasePolicy.attrFilter)) {
        err.push('attFilter');
      }
    }
    */
    return err;
  };
}
