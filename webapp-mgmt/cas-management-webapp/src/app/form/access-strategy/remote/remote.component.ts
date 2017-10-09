import {Component, Input, OnInit} from '@angular/core';
import {RemoteEndpointServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";
import {Data} from "../../data";

@Component({
  selector: 'app-remote',
  templateUrl: './remote.component.html',
  styleUrls: ['./remote.component.css']
})
export class RemoteComponent implements OnInit {

  accessStrategy: RemoteEndpointServiceAccessStrategy;
  codes = ["100","200","401","403","404","500"];

  constructor(public messages: Messages,
              public data: Data) {
    this.accessStrategy = data.service.accessStrategy as RemoteEndpointServiceAccessStrategy;
  }

  ngOnInit() {
  }

}
