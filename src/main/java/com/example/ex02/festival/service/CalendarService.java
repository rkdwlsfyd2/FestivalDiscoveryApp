package com.example.ex02.festival.service;

import com.example.ex02.festival.dto.CalendarFestivalDto;
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

    public Map<LocalDate, List<CalendarFestivalDto>> getCalendar(int year, int month, String region, Long userNo) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd   = ym.atEndOfMonth();

        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime   = monthEnd.atTime(23, 59, 59);

        // 0) 즐겨찾기 id 셋
        Set<Long> favoriteIdSet = Collections.emptySet();
        if (userNo != null) {
            List<Long> favIds = favoriteRepository.findFestivalNosByMember(userNo);
            favoriteIdSet = new HashSet<>(favIds);
        }

        // 1) 해당 월에 걸치는 축제 전체 조회
        List<FestivalEntity> festivals = festivalRepository.findFestivalsForMonth(
                startDateTime, endDateTime, region
        );

        // 🔹 1) 이번 달 축제 번호들
        List<Long> festNos = festivals.stream()
                .map(FestivalEntity::getFestivalNo)
                .toList();

        // 🔹 2) 태그 전체 한 번에 조회
        List<FestivalTagEntity> allTags = festivalTagRepository.findByFestival_FestivalNoIn(festNos);

        // 🔹 3) festivalNo → List<tagName> 맵 만들기
        Map<Long, List<String>> tagNamesByFestival = allTags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getFestival().getFestivalNo(),
                        Collectors.mapping(FestivalTagEntity::getTag, Collectors.toList())
                ));

        // 2) 월 전체 날짜를 미리 Map으로 생성
        Map<LocalDate, List<CalendarFestivalDto>> calendarMap = new LinkedHashMap<>();
        for (LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
            calendarMap.put(d, new ArrayList<>());
        }

        // todo 3) 즐겨찾기 축제별 색상 매핑
        String[] palette = {
                "#93c5fd", // 파랑
                "#f9a8d4", // 핑크
                "#6ee7b7", // 민트
                "#facc15", // 노랑
                "#fda4af", // 살구
                "#a5b4fc"  // 보라
        };
        Map<Long, String> colorByFestival = new HashMap<>();
        int colorIdx = 0;
        for (FestivalEntity fest : festivals) {
            if (favoriteIdSet.contains(fest.getFestivalNo())) {
                colorByFestival.put(fest.getFestivalNo(),
                        palette[colorIdx % palette.length]);
                colorIdx++;
            }
        }

        // 4) 즐겨찾기 축제들의 "이 달 기준 구간" 리스트
        class FavInterval {
            FestivalEntity fest;
            LocalDate visibleStart;
            LocalDate visibleEnd;
        }

        List<FavInterval> favIntervals = new ArrayList<>();

        for (FestivalEntity fest : festivals) {
            if (!favoriteIdSet.contains(fest.getFestivalNo())) continue;  // ★ 즐겨찾기만

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

        // 5) 시작일 빠른 순 + 종료일이 늦게 끝나는 순으로 정렬
        favIntervals.sort(
                Comparator
                        .comparing((FavInterval it) -> it.visibleStart)
                        .thenComparing(it -> it.visibleEnd, Comparator.reverseOrder())
        );

        // 6) greedy하게 레인 번호 배정 (한 번만!)
        List<LocalDate> laneEnd = new ArrayList<>();         // 각 레인별 마지막 날짜
        Map<Long, Integer> laneByFestival = new HashMap<>(); // festivalNo -> lane

        for (FavInterval interval : favIntervals) {
            int lane = 0;
            // 현재 축제 시작일과 겹치지 않는 가장 낮은 레인 찾기
            while (lane < laneEnd.size()
                    && !laneEnd.get(lane).isBefore(interval.visibleStart)) { // laneEnd >= start 이면 겹침
                lane++;
            }
            if (lane == laneEnd.size()) {
                laneEnd.add(interval.visibleEnd);
            } else {
                laneEnd.set(lane, interval.visibleEnd);
            }
            laneByFestival.put(interval.fest.getFestivalNo(), lane);
        }

        // 7) 이제 모든 축제를 날짜별로 쪼개서 넣기
        for (FestivalEntity fest : festivals) {

            boolean favorite = favoriteIdSet.contains(fest.getFestivalNo());

            LocalDate festStart = fest.getEventStartDate().toLocalDate();
            LocalDate festEnd   = fest.getEventEndDate().toLocalDate();
            if (festEnd.isBefore(monthStart) || festStart.isAfter(monthEnd)) {
                continue;
            }

            LocalDate visibleStart = festStart.isBefore(monthStart) ? monthStart : festStart;
            LocalDate visibleEnd   = festEnd.isAfter(monthEnd) ? monthEnd : festEnd;

            Integer lane = favorite ? laneByFestival.getOrDefault(fest.getFestivalNo(), 999) : 999;

            for (LocalDate d = visibleStart; !d.isAfter(visibleEnd); d = d.plusDays(1)) {
                CalendarFestivalDto dto = CalendarFestivalDto.from(fest, favorite);
                dto.setStartDate(festStart);
                dto.setEndDate(festEnd);

                if (favorite) {
                    dto.setColorCode(colorByFestival.get(fest.getFestivalNo()));
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

        // 8) 각 날짜별로 lane 기준 정렬
        for (List<CalendarFestivalDto> list : calendarMap.values()) {
            list.sort(Comparator.comparingInt(dto -> dto.getLane() == null ? 999 : dto.getLane()));
        }

        return calendarMap;
    }   // getCalendar

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
//                System.out.println("day added" + dayDto);
                current = current.plusDays(1);
            }
            weeks.add(week);
        }

        return weeks;
    }   // buildCalendar

    // 이 달 전체 기준 즐겨찾기된 축제 목록(중복 제거) – 켈린더 위에 표시용
    public List<CalendarFestivalDto> getMonthlyFavorites(int year, int month,
                                                         String region,
                                                         Long userNo) {
        Map<LocalDate, List<CalendarFestivalDto>> map =
                getCalendar(year, month, region, userNo);

        // flatten + 중복 제거
        Map<Long, CalendarFestivalDto> unique = new LinkedHashMap<>();
        for (List<CalendarFestivalDto> list : map.values()) {
            for (CalendarFestivalDto dto : list) {
                if (dto.isFavorite()) {
                    unique.putIfAbsent(dto.getFestivalNo(), dto);
                }
            }
        }

        List<CalendarFestivalDto> result = new ArrayList<>(unique.values());
//        result.sort(Comparator.comparing(CalendarFestivalDto::getStartDate));
        return result;
    }   // getMonthlyFavorites

}
