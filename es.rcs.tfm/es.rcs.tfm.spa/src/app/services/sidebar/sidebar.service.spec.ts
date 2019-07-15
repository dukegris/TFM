import { TestBed, inject } from '@angular/core/testing';
import { SideBarService } from './sidebar.service';

describe('SideBarService', () => {

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [SideBarService]
        });
    });

    it('should be created', inject([SideBarService], (service: SideBarService) => {
        expect(service).toBeTruthy();
    }));

});