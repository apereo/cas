import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {RegisteredServicePublicKeyImpl} from "../../../domain/public-key";

@Component({
  selector: 'app-pubkey',
  templateUrl: './pubkey.component.html'
})
export class PubkeyComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  constructor(public messages: Messages) { }

  ngOnInit() {
    if (!this.service.publicKey) {
      this.service.publicKey = new RegisteredServicePublicKeyImpl();
    }
  }

}
