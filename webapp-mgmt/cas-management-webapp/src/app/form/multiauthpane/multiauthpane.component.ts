import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  failureModes = ["NONE","OPEN","CLOSED","PHANTOM"];

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
  }

  saveProviders(providers: String) {
    this.data.service.multifactorPolicy.multifactorAuthenticationProviders = providers.split(',');
  }

}
