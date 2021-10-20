package top.noahlin.astera.async.handler;

import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.stereotype.Component;
import top.noahlin.astera.async.Event;
import top.noahlin.astera.async.EventHandler;
import top.noahlin.astera.async.EventType;
import top.noahlin.astera.common.EntityType;
import top.noahlin.astera.model.Feed;
import top.noahlin.astera.model.Question;
import top.noahlin.astera.model.User;
import top.noahlin.astera.service.FeedService;
import top.noahlin.astera.service.QuestionService;
import top.noahlin.astera.service.UserService;

import javax.annotation.Resource;
import java.util.*;

@Component
public class FeedHandler implements EventHandler {
    @Resource
    UserService userService;

    @Resource
    QuestionService questionService;

    @Resource
    FeedService feedService;

    @Override
    public void handle(Event event) {
        Feed feed = new Feed();
        feed.setType(event.getType().getValue());
        feed.setUserId(event.getActorId());
        feed.setCreateTime(new Date());
        feed.setData(buildData(event));
        if (feed.getData() == null){
            return;
        }
        feedService.addFeed(feed);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.COMMENT, EventType.FOLLOW);
    }

    private String buildData(Event event){
        Map<String, String> map = new HashMap<>();
        User actor = userService.getUser(event.getActorId());
        if (actor == null) {
            return null;
        }
        map.put("userId", String.valueOf(actor.getId()));
        map.put("userHead", actor.getHeadUrl());
        map.put("userName", actor.getName());
        if (event.getType() == EventType.COMMENT ||
            event.getType() == EventType.FOLLOW &&
            event.getEntityType() == EntityType.ENTITY_QUESTION.getTypeId()) {
            Question question = questionService.getQuestion(event.getEntityId());
            if(question == null){
                return null;
            }
            map.put("questionId", String.valueOf(question.getUserId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }
}
