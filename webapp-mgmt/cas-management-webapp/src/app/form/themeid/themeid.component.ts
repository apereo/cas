import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-themeid',
  templateUrl: './themeid.component.html'
})
export class ThemeidComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
