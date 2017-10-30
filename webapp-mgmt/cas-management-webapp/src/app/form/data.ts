/**
 * Created by tschmidt on 2/22/17.
 */

import {EventEmitter, Injectable} from '@angular/core';
import {AbstractRegisteredService} from '../../domain/registered-service';
import {FormData} from '../../domain/form-data';
import {FormComponent} from './form.component';

@Injectable()
export class Data {
  service: AbstractRegisteredService;
  formData: FormData = new FormData();
  submitted: boolean;
  save: EventEmitter<void> = new EventEmitter<void>();
  form: FormComponent;
}


