DROP TABLE IF EXISTS `board`;

CREATE TABLE `board`
(
    `id`        BIGINT       NOT NULL AUTO_INCREMENT,
    `category`  VARCHAR(50)  NOT NULL,
    `title`     VARCHAR(100) NOT NULL,
    `writer`    VARCHAR(50)  NOT NULL,
    `password`  VARCHAR(100) NOT NULL,
    `content`   TEXT         NOT NULL,
    `viewCount` INT          NOT NULL DEFAULT 0,
    `createdAt` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` DATETIME     NULL,
    PRIMARY KEY (`id`)
);