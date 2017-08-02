import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {
  RefuseRegisteredServiceProxyPolicy,
  RegexMatchingRegisteredServiceProxyPolicy
} from "../../../domain/proxy-policy,ts";

@Component({
  selector: 'app-proxy',
  templateUrl: './proxy.component.html'
})
export class ProxyComponent implements OnInit {

  @Input()
  service: AbstractRegisteredService;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
    switch (this.service.proxyPolicy["@class"]) {
      case "org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy" :
        this.type = "REFUSE";
        break;
      case "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy" :
        this.type = "REGEX";
        break;
    }
  }

  changeType() {
    switch(this.type) {
      case "REFUSE" :
        this.service.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();
        break;
      case "REGEX" :
        this.service.proxyPolicy = new RegexMatchingRegisteredServiceProxyPolicy();
        break;
    }
  }

}
