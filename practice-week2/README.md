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
- [ ] `entity/Category`
- [ ] `mapper/CategoryMapper` (인터페이스)
- [ ] `resources/mappers/CategoryMapper.xml`
- [ ] `dto/response/CategoryResponse`
- [ ] `service/CategoryService`
- [ ] `controller/CategoryController` (전체 카테고리 목록 조회 API)

### 2. Board CRUD
- [ ] `entity/Board`
- [ ] `mapper/BoardMapper` (인터페이스)
- [ ] `resources/mappers/BoardMapper.xml`
- [ ] `dto/request/BoardCreateRequest`, `BoardUpdateRequest`
- [ ] `dto/response/BoardListResponse`, `BoardDetailResponse`
- [ ] `service/BoardService`
- [ ] `controller/BoardController`
  - [ ] 목록 조회 (검색/기간/페이지네이션)
  - [ ] 상세 조회 (조회수 증가)
  - [ ] 등록
  - [ ] 수정
  - [ ] 삭제 (비밀번호 확인)

### 3. Comment CRUD (등록/조회만)
- [ ] `entity/Comment`
- [ ] `mapper/CommentMapper` (인터페이스)
- [ ] `resources/mappers/CommentMapper.xml`
- [ ] `dto/request/CommentCreateRequest`
- [ ] `dto/response/CommentResponse`
- [ ] `service/CommentService`
- [ ] `controller/CommentController`
  - [ ] 게시글별 댓글 조회
  - [ ] 댓글 등록
