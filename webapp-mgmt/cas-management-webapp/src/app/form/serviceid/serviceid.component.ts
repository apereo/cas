import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-serviceid',
  templateUrl: './serviceid.component.html'
})
export class ServiceidComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
