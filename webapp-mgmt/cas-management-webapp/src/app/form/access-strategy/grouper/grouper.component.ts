import {Component, Input, OnInit} from '@angular/core';
import {GrouperRegisteredServiceAccessStrategy} from '../../../../domain/access-strategy';
import {Messages} from '../../../messages';
import {Data} from '../../data';

@Component({
  selector: 'app-grouper',
  templateUrl: './grouper.component.html',
  styleUrls: ['./grouper.component.css']
})
export class GrouperComponent implements OnInit {

  groupFields = ['NAME', 'DISPLAY_NAME', 'EXTENSION', 'DISPLAY_EXTENSION'];

  accessStrategy: GrouperRegisteredServiceAccessStrategy;
  constructor(public messages: Messages,
              public data: Data) {
    this.accessStrategy = data.service.accessStrategy as GrouperRegisteredServiceAccessStrategy;

  }

  ngOnInit() {
  }

}
