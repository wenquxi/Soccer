package com.worldcup.forum.utils;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词过滤工具（简单实现，生产环境可改用 DFA 算法）
 */
@Component
public class SensitiveWordUtils {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            // 可根据需要扩展敏感词库
    ));

    /**
     * 检查内容是否包含敏感词
     * @return true=包含敏感词
     */
    public boolean containsSensitive(String text) {
        if (text == null || text.isEmpty()) return false;
        String lower = text.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lower.contains(word)) return true;
        }
        return false;
    }

    /**
     * 过滤敏感词（替换为*）
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        String result = text;
        for (String word : SENSITIVE_WORDS) {
            result = result.replaceAll("(?i)" + word, "*".repeat(word.length()));
        }
        return result;
    }
}
