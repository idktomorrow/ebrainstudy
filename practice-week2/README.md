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
