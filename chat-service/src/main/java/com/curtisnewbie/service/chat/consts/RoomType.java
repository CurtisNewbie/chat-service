package com.curtisnewbie.service.chat.consts;

import com.curtisnewbie.common.enums.IntEnum;

/**
 * <p>
 * Room Type
 * </p>
 *
 * @author yongjie.zhuang
 */
public enum RoomType implements IntEnum {

    /**
     * 1-Private room
     */
    PRIVATE(1),

    /**
     * 2-Public room
     */
    PUBLIC(2);

    private final int value;

    RoomType(int v) {
        this.value = v;
    }

    @Override
    public int getValue() {
        return this.value;
    }
}
