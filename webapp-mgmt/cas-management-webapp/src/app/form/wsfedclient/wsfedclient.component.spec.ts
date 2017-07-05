import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WsfedclientComponent } from './wsfedclient.component';

describe('WsfedclientComponent', () => {
  let component: WsfedclientComponent;
  let fixture: ComponentFixture<WsfedclientComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WsfedclientComponent ]
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
