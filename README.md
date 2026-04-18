# eb-study-template-1week

eBrainSoft 포트폴리오 스터디 1주차 템플릿 (게시판 프로젝트 스캐폴드).
요구사항은 리포 루트의 `[eBrainSoft] 웹개발 취준생을 위한 포트폴리오 스터디 - 게시판 V1.1.pdf` 참고.

## 기술 스택

- **JDK 17**
- **Jakarta Servlet API 6.0** (Jakarta EE 10)
- **Apache Tomcat 10.1+** (Tomcat 9 이하 불가 — `javax.servlet` 패키지 사용)
- **Gradle 8.14.3** (Wrapper 포함)
- **MySQL 8.0** (Docker)
- **MySQL Connector/J 9.3.0**
- **JUnit 5.12.2**

## 사전 준비

### 1. JDK 17 설치
### 2. Docker Desktop 설치
https://www.docker.com/products/docker-desktop/

### 3. Apache Tomcat 10.1+ 설치
https://tomcat.apache.org/download-10.cgi

## MySQL 실행 (Docker Compose)

```bash
cd docker
docker-compose up -d
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
# WAR 빌드 → build/libs/eb-study-template-1week-1.0-SNAPSHOT.war
./gradlew build

# 테스트
./gradlew test

# 클린
./gradlew clean
```

생성된 WAR를 Tomcat 10.1+ `webapps/`에 배포하거나 IntelliJ의 Tomcat 실행 설정으로 구동.
