# Book ID 전환 스펙

## 목표
`personalBookId` 식별자를 전면 제거하고, API/서비스/저장소 전 구간에서 `book` 테이블 PK(`bookId`)만을 사용한다. 외부 노출 필드명도 `bookId`로 통일한다.

## 범위
- API: 경로/쿼리/바디의 `personalBookId` → `bookId`로 리네이밍. `/api/book/{bookId}` 및 `/records` 하위 경로의 Swagger 설명·예시를 `book` 테이블 PK 기준으로 갱신하고, 응답/요청 스키마 필드명도 일괄 변경한다.
- DTO/프로젝션: 모든 `personalBookId` 필드를 `bookId`로 교체하고 `book.bookId` 값을 매핑한다. 개인/선반용 별도 ID 노출이 남아 있다면 제거하거나 `bookId` 하나로 수렴시킨다.
- 서비스/검증: 메서드 시그니처와 내부 변수명을 `bookId`로 변경하고, 조회·검증 로직이 `book` 테이블을 직접 참조하도록 수정한다. 기존 personal/shelf ID 의존부는 변환 계층 도입 또는 제거로 정리한다.
- 리포지토리/쿼리: 파라미터/별칭을 `bookId`로 변경하고, 쿼리 대상 컬럼을 `book.book_id`(또는 동등 필드)로 전환한다. `personal_book_id` 기반 쿼리나 조인은 대체/삭제한다.
- 문서: README/API 스니펫 등 모든 안내 문서에서 `personalBookId` 표현을 삭제하고 `bookId`(= `book` 테이블 PK)로 교체한다.
- 테스트: 컨트롤러/서비스/리포지토리/계약 테스트의 필드명, 경로, 픽스처를 `bookId` 기준으로 업데이트하고 동일 동작을 보장한다.

## 범위 외
- `book` 테이블 PK 타입/시퀀스 변경.
- `bookId` 이외의 새로운 식별자 추가.

## 구현 메모
- 레거시 personal/shelf ID가 DB나 캐시에 남아 있으면, 마이그레이션 또는 매핑 계층을 정의해 `bookId` 기반으로만 동작하도록 한다. 이행 기간 동안 입력 변환이 필요하면 명시적으로 처리한다.
- 로그/에러 메시지/모니터링 키에서도 `personalBookId` 문자열을 제거하고 `bookId`로 통일한다.

## 테스트
- 필드명과 데이터 정렬 후 `./gradlew test` 실행.
- API 계약/통합 테스트가 있다면 `bookId`(= `book.bookId`) 사용으로 검증한다.
