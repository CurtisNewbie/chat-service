package com.curtisnewbie.service.chat.vo;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * Member
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class MemberVo {

    /** primary key */
    private Integer id;

    /** username (must be unique) */
    private String username;

    public MemberVo(){}

    @Builder
    public MemberVo(Integer id, String username) {
        this.id = id;
        this.username = username;
    }
}
