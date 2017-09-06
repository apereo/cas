import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SurrogateComponent } from './surrogate.component';

describe('SurrogateComponent', () => {
  let component: SurrogateComponent;
  let fixture: ComponentFixture<SurrogateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SurrogateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SurrogateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
