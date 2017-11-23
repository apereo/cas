import {Component, Input, OnInit} from '@angular/core';
import {TimeBasedRegisteredServiceAccessStrategy} from '../../../../domain/access-strategy';
import {Messages} from '../../../messages';
import {Data} from '../../data';

@Component({
  selector: 'app-time',
  templateUrl: './time.component.html',
  styleUrls: ['./time.component.css']
})
export class TimeComponent implements OnInit {

  accessStrategy: TimeBasedRegisteredServiceAccessStrategy;

  constructor(public messages: Messages,
              public data: Data) {
    this.accessStrategy = data.service.accessStrategy as TimeBasedRegisteredServiceAccessStrategy;
  }

  ngOnInit() {
  }

}
