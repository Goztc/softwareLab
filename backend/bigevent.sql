-- 创建数据库
create database big_event;

-- 使用数据库
use big_event;
-- start
create table user
(
    id          int unsigned auto_increment comment 'ID'
        primary key,
    username    varchar(20)             not null comment '用户名',
    identity    varchar(50)  default '' null comment '身份',
    comment     varchar(50)  default '' null comment '评语',
    password    varchar(32)             null comment '密码',
    nickname    varchar(10)  default '' null comment '昵称',
    email       varchar(128) default '' null comment '邮箱',
    user_pic    varchar(128) default '' null comment '头像',
    create_time datetime                not null comment '创建时间',
    update_time datetime                not null comment '修改时间',
    constraint username
        unique (username)
)
    comment '用户表';

create table category
(
    id             int unsigned auto_increment comment 'ID'
        primary key,
    category_name  varchar(32)  not null comment '分类名称',
    category_alias varchar(32)  not null comment '分类别名',
    create_user    int unsigned not null comment '创建人ID',
    create_time    datetime     not null comment '创建时间',
    update_time    datetime     not null comment '修改时间',
    constraint fk_category_user
        foreign key (create_user) references user (id)
);

create table article
(
    id          int unsigned auto_increment comment 'ID'
        primary key,
    title       varchar(30)               not null comment '文章标题',
    content     varchar(10000)            not null comment '文章内容',
    cover_img   varchar(128)              not null comment '文章封面',
    state       varchar(3) default '草稿' null comment '文章状态: 只能是[已发布] 或者 [草稿]',
    category_id int unsigned              null comment '文章分类ID',
    create_user int unsigned              not null comment '创建人ID',
    create_time datetime                  not null comment '创建时间',
    update_time datetime                  not null comment '修改时间',
    constraint fk_article_category
        foreign key (category_id) references category (id),
    constraint fk_article_user
        foreign key (create_user) references user (id)
);

create table doctor_patient
(
    doctor_id  int unsigned not null comment '医生ID',
    patient_id int unsigned not null comment '病人ID',
    primary key (doctor_id, patient_id),
    constraint doctor_patient_ibfk_1
        foreign key (doctor_id) references user (id),
    constraint doctor_patient_ibfk_2
        foreign key (patient_id) references user (id)
)
    comment '医生与病人关联表';

create index patient_id
    on doctor_patient (patient_id);

create table uploaded_files
(
    id            int unsigned auto_increment comment '文件记录的唯一标识符'
        primary key,
    associated_id int unsigned  not null comment '与文件相关联的ID',
    file_name     varchar(255)  not null comment '文件名称',
    url           varchar(500)  null,
    file_path     varchar(255)  null comment '文件在服务器上的存储路径',
    age           int           null,
    sex           varchar(10)   null,
    cp            int           null,
    trestbps      int           null,
    chol          int           null,
    fbs           int           null,
    restecg       int           null,
    thalach       int           null,
    exang         int           null,
    oldpeak       decimal(4, 2) null,
    ca            int           null,
    thal          int           null,
    constraint uploaded_files_ibfk_1
        foreign key (associated_id) references user (id)
)
    comment '上传文件信息表';

create index associated_id
    on uploaded_files (associated_id);



-- end
-- 用户表
create table user (
                      id int unsigned primary key auto_increment comment 'ID',
                      username varchar(20) not null unique comment '用户名',
                      password varchar(32)  comment '密码',
                      nickname varchar(10)  default '' comment '昵称',
                      email varchar(128) default '' comment '邮箱',
                      user_pic varchar(128) default '' comment '头像',
                      create_time datetime not null comment '创建时间',
                      update_time datetime not null comment '修改时间'
) comment '用户表';


ALTER TABLE user
    ADD COLUMN identity varchar(50) default '' comment '身份' AFTER username;

-- 创建 doctor_patient 表
create table doctor_patient (
                                doctor_id int unsigned not null comment '医生ID',
                                patient_id int unsigned not null comment '病人ID',
                                primary key (doctor_id, patient_id),
                                foreign key (doctor_id) references user(id),
                                foreign key (patient_id) references user(id)
) comment '医生与病人关联表';

SELECT * FROM user WHERE username = 'niubi';
ALTER TABLE user
    ADD COLUMN comment varchar(50) default '' comment '评语' AFTER identity;

CREATE TABLE uploaded_files (
                                id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '文件记录的唯一标识符',
                                associated_id INT UNSIGNED NOT NULL COMMENT '与文件相关联的ID',
                                file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
                                file_path VARCHAR(255) COMMENT '文件在服务器上的存储路径',
                                FOREIGN KEY (associated_id) REFERENCES user(id) -- 如果 associated_id 是用户 ID，可以这样设置外键
) COMMENT '上传文件信息表';
