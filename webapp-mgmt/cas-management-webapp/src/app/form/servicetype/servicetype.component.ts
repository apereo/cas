import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService, RegexRegisteredService, RegisteredService} from "../../../domain/registered-service";
import {OAuthRegisteredService, OidcRegisteredService} from "../../../domain/oauth-service";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";
import {Data} from "../data";

@Component({
  selector: 'app-servicetype',
  templateUrl: './servicetype.component.html'
})
export class ServicetypeComponent implements OnInit {

  service: AbstractRegisteredService;
  selectOptions;
  type: String;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (OAuthRegisteredService.instanceOf(this.service)) {
      this.type = this.selectOptions.serviceTypeList[1].value;
    } else if (WSFederationRegisterdService.instanceOf(this.service)) {
      this.type = this.selectOptions.serviceTypeList[5].value;
    } else if (OidcRegisteredService.instanceOf(this.service)) {
      this.type = this.selectOptions.serviceTypeList[4].value;
    } else if (SamlRegisteredService.instanceOf(this.service)) {
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
    this.data.service = this.service;
  }

}
