import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HEADERS } from './models/Headers';
import { Member } from './models/Member';
import {
  PollMessageRequest,
  PollMessageResponse,
  SendMessageRequest,
} from './models/Message';
import { Paging } from './models/paging';
import { Resp } from './models/resp';
import {
  ConnectRoomRequest,
  CreateRoomRequest,
  DisconnectRoomRequest,
  ListRoomMemberRequest,
  Room,
} from './models/Room';
import { buildApiPath } from './util/api-util';

@Injectable({
  providedIn: 'root',
})
export class RoomService {
  roomId: string = null;
  isConnected: boolean = false;

  constructor(private http: HttpClient) {}

  /**
   * Connect to room
   */
  public connectRoom(param: ConnectRoomRequest): Observable<Resp<void>> {
    return this.http.post<Resp<void>>(
      buildApiPath('/room/connect'),
      param,
      HEADERS
    );
  }

  /**
   * Connect to room
   */
  public disconnectRoom(param: DisconnectRoomRequest): Observable<Resp<void>> {
    return this.http.post<Resp<void>>(
      buildApiPath('/room/disconnect'),
      param,
      HEADERS
    );
  }

  /**
   * Create new room
   * @param param
   * @returns roomId
   */
  public createNewRoom(param: CreateRoomRequest): Observable<Resp<string>> {
    return this.http.post<Resp<string>>(
      buildApiPath('/room/new'),
      param,
      HEADERS
    );
  }

  /**
   * Send message
   * @param param
   * @returns roomId
   */
  public sendMessage(param: SendMessageRequest): Observable<Resp<void>> {
    return this.http.post<Resp<void>>(
      buildApiPath('/room/message/post'),
      param,
      HEADERS
    );
  }

  /**
   * List members in room
   */
  public listMembers(param: ListRoomMemberRequest): Observable<Resp<Member[]>> {
    return this.http.post<Resp<Member[]>>(
      buildApiPath('/room/members'),
      param,
      HEADERS
    );
  }

  /**
   * Poll messages in room
   */
  public pollMessages(
    param: PollMessageRequest
  ): Observable<Resp<PollMessageResponse>> {
    return this.http.post<Resp<PollMessageResponse>>(
      buildApiPath('/room/message/poll'),
      param,
      HEADERS
    );
  }

  /**
   * List public rooms
   */
  public listPublicRooms(paging: Paging) {
    return this.http.post<Resp<{ rooms: Room[]; total: number }>>(
      buildApiPath('/room/public/list'),
      paging,
      HEADERS
    );
  }
}
