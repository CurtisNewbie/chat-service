package com.curtisnewbie.service.chat.service;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Factory of Room
 * </p>
 *
 * @author yongjie.zhuang
 */
@Validated
public interface RoomFactory {

    /**
     * Build room domain object
     *
     * @param roomId roomId
     */
    Room buildRoom(@NotNull String roomId);

}
