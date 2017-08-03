import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {OAuthRegisteredService, OidcRegisteredService} from "../../../domain/oauth-service";
import {Data} from "../data";

@Component({
  selector: 'app-oauthclient',
  templateUrl: './oauthclient.component.html'
})
export class OauthclientComponent implements OnInit {

  service: OAuthRegisteredService;
  selectOptions;
  @Input()
  type: String;

  showOAuthSecret: boolean;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service as OAuthRegisteredService;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
  }

}
