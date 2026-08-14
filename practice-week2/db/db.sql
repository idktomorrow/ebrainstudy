-- 자식 테이블부터 삭제해야 FK 제약에 안 걸림 (재실행 가능하게)
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `files`;
DROP TABLE IF EXISTS `board`;
DROP TABLE IF EXISTS `category`;

-- 카테고리: 게시글 분류 태그. DB에 값만 저장
CREATE TABLE `category` (
                            `id`   INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   -- 카테고리 고유 번호
                            `name` VARCHAR(50) NOT NULL           -- 카테고리명 (예: 공지, 자유)
);

-- 게시판: 자유게시판 게시글
CREATE TABLE `board` (
                         `id`          BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,  -- 게시글 고유 번호
                         `category_id` INT NOT NULL,                     -- 소속 카테고리 (category.id 참조)
                         `writer`      VARCHAR(50) NOT NULL,              -- 작성자명 (3자 이상 5자 미만, 서버에서 검증)
                         `title`       VARCHAR(100) NOT NULL,             -- 제목 (4자 이상 100자 미만)
                         `content`     TEXT NOT NULL,                     -- 본문 (4자 이상 2000자 미만)
                         `password`    VARCHAR(255) NOT NULL,             -- 수정/삭제용 비밀번호 (평문 저장)
                         `view_count`  INT NOT NULL DEFAULT 0,            -- 조회수 (상세 조회 시 증가)
                         `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 최초 등록일시
                         `updated_at`  DATETIME NULL                      -- 수정일시. 수정 이력 없으면 NULL(응답 시 '-'로 변환)
);

-- 첨부파일: 게시글 1 : 첨부파일 N. 게시글 삭제 시 함께 삭제됨
CREATE TABLE `files` (
                         `id`          INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   -- 첨부파일 고유 번호
                         `board_id`    BIGINT NOT NULL,                -- 소속 게시글 (board.id 참조)
                         `origin_name` VARCHAR(500) NOT NULL,          -- 사용자가 업로드한 원본 파일명
                         `stored_name` VARCHAR(500) NOT NULL,          -- 서버에 저장된 파일명 (충돌 방지용)
                         `file_path`   VARCHAR(500) NOT NULL,          -- 서버 내 저장 경로
                         `file_size`   BIGINT NOT NULL,                -- 파일 크기(byte)
                         `file_format` VARCHAR(50) NOT NULL            -- 확장자 (예: pdf, png)
);

-- 댓글: 게시글 1 : 댓글 N. 등록/조회만 지원 (수정/삭제 없음), 게시글 삭제 시 함께 삭제됨
CREATE TABLE `comment` (
                           `id`         INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   -- 댓글 고유 번호
                           `board_id`   BIGINT NOT NULL,                -- 소속 게시글 (board.id 참조)
                           `writer`     VARCHAR(50) NOT NULL,           -- 댓글 작성자명
                           `content`    TEXT NOT NULL,                  -- 댓글 내용
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 등록일시 (오래된 순 정렬 시 사용)
);

-- 외래 키 제약
ALTER TABLE `board` ADD CONSTRAINT `FK_Category_TO_Board_1`
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`);

ALTER TABLE `files` ADD CONSTRAINT `FK_Board_TO_Files_1`
    FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE CASCADE;

ALTER TABLE `comment` ADD CONSTRAINT `FK_Board_TO_Comment_1`
    FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE CASCADE;

-- 카테고리 시드 데이터 (테스트용 초기값, 나중에 필요하면 추가/변경 가능)
INSERT INTO `category` (`name`) VALUES
                                    ('공지'),
                                    ('자유');