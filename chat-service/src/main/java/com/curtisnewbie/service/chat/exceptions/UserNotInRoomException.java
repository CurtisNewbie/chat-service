package com.curtisnewbie.service.chat.exceptions;

/**
 * <p>
 * User is not in room
 * </p>
 *
 * @author yongjie.zhuang
 */
public class UserNotInRoomException extends Exception {

    public UserNotInRoomException() {
    }

    public UserNotInRoomException(String message) {
        super(message);
    }

    public UserNotInRoomException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotInRoomException(Throwable cause) {
        super(cause);
    }

    public UserNotInRoomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
