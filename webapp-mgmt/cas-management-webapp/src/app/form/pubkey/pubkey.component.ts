import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {RegisteredServicePublicKeyImpl} from '../../../domain/public-key';
import {Data} from '../data';

@Component({
  selector: 'app-pubkey',
  templateUrl: './pubkey.component.html'
})
export class PubkeyComponent implements OnInit {

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    if (!this.data.service.publicKey) {
      this.data.service.publicKey = new RegisteredServicePublicKeyImpl();
    }
  }

}
