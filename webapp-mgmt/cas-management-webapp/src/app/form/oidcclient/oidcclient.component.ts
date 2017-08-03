import {Component, Input, OnInit} from '@angular/core';
import {OidcRegisteredService} from "../../../domain/oauth-service";
import {Messages} from "../../messages";

@Component({
  selector: 'app-oidcclient',
  templateUrl: './oidcclient.component.html',
  styleUrls: ['./oidcclient.component.css']
})
export class OidcclientComponent implements OnInit {

  @Input()
  service: OidcRegisteredService;

  @Input()
  selectOptions;

  constructor(public messages: Messages) { }

  ngOnInit() {
    if(!this.service.scopes) {
      this.service.scopes = [];
    }
  }

}
