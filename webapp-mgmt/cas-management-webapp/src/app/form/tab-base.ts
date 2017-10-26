import {ApplicationRef, ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import 'rxjs/add/operator/switchMap';
import {Data} from './data';
import {Subscription} from 'rxjs/Subscription';

@Component({
  selector: 'app-tab-base',
  template: ''
})
export class TabBaseComponent implements OnInit, OnDestroy {

  @ViewChild('submit')
  submit: ElementRef;

  sub: Subscription;

  constructor(public data: Data,
              public changeRef: ChangeDetectorRef) {
  }

  ngOnInit() {
    if (this.data.submitted) {
      this.submit.nativeElement.click();
    }
    this.sub = this.data.save.asObservable().subscribe(() => {
      this.submit.nativeElement.click();
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

}
