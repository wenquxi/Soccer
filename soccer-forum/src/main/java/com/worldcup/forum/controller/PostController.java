/** 世界杯论坛 - 帖子控制器 */
package com.worldcup.forum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.worldcup.forum.common.Result;
import com.worldcup.forum.dto.request.PostCreateRequest;
import com.worldcup.forum.dto.response.PostVO;
import com.worldcup.forum.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 获取帖子列表（分页，每帖带最新5条回复）
     */
    @GetMapping
    public Result<Page<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return Result.success(postService.getPosts(page, size));
    }

    /**
     * 发布帖子（含限流+敏感词过滤）
     */
    @PostMapping
    public Result<PostVO> create(@Valid @RequestBody PostCreateRequest request,
                                 HttpServletRequest httpRequest) {
        return Result.success(postService.createPost(request, httpRequest));
    }

    /**
     * 管理员删除帖子（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success();
    }
}
