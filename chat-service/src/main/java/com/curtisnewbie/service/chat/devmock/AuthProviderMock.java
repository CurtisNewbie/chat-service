package com.curtisnewbie.service.chat.devmock;

import com.curtisnewbie.module.auth.config.AuthProvider;
import com.curtisnewbie.service.auth.remote.consts.UserRole;
import com.curtisnewbie.service.auth.remote.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * <p>
 * Mock bean for {@link AuthProvider}, only active under 'dev' profile for development.
 * </p>
 * <p>
 * This bean is used for development, such that the auth-service doesn't need to startup for it to work.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Profile("dev")
@Primary
@Component
public class AuthProviderMock extends AuthProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.isAuthenticated())
            return authentication;

        UserVo uv = new UserVo();
        uv.setId(3);
        uv.setUsername("zhuangyongj");
        uv.setRole(UserRole.ADMIN.getValue());

        log.info("Dev profile mock - authenticated user: {}", uv.toString());

        return new UsernamePasswordAuthenticationToken(uv,
                "123456",
                Arrays.asList(new SimpleGrantedAuthority(uv.getRole())));
    }
}
