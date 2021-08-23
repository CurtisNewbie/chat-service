import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './login/login.component';

const routes: Routes = [
  {
    path: 'login-page',
    component: LoginComponent,
  },
  // {
  //   path: 'room-list',
  //   component: null,
  // },
  // {
  //   path: 'chat-room',
  //   component: null,
  // },
  { path: '**', redirectTo: '/login-page' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
