package com.worldcup.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.worldcup.forum.dto.request.PostCreateRequest;
import com.worldcup.forum.dto.response.PostVO;
import com.worldcup.forum.dto.response.ReplyVO;
import com.worldcup.forum.entity.Post;
import com.worldcup.forum.mapper.PostMapper;
import com.worldcup.forum.utils.IpUtils;
import com.worldcup.forum.utils.SensitiveWordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务
 */
@Service
public class PostService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PostMapper postMapper;
    private final ReplyService replyService;
    private final RateLimitService rateLimitService;
    private final SensitiveWordUtils sensitiveWordUtils;

    public PostService(PostMapper postMapper, ReplyService replyService,
                       RateLimitService rateLimitService, SensitiveWordUtils sensitiveWordUtils) {
        this.postMapper = postMapper;
        this.replyService = replyService;
        this.rateLimitService = rateLimitService;
        this.sensitiveWordUtils = sensitiveWordUtils;
    }

    /**
     * 分页查询帖子列表，每帖附带最新5条回复
     */
    public Page<PostVO> getPosts(int page, int size) {
        Page<Post> pageRequest = Page.of(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = postMapper.selectPage(pageRequest, wrapper);

        List<PostVO> voList = postPage.getRecords().stream()
                .map(this::toPostVO)
                .collect(Collectors.toList());

        Page<PostVO> result = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    /**
     * 发布帖子（含限流检查 + 敏感词过滤）
     */
    public PostVO createPost(PostCreateRequest request, HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        // 限流检查
        if (!rateLimitService.tryAcquire(ip)) {
            throw new IllegalArgumentException("操作太频繁，请稍后再试");
        }

        // 敏感词过滤
        String filteredContent = sensitiveWordUtils.filter(request.getContent());

        Post post = new Post();
        post.setNickname(request.getNickname());
        post.setContent(filteredContent);
        post.setIp(ip);
        post.setReplyCount(0);

        postMapper.insert(post);
        return toPostVO(post);
    }

    /**
     * 管理员删除帖子（软删除）
     */
    public void deletePost(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        postMapper.deleteById(id);
    }

    /**
     * 将 Post 实体转为 PostVO（含最新5条回复）
     */
    private PostVO toPostVO(Post post) {
        List<ReplyVO> latestReplies = replyService.getLatestRepliesByPostId(post.getId(), 5);
        return new PostVO(
                post.getId(),
                post.getNickname(),
                post.getContent(),
                post.getReplyCount(),
                post.getCreatedAt() != null ? post.getCreatedAt().format(DTF) : null,
                latestReplies
        );
    }
}
