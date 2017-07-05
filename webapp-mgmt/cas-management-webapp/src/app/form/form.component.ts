import { Component, OnInit, ViewChild } from '@angular/core';
import {
  Data, Form, FormData, AttributeRelease, AttributeReleasePolicy, SupportAccess,
  UsernameAttributeProvider, PublicKey, Contact, MultiAuth, PrincipalAttribute
} from "../../domain/form";
import {Messages} from "../messages";
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {Location} from "@angular/common";
import {FormService} from "./form.service";
import ServiceProxyPolicy from "../../domain/service-proxy-policy";
import {TabService} from "./tab.service";
import {AlertComponent} from "../alert/alert.component";

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})
export class FormComponent implements OnInit {

  serviceData: Data = new Data();
  formData: FormData = new FormData();
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
              private service: FormService,
              public tabService: TabService,
              private location: Location) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: Form}) => {
        if (!data.resp || !data.resp.serviceData) {
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
    this.radioWatchBypass = true;

    this.showOAuthSecret = false;
    this.serviceData.assignedId = "-1",
    this.serviceData.evalOrder = -1,
    this.serviceData.type = this.tabService.selectOptions.serviceTypeList[0].value;
    this.serviceData.logoutType = this.tabService.selectOptions.logoutTypeList[1].value;
    this.serviceData.attrRelease = new AttributeRelease();
    this.serviceData.attrRelease.attrOption = 'DEFAULT';
    this.serviceData.attrRelease.attrPolicy = new AttributeReleasePolicy();
    this.serviceData.attrRelease.attrPolicy.type = 'all';
    this.serviceData.attrRelease.cachedTimeUnit = this.tabService.selectOptions.timeUnitsList[0].value;
    this.serviceData.attrRelease.mergingStrategy = this.tabService.selectOptions.mergeStrategyList[0].value;

    this.serviceData.supportAccess = new SupportAccess();
    this.serviceData.supportAccess.casEnabled = "true";
    this.serviceData.supportAccess.ssoEnabled = "true";
    this.serviceData.supportAccess.caseInsensitive = true;
    this.serviceData.supportAccess.type = this.tabService.selectOptions.selectType[0].value;
    this.serviceData.publicKey = new PublicKey();
    this.serviceData.userAttrProvider = new UsernameAttributeProvider();
    this.serviceData.proxyPolicy = new ServiceProxyPolicy();
    this.serviceData.proxyPolicy.type = 'REFUSE';
    this.serviceData.multiAuth = new MultiAuth();
    this.serviceData.multiAuth.failureMode = this.tabService.selectOptions.failureMode[1].value;

    this.serviceDataTransformation('load');
    //this.showInstructions();

    this.service.getService('-1').then(resp => {
      this.formData = resp.formData;
      this.serviceDataTransformation('load');
      this.radioWatchBypass = false;
    });

  };

  loadService(form: Form, duplicate) {
    this.radioWatchBypass = true;
    this.showOAuthSecret = false;
    if (this.formData != form.formData) {
      this.formData = form.formData;
    }
    this.serviceData = form.serviceData;
    if (duplicate) {
      this.serviceData.assignedId = "-1";
    }
    this.serviceDataTransformation('load');
    //this.showInstructions();

    this.radioWatchBypass = false;
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

  serviceDataTransformation(dir) {
    let data = this.serviceData;

    // Logic safeties
    this.formData.availableAttributes = this.formData.availableAttributes || [];
    data.supportAccess.requiredAttr = data.supportAccess.requiredAttr || new Map();
    data.supportAccess.requiredAttrStr = data.supportAccess.requiredAttrStr || new Map();
    data.supportAccess.rejectedAttr = data.supportAccess.rejectedAttr || [];
    data.attrRelease.attrPolicy.mapped = new Map();
    //data.multiAuth.principalAttr = new PrincipalAttribute();
    if (dir == 'load') {
      // console.log('load');

      this.formData.availableAttributes.forEach((item: any) => {
        data.supportAccess.requiredAttrStr[item] = this.textareaArrParse(dir, data.supportAccess.requiredAttr[item]);
      });

      data.reqHandlersStr = this.textareaArrParse(dir, data.requiredHandlers);
      data.userAttrProvider.valueAnon = (data.userAttrProvider.type == 'anon') ? data.userAttrProvider.value : '';
      data.userAttrProvider.valueAttr = (data.userAttrProvider.type == 'attr') ? data.userAttrProvider.value : '';
    } else {
      // console.log('else');
      this.formData.availableAttributes.forEach((item: any) => {
        data.supportAccess.requiredAttr[item] = this.textareaArrParse(dir, data.supportAccess.requiredAttrStr[item]);
      });

      data.requiredHandlers = this.textareaArrParse(dir, data.reqHandlersStr);
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

    this.serviceData = data;
    this.tabService.serviceData = data;
    this.tabService.formData = this.formData;
  };

  saveForm() {
    let formErrors;
    // console.log(serviceForm.serviceData);
    // return;
    this.clearErrors();
    this.serviceDataTransformation('save');
    formErrors = this.validateForm();
    if (formErrors.length > 0) {
      this.alert.show(this.messages.services_form_alert_formHasErrors, 'danger');
      formErrors.forEach((fieldId) => {
        document.getElementById(fieldId).classList.add('required-missing');
      });
      this.alert.show(this.messages.services_form_alert_formHasErrors,'danger');
      return;
    }

    this.service.saveService(this.serviceData)
      .then(resp => this.handleSave(resp))
      .catch(e => this.handleNotSaved(e));

  };

  clearErrors() {
    let missing = document.getElementsByClassName('required-missing');
    let i = 0;
    let j = missing.length;
    console.log("j = "+j);
    for(i = 0; i < j; i++) {
      console.log("j = "+j);
      missing.item(0).classList.remove('required-missing');
    }
  }

  handleSave(id: String) {
    let hasIdAssignedAlready = (this.serviceData.assignedId != undefined &&
    Number.parseInt(this.serviceData.assignedId.toString()) > 0);

    if (!hasIdAssignedAlready && id != null && id != "0") {
      this.serviceData.assignedId = id;
      this.alert.show(this.messages.services_form_alert_serviceAdded,'info');
    }
    else {
      this.alert.show(this.messages.services_form_alert_serviceUpdated,'info');
    }
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
    data = this.serviceData;

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

    if (!data.type) {
      err.push('serviceType');
    }

    // OAuth Client Options Only
    if (data.type == 'oauth') {
      if (!data.oauth.clientId) {
        err.push('oauthClientId');
      }
      if (!data.oauth.clientSecret){
        err.push('oauthClientSecret');
      }
    }
    // Username Attribute Provider Options
    if (!data.userAttrProvider.value) {
      if (data.userAttrProvider.type == 'attr'){
        err.push('uapUsernameAttribute');
      }
      if (data.userAttrProvider.type == 'anon'){
        err.push('uapSaltSetting');
      }
    }
    // Proxy Policy Options
    if (data.proxyPolicy.type == 'REGEX' && !data.proxyPolicy.value) {
      err.push('proxyPolicyRegex');
    }

    if (data.proxyPolicy.type == 'REGEX' && data.proxyPolicy.value != null) {
      if (!this.validateRegex(data.proxyPolicy.value)) {
        err.push('proxyPolicyRegex');
      }
    }


    // Principle Attribute Repository Options
    if (data.attrRelease.attrOption == 'CACHED') {
      if (!data.attrRelease.cachedTimeUnit){
        err.push('cachedTime');
      }
      if (!data.attrRelease.mergingStrategy){
        err.push('mergingStrategy');
      }
    }
    if (data.attrRelease.attrFilter != null) {
      if (!this.validateRegex(data.attrRelease.attrFilter)) {
        err.push('attFilter');
      }
    }
    return err;
  };
}
