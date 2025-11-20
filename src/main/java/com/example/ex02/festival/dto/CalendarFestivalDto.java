package com.example.ex02.festival.dto;

import com.example.ex02.common.util.FestivalTagEmoji;
import com.example.ex02.festival.entity.FestivalEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class CalendarFestivalDto {

    private Long festivalNo;
    private String title;
    private String addr;
    private LocalDate startDate;
    private LocalDate endDate;
    private String playtime;
    private String state;
    private LocalDate date;                         // 날짜 (yyyy-MM-dd)
    private boolean inMonth;                        // 이번 달에 속하는지 여부
    private List<CalendarFestivalDto> festivals;    // 이 날 진행되는 축제 리스트
    private String colorCode; // 예: "#93c5fd"
    private Integer lane;  // 즐겨찾기 바가 들어갈 줄 번호 (0,1,2,...)

    // 태그 이미지
    private List<String> tagImageUrls;

    // 즐겨찾기 여부 (현재 로그인 사용자 기준)
    private boolean favorite;

    // 구글 캘린더 스타일 바 표시용
    // "SINGLE", "START", "MIDDLE", "END" 중 하나로 쓸 예정
    private String barType;

    // 이 달에서 이 날짜에만 제목을 보여줄지 여부
    private boolean showTitle;

    // 편의를 위한 포맷 문자열
    public String getPeriodText() {
        if (startDate == null || endDate == null) return "";
        return startDate.getMonthValue() + "." + startDate.getDayOfMonth()
                + " ~ " +
                endDate.getMonthValue() + "." + endDate.getDayOfMonth();

    }

    public static CalendarFestivalDto from(FestivalEntity entity, boolean favorite) {
        return from(entity, favorite, Collections.emptyList());
    }

    public static CalendarFestivalDto from(FestivalEntity entity,
                                           boolean favorite,
                                           List<String> tagNames) {

        List<String> tagEmojis = tagNames.stream()
                .map(FestivalTagEmoji::getEmoji)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return CalendarFestivalDto.builder()
                .festivalNo(entity.getFestivalNo())
                .title(entity.getTitle())
                .addr(entity.getAddr())
                .startDate(entity.getEventStartDate().toLocalDate()) // LocalDateTime -> LocalDate
                .endDate(entity.getEventEndDate().toLocalDate())
                .playtime(entity.getPlaytime())
                .state(entity.getState())
                .favorite(favorite)
                .barType("SINGLE")
                .showTitle(true)
                .tagImageUrls(tagEmojis)
                .build();
    }
}
