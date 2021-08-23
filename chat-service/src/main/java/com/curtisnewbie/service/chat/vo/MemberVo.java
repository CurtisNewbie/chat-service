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
@Builder
public class MemberVo {

    /** primary key */
    private Integer id;

    /** username (must be unique) */
    private String username;

}
