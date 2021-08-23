package com.curtisnewbie.service.chat.service.impl;

import com.curtisnewbie.service.auth.remote.vo.UserVo;
import com.curtisnewbie.service.chat.service.Client;
import com.curtisnewbie.service.chat.service.ClientFactory;
import com.curtisnewbie.service.chat.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

/**
 * @author yongjie.zhuang
 */
@Slf4j
@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientFactory clientFactory;

    @Override
    public Client getClient(@NotNull UserVo user) {
        return clientFactory.buildClient(user);
    }
}
