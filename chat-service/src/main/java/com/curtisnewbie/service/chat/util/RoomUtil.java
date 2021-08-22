package com.curtisnewbie.service.chat.util;

/**
 * <p>
 * Util class for Room
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class RoomUtil {

    private RoomUtil() {
    }

    /**
     * Get lock key for the room
     */
    public static String getRoomLockKey(String roomId) {
        return "room:lock:" + roomId;
    }

    /**
     * Get key for zset for messages of the room
     */
    public static String getMsgScoredMapKey(String roomId) {
        return "room:message:" + roomId;
    }

    /**
     * Get map for information of the room
     */
    public static String getRoomInfoMapKey(String roomId) {
        return "room:info:" + roomId;
    }
}
