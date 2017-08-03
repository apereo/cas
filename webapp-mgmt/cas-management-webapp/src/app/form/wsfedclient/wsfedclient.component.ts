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

  service: WSFederationRegisterdService;
  selectOptions;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service as WSFederationRegisterdService;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
  }

}
