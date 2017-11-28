import { Component, OnInit } from '@angular/core';
import {SamlRegisteredService} from '../../../../domain/saml-service';
import {Messages} from '../../../messages';
import {Data} from '../../data';

@Component({
  selector: 'app-optional',
  templateUrl: './optional.component.html',
  styleUrls: ['./optional.component.css']
})
export class OptionalComponent implements OnInit {
  service: SamlRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
      this.service = data.service as SamlRegisteredService;
  }

  ngOnInit() {
  }

}
