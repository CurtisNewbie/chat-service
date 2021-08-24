import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  OnDestroy,
  AfterViewChecked,
} from '@angular/core';
import { Member } from '../models/Member';
import { RoomService } from '../room.service';
import { RoomType } from '../models/Room';
import { NotificationService } from '../notification.service';
import { Message, PollMessageResponse } from '../models/Message';
import { UserService } from '../user.service';
import { CdkVirtualScrollViewport } from '@angular/cdk/scrolling';
import { SocketService } from '../socket.service';
import { WebSocketSubject } from 'rxjs/webSocket';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chat-room',
  templateUrl: './chat-room.component.html',
  styleUrls: ['./chat-room.component.css'],
})
export class ChatRoomComponent implements OnInit, OnDestroy, AfterViewChecked {
  roomId: string = null;
  currMsg: string = null;
  isConnected: boolean = false;
  members: Member[] = [];
  messages: Message[] = [];
  username: string = null;

  private msgIdSet: Set<number> = new Set();
  private pollMsgInterval = null;
  private pollMembersInterval = null;
  private messageWebSocketSubscrtiption: Subscription = null;
  private messageWebSocketSubject: WebSocketSubject<Message> = null;

  @ViewChild('virtualScroll')
  virtualScroll: CdkVirtualScrollViewport;

  constructor(
    private roomService: RoomService,
    private notifi: NotificationService,
    private userService: UserService,
    private websocketServices: SocketService
  ) {}

  ngOnInit(): void {
    // poll messages and members for every 10 seconds
    this.pollMsgInterval = setInterval(() => this.pollMessages(), 10000);
    this.pollMembersInterval = setInterval(() => this.pollMembers(), 10000);
    if (!this.userService.hasUserInfo()) {
      this.userService.fetchUserInfo();
    }
    this.userService.usernameObservable.subscribe({
      next: (un) => {
        this.username = un;
      },
    });
  }

  ngOnDestroy(): void {
    this.clearIntervals();
    this.closeMessageWebSocket();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
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
          this.openMessageWebSocket();
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
          this.isConnected = false;
          this.clearIntervals();
          this.messages = [];
          this.members = [];
          this.msgIdSet.clear();
          this.closeMessageWebSocket();
          this.notifi.toast(`room disconnected`);
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
          this.openMessageWebSocket();
          this.pollMembers();
        },
      });
  }

  openMessageWebSocket() {
    console.log('Openning websocket for messages');
    this.messageWebSocketSubject =
      this.websocketServices.openMessageWebSocket();
    this.messageWebSocketSubscrtiption = this.messageWebSocketSubject.subscribe(
      {
        next: (msg) => {
          console.log('wss', msg);
          if (msg.messageId != null && !this.msgIdSet.has(msg.messageId)) {
            this.messages.push(msg);
            this.msgIdSet.add(msg.messageId);
            this.messages = [...this.messages];
          }
        },
        complete: () => {
          this.messageWebSocketSubscrtiption.unsubscribe();
        },
      }
    );
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
    if (
      this.messageWebSocketSubject == null ||
      this.messageWebSocketSubject.isStopped
    ) {
      return;
    }

    this.messageWebSocketSubject.next({
      sender: null,
      message: this.currMsg,
      messageId: null,
    });
    this.currMsg = null;

    // this.roomService
    //   .sendMessage({
    //     roomId: this.roomId,
    //     message: this.currMsg,
    //   })
    //   .subscribe({
    //     complete: () => {
    //       this.currMsg = null;
    //     },
    //   });
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
    return msg.sender === this.username;
  }

  msgInputKeyPressed(event: any): void {
    if (event.key === 'Enter') this.sendMsg();
  }

  clearIntervals(): void {
    if (this.pollMsgInterval) clearInterval(this.pollMsgInterval);
    if (this.pollMembersInterval) clearInterval(this.pollMembersInterval);
  }

  closeMessageWebSocket(): void {
    this.messageWebSocketSubscrtiption.unsubscribe();
    this.messageWebSocketSubject.unsubscribe();
    this.messageWebSocketSubject = null;
    this.messageWebSocketSubscrtiption = null;
  }

  scrollToBottom(): void {
    this.virtualScroll.scrollToIndex(this.messages.length - 1);
  }
}
