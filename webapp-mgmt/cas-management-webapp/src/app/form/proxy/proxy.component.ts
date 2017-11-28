import {Component, OnInit, Input} from '@angular/core';
import {Messages} from '../../messages';
import {AbstractRegisteredService} from '../../../domain/registered-service';
import {
  RefuseRegisteredServiceProxyPolicy,
  RegexMatchingRegisteredServiceProxyPolicy
} from '../../../domain/proxy-policy,ts';
import {Data} from '../data';

enum Type {
  REGEX,
  REFUSE
}

@Component({
  selector: 'app-proxy',
  templateUrl: './proxy.component.html'
})
export class ProxyComponent implements OnInit {

  type: Type;
  TYPE = Type;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    if (RefuseRegisteredServiceProxyPolicy.instanceOf(this.data.service.proxyPolicy)) {
      this.type = Type.REFUSE;
    } else if (RegexMatchingRegisteredServiceProxyPolicy.instanceOf(this.data.service.proxyPolicy)) {
      this.type = Type.REGEX;
    }
  }

  changeType() {
    switch (+this.type) {
      case Type.REFUSE :
        this.data.service.proxyPolicy = new RefuseRegisteredServiceProxyPolicy();
        break;
      case Type.REGEX :
        this.data.service.proxyPolicy = new RegexMatchingRegisteredServiceProxyPolicy();
        break;
    }
  }

}
