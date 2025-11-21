package com.example.ex02.member.service;

import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.entity.MemberAiScoreEntity;
import com.example.ex02.member.repository.MemberRepository;
import com.example.ex02.member.repository.MemberAiScoreRepository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalTagEntity;
import com.example.ex02.festival.repository.FestivalTagRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AiScoreAppService {

    /** 한글 태그 순서 */
    private static final List<String> TAGS = Arrays.asList(
            "체험", "아동", "문화", "먹거리", "자연", "야간", "계절"
    );

    private final MemberRepository memberRepository;
    private final FestivalTagRepository festivalTagRepository;
    private final MemberAiScoreRepository memberAiScoreRepository;

    /** tag_lift.json (한글 기반) */
    private final Map<String, Map<String, Map<String, Double>>> liftJson;


    /** ⭐ 회원 AI 점수 재계산 */
    public void recalculateForUser(Long userNo) {

        System.out.println("\n===============================");
        System.out.println("🔥 [AI SCORE] 시작 userNo = " + userNo);
        System.out.println("===============================\n");

        MemberEntity member = memberRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없음: " + userNo));

        System.out.println("회원 gender = " + member.getGender());
        System.out.println("회원 birthDate = " + member.getBirthDate());
        System.out.println("회원 favoriteTag(한글) = " + member.getFavoriteTag());

        String sex = member.getGender();
        String ageGroup = toAgeGroup(member.getBirthDate());
        String preferTag = member.getFavoriteTag();

        System.out.println("계산된 ageGroup = " + ageGroup);

        List<FestivalTagEntity> tagRows = festivalTagRepository.findCurrentAndFutureFestivalTags();

        System.out.println("👉 읽어온 FestivalTagEntity 개수 = " + tagRows.size());
        for (FestivalTagEntity row : tagRows) {
            System.out.println(" - festivalNo=" + row.getFestival().getFestivalNo()
                    + ", tag=" + row.getTag() + ", strength=" + row.getTagStrength());
        }

        List<FestivalTagVector> vectors = buildTagVectors(tagRows);
        System.out.println("\n👉 변환된 FestivalTagVector 개수 = " + vectors.size());

        for (FestivalTagVector v : vectors) {
            System.out.println("◆ festivalNo=" + v.festival.getFestivalNo()
                    + " | 체험=" + v.체험
                    + ", 아동=" + v.아동
                    + ", 문화=" + v.문화
                    + ", 먹거리=" + v.먹거리
                    + ", 자연=" + v.자연
                    + ", 야간=" + v.야간
                    + ", 계절=" + v.계절
            );
        }

        List<MemberAiScoreEntity> scores = computeAiScoresForUser(
                member,
                vectors,
                liftJson,
                sex,
                ageGroup,
                preferTag,
                1.15,
                1.0
        );

        System.out.println("\n👉 계산된 AI Score 개수 = " + scores.size());
        for (MemberAiScoreEntity s : scores) {
            System.out.println(" - festivalNo=" + s.getFestival().getFestivalNo()
                    + ", aiScore=" + s.getAiScore());
        }

//        System.out.println("\n🔥 기존 AI Score 삭제 수행");
        memberAiScoreRepository.deleteByUserNo(userNo); /*태그 수정시 aiscore 계산을 위해수정*/


        System.out.println("🔥 새 AI Score 저장");
        memberAiScoreRepository.saveAll(scores);

        System.out.println("\n===============================");
        System.out.println("🔥 [AI SCORE] 완료 userNo = " + userNo);
        System.out.println("===============================\n");
    }


    /** 연령대를 문자열로 변환 */
    private String toAgeGroup(LocalDate birthDate) {

        if (birthDate == null) return "20대";

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age <= 9) return "아동";
        else if (age <= 19) return "10대";
        else if (age <= 29) return "20대";
        else if (age <= 39) return "30대";
        else if (age <= 49) return "40대";
        else if (age <= 59) return "50대";
        else if (age <= 69) return "60대";
        else return "70세 이상";
    }


    /** 축제 태그 벡터 구조 */
    private static class FestivalTagVector {
        FestivalEntity festival;
        double 체험;
        double 아동;
        double 문화;
        double 먹거리;
        double 자연;
        double 야간;
        double 계절;
    }


    /** FESTIVAL_TAG 테이블 → 벡터로 변환 */
    private List<FestivalTagVector> buildTagVectors(List<FestivalTagEntity> tagRows) {

        System.out.println("\n[벡터 변환 시작]");

        Map<Long, FestivalTagVector> map = new HashMap<>();

        for (FestivalTagEntity row : tagRows) {

            FestivalEntity f = row.getFestival();
            Long fid = f.getFestivalNo();

            FestivalTagVector vec = map.computeIfAbsent(fid, id -> {
                System.out.println("새 벡터 생성 → festivalNo = " + id);
                FestivalTagVector v = new FestivalTagVector();
                v.festival = f;
                return v;
            });

            double strength = row.getTagStrength() != null ? row.getTagStrength() : 0.0;
            String tag = row.getTag();   // ★ 한글 태그 그대로

            System.out.println(" festivalNo=" + fid + " | tag=" + tag + " | strength=" + strength);

            switch (tag) {
                case "체험" -> vec.체험 = strength;
                case "아동" -> vec.아동 = strength;
                case "문화" -> vec.문화 = strength;
                case "먹거리" -> vec.먹거리 = strength;
                case "자연" -> vec.자연 = strength;
                case "야간" -> vec.야간 = strength;
                case "계절" -> vec.계절 = strength;
                default -> System.out.println("⚠ 알 수 없는 TAG 발견 = " + tag);
            }
        }

        System.out.println("[벡터 변환 종료]");
        return new ArrayList<>(map.values());
    }


    /** 사용자 선호 벡터 생성 */
    private double[] buildUserPref(
            Map<String, Map<String, Map<String, Double>>> liftJson,
            String sex,
            String ageGroup,
            String preferTag,
            double preferBoost,
            double defaultLift
    ) {

        System.out.println("\n[사용자 선호 벡터 생성]");
        System.out.println(" sex = " + sex);
        System.out.println(" ageGroup = " + ageGroup);
        System.out.println(" preferTag = " + preferTag);

        double[] base = new double[TAGS.size()];

        for (int i = 0; i < TAGS.size(); i++) {

            String tag = TAGS.get(i);
            System.out.println(" - lift 검색 : 태그=" + tag);

            Double lift = defaultLift;

            if (liftJson.containsKey(tag)) {
                System.out.println("   → liftJson 존재함");

                Map<String, Map<String, Double>> sexMap = liftJson.get(tag);
                if (sexMap.containsKey(sex)) {
                    lift = sexMap.get(sex).getOrDefault(ageGroup, defaultLift);
                }
            }

            System.out.println("   최종 lift = " + lift);
            base[i] = lift;
        }

        double[] weight = new double[TAGS.size()];
        Arrays.fill(weight, 1.0);

        if (preferTag != null && TAGS.contains(preferTag)) {
            weight[TAGS.indexOf(preferTag)] = preferBoost;
            System.out.println(" → 선호 태그 boost 적용: " + preferTag + " x " + preferBoost);
        }

        double[] finalPref = new double[TAGS.size()];
        for (int i = 0; i < TAGS.size(); i++) {
            finalPref[i] = base[i] * weight[i];
            System.out.println(" 최종 Pref[" + TAGS.get(i) + "] = " + finalPref[i]);
        }

        return finalPref;
    }


    /** 축제 벡터 → double[] */
    private double[] theta(FestivalTagVector v) {
        return new double[]{
                v.체험,
                v.아동,
                v.문화,
                v.먹거리,
                v.자연,
                v.야간,
                v.계절
        };
    }


    private double dot(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }


    private List<MemberAiScoreEntity> computeAiScoresForUser(
            MemberEntity member,
            List<FestivalTagVector> festivals,
            Map<String, Map<String, Map<String, Double>>> liftJson,
            String sex,
            String ageGroup,
            String preferTag,
            double preferBoost,
            double defaultLift
    ) {

        System.out.println("\n[AI SCORE 계산 시작]");

        double[] userPref = buildUserPref(liftJson, sex, ageGroup, preferTag, preferBoost, defaultLift);

        Map<Long, Double> rawMap = new HashMap<>();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (FestivalTagVector f : festivals) {

            double raw = dot(theta(f), userPref);

            System.out.println(" festivalNo=" + f.festival.getFestivalNo() + " | rawScore=" + raw);

            rawMap.put(f.festival.getFestivalNo(), raw);
            min = Math.min(min, raw);
            max = Math.max(max, raw);
        }

        System.out.println(" rawScore min=" + min + ", max=" + max);

        double diff = max - min;
        double eps = 1e-8;

        List<MemberAiScoreEntity> result = new ArrayList<>();

        for (FestivalTagVector f : festivals) {

            double raw = rawMap.get(f.festival.getFestivalNo());
            double ai = (diff < eps) ? 0.0 : (raw - min) / (diff + eps);

            System.out.println(" → AI Score festivalNo=" + f.festival.getFestivalNo()
                    + " | ai=" + ai);

            MemberAiScoreEntity s = new MemberAiScoreEntity();
            s.setMember(member);
            s.setFestival(f.festival);
            s.setAiScore(ai);
            s.setScoredAt(LocalDateTime.now());

            result.add(s);
        }

        System.out.println("[AI SCORE 계산 완료]");
        return result;
    }
}
