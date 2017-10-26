import { Component, OnInit } from '@angular/core';
import {Messages} from '../../messages';
import {Data} from '../data';

@Component({
  selector: 'app-expiration',
  templateUrl: './expiration.component.html',
  styleUrls: ['./expiration.component.css']
})
export class ExpirationComponent implements OnInit {

  constructor(public messages: Messages,
              public data: Data) { }

  ngOnInit() {
  }

}
