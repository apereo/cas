import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-logo',
  templateUrl: './logo.component.html'
})
export class LogoComponent implements OnInit {

  service: AbstractRegisteredService;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
  }

}
