package com.example.ex02.festival.service;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.entity.FavoriteEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalTagEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.festival.repository.FestivalTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final FestivalRepository festivalRepository;
    private final FestivalTagRepository festivalTagRepository;
    private final FavoriteRepository favoriteRepository;

    private static final String[] FAVORITE_PALETTE = {
            "#93c5fd", // 파랑
            "#f9a8d4", // 핑크
            "#6ee7b7", // 민트
            "#facc15", // 노랑
            "#fda4af", // 살구
            "#a5b4fc"  // 보라
    };

    // 즐겨찾기 막대 배치를 위한 내부 클래스
    private static class FavInterval {
        FestivalEntity fest;
        LocalDate visibleStart;
        LocalDate visibleEnd;
    }

    // 사용자 기준으로 최근 즐겨찾기 5개 축제 번호 Set
    private Set<Long> findRecentFavoriteFestivalIds(Long userNo) {
        if (userNo == null) {
            return Collections.emptySet();
        }

        List<FavoriteEntity> favorites =
                favoriteRepository.findTop5ByMember_UserNoOrderByFavoriteDateDesc(userNo);

        return favorites.stream()
                .map(f -> f.getFestival().getFestivalNo())
                .collect(Collectors.toSet());
    }

    // 켈린더에 들어갈 데이터 가져오기
    public Map<LocalDate, List<CalendarFestivalDto>> getCalendar(int year,
                                                                 int month,
                                                                 String region,
                                                                 Long userNo) {

        // 1) 월 범위 계산
        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime   = monthEnd.atTime(23, 59, 59);

        // 2) 즐겨찾기 축제 번호 조회
        Set<Long> favoriteIdSet = findFavoriteFestivalIds(userNo);
        Set<Long> recentFavoriteIdSet = findRecentFavoriteFestivalIds(userNo);

        // 3) 월에 걸치는 축제 조회
        List<FestivalEntity> festivals = festivalRepository.findFestivalsForMonth(
                startDateTime, endDateTime, region
        );

        // 4) 축제별 태그 맵 조회
        Map<Long, List<String>> tagNamesByFestival = loadTagNamesByFestival(festivals);

        // 5) 월 전체 날짜 Map 초기화
        Map<LocalDate, List<CalendarFestivalDto>> calendarMap =
                createEmptyCalendarMap(monthStart, monthEnd);

        // 6) 즐겨찾기 색상 매핑
        Map<Long, String> colorByFestival =
                assignFavoriteColors(festivals, favoriteIdSet);

        // 7) 즐겨찾기 축제의 월 기준 구간 + 레인 계산
        List<FavInterval> favIntervals =
                buildFavoriteIntervals(festivals, favoriteIdSet, monthStart, monthEnd);

        Map<Long, Integer> laneByFestival =
                assignFavoriteLanes(favIntervals);

        // 8) 축제들을 날짜별로 쪼개서 캘린더에 채우기
        fillCalendarWithFestivals(calendarMap,
                festivals,
                favoriteIdSet,
                tagNamesByFestival,
                colorByFestival,
                laneByFestival,
                monthStart,
                monthEnd);

        // 9) 날짜별 lane 기준 정렬
        sortCalendarByLane(calendarMap);

        return calendarMap;
    }   // getCalendar

    /* ===================== 헬퍼 메서드들 ===================== */

    // 즐겨찾기한 축제 id(userNo) 가져오기
    private Set<Long> findFavoriteFestivalIds(Long userNo) {
        if (userNo == null) {
            return Collections.emptySet();
        }
        List<Long> favIds = favoriteRepository.findFestivalNosByMember(userNo);
        return new HashSet<>(favIds);
    }

    // 축제의 태그 명 가져오기
    private Map<Long, List<String>> loadTagNamesByFestival(List<FestivalEntity> festivals) {
        if (festivals.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> festNos = festivals.stream()
                .map(FestivalEntity::getFestivalNo)
                .toList();

        List<FestivalTagEntity> allTags =
                festivalTagRepository.findByFestival_FestivalNoIn(festNos);

        return allTags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getFestival().getFestivalNo(),
                        Collectors.mapping(FestivalTagEntity::getTag, Collectors.toList())
                ));
    }

    // 켈린더 날짜별 들어갈 데이터를 리스트 형태로 저장
    private Map<LocalDate, List<CalendarFestivalDto>> createEmptyCalendarMap(LocalDate monthStart,
                                                                             LocalDate monthEnd) {
        Map<LocalDate, List<CalendarFestivalDto>> calendarMap = new LinkedHashMap<>();
        for (LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
            calendarMap.put(d, new ArrayList<>());
        }
        return calendarMap;
    }

    // 즐찾 하이라이팅 색
    private Map<Long, String> assignFavoriteColors(List<FestivalEntity> festivals,
                                                   Set<Long> favoriteIdSet) {
        Map<Long, String> colorByFestival = new HashMap<>();
        int colorIdx = 0;

        for (FestivalEntity fest : festivals) {
            Long festNo = fest.getFestivalNo();
            if (favoriteIdSet.contains(festNo)) {
                colorByFestival.put(festNo,
                        FAVORITE_PALETTE[colorIdx % FAVORITE_PALETTE.length]);
                colorIdx++;
            }
        }
        return colorByFestival;
    }

    // 즐겨찾기 축제의 시작일/종료일 저장하고 즐찾 목록들 정렬
    private List<FavInterval> buildFavoriteIntervals(List<FestivalEntity> festivals,
                                                     Set<Long> favoriteIdSet,
                                                     LocalDate monthStart,
                                                     LocalDate monthEnd) {

        List<FavInterval> favIntervals = new ArrayList<>();

        for (FestivalEntity fest : festivals) {
            Long festNo = fest.getFestivalNo();
            if (!favoriteIdSet.contains(festNo)) continue;  // 즐겨찾기만

            LocalDate festStart = fest.getEventStartDate().toLocalDate();
            LocalDate festEnd   = fest.getEventEndDate().toLocalDate();

            if (festEnd.isBefore(monthStart) || festStart.isAfter(monthEnd)) continue;

            LocalDate visibleStart = festStart.isBefore(monthStart) ? monthStart : festStart;
            LocalDate visibleEnd   = festEnd.isAfter(monthEnd) ? monthEnd : festEnd;

            FavInterval fi = new FavInterval();
            fi.fest = fest;
            fi.visibleStart = visibleStart;
            fi.visibleEnd = visibleEnd;

            favIntervals.add(fi);
        }

        // 시작일 오름차순 + 종료일 내림차순 정렬
        favIntervals.sort(
                Comparator
                        .comparing((FavInterval it) -> it.visibleStart)
                        .thenComparing(it -> it.visibleEnd, Comparator.reverseOrder())
        );

        return favIntervals;
    }

    // 축제 하이라이팅 레인 배정 (계단식으로 깨지는 것 방지)
    private Map<Long, Integer> assignFavoriteLanes(List<FavInterval> favIntervals) {
        List<LocalDate> laneEnd = new ArrayList<>();         // 레인별 마지막 날짜
        Map<Long, Integer> laneByFestival = new HashMap<>(); // festivalNo -> lane

        for (FavInterval interval : favIntervals) {
            int lane = 0;

            // 현재 축제 시작일과 겹치지 않는 가장 낮은 레인 찾기
            while (lane < laneEnd.size()
                    && !laneEnd.get(lane).isBefore(interval.visibleStart)) {
                lane++;
            }

            if (lane == laneEnd.size()) {
                laneEnd.add(interval.visibleEnd);
            } else {
                laneEnd.set(lane, interval.visibleEnd);
            }

            laneByFestival.put(interval.fest.getFestivalNo(), lane);
        }

        return laneByFestival;
    }

    // 즐겨찾기한 축제만 날짜 위에 출력 (여러 기간에 걸친 축제는 축제 명 한번만)
    private void fillCalendarWithFestivals(
            Map<LocalDate, List<CalendarFestivalDto>> calendarMap,
            List<FestivalEntity> festivals,
            Set<Long> favoriteIdSet,
            Map<Long, List<String>> tagNamesByFestival,
            Map<Long, String> colorByFestival,
            Map<Long, Integer> laneByFestival,
            LocalDate monthStart,
            LocalDate monthEnd
    ) {
        for (FestivalEntity fest : festivals) {

            Long festNo = fest.getFestivalNo();
            boolean favorite = favoriteIdSet.contains(festNo);

            LocalDate festStart = fest.getEventStartDate().toLocalDate();
            LocalDate festEnd   = fest.getEventEndDate().toLocalDate();

            if (festEnd.isBefore(monthStart) || festStart.isAfter(monthEnd)) {
                continue;
            }

            LocalDate visibleStart = festStart.isBefore(monthStart) ? monthStart : festStart;
            LocalDate visibleEnd   = festEnd.isAfter(monthEnd) ? monthEnd : festEnd;

            Integer lane = favorite ? laneByFestival.getOrDefault(festNo, 999) : 999;

            List<String> tagNames =
                    tagNamesByFestival.getOrDefault(festNo, List.of());

            for (LocalDate d = visibleStart; !d.isAfter(visibleEnd); d = d.plusDays(1)) {
                CalendarFestivalDto dto = CalendarFestivalDto.from(fest, favorite, tagNames);
                dto.setStartDate(festStart);
                dto.setEndDate(festEnd);

                if (favorite) {
                    dto.setColorCode(colorByFestival.get(festNo));
                    dto.setLane(lane);

                    if (visibleStart.equals(visibleEnd)) {
                        dto.setBarType("SINGLE");
                        dto.setShowTitle(true);
                    } else if (d.equals(visibleStart)) {
                        dto.setBarType("START");
                        dto.setShowTitle(true);
                    } else if (d.equals(visibleEnd)) {
                        dto.setBarType("END");
                        dto.setShowTitle(false);
                    } else {
                        dto.setBarType("MIDDLE");
                        dto.setShowTitle(false);
                    }
                } else {
                    dto.setBarType(null);
                    dto.setShowTitle(false);
                    dto.setColorCode(null);
                    dto.setLane(999);
                }

                calendarMap
                        .computeIfAbsent(d, k -> new ArrayList<>())
                        .add(dto);
            }
        }
    }

    // 즐겨찾기 하이라이팅을 위한 sorting
    private void sortCalendarByLane(Map<LocalDate, List<CalendarFestivalDto>> calendarMap) {
        for (List<CalendarFestivalDto> list : calendarMap.values()) {
            list.sort(Comparator.comparingInt(
                    dto -> dto.getLane() == null ? 999 : dto.getLane()
            ));
        }
    }
    //=============================================================//

    // 켈린더 그리드 생성 및 getCalendar를 바탕으로 데이터 채워넣기 (실질적인 켈린더 빌더)
    public List<List<CalendarFestivalDto>> buildCalendar(int year, int month, String region,Long userNo) {

        Map<LocalDate, List<CalendarFestivalDto>> calendarMap = getCalendar(year, month, region,userNo);

        // 2) 이번 달 1일과 요일 정보
        LocalDate firstDay = LocalDate.of(year, month, 1);

        // 1(월)~7(일)
        int dayOfWeekValue = firstDay.getDayOfWeek().getValue();

        // 달력 시작을 "일요일" 기준으로 맞추기 위해 며칠을 빼야 하는지
        int offset = dayOfWeekValue % 7;   // 월(1)→1, 화(2)→2, ..., 일(7)→0

        // 3) 실제 달력에서 맨 첫 칸에 들어갈 날짜 (이전 달 포함)
        LocalDate startDate = firstDay.minusDays(offset);

        // 4) 6주 × 7일 그리드 생성
        List<List<CalendarFestivalDto>> weeks = new ArrayList<>();
        LocalDate current = startDate;

        for (int w = 0; w < 6; w++) {
            List<CalendarFestivalDto> week = new ArrayList<>();
            for (int d = 0; d < 7; d++) {

                boolean inMonth = (current.getMonthValue() == month);

                // 이번 달 날짜면 calendarMap에서 축제 가져오고, 아니면 빈 리스트
                List<CalendarFestivalDto> festivals =
                        inMonth ? calendarMap.getOrDefault(current, Collections.emptyList())
                                : Collections.emptyList();

                CalendarFestivalDto dayDto = CalendarFestivalDto.builder()
                        .date(current)
                        .inMonth(inMonth)
                        .festivals(festivals)

                        .build();

                week.add(dayDto);
                current = current.plusDays(1);
            }
            weeks.add(week);
        }

        return weeks;
    }   // buildCalendar


}
