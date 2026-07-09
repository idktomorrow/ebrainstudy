DROP TABLE IF EXISTS `Comment`;

CREATE TABLE `Comment`
(
    `id`         INT         NOT NULL AUTO_INCREMENT,
    `board_id`   BIGINT      NOT NULL,
    `writer`     VARCHAR(50) NOT NULL,
    `content`    TEXT        NOT NULL,
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `Files`;

CREATE TABLE `Files`
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

DROP TABLE IF EXISTS `Board`;

CREATE TABLE `Board`
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

DROP TABLE IF EXISTS `Category`;

CREATE TABLE `Category`
(
    `id`   INT         NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`)
);

ALTER TABLE `Comment`
    ADD CONSTRAINT `FK_Board_TO_Comment_1` FOREIGN KEY (`board_id`)
        REFERENCES `Board` (`id`) ON DELETE CASCADE;

ALTER TABLE `Files`
    ADD CONSTRAINT `FK_Board_TO_Files_1` FOREIGN KEY (`board_id`)
        REFERENCES `Board` (`id`) ON DELETE CASCADE;

ALTER TABLE `Board`
    ADD CONSTRAINT `FK_Category_TO_Board_1` FOREIGN KEY (`category_id`)
        REFERENCES `Category` (`id`);