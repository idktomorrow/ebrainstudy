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

## 앞으로 채울 것

- [ ] `db/db.sql` — 테이블 스키마 직접 설계
- [ ] Category 조회
- [ ] Board CRUD
- [ ] Comment CRUD
