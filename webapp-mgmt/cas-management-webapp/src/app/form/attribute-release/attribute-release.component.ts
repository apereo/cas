import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {FormData} from "../../../domain/service-view-bean";
import {RegisteredServiceRegexAttributeFilter} from "../../../domain/attribute-release";
import {Data} from "../data";
import {OidcRegisteredService} from "../../../domain/oauth-service";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";

@Component({
  selector: 'app-attribute-release',
  templateUrl: './attribute-release.component.html',
})

export class AttributeReleaseComponent implements OnInit {

  service: AbstractRegisteredService;
  isOidc: boolean;
  isWsFed: boolean;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
    this.isOidc = OidcRegisteredService.instanceOf(this.service);
    this.isWsFed = WSFederationRegisterdService.instanceOf(this.service);
  }

}
