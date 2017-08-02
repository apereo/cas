import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-logo',
  templateUrl: './logo.component.html'
})
export class LogoComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
