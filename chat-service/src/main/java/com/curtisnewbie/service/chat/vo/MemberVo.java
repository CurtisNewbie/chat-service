package com.curtisnewbie.service.chat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Member
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberVo {

    /** primary key */
    private Integer id;

    /** username (must be unique) */
    private String username;

}
