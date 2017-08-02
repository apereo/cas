import {Component, Input, OnInit} from '@angular/core';
import {RemoteEndpointServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";

@Component({
  selector: 'app-remote',
  templateUrl: './remote.component.html',
  styleUrls: ['./remote.component.css']
})
export class RemoteComponent implements OnInit {

  @Input()
  accessStrategy: RemoteEndpointServiceAccessStrategy;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
