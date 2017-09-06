import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GrouperComponent } from './grouper.component';

describe('GrouperComponent', () => {
  let component: GrouperComponent;
  let fixture: ComponentFixture<GrouperComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GrouperComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GrouperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
