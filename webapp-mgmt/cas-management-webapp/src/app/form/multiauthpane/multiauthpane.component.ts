import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  @Input()
  selectOptions;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
  }

  saveProviders(providers: String) {
    this.service.multifactorPolicy.multifactorAuthenticationProviders = providers.split(',');
  }

}
