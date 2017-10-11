import {Component, ElementRef, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {Messages} from "./messages";
import {element} from "protractor";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})

export class AppComponent implements OnInit {

  type: String;

  constructor(private router: Router,
              public messages: Messages,
              public elRef: ElementRef) {
    this.type = this.elRef.nativeElement.getAttribute('type');
  }

  ngOnInit() {
    if (this.type === "DOMAIN") {
      this.router.navigate(['/domains']);
    } else {
      this.router.navigate(['services','default']);
    }
  }

}


