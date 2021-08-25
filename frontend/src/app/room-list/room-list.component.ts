import { NullTemplateVisitor } from '@angular/compiler';
import { Component, OnInit } from '@angular/core';
import { PagingController } from '../models/paging';
import { Room, RoomType } from '../models/Room';
import { Option } from '../models/select-util';
import { NavigationService, NavType } from '../navigation.service';
import { NotificationService } from '../notification.service';
import { RoomService } from '../room.service';

@Component({
  selector: 'app-room-list',
  templateUrl: './room-list.component.html',
  styleUrls: ['./room-list.component.css'],
})
export class RoomListComponent implements OnInit {
  newRoomParam: {
    roomType: RoomType;
    roomName: string;
  } = { roomType: RoomType.PRIVATE, roomName: '' };
  selectedRoom: Room = null;
  rooms: Room[] = [];
  pagingController: PagingController = new PagingController();
  readonly ROOM_TYPE_OPTIONS: Option<RoomType>[] = [
    { name: 'public', value: RoomType.PUBLIC },
    { name: 'private', value: RoomType.PRIVATE },
  ];

  constructor(
    private roomService: RoomService,
    private notifi: NotificationService,
    private nav: NavigationService
  ) {}

  ngOnInit(): void {
    this.fetchPublicRoomList();
  }

  /**
   * Create new room
   */
  createRoom(): void {
    if (this.newRoomParam == null || !this.newRoomParam.roomName) {
      this.notifi.toast("Please enter the new room's name");
      return;
    }

    this.roomService.createNewRoom(this.newRoomParam).subscribe({
      next: (resp) => {
        let roomId = resp.data;
        this.roomService.room = {
          roomId: roomId,
          roomName: this.newRoomParam.roomName,
          createdBy: '', // todo fix this
        };
        this.roomService.room.roomId = roomId;
        this.roomService.isConnected = true;
        this.notifi.toast(`Connected to room: ${roomId}`);
        this.nav.navigateTo(NavType.CHAT_ROOM);
      },
    });
  }

  /**
   * Connect to the chat room
   */
  connectRoom() {
    if (!this.selectedRoom) {
      this.notifi.toast('Please create a room or select a room to connect');
      return;
    }

    let roomId = this.selectedRoom.roomId;
    this.roomService.room = {
      roomId: roomId,
      roomName: this.newRoomParam.roomName,
      createdBy: this.selectedRoom.createdBy,
    };

    this.roomService.isConnected = false;
    this.notifi.toast(`Connected to room: ${roomId}`);
    this.nav.navigateTo(NavType.CHAT_ROOM);
  }

  /**
   * Fetch public rooms
   */
  fetchPublicRoomList() {
    this.roomService.listPublicRooms(this.pagingController.paging).subscribe({
      next: (resp) => {
        this.pagingController.updatePages(resp.data.total);
        this.rooms = resp.data.rooms;
      },
    });
  }

  handle(event) {
    this.pagingController.handle(event);
    this.fetchPublicRoomList();
  }

  selectRoom(room: Room) {
    this.selectedRoom = { ...room };
  }
}
