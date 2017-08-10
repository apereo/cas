import { Component, OnInit, ViewChild } from '@angular/core';
import {Messages} from "../messages";
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";
import {Location} from "@angular/common";
import {FormService} from "./form.service";
import {Data} from "./data";
import {AlertComponent} from "../alert/alert.component";
import {AbstractRegisteredService, RegexRegisteredService} from "../../domain/registered-service";
import {CachingPrincipalAttributesRepository} from "../../domain/attribute-repo";
import {
  AnonymousRegisteredServiceUsernameProvider,
  PrincipalAttributeRegisteredServiceUsernameProvider
} from "../../domain/attribute-provider";
import {
  RegexMatchingRegisteredServiceProxyPolicy
} from "../../domain/proxy-policy,ts";
import {OAuthRegisteredService, OidcRegisteredService} from "../../domain/oauth-service";
import {SamlRegisteredService} from "../../domain/saml-service";
import {WSFederationRegisterdService} from "../../domain/wsed-service";

enum Tabs {
  BASICS,
  TYPE,
  LOGOUT,
  ACCESS_STRATEGY,
  MULTIFACTOR,
  PROXY,
  USERNAME_ATTRIBUTE,
  ATTRIBUTE_RELEASE,
  PROPERTIES,
  ADVANCED
}

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.css']
})

export class FormComponent implements OnInit {

  id: String;
  path: String;

  @ViewChild('alert')
  alert: AlertComponent;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: FormService,
              public data: Data,
              private location: Location) {
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: AbstractRegisteredService}) => {
        if (data.resp) {
          this.loadService(data.resp);
        }
      });

    this.route.url.subscribe((url: UrlSegment[]) => {
      this.path = url[0].path;
      this.route.params.subscribe((params) => {
        this.id = params['id'];
        this.goto(Tabs.BASICS);
      });
    });
  }

  goto(tab:Tabs) {
    let route: any[] = [this.path,this.id];
    route.push({outlets: {form: [this.tabRoute(tab)]}});
    this.router.navigate(route,{skipLocationChange: true} );
  }

  loadService(form: AbstractRegisteredService) {
    this.data.service = form;

    this.service.formData().then(resp => {
      this.data.formData = resp;
    });
  }

  isOidc(): boolean {
    return OidcRegisteredService.instanceOf(this.data.service)
  }

  isSaml(): boolean {
    return SamlRegisteredService.instanceOf(this.data.service)
  }

  isWsFed(): boolean {
    return WSFederationRegisterdService.instanceOf(this.data.service)
  }

  isOauth() {
    return OAuthRegisteredService.instanceOf(this.data.service)
  }

  isCas() {
    return RegexRegisteredService.instanceOf(this.data.service);
  }

  tabRoute(tab: Tabs): String {
    if(tab > 0 && this.isCas()) {
      tab++
    }
    switch(tab) {
      case Tabs.BASICS :
        return 'basics';
      case Tabs.TYPE :
        if(this.isSaml())
          return 'saml';
        if(this.isOauth() || this.isOidc())
          return 'oauth';
        if(this.isWsFed())
          return 'wsfed';
      case Tabs.LOGOUT :
        return 'logout';
      case Tabs.ACCESS_STRATEGY :
        return 'accessstrategy';
      case Tabs.MULTIFACTOR :
        return 'multiauth';
      case Tabs.PROXY :
        return 'proxy';
      case Tabs.USERNAME_ATTRIBUTE :
        return 'userattr';
      case Tabs.ATTRIBUTE_RELEASE :
        return 'attrRelease';
      case Tabs.PROPERTIES :
        return 'properties'
      case Tabs.ADVANCED :
        return 'advanced';
    }
  }

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

    this.service.saveService(this.data.service)
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
    let hasIdAssignedAlready = this.data.service.id && this.data.service.id > 0;

    if (!hasIdAssignedAlready && id && id != -1) {
      this.data.service.id = id;
      this.alert.show(this.messages.services_form_alert_serviceAdded,'info');
    }
    else {
      this.alert.show(this.messages.services_form_alert_serviceUpdated,'info');
    }

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

    // OAuth Client Options Only
    if (OAuthRegisteredService.instanceOf(data)) {
      let oauth: OAuthRegisteredService = data as OAuthRegisteredService;
      if (!oauth.clientId) {
        err.push('oauthClientId');
      }
      if (!oauth.clientSecret) {
        err.push('oauthClientSecret');
      }
    }

    // Username Attribute Provider Options
    if (PrincipalAttributeRegisteredServiceUsernameProvider.instanceOf(data.usernameAttributeProvider)) {
      let attrProvider: PrincipalAttributeRegisteredServiceUsernameProvider = data.usernameAttributeProvider as PrincipalAttributeRegisteredServiceUsernameProvider;
      if (!attrProvider.usernameAttribute) {
        err.push('uapUsernameAttribute');
      }
    }
    if (AnonymousRegisteredServiceUsernameProvider.instanceOf(data.usernameAttributeProvider)) {
      let anonProvider: AnonymousRegisteredServiceUsernameProvider = data.usernameAttributeProvider as AnonymousRegisteredServiceUsernameProvider;
      if (anonProvider.persistentIdGenerator) {
        err.push('uapSaltSetting');
      }
    }

    // Proxy Policy Options
    if (RegexMatchingRegisteredServiceProxyPolicy.instanceOf(data.proxyPolicy)) {
      let regPolicy: RegexMatchingRegisteredServiceProxyPolicy = data.proxyPolicy as RegexMatchingRegisteredServiceProxyPolicy;
      if (!regPolicy.pattern || !this.validateRegex(regPolicy.pattern)) {
        err.push('proxyPolicyRegex');
      }
    }



    // Principle Attribute Repository Options
    if (CachingPrincipalAttributesRepository.instanceOf(data.attributeReleasePolicy.principalAttributesRepository)) {
      let cache: CachingPrincipalAttributesRepository = data.attributeReleasePolicy.principalAttributesRepository as CachingPrincipalAttributesRepository;
      if (!cache.timeUnit){
        err.push('cachedTime');
      }
      if (!cache.mergingStrategy){
        err.push('mergingStrategy');
      }
    }
    if (data.attributeReleasePolicy.attributeFilter != null) {
      if (!this.validateRegex(data.attributeReleasePolicy.attributeFilter.pattern)) {
        err.push('attFilter');
      }
    }

    return err;
  };
}
