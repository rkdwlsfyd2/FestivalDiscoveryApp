# FestivalDiscoveryApp (코친놈s)

![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot 3.4.9](https://img.shields.io/badge/Spring_Boot-3.4.9-6DB33F?logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python-Notebook_Workflow-3776AB?logo=python&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

[Demo Video](./산출물/기능%20영상%20산출물.mp4) | [Project PPT](./산출물/2조%20프로젝트PPT_제출용.pdf) | [ER Diagram](./산출물/ER%20다이어그램.png) | [Site Map](./산출물/Site%20Map.png)

대한민국 축제 데이터를 모아 검색, 지도 탐색, 일정 확인, 리뷰 작성, 즐겨찾기, 개인화 추천을 제공하는 AI 중심 축제 추천 플랫폼입니다.  
핵심 가치는 "단순 검색"이 아니라, 축제 설명 텍스트와 방문 통계 데이터를 함께 사용해 사용자 프로필에 맞는 후보를 선별한다는 점입니다.  
특히 이 레포는 `Python 기반 학습 파이프라인 + Spring Boot 기반 서빙/제품화`가 한 저장소에 함께 있어 재현성과 구현 깊이를 확인하기 좋습니다.

## Why it matters

- 공공 축제 데이터는 정보량이 많지만, 사용자가 "나에게 맞는 축제"를 찾기에는 탐색 비용이 큽니다.
- 이 프로젝트는 축제 소개 텍스트를 멀티라벨 태그로 구조화하고, 성별/연령 방문 통계와 결합해 개인화 추천 점수를 계산합니다.
- 결과적으로 사용자는 지도, 목록, 캘린더, 상세 페이지에서 같은 데이터를 보더라도 AI 추천 배지를 통해 우선순위가 붙은 탐색 경험을 얻습니다.

English Version : https://github.com/rkdwlsfyd2/FestivalDiscoveryApp/blob/main/README.en.md
---

## Overview

| 항목 | 내용 |
|---|---|
| 프로젝트명 | `FestivalDiscoveryApp` / 서비스명 `코친놈s` |
| 한 줄 소개 | 공공 축제 데이터와 텍스트 분류 모델을 이용해 개인화 추천을 제공하는 축제 탐색 플랫폼 |
| 데모 | [기능 영상](./산출물/기능%20영상%20산출물.mp4) |
| 개발 형태 | '팀' |
| 내 역할 | `AI 머신러닝,백엔드,프론트엔드` |
| 개발 기간 | `2025.11.03~2025.12.24` |
| 레포 구조 | `Spring Boot + Thymeleaf` 웹앱, `Python/Jupyter` AI 학습 파이프라인, Oracle 기반 엔티티/리포지토리 |

## Repository structure

```text
.
├─ src/
│  ├─ main/java/com/example/ex02/
│  │  ├─ admin/                 # 관리자 대시보드, 회원/리뷰/축제 관리
│  │  ├─ festival/              # 축제 목록, 달력, 즐겨찾기, 상세
│  │  ├─ member/                # 회원가입, 로그인, 마이페이지, AI 점수 저장
│  │  ├─ review/                # 리뷰 등록
│  │  └─ config/                # Security, WebMvc, tag_lift 로더
│  └─ main/resources/
│     ├─ templates/             # Thymeleaf UI
│     ├─ static/                # JS/CSS/이미지/영상
│     └─ tag_lift.json          # 추천 lift 가중치
├─ python/
│  ├─ data/                     # 공공데이터/관광 API 원본 JSON
│  ├─ ml_data/                  # 학습용 CSV
│  ├─ model/                    # 분류 파이프라인, vocabulary, config
│  ├─ statistics_data/          # 축제별 성/연령 방문 통계 CSV
│  └─ *.ipynb                   # 수집/병합/라벨링/모델링/AI 점수 계산
└─ 산출물/                      # 영상, PPT, IA, ERD 등 문서
```

---

## AI Core

### Problem definition

이 프로젝트의 AI는 "사용자가 어떤 축제를 좋아할지"를 직접 예측하기보다, 먼저 축제 텍스트를 구조화된 태그 벡터로 바꾸고, 이후 인구통계 기반 선호 가중치를 적용해 개인화 점수를 계산합니다.

| 구분 | 내용 |
|---|---|
| 입력 | 축제 소개 텍스트(`infotext`), 사용자 성별/생년월일/선호 태그, 축제별 태그 강도, 성별·연령대 방문 통계 |
| 출력 | 멀티라벨 태그(`activity`, `children`, `culture`, `food`, `nature`, `night`, `season`) + 사용자별 `ai_score(0~1)` + 상위 추천 축제 후보 |

### Model / approach

| 항목 | 내용 |
|---|---|
| 모델 유형 | 텍스트 멀티라벨 분류 + 규칙/통계 결합형 랭킹 |
| 학습 모델 | `OneVsRestClassifier(LogisticRegression)` |
| 특징 추출 | `TfidfVectorizer(ngram_range=(1,2), min_df=2, max_df=0.9)` |
| 보정 방식 | 태그별 threshold 튜닝 + 성별/연령 lift 가중치 + 선호 태그 boost |
| 런타임 추천 | `dot(festival_tag_vector, user_preference_vector)` 후 min-max normalization |
| 서빙 방식 | Python에서 학습/산출물 생성, Spring Boot가 `tag_lift.json`과 DB 태그를 읽어 실시간 점수 계산 |

### Data

| 항목 | 내용 |
|---|---|
| 출처 | `전국문화축제표준데이터.json`, 관광 정보 조회 JSON, 축제별 성/연령 방문 통계 CSV |
| 학습 데이터 규모 | `classification_data_manual.csv`: 500건, `regression_data.csv`: 500건 |
| 통계 데이터 규모 | `statistics_data/*.csv`: 92개 파일 |
| 라벨 수 | 7개 태그 |
| 라벨링 방식 | 축제 소개문(`infotext`)에 대해 태그별 수동/정제 라벨을 만든 뒤 학습 CSV 생성 |
| 학습 산출물 | `pipe_ovr_v1.pkl`, `vocabulary_v1.json`, `config_v1.json`, `tag_name_v1.json`, `combined_scores_v1.json` |

### AI pipeline summary

| 단계 | 구현 위치 | 입력 | 처리 | 출력 |
|---|---|---|---|---|
| 데이터 수집 | `python/데이터 수집.ipynb` | 공공 축제/관광 API JSON | 원본 수집 및 저장 | `python/data/*.json` |
| 데이터 병합 | `python/데이터 병합.ipynb` | 여러 JSON 소스 | 텍스트/메타데이터 결합 | 학습/서빙용 통합 데이터 |
| 데이터 라벨링 | `python/데이터 라벨링.ipynb` | 축제 설명 텍스트 | 7개 태그 라벨 부여 | `classification_data_manual.csv` |
| 모델 학습 | `python/머신러닝 모델링.ipynb` | 라벨링 CSV | TF-IDF + OVR Logistic Regression 학습 | `pipe_ovr_v1.pkl` |
| 통계 lift 계산 | `python/ai 스코어 계산.ipynb` | 성/연령 통계 CSV | 태그별 lift 계산 | `tag_lift.json` |
| 런타임 추론 | `AiScoreAppService` | 사용자 프로필 + 축제 태그 강도 | 선호 벡터 생성, 내적, 정규화 | `MEMBER_AI_SCORE` 저장 |
| 제품 노출 | Thymeleaf list/map/calendar | `ai_score >= 0.8` 상위 3개 | AI 추천 배지 표시 | 개인화 탐색 UI |

### Training flow

```text
Public festival JSON
  -> merge / clean infotext
  -> manual multi-label annotation
  -> TF-IDF vocabulary build
  -> One-vs-Rest Logistic Regression training
  -> threshold tuning / CV
  -> export model + vocabulary + config
  -> compute tag_lift.json from demographic statistics
```

### Inference flow

```text
Member signup/profile update
  -> user profile: gender + birthDate + favoriteTag
  -> load demographic lift from tag_lift.json
  -> fetch festival tag strengths from FESTIVAL_TAG
  -> build user preference vector
  -> dot product with each festival tag vector
  -> min-max normalize to ai_score(0~1)
  -> persist to MEMBER_AI_SCORE
  -> expose top-3 recommendations when ai_score >= 0.8
```

### Evaluation

`python/머신러닝 모델링.ipynb`에 저장된 출력 기준:

| 지표 | 결과 |
|---|---|
| Hold-out ROC-AUC (macro) | `0.940728` |
| Hold-out F1 (macro) | `0.781169` |
| Hold-out ROC-AUC (micro) | `0.939730` |
| Hold-out F1 (micro) | `0.806045` |
| Hold-out F1 (weighted) | `0.811891` |
| 10-fold CV Average Train F1 | `0.9168` |
| 10-fold CV Average Val F1 | `0.8050` |
| Overfitting gap | `0.1118` |

#### Representative per-tag validation signals

| 태그 | F1 (validation aggregated) |
|---|---|
| activity | `0.81` |
| children | `0.77` |
| culture | `0.87` |
| food | `0.85` |
| nature | `0.67` |
| night | `0.65` |
| season | `0.73` |

### Error analysis

1. `nature`, `night` 계열은 상대적으로 낮은 F1을 보여, 설명문에서 비유적 표현이나 간접 서술이 많을 때 누락될 가능성이 있습니다.
2. `children` 태그는 표본 수가 작아 일반화가 불안정할 수 있으며, 가족 친화 행사와 일반 체험형 행사 간 경계가 흐릴 수 있습니다.
3. 런타임 추천은 사용자의 행동 로그가 아니라 성별/연령/선호 태그를 주로 사용하므로, 실제 클릭/찜/리뷰 패턴을 반영하지 못합니다.

### Model card

| 항목 | 내용 |
|---|---|
| 모델 목적 | 축제 설명 텍스트를 7개 관심 태그로 구조화하고, 사용자 맞춤 추천의 입력 피처로 사용 |
| 예상 사용자 | 축제 탐색이 어려운 일반 사용자, 개인화 큐레이션이 필요한 관광/문화 서비스 |
| 학습 데이터 | 500개 축제 설명 텍스트와 7개 멀티라벨, 92개 축제 방문 통계 CSV |
| 입력 스키마 | `infotext`, `gender`, `birthDate`, `favoriteTag`, `festival tag strengths` |
| 출력 스키마 | `tag probabilities`, `festival tag strengths`, `member ai_score` |
| 핵심 지표 | Hold-out F1 macro `0.7812`, CV Val F1 `0.8050` |
| 강점 | 해석 가능성 높음, 경량 추론, 텍스트 기반 구조화와 통계 기반 개인화 결합 |
| 주의사항 | 행동 로그 미사용, 표본 수 제한, 계절/야간/자연 계열 클래스 성능 편차, 인구통계 편향 가능 |

### Limitations

1. 학습 데이터가 500건 수준이라 희소 태그 일반화에 한계가 있습니다.
2. 추천은 주로 성별/연령/선호 태그에 의존하므로 실제 사용자 행동 기반 personalization이 아닙니다.
3. `AiScoreAppService`는 Spring 내부 서비스로 구현되어 있지만 별도 모델 서빙 API, 배치 스케줄러, feature store가 없습니다.
4. `SecurityConfig`가 모든 요청을 `permitAll`로 열고 CSRF를 비활성화하고 있어 운영 보안 수준으로는 부족합니다.
5. 레포에 `application.properties`, `.env`, Docker 구성이 없어 완전한 재현은 추가 설정 없이는 어렵습니다.
6. 지도 키 관리가 일관적이지 않습니다. Kakao 키는 환경변수 의존이지만, Naver 지도 키는 템플릿에 직접 노출되어 있습니다.
7. 추천 노출 조건이 `ai_score >= 0.8` 고정이라 필터링 결과에 따라 추천이 0개가 될 수 있습니다.

### Roadmap

1. `P0` 행동 로그(조회/찜/리뷰) 기반 랭킹 피처를 추가해 인구통계 중심 추천 한계를 줄입니다.
2. `P0` 환경설정과 배포 구성을 분리해 `.env.example`, `application-example.properties`, `docker-compose.yml`을 정식 제공합니다.
3. `P1` 클래스 불균형 보완을 위해 `nature/night/season` 라벨을 확장하고 액티브 러닝 또는 재라벨링을 수행합니다.
4. `P1` 추천/분류 추론을 별도 AI 서비스로 분리해 배치 재계산과 온라인 API를 명확히 구분합니다.
5. `P2` 보안 강화를 위해 인증/인가, CSRF, 비밀키 관리, 감사 로그를 추가하고 운영 설정을 분리합니다.

---

## System / Product

### Architecture

```text
                       Offline training pipeline
┌──────────────────────────────────────────────────────────────────┐
│ Public festival JSON + tourism API + visitor stats CSV          │
│   -> Jupyter notebooks (collect / merge / label / train)        │
│   -> model artifacts (.pkl, vocabulary, config, tag_lift.json)  │
└──────────────────────────────────────────────────────────────────┘

                                      runtime
┌──────────┐    HTTP/HTML/AJAX    ┌──────────────────────┐
│ Client   │ ───────────────────> │ Spring Boot API/UI   │
│ Browser  │                      │ - Thymeleaf          │
│ - Map    │ <─────────────────── │ - Member/Festival    │
│ - List   │                      │ - Review/Admin       │
│ -Calendar│                      │ - AiScoreAppService  │
└──────────┘                      └─────────┬────────────┘
                                           │
                            reads/writes    │
                                           ▼
                               ┌──────────────────────┐
                               │ Oracle DB            │
                               │ FESTIVAL             │
                               │ FESTIVAL_TAG         │
                               │ FAVORITE / REVIEW    │
                               │ MEMBER_AI_SCORE      │
                               └──────────────────────┘
                                           ▲
                                           │ load artifact
                                           │
                               ┌──────────────────────┐
                               │ Storage / Resources  │
                               │ tag_lift.json        │
                               │ model/*.pkl,json     │
                               └──────────────────────┘
```

### Core features

#### User-facing

- 지도 기반 축제 탐색
- 지역, 키워드, 태그, 진행 중 여부 필터링
- 달력 기반 월간 축제 탐색
- 회원가입/로그인/비밀번호 재설정
- 즐겨찾기와 리뷰 작성
- 마이페이지에서 선호 태그 및 계정 정보 수정

#### AI-facing

- 축제 설명문 멀티라벨 태깅
- 태그 강도 기반 축제 벡터화
- 성별/연령대 방문 통계 기반 선호 lift 반영
- 사용자 선호 태그 boost
- 개인별 `MEMBER_AI_SCORE` 저장 및 상위 추천 배지 노출

### Main API spec

실제 구현은 HTML 렌더링과 폼 기반 요청이 섞여 있습니다. 아래는 코드상 확인 가능한 핵심 엔드포인트입니다.

#### 1) Check user id duplication

| 항목 | 내용 |
|---|---|
| Endpoint | `GET /api/member/check-userid?userId={userId}` |
| Purpose | 회원가입 시 ID 중복 및 활성 상태 확인 |

```json
{
  "request": {
    "userId": "festival_fan"
  },
  "response": {
    "exists": false,
    "active": false
  }
}
```

#### 2) Check email duplication

| 항목 | 내용 |
|---|---|
| Endpoint | `GET /api/member/check-email?email={email}` |
| Purpose | 회원가입 전 이메일 중복 확인 |

```json
{
  "request": {
    "email": "user@example.com"
  },
  "response": {
    "exists": false
  }
}
```

#### 3) Verify email code

| 항목 | 내용 |
|---|---|
| Endpoint | `POST /api/member/verify-email-code` |
| Purpose | 이메일 인증코드 검증 |

```json
{
  "request": {
    "email": "user@example.com",
    "code": "123456"
  },
  "response": true
}
```

#### 4) Toggle favorite

| 항목 | 내용 |
|---|---|
| Endpoint | `POST /favorite/toggle` |
| Purpose | 축제 찜/해제 |

```json
{
  "request": {
    "festivalNo": 101
  },
  "response_text": "added"
}
```

### Tech stack

| 영역 | 스택 |
|---|---|
| Frontend | Thymeleaf, Tailwind CSS, Vanilla JavaScript |
| Backend | Java 17, Spring Boot 3.4.9, Spring MVC, Spring Security, JPA, MyBatis |
| AI | Python, Jupyter Notebook, scikit-learn, joblib |
| Database | Oracle (`ojdbc11`) |
| Infra | `[TBD]` |
| DevTools | Maven Wrapper, Lombok, ModelMapper |

---

## Reproducibility / Run

### Requirements

| 항목 | 버전 |
|---|---|
| JDK | 17 |
| Maven | Wrapper 포함(`mvnw`, `mvnw.cmd`) |
| Python | `[TBD]` |
| Database | Oracle |

### Environment variables

레포에 실제 설정 파일은 포함되어 있지 않습니다. 아래 키는 코드에서 유추 가능한 최소 항목입니다.

```env
# Spring / DB
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_JPA_HIBERNATE_DDL_AUTO=

# Mail
SPRING_MAIL_HOST=
SPRING_MAIL_PORT=
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=

# Kakao Map
KAKAO_JAVASCRIPT_KEY=

# App
APP_BASE_URL=
SERVER_PORT=
```

### Local run

#### 1) Backend

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

예상 기본 포트는 Spring Boot 기본값 기준 `8080`입니다. 다만 코드 주석에는 `9898`이 언급되고, 실제 설정 파일은 레포에 없으므로 로컬 환경에 따라 달라질 수 있습니다.

#### 2) AI notebooks

```powershell
jupyter notebook python
```

주요 노트북:

- `python/데이터 수집.ipynb`
- `python/데이터 병합.ipynb`
- `python/데이터 라벨링.ipynb`
- `python/머신러닝 모델링.ipynb`
- `python/ai 스코어 계산.ipynb`

#### 3) Docker Compose

```bash
# docker-compose / compose.yaml is not included in this repository
# status: [TBD]
```

### Example commands

```powershell
# labeled dataset size
Import-Csv python\ml_data\classification_data_manual.csv | Measure-Object

# visitor statistics file count
(Get-ChildItem python\statistics_data\*.csv | Measure-Object).Count

# model config
Get-Content python\model\config_v1.json

# spring runtime artifact
Get-Content src\main\resources\tag_lift.json -TotalCount 20
```

### Tests / validation

#### 1) Health check

```powershell
curl http://localhost:8080/
```

Expected: HTML for the main page.

#### 2) Sample API inference-adjacent check

```powershell
curl "http://localhost:8080/api/member/check-email?email=test@example.com"
```

Expected:

```json
{"exists":false}
```

#### 3) Favorite / end-to-end flow

1. 회원가입 시 성별, 생년월일, 선호 태그를 입력합니다.
2. 로그인 후 축제 목록에서 즐겨찾기를 토글합니다.
3. 캘린더/리스트에서 AI 추천 배지가 노출되는지 확인합니다.

#### 4) Maven test

```powershell
.\mvnw.cmd test
```

현재 이 작업 환경에서는 `JAVA_HOME` 미설정으로 실행되지 않았습니다.

---

## Security / Privacy / Ethics

### Data sensitivity

- 회원 데이터에는 이메일, 전화번호, 생년월일, 성별, 선호 태그가 포함됩니다.
- 추천 로직은 성별/연령 정보를 사용하므로 인구통계 기반 편향 가능성을 반드시 고려해야 합니다.

### Current handling in repo

- 비밀번호는 `BCryptPasswordEncoder`로 해시 저장합니다.
- 비밀번호 재설정 토큰은 `UUID`와 만료시간(30분) 기반으로 생성합니다.
- 이메일 인증코드/메일 전송 기능이 구현되어 있습니다.

### Risks observed

- `SecurityConfig`가 `permitAll + csrf.disable()` 상태입니다.
- Naver Map 키가 템플릿에 하드코딩되어 있어 키 노출 위험이 있습니다.
- 실제 운영용 비밀키/DB 설정이 레포에 포함되지 않았고, 배포 방식도 명시되지 않았습니다.
- 프롬프트 인젝션 문제는 LLM 미사용으로 직접적이지 않지만, 모델 악용 측면에서는 스팸 리뷰/입력 조작에 대한 방어가 별도로 보이지 않습니다.

### Recommended guardrails

1. 운영 환경에서 비밀키를 환경변수/시크릿 매니저로 분리합니다.
2. 인증이 필요한 POST 요청에 CSRF 또는 대체 보호장치를 적용합니다.
3. 성별/연령 기반 추천 결과에 대한 bias review를 정기적으로 수행합니다.
4. 리뷰/회원 입력값에 대한 rate limiting과 moderation을 추가합니다.
5. 지도/API 키 로테이션 정책을 문서화합니다.

---

## Troubleshooting

| 문제 | 원인 | 해결 |
|---|---|---|
| `JAVA_HOME environment variable is not defined correctly` | JDK 경로 미설정 | `JAVA_HOME`을 JDK 17 경로로 지정 후 재실행 |
| 앱 실행 시 DB 연결 실패 | Oracle 접속 정보 미설정 | `SPRING_DATASOURCE_*` 값 추가 |
| 메일 인증이 동작하지 않음 | SMTP 설정 부재 | `SPRING_MAIL_*` 환경변수 설정 |
| 카카오 지도 스크립트가 로드되지 않음 | `kakao.javascript-key` 누락 | Kakao JavaScript 키 설정 |
| AI 추천이 보이지 않음 | 사용자 프로필 또는 `MEMBER_AI_SCORE` 미생성 | 성별/생년월일/선호 태그 입력 여부와 점수 재계산 로직 확인 |

---


- [ ] `.\mvnw.cmd test` 실행 전 `JAVA_HOME`이 필요하다는 점을 재현한다.

