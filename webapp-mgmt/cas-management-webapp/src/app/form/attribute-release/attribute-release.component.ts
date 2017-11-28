import {Component, OnInit} from '@angular/core';
import {Messages} from '../../messages';
import {Data} from '../data';
import {OidcRegisteredService} from '../../../domain/oauth-service';
import {WSFederationRegisterdService} from '../../../domain/wsed-service';

@Component({
  selector: 'app-attribute-release',
  templateUrl: './attribute-release.component.html',
})

export class AttributeReleaseComponent implements OnInit {

  isOidc: boolean;
  isWsFed: boolean;
  oidcService: OidcRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    this.isOidc = OidcRegisteredService.instanceOf(this.data.service);
    this.isWsFed = WSFederationRegisterdService.instanceOf(this.data.service);
    if (this.isOidc) {
      this.oidcService = this.data.service as OidcRegisteredService;
    }
  }

}
