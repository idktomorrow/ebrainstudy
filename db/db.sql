DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment`
(
    `id`         INT         NOT NULL AUTO_INCREMENT,
    `board_id`   BIGINT      NOT NULL,
    `writer`     VARCHAR(50) NOT NULL,
    `content`    TEXT        NOT NULL,
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `files`;

CREATE TABLE `files`
(
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `board_id`    BIGINT       NOT NULL,
    `origin_name` VARCHAR(500) NOT NULL,
    `stored_name` VARCHAR(500) NOT NULL,
    `file_path`   VARCHAR(500) NOT NULL,
    `file_size`   BIGINT       NOT NULL,
    `file_format` VARCHAR(500) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `board`;

CREATE TABLE `board`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `category_id` INT          NOT NULL,
    `title`       VARCHAR(100) NOT NULL,
    `writer`      VARCHAR(50)  NOT NULL,
    `content`     TEXT         NOT NULL,
    `password`    VARCHAR(50)  NOT NULL,
    `view_count`  INT          NOT NULL DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category`
(
    `id`   INT         NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`)
);

ALTER TABLE `comment`
    ADD CONSTRAINT `FK_board_TO_comment_1` FOREIGN KEY (`board_id`)
        REFERENCES `board` (`id`) ON DELETE CASCADE;

ALTER TABLE `files`
    ADD CONSTRAINT `FK_board_TO_files_1` FOREIGN KEY (`board_id`)
        REFERENCES `board` (`id`) ON DELETE CASCADE;

ALTER TABLE `board`
    ADD CONSTRAINT `FK_category_TO_board_1` FOREIGN KEY (`category_id`)
        REFERENCES `category` (`id`);