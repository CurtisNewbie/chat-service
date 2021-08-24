package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;

/**
 * <p>
 * Client domain object (think of it as a user, not using the name 'user' is to avoid confusion)
 * </p>
 *
 * @author yongjie.zhuang
 */
public interface Client {

    /**
     * Get room id that the client is in
     */
    String getRoomId();

    /**
     * Clear all room id
     */
    void clearRoomId();

    /**
     * Record room that the client has joined
     */
    void addRoomId(String roomId);

    /**
     * Get user, which is who this client object represents
     */
    UserVo getUser();

}
