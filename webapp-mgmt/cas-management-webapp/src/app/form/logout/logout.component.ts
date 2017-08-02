import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html'
})
export class LogoutComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
