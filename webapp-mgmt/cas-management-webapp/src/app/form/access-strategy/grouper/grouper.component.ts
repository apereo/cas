import {Component, Input, OnInit} from '@angular/core';
import {GrouperRegisteredServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";

@Component({
  selector: 'app-grouper',
  templateUrl: './grouper.component.html',
  styleUrls: ['./grouper.component.css']
})
export class GrouperComponent implements OnInit {

  @Input()
  accessStrategy: GrouperRegisteredServiceAccessStrategy;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
