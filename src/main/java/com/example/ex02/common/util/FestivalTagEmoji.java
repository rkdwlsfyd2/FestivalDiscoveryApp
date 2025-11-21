package com.example.ex02.common.util;

import java.util.Map;

public final class FestivalTagEmoji {
    private FestivalTagEmoji() {}

    // 태그명 → 이모지 매핑
    private static final Map<String, String> TAG_EMOJI_MAP = Map.ofEntries(
            Map.entry("자연", "🌿"),
            Map.entry("야간", "🌙"),
            Map.entry("문화", "🎭"),
            Map.entry("먹거리", "🍜"),
            Map.entry("체험", "🎨"),
            Map.entry("아동", "\uD83D\uDC66"),
            Map.entry("계절", "❄️")
    );

    public static String getEmoji(String tagName) {
        return TAG_EMOJI_MAP.get(tagName);
    }
}
