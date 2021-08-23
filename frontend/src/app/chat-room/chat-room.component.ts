import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { SocketService } from '../socket.service';
import { WebSocketSubject } from 'rxjs/webSocket';

@Component({
  selector: 'app-chat-room',
  templateUrl: './chat-room.component.html',
  styleUrls: ['./chat-room.component.css'],
})
export class ChatRoomComponent implements OnInit {
  private wss: WebSocketSubject<string> = null;
  username: string;
  roomKey: string;
  chatMsgs: string = '';
  currMsg: string = '';
  members: string[] = [];

  @ViewChild('chatTextArea')
  chatTextArea: ElementRef;

  constructor(private socket: SocketService) {}

  ngOnInit(): void {}

  /**
   * Ask the backend to create a new chat room and fetches the key to this room
   */
  createRoom() {
    this.socket.fetchRoomKey().subscribe({
      next: (v) => {
        this.roomKey = v;
      },
      error: (e) => {
        console.log(e);
      },
      complete: () => {
        this.connectRoom();
      },
    });
  }

  /**
   * Connect to the chat room
   */
  connectRoom() {
    let name = this.username;
    let key = this.roomKey;
    this.wss = this.socket.openWsConn(name, key);
    if (this.wss != null) {
      console.log(`Connected to room: ${key} using ${name}`);
      // subscribe to webSocketSubject
      let subscription = this.wss.subscribe({
        next: (msg: string) => {
          this.chatMsgs += msg + '\n';
          this.scrollTextAreaToBtm();
          // fetch members
          this.fetchMembers();
        },
        error: (err: any) => {
          console.log(err);
        },
        complete: () => {
          alert(
            'Connection is lost, please create a new room or connect to another one.'
          );
          subscription.unsubscribe();
          this.wss = null;
        },
      });
    }
  }

  /**
   * Send a message
   */
  sendMsg() {
    if (this.wss != null) {
      this.wss.next(this.currMsg);
      this.currMsg = '';
      this.scrollTextAreaToBtm();
    } else {
      alert("You haven't connected to any room, try create or connect one");
    }
  }

  /**
   * Fetch room members
   */
  fetchMembers() {
    this.socket.fetchRoomMember().subscribe({
      next: (val: string[]) => {
        this.members = val;
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
