package com.curtisnewbie.service.chat.vo;

import lombok.Data;

/**
 * @author yongjie.zhuang
 */
@Data
public class UserCsVo {

    /** id */
    private Integer id;

    /**
     * username
     */
    private String username;

    /**
     * role
     */
    private String role;
}
