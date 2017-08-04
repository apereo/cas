import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  service: AbstractRegisteredService;
  failureModes = ["NONE","OPEN","CLOSED","PHANTOM"];

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
  }

  ngOnInit() {
  }

  saveProviders(providers: String) {
    this.service.multifactorPolicy.multifactorAuthenticationProviders = providers.split(',');
  }

}
