# FestivalDiscoveryApp (Cochinnoms)

![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot 3.4.9](https://img.shields.io/badge/Spring_Boot-3.4.9-6DB33F?logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python-Notebook_Workflow-3776AB?logo=python&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

[Demo Video](./산출물/기능%20영상%20산출물.mp4) | [Project PPT](./산출물/2조%20프로젝트PPT_제출용.pdf) | [ER Diagram](./산출물/ER%20다이어그램.png) | [Site Map](./산출물/Site%20Map.png)

An AI-centered festival discovery platform that combines public festival data, text classification, demographic statistics, and a web product experience.  
The core value is not just search, but ranking festivals based on structured festival tags and user-specific preference signals.  
This repository is useful for interviews because it contains both the offline ML workflow and the online Spring Boot productization in one place.

## Why it matters

- Public festival datasets contain a lot of information, but the cost of finding "the right festival for me" is still high.
- This project structures festival descriptions into multi-label tags, then combines them with age/gender visit statistics for personalized ranking.
- As a result, the same festival inventory is surfaced through maps, lists, calendars, and detail pages with AI recommendation signals.

---

## Overview

| Item | Details |
|---|---|
| Project name | `FestivalDiscoveryApp` / service name `Cochinnoms` |
| One-line summary | A festival exploration platform with AI-driven personalization built on public festival data and text classification |
| Demo | [Demo video](./산출물/기능%20영상%20산출물.mp4) |
| Development type | `Team project` |
| My role | `AI/ML, backend, frontend` |
| Development period | `2025.11.03 ~ 2025.12.24` |
| Repository shape | `Spring Boot + Thymeleaf` web app, `Python/Jupyter` ML pipeline, Oracle-based entities/repositories |

## Repository structure

```text
.
├─ src/
│  ├─ main/java/com/example/ex02/
│  │  ├─ admin/                 # admin dashboard, member/review/festival management
│  │  ├─ festival/              # festival list, calendar, favorites, detail
│  │  ├─ member/                # signup, login, mypage, AI score persistence
│  │  ├─ review/                # review creation
│  │  └─ config/                # security, web mvc, tag_lift loader
│  └─ main/resources/
│     ├─ templates/             # Thymeleaf templates
│     ├─ static/                # JS/CSS/images/video
│     └─ tag_lift.json          # recommendation lift weights
├─ python/
│  ├─ data/                     # public/tourism API raw JSON
│  ├─ ml_data/                  # training CSVs
│  ├─ model/                    # trained pipeline, vocabulary, config
│  ├─ statistics_data/          # festival visitor statistics by age/gender
│  └─ *.ipynb                   # collection / merge / labeling / modeling / scoring
└─ 산출물/                      # demo video, PPT, IA, ERD and related docs
```

---

## AI Core

### Problem definition

The AI in this project does not directly predict a single festival label for a user. It first converts free-text festival descriptions into structured tag vectors, then applies demographic preference weights to compute a personalized festival score.

| Category | Details |
|---|---|
| Input | Festival description text (`infotext`), user gender/date of birth/favorite tag, festival tag strengths, age/gender visit statistics |
| Output | Multi-label tags (`activity`, `children`, `culture`, `food`, `nature`, `night`, `season`), user-specific `ai_score(0~1)`, top recommendation candidates |

### Model / approach

| Item | Details |
|---|---|
| Model type | Text multi-label classification + statistics-assisted ranking |
| Training model | `OneVsRestClassifier(LogisticRegression)` |
| Feature extraction | `TfidfVectorizer(ngram_range=(1,2), min_df=2, max_df=0.9)` |
| Calibration | Per-tag threshold tuning + demographic lift weighting + favorite-tag boost |
| Runtime ranking | `dot(festival_tag_vector, user_preference_vector)` followed by min-max normalization |
| Serving | Python generates model/stat artifacts, Spring Boot loads `tag_lift.json` and DB tag vectors for online score calculation |

### Data

| Item | Details |
|---|---|
| Sources | National culture festival standard data JSON, tourism information JSON, visitor statistics CSVs |
| Training set size | `classification_data_manual.csv`: 500 rows, `regression_data.csv`: 500 rows |
| Statistics data size | `statistics_data/*.csv`: 92 files |
| Number of labels | 7 tags |
| Labeling method | Festival descriptions were manually/iteratively labeled into 7 categories and exported as training CSV |
| Artifacts | `pipe_ovr_v1.pkl`, `vocabulary_v1.json`, `config_v1.json`, `tag_name_v1.json`, `combined_scores_v1.json` |

### AI pipeline summary

| Stage | Implementation | Input | Processing | Output |
|---|---|---|---|---|
| Data collection | `python/데이터 수집.ipynb` | public festival / tourism API JSON | collect and save raw data | `python/data/*.json` |
| Data merge | `python/데이터 병합.ipynb` | multiple JSON sources | merge text and metadata | integrated training/serving data |
| Labeling | `python/데이터 라벨링.ipynb` | festival description text | assign 7 labels | `classification_data_manual.csv` |
| Model training | `python/머신러닝 모델링.ipynb` | labeled CSV | TF-IDF + OVR logistic regression training | `pipe_ovr_v1.pkl` |
| Lift computation | `python/ai 스코어 계산.ipynb` | age/gender visitor statistics | compute tag-wise lift | `tag_lift.json` |
| Runtime inference | `AiScoreAppService` | user profile + festival tag strengths | build preference vector, dot product, normalization | `MEMBER_AI_SCORE` |
| Product surfacing | Thymeleaf list/map/calendar | top 3 with threshold | render AI recommendation badge | personalized discovery UI |

### Training flow

```text
Public festival JSON
  -> merge / clean infotext
  -> manual multi-label annotation
  -> TF-IDF vocabulary build
  -> One-vs-Rest Logistic Regression training
  -> threshold tuning / cross-validation
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

Based on saved notebook outputs in `python/머신러닝 모델링.ipynb`:

| Metric | Result |
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

| Tag | F1 (validation aggregated) |
|---|---|
| activity | `0.81` |
| children | `0.77` |
| culture | `0.87` |
| food | `0.85` |
| nature | `0.67` |
| night | `0.65` |
| season | `0.73` |

### Error analysis

1. `nature` and `night` are weaker than top-performing tags, which suggests indirect or metaphorical phrasing is harder to classify.
2. `children` likely suffers from lower support and boundary ambiguity between family-friendly and general experience-type festivals.
3. Runtime recommendation depends mostly on demographic/profile features rather than real behavioral logs, so personalization depth is limited.

### Model card

| Item | Details |
|---|---|
| Intended use | Convert festival descriptions into structured tags and use them as inputs for personalized recommendation |
| Target users | Festival explorers, users needing personalized cultural/tourism discovery |
| Training data | 500 labeled festival descriptions and 92 visitor-statistics CSVs |
| Input schema | `infotext`, `gender`, `birthDate`, `favoriteTag`, `festival tag strengths` |
| Output schema | `tag probabilities`, `festival tag strengths`, `member ai_score` |
| Core metrics | Hold-out F1 macro `0.7812`, CV Val F1 `0.8050` |
| Strengths | Interpretable, lightweight, easy to serve, combines text understanding with demographic priors |
| Cautions | No behavior logs, limited sample size, class imbalance, possible demographic bias |

### Limitations

1. The labeled dataset is still relatively small at 500 rows.
2. Recommendation is mainly driven by gender/age/favorite tag rather than behavioral personalization.
3. `AiScoreAppService` exists inside the Spring app rather than as a dedicated AI serving service.
4. `SecurityConfig` is currently `permitAll` with CSRF disabled, which is not production-grade.
5. The repository does not include `application.properties`, `.env`, or Docker config, so full reproduction still needs manual setup.
6. Map key handling is inconsistent: Kakao uses a property, while the Naver map key is directly embedded in a template.
7. The AI badge threshold is fixed at `ai_score >= 0.8`, so recommendations may disappear depending on filter conditions.

### Roadmap

1. `P0` Add behavioral features such as clicks, favorites, and reviews to reduce reliance on demographic-only ranking.
2. `P0` Provide reproducible runtime config with `.env.example`, `application-example.properties`, and `docker-compose.yml`.
3. `P1` Improve low-performing classes such as `nature`, `night`, and `season` with more labeling and better balancing.
4. `P1` Split recommendation/classification into a dedicated AI service for cleaner batch and online inference boundaries.
5. `P2` Harden security with proper authz/authn, CSRF protection, secret management, and auditability.

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

- Map-based festival discovery
- Filtering by region, keyword, tag, and ongoing state
- Calendar-based monthly festival browsing
- Signup, login, and password reset
- Favorites and review creation
- Favorite-tag and profile management in My Page

#### AI-facing

- Multi-label tagging of festival descriptions
- Festival vector construction from tag strengths
- Demographic lift weighting using age/gender visit statistics
- Favorite-tag boost
- Per-user `MEMBER_AI_SCORE` persistence and AI badge rendering

### Main API spec

The implementation mixes rendered HTML and form-based endpoints. The following are core endpoints verified from the codebase.

#### 1) Check user id duplication

| Item | Details |
|---|---|
| Endpoint | `GET /api/member/check-userid?userId={userId}` |
| Purpose | Check duplicate user ID and activation state during signup |

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

| Item | Details |
|---|---|
| Endpoint | `GET /api/member/check-email?email={email}` |
| Purpose | Check duplicate email before signup |

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

| Item | Details |
|---|---|
| Endpoint | `POST /api/member/verify-email-code` |
| Purpose | Verify signup email code |

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

| Item | Details |
|---|---|
| Endpoint | `POST /favorite/toggle` |
| Purpose | Add/remove a festival favorite |

```json
{
  "request": {
    "festivalNo": 101
  },
  "response_text": "added"
}
```

### Tech stack

| Layer | Stack |
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

| Item | Version |
|---|---|
| JDK | 17 |
| Maven | wrapper included (`mvnw`, `mvnw.cmd`) |
| Python | `[TBD]` |
| Database | Oracle |

### Environment variables

The repository does not include actual config files. These are the minimum keys implied by the code.

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

The expected default port is `8080` by Spring Boot convention. Some comments mention `9898`, but no runtime config file is included in the repository.

#### 2) AI notebooks

```powershell
jupyter notebook python
```

Key notebooks:

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

#### 2) Sample API check

```powershell
curl "http://localhost:8080/api/member/check-email?email=test@example.com"
```

Expected:

```json
{"exists":false}
```

#### 3) Favorite / end-to-end flow

1. Sign up with gender, birth date, and favorite tag.
2. Log in and toggle festival favorites.
3. Confirm AI badges are rendered in list/calendar views.

#### 4) Maven test

```powershell
.\mvnw.cmd test
```

In this workspace, the command did not run because `JAVA_HOME` was not configured.

---

## Security / Privacy / Ethics

### Data sensitivity

- Member data includes email, phone number, date of birth, gender, and favorite tag.
- Since recommendation uses gender/age information, demographic bias should be explicitly reviewed.

### Current handling in repo

- Passwords are stored with `BCryptPasswordEncoder`.
- Password reset uses UUID tokens with a 30-minute expiration.
- Email verification and mail sending are implemented.

### Risks observed

- `SecurityConfig` currently uses `permitAll` and disables CSRF.
- The Naver map key is embedded in a template.
- Production secret management and deployment configuration are not documented in the repository.
- There is no visible protection against spam reviews or abusive input patterns.

### Recommended guardrails

1. Move all secrets into environment variables or a secret manager.
2. Add CSRF or equivalent protection for authenticated write actions.
3. Regularly review demographic bias in recommendation outputs.
4. Add rate limiting and moderation for reviews and account-related endpoints.
5. Document key rotation and deployment security practices.

---

## Troubleshooting

| Problem | Cause | Fix |
|---|---|---|
| `JAVA_HOME environment variable is not defined correctly` | JDK path not set | set `JAVA_HOME` to a JDK 17 path |
| App fails to connect to DB | Oracle config missing | provide `SPRING_DATASOURCE_*` values |
| Email verification does not work | SMTP config missing | provide `SPRING_MAIL_*` values |
| Kakao map script does not load | `kakao.javascript-key` missing | set the Kakao JavaScript key |
| AI recommendation does not appear | user profile incomplete or `MEMBER_AI_SCORE` not generated | verify gender/birthDate/favoriteTag and score recalculation flow |

---


