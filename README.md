# eb-study-template-1week

eBrainSoft 포트폴리오 스터디 게시판 프로젝트.
1주차(Servlet/JSP)로 시작해서, 2~3주차부터는 SpringBoot + MyBatis 기반 REST API로 전환했습니다.
요구사항은 리포 루트의 `[eBrainSoft] 웹개발 취준생을 위한 포트폴리오 스터디 - 게시판 V1.1.pdf` 참고.

## 기술 스택

- **JDK 17**
- **Spring Boot 3.5.14** (내장 Tomcat)
- **MyBatis** (`mybatis-spring-boot-starter`)
- **MySQL 8.0** (Docker)
- **springdoc-openapi** (Swagger UI, API 문서 자동 생성)
- **Lombok**, **MapStruct** (Entity ↔ DTO 변환)
- **Gradle 8.14.3** (Wrapper 포함)
- **JUnit 5.12.2**

## 사전 준비

### 1. JDK 17 설치
### 2. Docker Desktop 설치
https://www.docker.com/products/docker-desktop/

## MySQL 실행 (Docker Compose)

```bash
cd docker
docker compose up -d
```

| 항목 | 값 |
|---|---|
| Host | `localhost` |
| Port | `3308` (컨테이너 내부 3306 매핑) |
| Database | `ebrainsoft_study` |
| User / Password | `ebsoft` / `ebsoft` |
| Root Password | `admin!32` |
| Charset | `utf8mb4` / `utf8mb4_unicode_ci` |

## 빌드 & 실행

```bash
# 실행 (내장 Tomcat으로 바로 뜸, 별도 WAR 배포 불필요)
./gradlew bootRun

# 테스트
./gradlew test

# 빌드
./gradlew build
```

기본 포트는 `8080`.

## API 문서

앱 실행 후 아래 주소에서 전체 API 목록/요청·응답 형식을 확인하고, 브라우저에서 바로 호출도 가능합니다.

```
http://localhost:8080/swagger-ui/index.html
```

## 주요 기능

- 카테고리 조회
- 게시글 등록/목록(검색·페이징)/상세/수정/삭제, 비밀번호 확인
- 댓글 등록/조회
- 첨부파일 업로드/다운로드/삭제, 게시글과 연동(등록·수정 시 함께 처리, 삭제 시 정리)
- 전역 예외 처리 (통일된 에러 응답 포맷)
