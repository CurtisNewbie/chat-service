import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Message } from './models/Message';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class SocketService {
  constructor(private notifi: NotificationService) {}

  /**
   * Open websocket connection for messages
   *
   */
  openMessageWebSocket(): WebSocketSubject<Message> {
    let wss: WebSocketSubject<Message> = webSocket({
      url: `ws://localhost:8081/socket/messages`,
      openObserver: {
        next(event) {
          console.log('Message web socket opened');
        },
      },
      // deserializer: (msg) => msg.data,
      closeObserver: {
        next(closeEvent) {
          console.log(
            `CloseEvent_ Code: ${closeEvent.code}, Reason: ${closeEvent.reason}`
          );
          if (closeEvent.code == 1006) {
            console.log(
              'Failed to establish connection, something might have gone wrong.'
            );
          }
        },
      },
    });
    return wss;
  }
}
