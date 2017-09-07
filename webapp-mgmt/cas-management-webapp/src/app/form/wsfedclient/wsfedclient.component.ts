import {Component, Input, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {WSFederationRegisterdService} from "../../../domain/wsed-service";
import {Data} from "../data";

@Component({
  selector: 'app-wsfedclient',
  templateUrl: './wsfedclient.component.html',
  styleUrls: ['./wsfedclient.component.css']
})
export class WsfedclientComponent implements OnInit {

  selectOptions;
  service: WSFederationRegisterdService;

  constructor(public messages: Messages,
              public data: Data) {
    this.selectOptions = data.selectOptions;
    this.service = data.service as WSFederationRegisterdService;
  }

  ngOnInit() {
  }

}
