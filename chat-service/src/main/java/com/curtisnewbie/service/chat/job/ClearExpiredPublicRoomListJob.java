package com.curtisnewbie.service.chat.job;

import com.curtisnewbie.service.chat.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A scheduled job that removes expired public rooms from the public room list
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
@Component
public class ClearExpiredPublicRoomListJob implements Job {

    @Autowired
    private RoomService roomService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int limit = 50;
        int page = 1;

        Set<String> publicRoomIds = roomService.getPublicRoomIds(page, limit);
        while (!publicRoomIds.isEmpty()) {
            Set<String> toBeRemoved = new HashSet<>();

            // haven't seen this room id, and this room id doesn't exist anymore
            for (String r : publicRoomIds) {
                if (!toBeRemoved.contains(r) && !roomService.roomExists(r)) {
                    toBeRemoved.add(r);
                }
            }

            // remove these rooms in batch
            roomService.removeFromPublicRooms(toBeRemoved);

            log.info("Found {} outdated public rooms, removing them", toBeRemoved.size());

            // next page
            publicRoomIds = roomService.getPublicRoomIds(++page, limit);
        }
    }
}
