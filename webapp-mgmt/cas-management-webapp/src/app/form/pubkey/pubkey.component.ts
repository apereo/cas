import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {RegisteredServicePublicKeyImpl} from "../../../domain/public-key";
import {Data} from "../data";

@Component({
  selector: 'app-pubkey',
  templateUrl: './pubkey.component.html'
})
export class PubkeyComponent implements OnInit {

  service: AbstractRegisteredService;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
    if (!this.service.publicKey) {
      this.service.publicKey = new RegisteredServicePublicKeyImpl();
    }
  }

}
