package com.codeahoy.alluvium.server.user;

import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author umansoor
 */
@Component
public class Registry {
    /**
     * A map for keeping {@link ChannelId} to {@link User} mapping.
     */
    private final Map<ChannelId, User> channelIdUserMap = new ConcurrentHashMap<>();

    /**
     * A map for keeping {@link ChannelId} to {@link User} mapping.
     */
    private final Map<String, User> idUserMap = new ConcurrentHashMap<>();

    public Optional<User> getUserByChannelId(ChannelId channelId) {
         return Optional.ofNullable(channelIdUserMap.get(channelId));
    }

    public int totalUsers() {
        return channelIdUserMap.size();
    }

    public int totalIdentifiedUsers() {
        return idUserMap.size();
    }

    public Optional<User> getUserById(String id) {
        return Optional.ofNullable(idUserMap.get(id));
    }

    public void addUser(ChannelId channelId, User user) {
        channelIdUserMap.put(channelId, user);
    }

    /**
     * This removes users from both
     * @param channelId
     */
    public synchronized void removeByChannelId(ChannelId channelId) {
        User user = getUserByChannelId(channelId).orElseThrow(IllegalStateException::new);
        if (user.id() != null) {
            idUserMap.remove(user.id());
        }

        channelIdUserMap.remove(channelId);
    }

}