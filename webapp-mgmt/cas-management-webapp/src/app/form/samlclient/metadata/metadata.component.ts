import { Component, OnInit } from '@angular/core';
import {Messages} from '../../../messages';
import {Data} from '../../data';
import {SamlRegisteredService} from '../../../../domain/saml-service';

@Component({
  selector: 'app-metadata',
  templateUrl: './metadata.component.html',
  styleUrls: ['./metadata.component.css']
})
export class MetadataComponent implements OnInit {
  service: SamlRegisteredService;

  constructor(public messages: Messages,
              public data: Data) {
      this.service = data.service as SamlRegisteredService;
  }

  ngOnInit() {
  }

}
