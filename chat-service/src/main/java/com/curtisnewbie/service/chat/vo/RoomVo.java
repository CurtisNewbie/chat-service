package com.curtisnewbie.service.chat.vo;

import lombok.Builder;
import lombok.Data;

/**
 * VO of Room
 *
 * @author yongjie.zhuang
 */
@Data
public class RoomVo {

    /**
     * Room's id
     */
    private String roomId;

    /**
     * Room's name
     */
    private String roomName;

    @Builder
    public RoomVo(String roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    public RoomVo() {
    }
}

