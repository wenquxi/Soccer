package com.worldcup.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.worldcup.forum.dto.request.ReplyCreateRequest;
import com.worldcup.forum.dto.response.ReplyVO;
import com.worldcup.forum.entity.Post;
import com.worldcup.forum.entity.Reply;
import com.worldcup.forum.mapper.PostMapper;
import com.worldcup.forum.mapper.ReplyMapper;
import com.worldcup.forum.utils.IpUtils;
import com.worldcup.forum.utils.SensitiveWordUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 回复服务
 */
@Service
public class ReplyService {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReplyMapper replyMapper;
    private final PostMapper postMapper;
    private final RateLimitService rateLimitService;
    private final SensitiveWordUtils sensitiveWordUtils;

    public ReplyService(ReplyMapper replyMapper, PostMapper postMapper,
                        RateLimitService rateLimitService, SensitiveWordUtils sensitiveWordUtils) {
        this.replyMapper = replyMapper;
        this.postMapper = postMapper;
        this.rateLimitService = rateLimitService;
        this.sensitiveWordUtils = sensitiveWordUtils;
    }

    /**
     * 发布回复（含限流检查 + 敏感词过滤 + 更新帖子回复数）
     */
    @Transactional(rollbackFor = Exception.class)
    public ReplyVO createReply(ReplyCreateRequest request, HttpServletRequest httpRequest) {
        // 校验帖子存在
        Post post = postMapper.selectById(request.getPostId());
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }

        String ip = IpUtils.getClientIp(httpRequest);
        // 限流检查
        if (!rateLimitService.tryAcquire(ip)) {
            throw new IllegalArgumentException("操作太频繁，请稍后再试");
        }

        // 敏感词过滤
        String filteredContent = sensitiveWordUtils.filter(request.getContent());

        Reply reply = new Reply();
        reply.setPostId(request.getPostId());
        reply.setNickname(request.getNickname());
        reply.setContent(filteredContent);
        reply.setIp(ip);
        replyMapper.insert(reply);

        // 更新帖子的回复计数
        post.setReplyCount(post.getReplyCount() + 1);
        postMapper.updateById(post);

        return toReplyVO(reply);
    }

    /**
     * 管理员删除回复（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Long id) {
        Reply reply = replyMapper.selectById(id);
        if (reply == null) {
            throw new IllegalArgumentException("回复不存在");
        }

        replyMapper.deleteById(id);

        // 更新帖子回复计数
        Post post = postMapper.selectById(reply.getPostId());
        if (post != null && post.getReplyCount() > 0) {
            post.setReplyCount(post.getReplyCount() - 1);
            postMapper.updateById(post);
        }
    }

    /**
     * 获取某帖子的最新N条回复
     */
    public List<ReplyVO> getLatestRepliesByPostId(Long postId, int limit) {
        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<Reply>()
                .eq(Reply::getPostId, postId)
                .orderByDesc(Reply::getCreatedAt)
                .last("LIMIT " + limit);

        return replyMapper.selectList(wrapper).stream()
                .map(this::toReplyVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取某帖子的所有回复（分页）
     */
    public List<ReplyVO> getRepliesByPostId(Long postId) {
        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<Reply>()
                .eq(Reply::getPostId, postId)
                .orderByAsc(Reply::getCreatedAt);

        return replyMapper.selectList(wrapper).stream()
                .map(this::toReplyVO)
                .collect(Collectors.toList());
    }

    private ReplyVO toReplyVO(Reply reply) {
        return new ReplyVO(
                reply.getId(),
                reply.getPostId(),
                reply.getNickname(),
                reply.getContent(),
                reply.getCreatedAt() != null ? reply.getCreatedAt().format(DTF) : null
        );
    }
}
