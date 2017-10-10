import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NameidComponent } from './nameid.component';

describe('NameidComponent', () => {
  let component: NameidComponent;
  let fixture: ComponentFixture<NameidComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NameidComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NameidComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
