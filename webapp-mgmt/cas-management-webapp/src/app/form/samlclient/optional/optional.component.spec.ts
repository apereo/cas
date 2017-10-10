import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OptionalComponent } from './optional.component';

describe('OptionalComponent', () => {
  let component: OptionalComponent;
  let fixture: ComponentFixture<OptionalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OptionalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OptionalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
