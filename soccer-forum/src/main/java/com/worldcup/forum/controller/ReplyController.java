package com.worldcup.forum.controller;

import com.worldcup.forum.common.Result;
import com.worldcup.forum.dto.request.ReplyCreateRequest;
import com.worldcup.forum.dto.response.ReplyVO;
import com.worldcup.forum.service.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回复控制器
 */
@RestController
@RequestMapping("/api/replies")
public class ReplyController {

    private final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    /**
     * 获取某个帖子的所有回复
     */
    @GetMapping("/post/{postId}")
    public Result<List<ReplyVO>> listByPost(@PathVariable Long postId) {
        return Result.success(replyService.getRepliesByPostId(postId));
    }

    /**
     * 发布回复（含限流+敏感词过滤）
     */
    @PostMapping
    public Result<ReplyVO> create(@Valid @RequestBody ReplyCreateRequest request,
                                  HttpServletRequest httpRequest) {
        return Result.success(replyService.createReply(request, httpRequest));
    }

    /**
     * 管理员删除回复（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        replyService.deleteReply(id);
        return Result.success();
    }
}
