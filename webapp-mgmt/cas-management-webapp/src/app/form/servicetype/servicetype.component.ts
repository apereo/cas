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

  selectOptions;
  type: Type;
  TYPE = Type;
  types = [Type.CAS,Type.OAUTH,Type.OIDC,Type.SAML,Type.WS_Fed];
  display = ["CAS Client","OAuth2 Client","OpenID Connect Client","SAML2 Service Provider","WS Federation"];

  constructor(public messages: Messages,
              private data: Data) {
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (OAuthRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.OAUTH;
    } else if (WSFederationRegisterdService.instanceOf(this.data.service)) {
      this.type = Type.WS_Fed;
    } else if (OidcRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.OIDC;
    } else if (SamlRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.SAML;
    } else {
      this.type = Type.CAS;
    }
  }

  changeType() {
    switch(+this.type) {
      case Type.CAS :
        this.data.service = new RegexRegisteredService(this.data.service);
        break;
      case Type.OAUTH :
        this.data.service = new OAuthRegisteredService(this.data.service);
        break;
      case Type.OIDC :
        this.data.service = new OidcRegisteredService(this.data.service);
        break;
      case Type.SAML :
        this.data.service = new SamlRegisteredService(this.data.service);
        break;
      case Type.WS_Fed:
        this.data.service = new WSFederationRegisterdService(this.data.service);
        break;
    }
  }

}
