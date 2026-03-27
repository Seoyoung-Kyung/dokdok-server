# DokDok Server

독서 모임 관리 및 회고 플랫폼의 백엔드 서버입니다.
사용자들이 독서 모임을 만들고, 책을 함께 읽으며, 토론 주제를 제안하고, 회고를 기록할 수 있는 서비스입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.1 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA, QueryDSL 5.0.0 |
| Auth | Spring Security, Kakao OAuth2 |
| Storage | MinIO (S3 호환) |
| Monitoring | Prometheus, Grafana |
| Build | Gradle |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions → Docker Hub → EC2 |

---

## 주요 기능

- **사용자 관리**: Kakao OAuth2 소셜 로그인, 온보딩, 프로필 관리
- **모임 관리**: 독서 모임 생성/참가 (초대 링크 방식), 멤버 역할 관리
- **책 관리**: 개인 책장, 읽기 상태 관리, 리뷰 및 키워드 태깅
- **회의 관리**: 모임별 회의 스케줄, 참석자 관리, 비동기 조회
- **토픽 관리**: 회의 주제 제안, 좋아요, 확정 기능
- **회고**: 개인/공동 회고 작성 및 AI 기반 요약
- **STT**: 음성 파일 업로드 후 텍스트 변환 (AI 서버 연동)

---

## 프로젝트 구조

```
src/main/java/com/dokdok/
├── book/           # 책 관리 (개인 책장, 리뷰, 독서 기록)
├── gathering/      # 모임 관리
├── meeting/        # 회의 관리
├── topic/          # 토픽 및 답변 관리
├── retrospective/  # 회고 관리
├── user/           # 사용자 및 인증
├── keyword/        # 키워드 관리
├── stt/            # 음성-텍스트 변환
├── storage/        # MinIO 파일 스토리지
├── ai/             # AI 서비스 연동
├── history/        # 변경 이력 추적
├── oauth2/         # Kakao OAuth2
└── global/         # 공통 설정, 예외 처리, 보안
```

---

## 인프라 구성

```
┌─────────────────────────────────────────┐
│              Docker Compose              │
│                                         │
│  ┌──────────┐  ┌──────────┐  ┌───────┐ │
│  │ dokdok   │  │PostgreSQL│  │ MinIO │ │
│  │   app    │→ │   :5432  │  │ :9000 │ │
│  │  :8080   │  └──────────┘  └───────┘ │
│  └──────────┘                           │
└─────────────────────────────────────────┘
```

- **dokdok-app**: Spring Boot 애플리케이션 (포트 8080)
- **dokdok-postgres**: PostgreSQL 16 (포트 5432)
- **dokdok-minio**: MinIO 객체 스토리지 (API 9000, Console 9001)

---

## 로컬 환경 설정

### 사전 요구사항

- Java 21
- Docker & Docker Compose

### 1. 환경변수 설정

```bash
cp .env.example .env
```

`.env` 파일에 아래 값을 채워주세요:

```env
SPRING_PROFILE=local

# PostgreSQL
POSTGRES_DB=dokdok
POSTGRES_USER=dokdok
POSTGRES_PASSWORD=your_password

DB_USERNAME=dokdok
DB_PASSWORD=your_password

# Kakao
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_API_KEY=your_kakao_api_key
KAKAO_API_BASE_URL=https://dapi.kakao.com

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET=dokdok-storage
MINIO_EXTERNAL_ENDPOINT=http://localhost:9000
```

### 2. 인프라 실행 (DB + MinIO)

```bash
docker compose --profile local up -d
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. 전체 스택 실행 (앱 포함)

```bash
docker compose --profile app up -d
```

---

## API 문서

애플리케이션 실행 후 Swagger UI에서 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

### 주요 엔드포인트

| 도메인 | 엔드포인트 | 설명 |
|--------|-----------|------|
| Auth | `GET /api/auth/me` | 현재 로그인 사용자 정보 |
| Auth | `POST /api/auth/logout` | 로그아웃 |
| User | `PATCH /api/users/onboarding` | 온보딩 처리 |
| User | `GET /api/users/me` | 사용자 정보 조회 |
| Book | `GET /api/book/search` | 책 검색 (Kakao API) |
| Book | `GET /api/book` | 내 책장 조회 |
| Book | `POST /api/book` | 내 책장에 책 추가 |
| Gathering | `POST /api/gatherings` | 모임 생성 |
| Gathering | `POST /api/gatherings/join-request/{invitationLink}` | 초대 링크로 모임 참가 |
| Meeting | `POST /api/meetings` | 회의 생성 |
| Topic | `POST /api/gatherings/{gatheringId}/meetings/{meetingId}/topics` | 토픽 제안 |
| Retrospective | `POST /api/meetings/{meetingId}/retrospectives` | 공동 회고 작성 |
| STT | `POST /api/stt/upload` | 음성 파일 업로드 |

---

## CI/CD

`main` 브랜치에 push 시 자동으로 배포됩니다.

```
main 브랜치 push
    → GitHub Actions
    → Gradle 빌드
    → Docker 이미지 빌드 & Docker Hub push
    → EC2 SSH 접속
    → 최신 이미지 pull & 컨테이너 재시작
```

### 필요한 GitHub Secrets

| Secret | 설명 |
|--------|------|
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 |
| `DOCKERHUB_TOKEN` | Docker Hub 액세스 토큰 |
| `EC2_HOST` | EC2 인스턴스 주소 |
| `EC2_USERNAME` | EC2 접속 사용자 |
| `EC2_KEY` | EC2 SSH 프라이빗 키 |

---

## 모니터링

Prometheus + Grafana로 애플리케이션 메트릭을 수집합니다.

- **Actuator Health**: `GET /actuator/health`
- **Prometheus 메트릭**: `GET /actuator/prometheus`

```bash
# 모니터링 스택 실행
docker compose -f monitoring/docker-compose.yml up -d
```

---

## 응답 형식

모든 API는 아래 형식으로 응답합니다.

```json
{
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```

에러 코드 목록은 [docs/ErrorCode.md](docs/ErrorCode.md)를 참고해주세요.