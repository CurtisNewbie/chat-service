package com.curtisnewbie.service.chat.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yongjie.zhuang
 */
@Data
public class ListPublicRoomRespVo {

    private List<RoomVo> rooms;

    private Integer total;

    @Builder
    public ListPublicRoomRespVo(List<RoomVo> rooms, Integer total) {
        this.rooms = rooms;
        this.total = total;
    }

    public ListPublicRoomRespVo() {
    }
}
