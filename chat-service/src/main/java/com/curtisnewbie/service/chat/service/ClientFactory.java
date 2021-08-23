package com.curtisnewbie.service.chat.service;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Factory of Client
 * </p>
 *
 * @author yongjie.zhuang
 */
@Validated
public interface ClientFactory {

    /**
     * Build Client domain object
     *
     * @param user
     */
    Client buildClient(@NotNull UserVo user);

}
