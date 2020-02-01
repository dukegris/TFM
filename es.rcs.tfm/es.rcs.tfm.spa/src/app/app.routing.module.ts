import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AppIndexComponent } from 'src/app/components/common/index/app.index.component';

import { UserLoginComponent } from 'src/app/components/security/user-login/user-login.component';

const routes: Routes = [
	{ path: '', component: AppIndexComponent },

	{ path: 'account/login', component: UserLoginComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
