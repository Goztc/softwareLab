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
                        `create_time` DATETIME NOT NULL,
                        `update_time` DATETIME NOT NULL,
                        `secret_key` VARCHAR(32) UNIQUE
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

-- 文件夹表
CREATE TABLE user_folder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    folder_name VARCHAR(255) NOT NULL,
    parent_id BIGINT,  -- 改为BIGINT类型
    user_id BIGINT NOT NULL,  -- 改为BIGINT类型
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_id) REFERENCES user_folder(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文件表
CREATE TABLE user_file (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,  -- 原始文件名
    file_path VARCHAR(500) NOT NULL,      -- 存储路径
    file_size BIGINT NOT NULL,            -- 文件大小（字节）
    file_type VARCHAR(50),                -- 文件类型
    folder_id BIGINT,                        -- 所属文件夹ID
    user_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (folder_id) REFERENCES user_folder(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

