import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {Data} from '../data';

@Component({
  selector: 'app-logouttypeeval',
  templateUrl: './logouttypeeval.component.html'
})
export class LogouttypeevalComponent implements OnInit {

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

}
