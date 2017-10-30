import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WsfedclientComponent } from './wsfedclient.component';
import {SharedModule} from '../../shared/shared.module';
import {FormModule} from '../form.module';
import {FormsModule} from '@angular/forms';
import {Messages} from '../../messages';
import {Data} from '../data';
import {RegexRegisteredService} from '../../../domain/registered-service';
import {WSFederationRegisterdService} from '../../../domain/wsed-service';

describe('WsfedclientComponent', () => {
  let component: WsfedclientComponent;
  let fixture: ComponentFixture<WsfedclientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ FormsModule, SharedModule ],
      declarations: [ WsfedclientComponent ],
      providers: [Messages]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WsfedclientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
