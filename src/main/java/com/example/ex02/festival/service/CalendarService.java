package com.example.ex02.festival.service;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final FestivalRepository festivalRepository;
    private final FavoriteRepository favoriteRepository;

    // year, month, region 기준으로 달력 데이터 조회
    public Map<LocalDate, List<CalendarFestivalDto>> getCalendar(int year, int month, String region,Long userNo) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        // 현재 로그인 사용자의 즐겨찾기 축제 id 목록
        Set<Long> favoriteIdSet = Collections.emptySet();
        if (userNo != null) {
            List<Long> favIds = favoriteRepository.findFestivalNosByMember(userNo);
            favoriteIdSet = new HashSet<>(favIds);
        }

        // 1) 해당 월에 겹치는 축제 전체 조회
        List<FestivalEntity> festivals = festivalRepository.findFestivalsForMonth(
                startDateTime, endDateTime, region
        );

        // 2) 월 전체 날짜를 미리 Map으로 생성
        Map<LocalDate, List<CalendarFestivalDto>> calendarMap = new LinkedHashMap<>();
        LocalDate cursor = startOfMonth;
        while (!cursor.isAfter(endOfMonth)) {
            calendarMap.put(cursor, new ArrayList<>());
            cursor = cursor.plusDays(1);
        }

        // 3) 각 축제를 날짜별로 쪼개서 넣기
        for (FestivalEntity festival : festivals) {
//            CalendarFestivalDto dto = CalendarFestivalDto.from(festival);
            boolean isFavorite = favoriteIdSet.contains(festival.getFestivalNo());
            CalendarFestivalDto dto = CalendarFestivalDto.from(festival, isFavorite);

            // 즐겨찾기 표시
            if (favoriteIdSet.contains(dto.getFestivalNo())) {
                dto.setFavorite(true);
            }

            System.out.println("=== getCalendar ===");
            System.out.println("memberNo       = " + userNo);
            System.out.println("festivals.size = " + festivals.size());
            System.out.println("favoriteIdSet  = " + favoriteIdSet);

            LocalDate festivalStart = festival.getEventStartDate().toLocalDate();
            LocalDate festivalEnd = festival.getEventEndDate().toLocalDate();

            LocalDate applyStart = festivalStart.isBefore(startOfMonth) ? startOfMonth : festivalStart;
            LocalDate applyEnd = festivalEnd.isAfter(endOfMonth) ? endOfMonth : festivalEnd;

            LocalDate day = applyStart;
            while (!day.isAfter(applyEnd)) {
                List<CalendarFestivalDto> list = calendarMap.get(day);
                if (list != null) {
                    list.add(dto);
                }
                day = day.plusDays(1);
            }
        }

        // 각 날짜별로 "즐겨찾기 먼저" 정렬
        for (List<CalendarFestivalDto> list : calendarMap.values()) {
            list.sort(Comparator
                    .comparing(CalendarFestivalDto::isFavorite).reversed()
                    .thenComparing(CalendarFestivalDto::getStartDate));
        }

        return calendarMap;
    }

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
    }

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
        result.sort(Comparator.comparing(CalendarFestivalDto::getStartDate));
        return result;
    }

}
