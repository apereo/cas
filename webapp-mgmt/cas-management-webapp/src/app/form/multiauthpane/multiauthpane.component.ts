import {Component, OnInit} from '@angular/core';
import {Messages} from '../../messages';
import {Data} from '../data';
import {FormData} from '../../../domain/form-data';

@Component({
  selector: 'app-multiauthpane',
  templateUrl: './multiauthpane.component.html'
})
export class MultiauthpaneComponent implements OnInit {
  formData: FormData;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
  }

}
