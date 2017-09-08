import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Messages} from "../messages";
import {Router} from "@angular/router";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  @ViewChild("search") search: ElementRef;

  constructor(public messages: Messages,
              public router: Router) { }

  ngOnInit() {
    Observable.fromEvent(this.search.nativeElement, 'keyup')
      .debounceTime(250)
      .distinctUntilChanged()
      .subscribe(() => {
        this.router.navigate(['search', this.search.nativeElement.value]);
      });
  }

}
