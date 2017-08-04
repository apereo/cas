import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService, RegexRegisteredService, RegisteredService} from "../../../domain/registered-service";
import {OAuthRegisteredService, OidcRegisteredService} from "../../../domain/oauth-service";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";
import {Data} from "../data";

enum Type {
  CAS,
  OAUTH,
  OIDC,
  SAML,
  WS_Fed,
}

@Component({
  selector: 'app-servicetype',
  templateUrl: './servicetype.component.html'
})
export class ServicetypeComponent implements OnInit {

  service: AbstractRegisteredService;
  selectOptions;
  type: Type;
  TYPE = Type;
  types = [Type.CAS,Type.OAUTH,Type.OIDC,Type.SAML,Type.WS_Fed];
  display = ["CAS Client","OAuth2 Client","OpenID Connect Client","SAML2 Service Provider","WS Federation"];

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (OAuthRegisteredService.instanceOf(this.service)) {
      this.type = Type.OAUTH;
    } else if (WSFederationRegisterdService.instanceOf(this.service)) {
      this.type = Type.WS_Fed;
    } else if (OidcRegisteredService.instanceOf(this.service)) {
      this.type = Type.OIDC;
    } else if (SamlRegisteredService.instanceOf(this.service)) {
      this.type = Type.SAML;
    } else {
      this.type = Type.CAS;
    }
  }

  changeType() {
    switch(+this.type) {
      case Type.CAS :
        this.service = new RegexRegisteredService(this.service);
        break;
      case Type.OAUTH :
        this.service = new OAuthRegisteredService(this.service);
        break;
      case Type.OIDC :
        this.service = new OidcRegisteredService(this.service);
        break;
      case Type.SAML :
        this.service = new SamlRegisteredService(this.service);
        break;
      case Type.WS_Fed:
        this.service = new WSFederationRegisterdService(this.service);
        break;
    }
    this.data.service = this.service;
  }

}
