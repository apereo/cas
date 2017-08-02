import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  @Input()
  selectOptions;

  @Input()
  isAdmin: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

  saveProviders(providers: String) {
    this.service.multifactorPolicy.multifactorAuthenticationProviders = providers.split(',');
  }

}
