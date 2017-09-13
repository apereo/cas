import {Component, ElementRef} from '@angular/core';
import {Router} from "@angular/router";
import {Messages} from "./messages";
import {element} from "protractor";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})

export class AppComponent {

  type: String;

  constructor(private router: Router,
              public messages: Messages,
              public elRef: ElementRef) {
    this.type = this.elRef.nativeElement.getAttribute('type');
    if (this.type === "DOMAIN") {
      router.navigate(['/domains']);
    } else {
      router.navigate(['services','default']);
    }
  }

}


