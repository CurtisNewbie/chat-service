import {
  Component,
  OnInit,
  ViewChild,
  OnDestroy,
  AfterViewInit,
} from '@angular/core';
import { Member } from '../models/Member';
import { RoomService } from '../room.service';
import { NotificationService } from '../notification.service';
import { Message, PollMessageResponse } from '../models/Message';
import { UserService } from '../user.service';
import {
  CdkVirtualScrollViewport,
  ScrollDispatcher,
} from '@angular/cdk/scrolling';
import { SocketService } from '../socket.service';
import { WebSocketSubject } from 'rxjs/webSocket';
import { Subscription } from 'rxjs';
import { NavigationService, NavType } from '../navigation.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-chat-room',
  templateUrl: './chat-room.component.html',
  styleUrls: ['./chat-room.component.css'],
})
export class ChatRoomComponent implements OnInit, OnDestroy, AfterViewInit {
  roomId: string = null;
  currMsg: string = null;
  members: Member[] = [];
  messages: Message[] = [];
  username: string = null;
  roomName: string = '';
  hasNewMessages: boolean = false;

  private hasNewMsgSub: Subscription = null;
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
    private websocketServices: SocketService,
    private nav: NavigationService,
    private scrollDispatcher: ScrollDispatcher
  ) {}

  ngOnInit(): void {
    // get name of current user
    if (!this.userService.hasUserInfo()) {
      this.userService.fetchUserInfo();
    } else {
      this.username = this.userService.getUserInfo().username;
    }
    this.userService.usernameObservable.subscribe({
      next: (un) => {
        this.username = un;
      },
    });

    // page refresh (e.g., F5)
    if (
      this.roomService.room == null ||
      (!this.roomService.isConnected && !this.roomService.room.roomId)
    ) {
      this.nav.navigateTo(NavType.ROOM_LIST);
      return;
    }

    // we have roomId, but we are not connected yet
    this.roomId = this.roomService.room.roomId;
    this.roomName = this.roomService.room.roomName;
    if (!this.roomService.isConnected) {
      this.roomService.connectRoom({ roomId: this.roomId }).subscribe({
        next: (resp) => {
          if (resp.hasError) {
            this.nav.navigateTo(NavType.ROOM_LIST);
            return;
          }

          // once we enter the room, we open websocket
          this.openMessageWebSocket();
          this.pollMembers();
        },
      });
    } else {
      // we are in the room already, open the webscoket
      this.openMessageWebSocket();
      this.pollMembers();
    }

    // poll members for every 10 seconds
    this.pollMembersInterval = setInterval(() => this.pollMembers(), 10000);
  }

  ngOnDestroy(): void {
    this.clearIntervals();
    this.closeMessageWebSocket();
    this.hasNewMsgSub.unsubscribe();
  }

  ngAfterViewInit(): void {
    if (this.hasNewMsgSub != null) {
      return;
    }
    this.hasNewMsgSub = this.scrollDispatcher
      .scrolled()
      .pipe(
        filter(
          (event) => this.virtualScroll.measureScrollOffset('bottom') === 0
        )
      )
      .subscribe((event) => {
        // already seen the last message
        this.hasNewMessages = false;
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
          this.clearIntervals();
          this.messages = [];
          this.members = [];
          this.msgIdSet.clear();
          this.closeMessageWebSocket();
          this.notifi.toast(`Room disconnected`);
          this.nav.navigateTo(NavType.ROOM_LIST);
        },
      });
  }

  openMessageWebSocket() {
    if (!this.roomId) return;

    console.log('Connecting websocket for messages');
    this.messageWebSocketSubject =
      this.websocketServices.openMessageWebSocket();

    if (this.messageWebSocketSubject.hasError) {
      console.log(this.messageWebSocketSubject.thrownError);
      this.notifi.toast('Failed to connect to room, please try again later');
    }

    this.messageWebSocketSubscrtiption = this.messageWebSocketSubject.subscribe(
      {
        next: (msg) => {
          console.log('wss', msg);
          if (msg.messageId != null && !this.msgIdSet.has(msg.messageId)) {
            this.messages.push(msg);
            this.msgIdSet.add(msg.messageId);
            this.messages = [...this.messages];
            this.hasNewMessages = true;
          }
        },
      }
    );

    console.log('Websocket connected for messages');
  }

  pollMessages() {
    if (!this.roomId) return;

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
          if (resp.hasError) return;
          let mp: PollMessageResponse = resp.data;
          let changed: boolean = false;
          for (let m of mp.messages) {
            if (m.messageId != null && !this.msgIdSet.has(m.messageId)) {
              changed = true;
              this.messages.push(m);
              this.msgIdSet.add(m.messageId);
            }
          }
          if (changed) {
            this.messages = [...this.messages];
            this.hasNewMessages = true;
          }
        },
      });
  }

  pollMembers(): void {
    if (!this.roomId) return;

    this.roomService
      .listMembers({
        roomId: this.roomId,
      })
      .subscribe({
        next: (resp) => {
          if (resp.hasError) return;
          this.members = resp.data;
        },
      });
  }

  /**
   * Send a message
   */
  sendMsg() {
    if (!this.currMsg) return;
    if (
      this.messageWebSocketSubject == null ||
      this.messageWebSocketSubject.isStopped
    ) {
      return;
    }

    console.log('Send', this.currMsg);
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
        if (resp.hasError) return;
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
    if (
      this.messageWebSocketSubscrtiption != null &&
      !this.messageWebSocketSubscrtiption.closed
    ) {
      this.messageWebSocketSubscrtiption.unsubscribe();
      this.messageWebSocketSubscrtiption = null;
    }
    if (
      this.messageWebSocketSubject != null &&
      !this.messageWebSocketSubject.closed
    ) {
      this.messageWebSocketSubject.unsubscribe();
      this.messageWebSocketSubject = null;
    }
  }

  scrollToBottom(): void {
    this.virtualScroll.scrollToIndex(this.messages.length - 1);
    this.hasNewMessages = false;
  }
}
