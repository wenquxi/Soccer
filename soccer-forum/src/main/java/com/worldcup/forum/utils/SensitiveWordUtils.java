/** 世界杯论坛 - 敏感词过滤工具 */
package com.worldcup.forum.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词过滤工具（简单实现，生产环境可改用 DFA 算法）
 */
@Component
public class SensitiveWordUtils {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordUtils.class);

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            // 可根据需要扩展敏感词库

    ));

    /**
     * 检查内容是否包含敏感词
     * @return true=包含敏感词
     */
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        try {
            String lower = text.toLowerCase();
            for (String word : SENSITIVE_WORDS) {
                if (lower.contains(word)) return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("敏感词检测异常，按未命中处理: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 过滤敏感词（替换为*）
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        try {
            String result = text;
            for (String word : SENSITIVE_WORDS) {
                result = result.replaceAll("(?i)" + java.util.regex.Pattern.quote(word), "*".repeat(word.length()));
            }
            return result;
        } catch (Exception e) {
            log.warn("敏感词过滤异常，返回原文: {}", e.getMessage());
            return text;
        }
    }
}
