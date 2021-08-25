export interface Message {
  /** Sender's id */
  sender: string;
  /** Message */
  message: string;
  /** Message's id */
  messageId: number;
  /** Message Date */
  dateSent?: Date;
}

export interface MessageVo {
  /** Sender's id */
  sender: string;
  /** Message */
  message: string;
  /** Message's id */
  messageId: number;
  /** Message Date */
  dateSent?: number;
}

export interface SendMessageRequest {
  /** Room's id */
  roomId: string;
  /** Message */
  message: string;
}

export interface PollMessageRequest {
  /** Room's id */
  roomId: string;
  /** Id of last message */
  lastMessageId: number;
  /** Limit */
  limit: number;
}

export interface PollMessageResponse {
  /**
   * Messages
   */
  messages: Message[];

  /**
   * Has more messages to poll
   */
  hasMore: boolean;
}
