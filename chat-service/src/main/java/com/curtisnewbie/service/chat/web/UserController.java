package com.curtisnewbie.service.chat.web;

import com.curtisnewbie.common.util.BeanCopyUtils;
import com.curtisnewbie.common.vo.Result;
import com.curtisnewbie.module.auth.aop.LogOperation;
import com.curtisnewbie.module.auth.util.AuthUtil;
import com.curtisnewbie.service.auth.remote.api.RemoteUserService;
import com.curtisnewbie.service.auth.remote.exception.InvalidAuthenticationException;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.vo.UserCsVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yongjie.zhuang
 */
@RestController
@RequestMapping("${web.base-path}/user")
public class UserController {

    @DubboReference(lazy = true)
    private RemoteUserService userService;

    @LogOperation(name = "/user/info", description = "get user info", enabled = false)
    @GetMapping("/info")
    public Result<UserCsVo> getUserInfo() throws InvalidAuthenticationException {
        // user is not authenticated yet
        if (!AuthUtil.isPrincipalPresent(UserVo.class)) {
            return Result.ok();
        }
        UserVo ue = AuthUtil.getUser();
        return Result.of(BeanCopyUtils.toType(ue, UserCsVo.class));
    }
}
