package com.curtisnewbie.service.chat.service;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Builder of Room
 * </p>
 *
 * @author yongjie.zhuang
 */
@Validated
public interface RoomBuilder {

    /**
     * Build room domain object
     *
     * @param roomId roomId
     */
    Room buildRoom(@NotNull String roomId);

}
