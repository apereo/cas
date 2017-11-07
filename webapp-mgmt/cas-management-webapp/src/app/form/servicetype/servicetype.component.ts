import {Component, OnInit} from '@angular/core';
import {Messages} from '../../messages';
import {RegexRegisteredService} from '../../../domain/registered-service';
import {OAuthRegisteredService, OidcRegisteredService} from '../../../domain/oauth-service';
import {SamlRegisteredService} from '../../../domain/saml-service';
import {WSFederationRegisterdService} from '../../../domain/wsed-service';
import {Data} from '../data';

enum Type {
  CAS = 'cas',
  OAUTH = 'oauth',
  SAML = 'saml',
  OIDC = 'oidc',
  WS_FED = 'wsfed'
}

@Component({
  selector: 'app-servicetype',
  templateUrl: './servicetype.component.html'
})
export class ServicetypeComponent implements OnInit {


  type: string;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    if (OAuthRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.OAUTH;
    } else if (WSFederationRegisterdService.instanceOf(this.data.service)) {
      this.type = Type.WS_FED;
    } else if (OidcRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.OIDC;
    } else if (SamlRegisteredService.instanceOf(this.data.service)) {
      this.type = Type.SAML;
    } else {
      this.type = Type.CAS;
    }
  }

  changeType() {
    switch (this.type) {
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
      case Type.WS_FED :
        this.data.service = new WSFederationRegisterdService(this.data.service);
        break;
    }
  }

}
