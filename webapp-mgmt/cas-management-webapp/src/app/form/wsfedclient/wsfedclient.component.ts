import {Component, Input, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";

@Component({
  selector: 'app-wsfedclient',
  templateUrl: './wsfedclient.component.html',
  styleUrls: ['./wsfedclient.component.css']
})
export class WsfedclientComponent implements OnInit {

  @Input()
  service: WSFederationRegisterdService;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
