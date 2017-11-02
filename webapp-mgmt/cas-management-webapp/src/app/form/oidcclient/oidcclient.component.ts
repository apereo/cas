import {Component, Input, OnInit} from '@angular/core';
import {OidcRegisteredService} from '../../../domain/oauth-service';
import {Messages} from '../../messages';
import {Data} from '../data';

@Component({
  selector: 'app-oidcclient',
  templateUrl: './oidcclient.component.html',
  styleUrls: ['./oidcclient.component.css']
})
export class OidcclientComponent implements OnInit {

  service: OidcRegisteredService;
  showOAuthSecret: boolean;

  constructor(public messages: Messages,
              public data: Data) {
    this.service = data.service as OidcRegisteredService;
  }

  ngOnInit() {
    if (!this.service.scopes) {
      this.service.scopes = [];
    }
  }

}
