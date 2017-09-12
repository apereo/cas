import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainsComponent } from './domains.component';

describe('DomainsComponent', () => {
  let component: DomainsComponent;
  let fixture: ComponentFixture<DomainsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
