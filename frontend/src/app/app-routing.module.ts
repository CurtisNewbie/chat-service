import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ChatRoomComponent } from './chat-room/chat-room.component';
import { LoginComponent } from './login/login.component';
import { RoomListComponent } from './room-list/room-list.component';

const routes: Routes = [
  {
    path: 'login-page',
    component: LoginComponent,
  },
  {
    path: 'room-list',
    component: RoomListComponent,
  },
  {
    path: 'chat-room',
    component: ChatRoomComponent,
  },
  { path: '**', redirectTo: '/login-page' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
