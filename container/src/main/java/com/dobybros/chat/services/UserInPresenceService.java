package com.dobybros.chat.services;

import com.dobybros.chat.data.userinpresence.UserInPresence;

import java.util.Collection;

/**
 * Created by zhanjing on 2017/9/12.
 *
 */
public interface UserInPresenceService {

    public UserInPresence getLanServer(String userId);

    public UserInPresence saveIfAbsent(String userId, UserInPresence userInPresence);

    public Long save(String userId, UserInPresence userInPresence);

    public void delete(String userId);

    public void deleteMany(Collection<String> userIds);

}
