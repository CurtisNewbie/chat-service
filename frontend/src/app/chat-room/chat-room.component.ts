import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import { Member } from '../models/Member';
import { RoomService } from '../room.service';
import { RoomType } from '../models/Room';
import { NotificationService } from '../notification.service';
import { Message, PollMessageResponse } from '../models/Message';
import { UserService } from '../user.service';
import { CdkVirtualScrollViewport } from '@angular/cdk/scrolling';

@Component({
  selector: 'app-chat-room',
  templateUrl: './chat-room.component.html',
  styleUrls: ['./chat-room.component.css'],
})
export class ChatRoomComponent implements OnInit, OnDestroy {
  roomId: string = null;
  currMsg: string = null;
  isConnected: boolean = false;
  members: Member[] = [];
  messages: Message[] = [];
  msgIdSet: Set<number> = new Set();
  pollMsgInterval = null;
  pollMembersInterval = null;

  @ViewChild('virtualScroll')
  virtualScroll: CdkVirtualScrollViewport;

  constructor(
    private roomService: RoomService,
    private notifi: NotificationService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // poll messages for every 0.5 seconds
    this.pollMsgInterval = setInterval(() => this.pollMessages(), 1000);
    this.pollMembersInterval = setInterval(() => this.pollMembers(), 3000);
  }

  ngOnDestroy(): void {
    this.clearIntervals();
  }

  /**
   * Create new room
   */
  createRoom() {
    this.roomService
      .createNewRoom({
        // todo this part isn't implemented yet
        roomType: RoomType.PRIVATE,
      })
      .subscribe({
        next: (resp) => {
          this.roomId = resp.data;
          this.notifi.toast(`Connected to room: ${this.roomId}`);
          this.isConnected = true;
          this.pollMembers();
        },
      });
  }

  /**
   * Create new room
   */
  disconnectRoom() {
    this.roomService
      .disconnectRoom({
        roomId: this.roomId,
      })
      .subscribe({
        complete: () => {
          this.roomId = null;
          this.notifi.toast(`room disconnected`);
          this.isConnected = false;
          this.clearIntervals();
          this.messages = [];
          this.msgIdSet.clear();
        },
      });
  }

  /**
   * Connect to the chat room
   */
  connectRoom() {
    if (!this.roomId) {
      this.notifi.toast('Please create or connect to a room first');
      return;
    }

    this.roomService
      .connectRoom({
        roomId: this.roomId,
      })
      .subscribe({
        complete: () => {
          this.notifi.toast(`Connected to room: ${this.roomId}`);
          this.isConnected = true;
        },
      });
  }

  pollMessages() {
    if (!this.isConnected) return;

    let lastId = null;
    if (this.messages.length > 0)
      lastId = this.messages[this.messages.length - 1].messageId;

    this.roomService
      .pollMessages({
        roomId: this.roomId,
        lastMessageId: lastId,
        limit: 20,
      })
      .subscribe({
        next: (resp) => {
          let mp: PollMessageResponse = resp.data;
          let changed: boolean = false;
          for (let m of mp.messages) {
            if (!this.msgIdSet.has(m.messageId)) {
              changed = true;
              this.messages.push(m);
              this.msgIdSet.add(m.messageId);
            }
          }
          if (changed) {
            this.messages = [...this.messages];
            this.virtualScroll.scrollToIndex(0);
          }
        },
      });
  }

  pollMembers(): void {
    if (!this.isConnected) return;

    this.roomService
      .listMembers({
        roomId: this.roomId,
      })
      .subscribe({
        next: (resp) => {
          this.members = resp.data;
        },
      });
  }

  /**
   * Send a message
   */
  sendMsg() {
    if (!this.isConnected) return;
    if (!this.currMsg) return;

    this.roomService
      .sendMessage({
        roomId: this.roomId,
        message: this.currMsg,
      })
      .subscribe({
        complete: () => {
          this.currMsg = null;
        },
      });
  }

  /**
   * Fetch room members
   */
  fetchMembers() {
    this.roomService.listMembers({ roomId: this.roomId }).subscribe({
      next: (resp) => {
        this.members = resp.data;
      },
    });
  }

  sentByCurrUser(msg: Message): boolean {
    if (!this.userService.hasUserInfo()) return false;
    return msg.sender === this.userService.getUserInfo().username;
  }

  msgInputKeyPressed(event: any): void {
    if (event.key === 'Enter') this.sendMsg();
  }

  clearIntervals(): void {
    if (this.pollMsgInterval) clearInterval(this.pollMsgInterval);
    if (this.pollMembersInterval) clearInterval(this.pollMembersInterval);
  }
}
