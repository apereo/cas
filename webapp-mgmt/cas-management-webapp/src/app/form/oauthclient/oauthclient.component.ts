import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {OAuthRegisteredService, OidcRegisteredService} from '../../../domain/oauth-service';
import {Data} from '../data';

@Component({
  selector: 'app-oauthclient',
  templateUrl: './oauthclient.component.html'
})
export class OauthclientComponent implements OnInit {

  service: OAuthRegisteredService;
  showOAuthSecret: boolean;

  constructor(public messages: Messages,
              public data: Data) {
    this.service = data.service as OAuthRegisteredService;
  }

  ngOnInit() {

  }

  isOidc() {
    return OidcRegisteredService.instanceOf(this.data.service);
  }
}
