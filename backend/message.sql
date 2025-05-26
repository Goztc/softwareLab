-- 创建数据库
create database go_ztc;

-- 使用数据库
use go_ztc;
-- start

CREATE TABLE `user` (
                        `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                        `username` VARCHAR(50) UNIQUE NOT NULL,
                        `password` VARCHAR(100) NOT NULL,
                        `nickname`    varchar(10)  default '' null comment '昵称',
                        `email`       varchar(128) default '' null comment '邮箱',
                        `user_pic`    varchar(128) default '' null comment '头像',
                        `create_time` DATETIME NOT NULL
);

CREATE TABLE `chat_session` (
                                `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                `user_id` BIGINT NOT NULL,
                                `session_name` VARCHAR(100) NOT NULL,
                                `create_time` DATETIME NOT NULL,
                                `update_time` DATETIME NOT NULL,
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `chat_message` (
                                `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                `session_id` BIGINT NOT NULL,
                                `user_id` BIGINT NOT NULL,
                                `content` TEXT NOT NULL,
                                `role` VARCHAR(20) NOT NULL,
                                `create_time` DATETIME NOT NULL,
                                FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`),
                                FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `folder` (
                          `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                          `parent_id` BIGINT DEFAULT 0 COMMENT '父文件夹ID',
                          `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
                          `folder_name` VARCHAR(255) NOT NULL COMMENT '文件夹名称',
                          `create_time` DATETIME NOT NULL,
                          `update_time` DATETIME NOT NULL,
                          FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE `file` (
                        `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                        `user_id` BIGINT NOT NULL COMMENT '上传用户ID',
                        `folder_id` BIGINT DEFAULT 0 COMMENT '所属文件夹ID',
                        `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
                        `file_path` VARCHAR(255) NOT NULL COMMENT '存储路径',
                        `file_type` VARCHAR(50) COMMENT '文件类型',
                        `upload_time` DATETIME NOT NULL COMMENT '上传时间',
                        FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
                        FOREIGN KEY (`folder_id`) REFERENCES `folder`(`id`)
);
