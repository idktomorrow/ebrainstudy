# practice-week2

2주차(SpringBoot + MyBatis + MySQL, 프론트 없이 REST API만) 기본기 복습용 독립 연습 프로젝트.
`workspace` 메인 프로젝트와는 별개의 Gradle 프로젝트로, 같은 레포 안에서만 폴더로 분리되어 있음.

의도적으로 `mapstruct`, `spring-boot-starter-validation`, `springdoc` 등은 넣지 않음 — 처음엔 직접 다 손으로 짜보는 게 목표.

## 실행

```bash
cd docker
docker compose up -d
cd ..
./gradlew bootRun
```

| 항목 | 값 |
|---|---|
| Port | `3309` |
| Database | `practice_week2` |
| User / Password | `practice` / `practice` |

## 진행 순서 & 체크리스트

### 0. DB 세팅
- [x] `db/db.sql` — 테이블 스키마 설계 (`category`, `board`, `files`, `comment`)
- [x] `docker-compose.yml`에 `db.sql` 초기화 볼륨 마운트
- [x] `docker compose up -d`로 DB 기동, 테이블/시드 데이터 확인

### 1. Category 조회
- [x] `entity/Category`
- [x] `mapper/CategoryMapper` (인터페이스) — `findAll`, `findById`
- [x] `resources/mappers/CategoryMapper.xml`
- [x] `dto/response/CategoryResponse`
- [x] `service/CategoryService`
- [x] `controller/CategoryController` (전체 카테고리 목록 조회 API)
- [x] **개선**: `findAll` 쿼리에 `ORDER BY`가 없어서 목록 순서가 DB 옵티마이저에 따라
      달라질 수 있던 문제. `ORDER BY id` 추가해서 항상 일관된 순서로 응답되게 함.

### 2. Board CRUD
- [x] `entity/Board`
- [x] `mapper/BoardMapper` (인터페이스)
- [x] `resources/mappers/BoardMapper.xml` (동적 SQL, `<sql>`/`<include>` 재사용)
- [x] `dto/request/BoardCreateRequest`, `BoardUpdateRequest`, `BoardDeleteRequest`, `BoardSearchRequest`
- [x] `dto/response/BoardSummaryResponse`, `BoardListResponse`, `BoardDetailResponse`
- [x] `service/BoardService` (검증, 비밀번호 확인, 카테고리명 매칭, 제목 축약)
- [x] `controller/BoardController`
  - [x] 목록 조회 (검색/기간/카테고리/페이지네이션) — 실제 호출 테스트 완료
  - [x] 상세 조회 (조회수 증가) — 실제 호출 테스트 완료
  - [x] 등록 — 실제 호출 테스트 완료
  - [x] 수정 — 실제 호출 테스트 완료
  - [x] 삭제 (비밀번호 확인) — 실제 호출 테스트 완료 (틀린 비밀번호 거부까지 확인)

**알려진 이슈**: 검증 실패/비밀번호 불일치 시 지금은 500(Internal Server Error)으로 응답함.
`@RestControllerAdvice`로 전역 예외 처리 붙여서 400/404로 정리하는 작업이 남아있음
(`IllegalArgumentException` -> 400, `NoSuchElementException` -> 404).

### 3. Comment CRUD (등록/조회만)
- [x] `entity/Comment`
- [x] `mapper/CommentMapper` (인터페이스)
- [x] `resources/mappers/CommentMapper.xml`
- [x] `dto/request/CommentCreateRequest`
- [x] `dto/response/CommentResponse`
- [x] `service/CommentService` (게시글 존재 확인 + 필드 검증)
- [x] `controller/CommentController` (`/api/boards/{boardId}/comments`)
  - [x] 게시글별 댓글 조회 — 실제 호출 테스트 완료 (오래된 순 정렬 확인)
  - [x] 댓글 등록 — 실제 호출 테스트 완료
  - [x] 게시글 삭제 시 댓글 CASCADE 삭제 확인 완료
- [x] **버그 수정**: `comment.writer` 컬럼이 `VARCHAR(50)`인데 길이 검증이 공백 체크뿐이라,
      50자 넘는 작성자명을 보내면 DB에서 `Data too long for column` 에러가 나서 500으로
      응답되던 버그. 실제 재현(작성자 100자로 등록 시도 -> 500) 확인 후 `CommentService`에
      50자 제한 검증 추가해서 400으로 정리.
- [x] **개선**: `comment.content`(`TEXT`)는 DB 제약상 에러는 안 나지만(6만자는 넘어야 문제),
      게시글 `content`(2000자 제한)와의 일관성 + 비정상적으로 긴 댓글 방지를 위해 같은
      기준(2000자)으로 상한 추가.

### 4. 첨부파일(Attachment)
- [x] `entity/Attachment` (`files` 테이블. 클래스명은 `java.nio.file.Files`와 겹치지 않게 `Attachment`로 명명)
- [x] `mapper/AttachmentMapper` (인터페이스)
- [x] `resources/mappers/AttachmentMapper.xml`
- [x] `dto/response/AttachmentResponse`
- [x] `service/AttachmentService` (로컬 디스크 저장, UUID 파일명으로 충돌 방지)
- [x] `controller/AttachmentController`
  - [x] `POST /api/boards/{boardId}/files` — 업로드 (여러 개 가능) — 실제 호출 테스트 완료
  - [x] `GET /api/files/{id}` — 바이너리 다운로드 (URI 링크 아님, `Content-Disposition`) — 원본과 바이트 단위 일치 확인
  - [x] `DELETE /api/files/{id}` — 삭제 (DB + 디스크 파일 모두) — 실제 삭제 확인
- [x] 게시글 목록에 `hasAttachment` 추가 (EXISTS 서브쿼리), 상세에 `attachments` 목록 추가
- [x] 업로드 확장자 화이트리스트 (`pdf/doc/docx/hwp/xls/xlsx/ppt/pptx/txt/jpg/jpeg/png/gif/zip`만 허용,
      exe 등 위험한 확장자 차단) — 여러 파일 업로드 시 하나라도 거부되면 전부 저장 안 하는
      all-or-nothing으로 처리 (부분 저장 방지)
- [x] 게시글 삭제 시 첨부파일 디스크 정리 (`AttachmentService.deleteFilesByBoardId`) — DB의 `files`
      행은 FK CASCADE로 자동 삭제되지만 디스크 파일은 안 지워지는 고아 파일 버그 발견 후 수정
- [x] **개선**: `findByBoardId`(첨부파일 목록)에도 `ORDER BY`가 없어서 순서가 안 보장되던 문제.
      Category의 같은 문제 고칠 때 발견해서 같이 정리 — `ORDER BY id`로 업로드 순서 고정.

`application.yaml`에 `spring.servlet.multipart`(최대 10MB/파일, 20MB/요청), `app.upload-dir`(`./uploads`, 루트 `.gitignore`에 이미 포함) 설정 추가.

### 5. 전역 예외 처리
- [x] `global/exception/ErrorResponse` — 공통 에러 응답 형태 (`status`, `message`)
- [x] `global/exception/GlobalExceptionHandler` (`@RestControllerAdvice`)
  - [x] `IllegalArgumentException` -> 400 (검증 실패, 비밀번호 불일치)
  - [x] `NoSuchElementException` -> 404 (존재하지 않는 리소스)
  - [x] 그 외 `Exception` -> 500 (원인은 서버 로그에만 남기고 클라이언트엔 상세 노출 안 함)
- [x] 실제 호출로 400/404/200 전부 확인 완료

### 6. 전체 버그 점검에서 발견/수정한 것
- [x] **전역 예외 처리기가 Spring 프레임워크 자체의 400 에러까지 500으로 덮어쓰던 버그.**
      `@ExceptionHandler(Exception.class)`가 너무 광범위해서 `MethodArgumentTypeMismatchException`
      (숫자 파라미터에 문자열 전달 등), `HttpMessageNotReadableException`(JSON 문법 오류),
      `MissingServletRequestPartException`/`MultipartException`(멀티파트 파트 누락)까지 다
      가로채서 500으로 응답하고 있었음. 이 예외들을 400으로 매핑하는 핸들러를 catch-all보다
      먼저 두는 식으로 수정.
- [x] **페이지네이션 `page`/`size`에 0 이하 값이 들어오면 SQL `LIMIT`에 음수가 들어가서
      500(SQL 문법 에러)이 나던 버그.** `BoardService.getBoardList()`에 `page < 1`,
      `size < 1` 검증 추가해서 400으로 정리.
- [x] **첨부파일 업로드 시, 디스크 저장은 성공했는데 DB 메타데이터 저장이 실패하면 파일이
      고아로 남는 버그.** 게시글 삭제 시 고아 파일 버그(4번 항목)의 반대 케이스.
      `AttachmentService.uploadOne()`에서 DB insert 실패 시 방금 쓴 디스크 파일을
      정리하도록 수정.
- [x] **[가장 심각] 첨부파일 업로드/삭제에 비밀번호 검증이 아예 빠져있던 인가(authorization)
      버그.** 게시글 수정/삭제는 비밀번호를 확인하는데, `POST /api/boards/{boardId}/files`
      (업로드)와 `DELETE /api/files/{id}`(삭제)는 아무 검증 없이 누구나 호출 가능했음 —
      게시글 id/파일 id만 알면(둘 다 순차 증가하는 정수라 추측도 쉬움) 타인의 게시글에
      파일을 첨부하거나 지울 수 있었음. 실제로 비밀번호 없이 업로드/삭제가 되는 것까지
      재현 확인 후 수정.
      - 업로드: `@RequestParam("password")` 추가, 게시글 비밀번호와 대조
      - 삭제: `AttachmentDeleteRequest(password)` 바디 추가, 첨부파일이 속한 게시글의
        비밀번호와 대조
      - 게시글 A/B 두 개로 "다른 게시글 비밀번호로는 안 되는지"까지 교차 검증 완료
- [x] **첨부파일 원본 파일명(`origin_name VARCHAR(500)`) 길이 미검증.** comment.writer와
      같은 패턴의 버그 — `AttachmentService`에 업로드 전 파일명 길이 검증 추가.
      (500자 넘는 파일명은 Tomcat 자체가 요청 단계에서 먼저 막아서 실제 500 에러로
      재현은 못 했지만, 코드상 동일한 취약점 패턴이라 선제적으로 수정)

**참고 (수정 안 한 부분)**: `AttachmentService.deleteFilesByBoardId()`는 여러 파일 중
하나 삭제에 실패하면 예외를 던지고 게시글 삭제 자체가 취소됨 — 그 시점까지 지운 파일과
아직 안 지운 파일이 섞인 상태가 될 수 있음. DB 트랜잭션과 파일시스템 작업을 같이 묶는 건
근본적으로 어려운 문제라(분산 트랜잭션 문제), 실제 디스크 I/O 에러가 나야 발생하는
아주 드문 케이스라 지금은 그대로 둠.

이걸로 eBrainSoft 게시판 V1.1 스펙 전체(카테고리/게시글/댓글/첨부파일) + 공통 에러 처리 +
발견된 버그 수정까지 완료.

### 7. 테스트 코드 (진행 중)
- [x] `spring-boot-starter-test` 추가 (JUnit5 + Mockito + AssertJ + Spring TestContext)
- [x] `CategoryServiceTest` — 워밍업용, 제일 단순한 도메인부터. Mapper를 mock 처리해서
      Entity -> DTO 변환 로직만 검증 (정상 매핑 + 빈 목록 케이스). 2개 테스트 모두 통과 확인.
- [x] `BoardServiceTest` — 등록/상세조회/수정/삭제/목록 5개 그룹, 총 19개 테스트
      (`@Nested`로 그룹화). 검증 로직, 비밀번호 확인, 페이지네이션 검증, 조회수 +1 반영,
      삭제 시 "첨부파일 정리 -> 게시글 삭제" 순서(`InOrder`)까지 커버. 전부 통과 확인.
- [x] `CommentServiceTest` — 등록/조회 2개 그룹, 총 8개 테스트. 존재하지 않는 게시글,
      작성자/내용 공백·길이 검증(50자/2000자), 정상 등록·조회 매핑까지 커버. 전부 통과 확인.
- [x] `AttachmentServiceTest` — 업로드/조회/다운로드/삭제/게시글삭제시정리 5개 그룹, 총
      14개 테스트. `@TempDir`로 실제 디스크 I/O를 격리해서 검증. 확장자 화이트리스트,
      파일명 길이, 비밀번호 검증, all-or-nothing 저장, DB 실패 시 고아 파일 정리까지
      전부 실제 파일 생성/삭제로 확인 (Mock이 아니라 진짜 파일 존재 여부를 assert).
      전부 통과 확인.
- [x] `BoardControllerTest` — `@WebMvcTest`로 Spring MVC 요청 처리 파이프라인
      (DispatcherServlet, JSON 직렬화, `GlobalExceptionHandler`)까지 통째로 검증. Service는
      `@MockitoBean`으로 대체, DB는 안 건드림. 4개 테스트: Service 예외별 HTTP 상태코드
      매핑(400/404), 정상 응답(200), 그리고 **`page=abc` 요청이 400으로 응답되는지** —
      이건 예전에 실제로 겪었던 "예외 처리기가 Spring 자체 400을 500으로 덮어쓰던 버그"의
      회귀 테스트. 전부 통과 확인.

이걸로 계획했던 테스트 커버리지(Day 1~5) 전부 완료.

- [x] `CategoryControllerTest` — 파라미터/바디 없는 제일 단순한 컨트롤러. 목록 응답,
      빈 배열 응답 2개 테스트. 전부 통과 확인.

- [x] `CommentControllerTest` — 목록 조회, 존재하지 않는 게시글(404), 정상 등록(200),
      검증 실패(400) 4개 테스트. 전부 통과 확인.

- [x] `AttachmentControllerTest` — 멀티파트 업로드(`MockMvc.multipart()`), 다운로드
      (`Content-Disposition` 헤더 + 바이너리 내용 확인), 삭제까지 7개 테스트. 전부 통과 확인.

**이걸로 4개 도메인(Category/Board/Comment/Attachment) 전부 Service + Controller
테스트가 갖춰짐.**

### 8. 완전 통합 테스트
- [x] `BoardIntegrationTest` — `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`로
      애플리케이션 전체(Controller -> Service -> Mapper -> **실제 MySQL**)를 그대로 띄워서
      검증. 지금까지는 Mapper/Service를 Mock으로 대체했지만, 여기서는 XML의 실제 SQL(동적
      조건, 조인, 페이지네이션)이 진짜로 동작하는지까지 확인함.
      - 게시글 등록 -> 조회(카테고리 조인 + 조회수 실제 누적) -> 수정 -> 댓글 등록/조회 ->
        삭제 -> 삭제 후 404 확인, 전체 라이프사이클 1개 테스트
      - 키워드/카테고리 필터가 적용된 검색 결과가 실제 DB에서 정확히 나오는지 1개 테스트
      - `@Transactional`로 각 테스트 종료 시 자동 롤백 -> 실제 DB(`docker compose`로 띄운
        MySQL)에 테스트 데이터가 전혀 안 남는 것까지 직접 확인 (board/comment 3건+1건
        생성했다가 롤백 후 0건인 것 확인)

- [x] `AttachmentIntegrationTest` — 업로드/다운로드/삭제가 실제 디스크+DB+HTTP까지 전부
      합쳐서 동작하는지 검증 (이번 세션에서 버그가 제일 많이 나왔던 도메인이라 제일
      중요한 회귀 테스트). `app.upload-dir`을 `@DynamicPropertySource`로 JUnit `@TempDir`
      로 바꿔치기해서 실제 프로젝트 `uploads/` 폴더는 안 건드림.
      - 업로드 -> 상세조회에 포함 -> 다운로드(원본과 바이트 단위 일치) -> 삭제(디스크
        파일도 실제로 사라짐), 전체 라이프사이클 1개
      - **첨부파일 API를 안 거치고 게시글만 삭제해도 디스크 파일이 같이 정리되는지** —
        예전에 실제로 겪었던 고아 파일 버그의 회귀 테스트를 Mock이 아니라 진짜
        HTTP+DB+디스크로 검증. 1개
      - 테스트 후 실제 `uploads/` 폴더가 그대로 비어있는 것, 실제 DB의 board/files
        테이블도 0건인 것까지 직접 확인.

- [x] **`BoardIntegrationTest`에 등록일 범위(`startDate`/`endDate`) 검색 테스트 추가.**
      Service 단위 테스트는 Mapper를 mock해서 건너뛰었고, 세션 내내 curl로도 실제
      테스트한 적 없던 `DATE_ADD(...)` 로직(종료일 당일까지 포함시키는 SQL)을 처음으로
      실제 DB로 검증. 오늘 등록한 글이 "오늘~오늘" 범위엔 걸리고 먼 과거 범위엔 안
      걸리는 것 확인.

- [x] **버그 발견/수정: 목록 정렬이 동점 상황에서 순서 보장이 안 되던 문제.**
      `board.created_at`/`comment.created_at`이 `DATETIME`이라 초 단위까지만 저장됨 —
      같은 초에 여러 건이 등록되면 `ORDER BY created_at`만으로는 SQL 표준상 순서가
      보장되지 않음 (우연히 맞게 나오고 있었을 뿐, 보장된 동작이 아니었음). `id`를 2차
      정렬 기준으로 추가해서 확정지음 (`BoardMapper.findAll`: `created_at DESC, id DESC`,
      `CommentMapper.findByBoardId`: `created_at ASC, id ASC`). `BoardIntegrationTest`에
      실제로 짧은 시간에 여러 건을 등록해서 순서가 안정적으로 나오는지 검증하는 테스트
      2개 추가.

- [x] **버그 발견/수정: Board의 작성자/제목/내용 검증이 공백만 있는 값을 못 걸러냄.**
      `validateWriter`/`validateTitle`/`validateContent`가 길이만 체크해서, `"    "`
      (공백 4칸)처럼 길이 조건은 만족하지만 실질적으로 빈 값인 입력이 그대로 등록됐음.
      실제로 공백 제목으로 게시글 생성이 되는 것까지 재현 확인. Comment는 이미
      `isBlank()`를 체크하고 있었는데 Board만 빠져있던 것 — `isBlank()` 체크 추가로
      통일. `BoardServiceTest`에 회귀 테스트 추가.

- [x] **점검: 게시글 수정 후 `updatedAt`이 실제로 채워지는지 확인.**
      `BoardIntegrationTest`의 라이프사이클 테스트가 수정 후 `title`/`writer`만
      검증하고 `updatedAt`은 한 번도 검증한 적이 없었음. curl로 직접 재현해보니
      수정 전 `null` → 수정 후 실제 타임스탬프로 정상적으로 채워지는 것을 확인 —
      버그는 아니었지만 테스트 공백이었으므로 `.andExpect(jsonPath("$.updatedAt")
      .exists())` 검증을 라이프사이클 테스트에 보강.

전체 68개 테스트 통과 (Category 2 + Board 20 + Comment 8 + Attachment 14 +
BoardController 4 + CategoryController 2 + CommentController 4 + AttachmentController 7 +
BoardIntegrationTest 5 + AttachmentIntegrationTest 2).
