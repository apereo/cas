import {Component, Input, OnInit} from '@angular/core';
import {TimeBasedRegisteredServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";

@Component({
  selector: 'app-time',
  templateUrl: './time.component.html',
  styleUrls: ['./time.component.css']
})
export class TimeComponent implements OnInit {

  @Input()
  accessStrategy: TimeBasedRegisteredServiceAccessStrategy;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
