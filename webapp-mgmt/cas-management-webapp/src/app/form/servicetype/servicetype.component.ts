import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService, RegexRegisteredService, RegisteredService} from "../../../domain/registered-service";
import {OAuthRegisteredService, OidcRegisteredService} from "../../../domain/oauth-service";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";
import {TabService} from "../tab.service";

@Component({
  selector: 'app-servicetype',
  templateUrl: './servicetype.component.html'
})
export class ServicetypeComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  @Input()
  selectOptions;

  type: String;

  wsfed = WSFederationRegisterdService.instanceOf;
  oauth = OAuthRegisteredService.instanceOf;
  oidc = OidcRegisteredService.instanceOf;
  saml = SamlRegisteredService.instanceOf;

  constructor(public messages: Messages,
              private tabService: TabService) { }

  ngOnInit() {
    if (this.oauth(this.service)) {
      this.type = this.selectOptions.serviceTypeList[1].value;
    } else if (this.wsfed(this.service)) {
      this.type = this.selectOptions.serviceTypeList[5].value;
    } else if (this.oidc(this.service)) {
      this.type = this.selectOptions.serviceTypeList[4].value;
    } else if (this.saml(this.service)) {
      this.type = this.selectOptions.serviceTypeList[3].value;
    } else {
      this.type = this.selectOptions.serviceTypeList[0].value;
    }

  }

  changeType() {
    switch(this.type) {
      case this.selectOptions.serviceTypeList[0].value :
        this.service = new RegexRegisteredService(this.service);
        break;
      case this.selectOptions.serviceTypeList[1].value :
        this.service = new OAuthRegisteredService(this.service);
        break;
      case this.selectOptions.serviceTypeList[4].value :
        this.service = new OidcRegisteredService(this.service);
        break;
      case this.selectOptions.serviceTypeList[3].value :
        this.service = new SamlRegisteredService(this.service);
        break;
      case this.selectOptions.serviceTypeList[5].value :
        this.service = new WSFederationRegisterdService(this.service);
        break;
    }
    this.tabService.service = this.service;
    console.log("Switch : "+this.service["@class"]);
  }

}
