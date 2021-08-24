package com.curtisnewbie.service.chat.job;

import com.curtisnewbie.service.chat.exceptions.RoomNotFoundException;
import com.curtisnewbie.service.chat.service.Room;
import com.curtisnewbie.service.chat.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.curtisnewbie.common.util.DateUtils.localDateTimeOf;

/**
 * <p>
 * A scheduled job as a backup approach, which removes rooms that are supposed to be expired
 * </p>
 * <p>
 * Ideally, this kind of job should only be run at mid night.
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class ClearRoomJob implements Job {

    private static final String ROOM_INFO_KEY_PATTERN = "chat:room:info:*";
    private static final String ROOM_INFO_KEY_PREFIX = "chat:room:info:";

    @Autowired
    private RoomService roomService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Set<String> keySet = new HashSet<>();
        // delete rooms that are created 1 day ago
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        RKeys rKeys = redissonClient.getKeys();
        Iterator<String> iter = rKeys.getKeysWithLimit(ROOM_INFO_KEY_PATTERN, 50).iterator();
        while (iter.hasNext()) {
            final String roomInfoKey = iter.next();

            if (keySet.contains(roomInfoKey)) {
                continue;
            }
            keySet.add(roomInfoKey);

            final String roomId = stripOffPrefix(roomInfoKey);
            try {
                Room room = roomService.getRoom(roomId);
                final Date createDate = room.getCreateDate();
                if (localDateTimeOf(createDate).isBefore(yesterday)) {
                    room.delete();
                    log.info("Deleted room: {}, created at: {}", room.getRoomId(), room.getCreateDate());
                }
            } catch (RoomNotFoundException e) {
                continue;
            }
        }
    }

    private String stripOffPrefix(String roomInfoMapKey) {
        return roomInfoMapKey.substring(ROOM_INFO_KEY_PREFIX.length());
    }

}
