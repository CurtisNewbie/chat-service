package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Client service (for users)
 * </p>
 *
 * @author yongjie.zhuang
 */
public interface ClientService {

    /**
     * Get client
     */
    Client getClient(@NotNull UserVo user);

}
