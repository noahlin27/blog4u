package top.noahlin.astera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.noahlin.astera.common.EntityTypeEnum;
import top.noahlin.astera.model.Comment;
import top.noahlin.astera.model.HostHolder;
import top.noahlin.astera.model.Question;
import top.noahlin.astera.model.ViewObject;
import top.noahlin.astera.service.CommentService;
import top.noahlin.astera.service.LikeService;
import top.noahlin.astera.service.QuestionService;
import top.noahlin.astera.service.UserService;
import top.noahlin.astera.util.JsonUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Resource
    QuestionService questionService;

    @Resource
    CommentService commentService;

    @Resource
    UserService userService;

    @Resource
    LikeService likeService;

    @Resource
    HostHolder hostHolder;

    @PostMapping("/question/add")
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content) {
        try {
            Question question = new Question();
            if (hostHolder.getUser() == null) {
                return JsonUtil.getJSONString(3);
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            question.setTitle(title);
            question.setContent(content);
            question.setCreateTime(new Date());
            question.setCommentCount(0);
            if (questionService.addQuestion(question) > 0) {
                return JsonUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加问题失败" + e.getMessage());
        }
        return JsonUtil.getJSONString(1, "增加问题失败");
    }

    @GetMapping("/question/{questionId}")
    public String questionDetail(HttpServletRequest request,
                           @PathVariable("questionId") int id) {
        ViewObject questionVO = new ViewObject();
        Question question = questionService.getQuestion(id);
        questionVO.set("user", userService.getUser(question.getUserId()));
        questionVO.set("question", question);
        request.setAttribute("questionVO", questionVO);

        List<Comment> comments = commentService.getCommentsByEntity(id, EntityTypeEnum.ENTITY_QUESTION.getTypeId());
        List<ViewObject> vos = new ArrayList<>();
        for (Comment comment: comments){
            ViewObject commentVO = new ViewObject();
            commentVO.set("comment", comment);
            if (hostHolder.getUser()==null){
                commentVO.set("likeStatus", 0);
            }else {
                commentVO.set("likeStatus", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityTypeEnum.ENTITY_COMMENT.getTypeId(), comment.getId()));
            }
            commentVO.set("likeCount", likeService.getLikeCount(EntityTypeEnum.ENTITY_COMMENT.getTypeId(), comment.getId()));
            commentVO.set("user", userService.getUser(comment.getUserId()));
            vos.add(commentVO);
        }
        request.setAttribute("vos", vos);
        return "questionDetail";
    }
}
