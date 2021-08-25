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

    /**
     * Created by
     */
    private String createdBy;

    @Builder
    public RoomVo(String roomId, String roomName, String createdBy) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.createdBy = createdBy;
    }

    public RoomVo() {
    }
}

