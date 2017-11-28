/**
 * Created by tsschmi on 3/13/17.
 */
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

@Injectable()
export class ActivatedRouteStub {

  // ActivatedRoute.params is Observable
  private subject = new BehaviorSubject(this.testParams);
  params = this.subject.asObservable();
  private dataSubject = new BehaviorSubject(this.testData);
  data = this.dataSubject.asObservable();
  private urlSubject = new BehaviorSubject(this.testUrl);
  url = this.urlSubject.asObservable();

  // Test parameters
  private _testParams: {};
  get testParams() { return this._testParams; }
  set testParams(params: {}) {
    this._testParams = params;
    this.subject.next(params);
  }

  private _testData: {};
  get testData() { return this._testData; }
  set testData(data: {}) {
    this._testData = data;
    this.dataSubject.next(data);
  }

  private _testUrl: {} = { path() { return ''; }};
  get testUrl() { return this._testUrl; }
  set testUrl(url: {}) {
    this._testUrl = url;
    this.urlSubject.next(url);
  }

  // ActivatedRoute.snapshot.params
  get snapshot() {
    return { params: this.testParams };
  }
}
