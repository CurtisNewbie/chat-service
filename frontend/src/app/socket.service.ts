import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CONFIG } from 'src/environments/config';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable } from 'rxjs';

const FETCH_KEY_URL = `http://${CONFIG.host}:${CONFIG.port}/api/room/key`;
const FETCH_MEMBER_URL = `http://${CONFIG.host}:${CONFIG.port}/api/room/key`;

@Injectable({
  providedIn: 'root',
})
export class SocketService {
  private roomKey: string;

  constructor(private http: HttpClient) {}

  /**
   * Fetch the key of a new room from backend for websocket connection
   */
  fetchRoomKey(): Observable<string> {
    return this.http.get(FETCH_KEY_URL, { responseType: 'text' });
  }

  /**
   * Fetch the name of the members in the room
   */
  fetchRoomMember(): Observable<Object> {
    if (this.roomKey) {
      return this.http.get(`${FETCH_MEMBER_URL}/${this.roomKey}/members`, {
        responseType: 'json',
      });
    }
  }

  /**
   * Open websocket connection and return an Observable of string
   *
   * @returns WebSocketSubject of string or NULL if either the username or the roomKey is null or empty
   */
  openWsConn(username: string, roomKey: string): WebSocketSubject<string> {
    if (username && roomKey) {
      this.roomKey = roomKey;
      let wss: WebSocketSubject<string> = webSocket({
        url: `ws://${CONFIG.host}:${CONFIG.port}/chat/room/${roomKey}/name/${username}`,
        deserializer: (msg) => msg.data,
        closeObserver: {
          next(closeEvent) {
            console.log(
              `CloseEvent_ Code: ${closeEvent.code}, Reason: ${closeEvent.reason}`
            );
            if (closeEvent.code == 1006)
              alert(
                "Failed to establish connection. Same username may have been used or the room doesn't exist."
              );
          },
        },
      });
      return wss;
    } else {
      return null;
    }
  }
}
