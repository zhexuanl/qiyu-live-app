CREATE DATABASE qiyu_live_common CHARACTER set utf8mb3 COLLATE = utf8_bin;

DROP DATABASE qiyu_live_common;

USE qiyu_live_common;

CREATE TABLE `t_id_generate_config`
(
    `id`             int NOT NULL AUTO_INCREMENT COMMENT 'primary_key',
    `remark`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'description',
    `next_threshold` BIGINT                                                        DEFAULT NULL COMMENT 'Threshold for the current id phase',
    `init_num`       BIGINT                                                        DEFAULT NULL COMMENT 'initial value',
    `current_start`  BIGINT                                                        DEFAULT NULL COMMENT 'The start value of the current id phase',
    `step`           int                                                           DEFAULT NULL COMMENT 'id incremental interval',
    `is_seq`         tinyint                                                       DEFAULT null COMMENT 'in order (0 unordered, 1 ordered)',
    `id_prefix`      VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Business prefix code, if it is not present, it is returned without it',
    `version`        int NOT NULL                                                  DEFAULT '0' COMMENT 'Optimistic Lock Version No.',
    `create_time`    DATETIME                                                      DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME                                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO `t_id_generate_config` (`id`, `remark`, `next_threshold`, `init_num`, `current_start`, `step`, `is_seq`,
                                    `id_prefix`, `version`, `create_time`, `update_time`)
VALUES (1, 'user id generation strategy', 10500, 10000, 10000, 500, 1, 'user_', 0, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)