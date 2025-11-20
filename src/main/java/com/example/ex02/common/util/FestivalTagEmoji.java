package com.example.ex02.common.util;

import java.util.Map;

public final class FestivalTagEmoji {
    private FestivalTagEmoji() {}

    // 태그명 → 이모지 매핑
    private static final Map<String, String> TAG_EMOJI_MAP = Map.ofEntries(
            Map.entry("nature", "🌿"),
            Map.entry("night", "🌙"),
            Map.entry("culture", "🎭"),
            Map.entry("food", "🍜"),
            Map.entry("activity", "🎨"),
            Map.entry("children", "👨‍👩‍👧‍👦"),
            Map.entry("season", "❄️")
    );

    public static String getEmoji(String tagName) {
        return TAG_EMOJI_MAP.get(tagName);
    }
}
