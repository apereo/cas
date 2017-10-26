import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {Data} from '../data';

@Component({
  selector: 'app-servicename',
  templateUrl: './servicename.component.html'
})
export class ServicenameComponent implements OnInit {

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

}
