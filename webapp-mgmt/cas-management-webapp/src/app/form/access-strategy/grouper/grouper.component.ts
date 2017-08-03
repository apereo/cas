import {Component, Input, OnInit} from '@angular/core';
import {GrouperRegisteredServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";
import {Data} from "../../data";

@Component({
  selector: 'app-grouper',
  templateUrl: './grouper.component.html',
  styleUrls: ['./grouper.component.css']
})
export class GrouperComponent implements OnInit {

  accessStrategy: GrouperRegisteredServiceAccessStrategy;
  selectOptions;

  constructor(public messages: Messages,
              private data: Data) {
    this.accessStrategy = data.service.accessStrategy as GrouperRegisteredServiceAccessStrategy;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
  }

}
