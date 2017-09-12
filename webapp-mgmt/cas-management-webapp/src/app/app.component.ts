import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {Messages} from "./messages";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})

export class AppComponent {

  constructor(private router: Router, public messages: Messages) {
    router.navigate(['/domains']);
  }

}


