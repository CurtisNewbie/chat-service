export interface ConnectRoomRequest {
  /** Room's id */
  roomId: string;
}

export interface DisconnectRoomRequest {
  /** Room's id */
  roomId: string;
}

export interface CreateRoomRequest {
  /**
   * Room type
   */
  roomType: RoomType | number;

  /**
   * New room's name
   */
  roomName: string;
}

export enum RoomType {
  /** Private */
  PRIVATE = 1,
  /** Public */
  PUBLIC = 2,
}

export interface ListRoomMemberRequest {
  /** Room's id */
  roomId: string;
}

export interface Room {
  /** Room's id */
  roomId: string;

  /** Room's name */
  roomName: string;
}
