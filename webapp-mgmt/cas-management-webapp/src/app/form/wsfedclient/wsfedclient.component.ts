import {Component, Input, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {ServiceData} from "../../../domain/service-edit-bean";

@Component({
  selector: 'app-wsfedclient',
  templateUrl: './wsfedclient.component.html',
  styleUrls: ['./wsfedclient.component.css']
})
export class WsfedclientComponent implements OnInit {

  @Input()
  serviceData: ServiceData;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
