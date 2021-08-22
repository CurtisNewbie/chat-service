package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.vo.MemberVo;
import com.curtisnewbie.service.chat.vo.PollMessageRespVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
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

    /**
     * Send message to other members in the room
     */
    void sendMessage(@NotNull UserVo user, @NotNull String msg);

    /**
     * Add member
     */
    void addMember(@NotNull UserVo userVo);

    /**
     * Remove member
     */
    void removeMember(@NotNull UserVo userVo);

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
     * Refresh expiration
     */
    void refreshExpiration();

    /**
     * Create this room (should only called when this room doesn't exists)
     */
    void create(@NotNull UserVo user);

    /**
     * Check if the room contains specified user
     */
    boolean contains(int userId);
}
