import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import { WebSocketSubject } from 'rxjs/webSocket';
import { Member } from '../models/Member';
import { RoomService } from '../room.service';
import { RoomType } from '../models/Room';
import { NotificationService } from '../notification.service';
import { Message, PollMessageResponse } from '../models/Message';

@Component({
  selector: 'app-chat-room',
  templateUrl: './chat-room.component.html',
  styleUrls: ['./chat-room.component.css'],
})
export class ChatRoomComponent implements OnInit, OnDestroy {
  private wss: WebSocketSubject<string> = null;
  username: string;
  roomId: string;
  chatMsgs: string = '';
  currMsg: string = '';
  isConnected: boolean = false;
  members: Member[] = [];
  messages: Message[] = [];
  msgIdSet: Set<number> = new Set();
  pollMsgInterval = null;

  @ViewChild('chatTextArea')
  chatTextArea: ElementRef;

  constructor(
    private roomService: RoomService,
    private notifi: NotificationService
  ) {}

  ngOnInit(): void {
    // poll messages for every 0.5 seconds
    this.pollMsgInterval = setInterval(() => this.pollMessages(), 500);
  }

  ngOnDestroy(): void {
    clearInterval(this.pollMsgInterval);
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
        limit: 10,
      })
      .subscribe({
        next: (resp) => {
          let mp: PollMessageResponse = resp.data;
          for (let m of mp.messages) {
            if (!this.msgIdSet.has(m.messageId)) {
              this.messages.push(m);
            }
          }
        },
      });
  }

  /**
   * Send a message
   */
  sendMsg() {
    if (!this.isConnected) return;

    this.roomService
      .sendMessage({
        roomId: this.roomId,
        message: this.currMsg,
      })
      .subscribe({
        complete: () => {
          this.notifi.toast('Message sent');
          this.scrollTextAreaToBtm();
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

  /**
   * Scroll the textarea to its bottom
   */
  private scrollTextAreaToBtm() {
    let textArea: HTMLTextAreaElement = this.chatTextArea.nativeElement;
    textArea.scrollTop = textArea.scrollHeight;
  }
}
