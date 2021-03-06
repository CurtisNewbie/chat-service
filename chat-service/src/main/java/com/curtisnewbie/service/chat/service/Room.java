package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.consts.RoomType;
import com.curtisnewbie.service.chat.vo.MemberVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * A Room domain object
 * </p>
 *
 * @author yongjie.zhuang
 */
@Validated
public interface Room {

    // todo, now the server has adapted web socket, so this isn't used anymore, but it's useful when
    //  we want to support user to view the previous messages in the room, this might be suppored later
    /**
     * Send message to other members in the room
     */
    long sendMessage(@NotNull UserVo user, @NotNull String msg);

    /**
     * Get next message id
     */
    long nextMessageId();

    /**
     * Add member
     */
    void addMember(@NotNull Client client);

    /**
     * Remove member
     */
    void removeMember(@NotNull Client client);

    /**
     * List members
     */
    List<MemberVo> listMembers();

    /**
     * Poll messages after messageId
     *
     * @param messageId messageId
     * @param limit     max number of messages polled
     */
    PollMessageRespVo getMessagesAfter(long messageId, int limit);

    /**
     * Poll last message
     */
    PollMessageRespVo getLastMessage();

    /**
     * Get roomId
     */
    String getRoomId();

    /**
     * Create this room
     */
    void create(@NotNull Client client, @NotNull RoomType roomType, @NotEmpty String roomName);

    /**
     * Get room type
     */
    RoomType getRoomType();

    /**
     * Check if the room contains specified user
     */
    boolean containsUser(int userId);

    /**
     * Check if the room exists
     */
    boolean exists();

    /**
     * Get create date
     *
     * @return create date or null if the room doesn't exist
     */
    Date getCreateDate();

    /**
     * Delete this room
     */
    void delete();
}
