import {Component, OnInit} from '@angular/core';
import {Messages} from './messages';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})

export class AppComponent implements OnInit {

  constructor(public messages: Messages) {
  }

  ngOnInit() {

  }

}


