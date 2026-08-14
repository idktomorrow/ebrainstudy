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

`application.yaml`에 `spring.servlet.multipart`(최대 10MB/파일, 20MB/요청), `app.upload-dir`(`./uploads`, 루트 `.gitignore`에 이미 포함) 설정 추가.

### 5. 남은 작업
- [ ] `@RestControllerAdvice` 전역 예외 처리 (`IllegalArgumentException`->400, `NoSuchElementException`->404)
