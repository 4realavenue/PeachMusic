CREATE TABLE users
(
    user_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '유저 식별자',
    name        VARCHAR(50)  NOT NULL COMMENT '유저 이름',
    nickname    VARCHAR(10)  NOT NULL COMMENT '회원 닉네임',
    email       VARCHAR(255) NOT NULL COMMENT '회원 이메일',
    password    VARCHAR(255) NOT NULL COMMENT '회원 비밀번호(BCrypt 인코딩)',
    role        VARCHAR(10)  NOT NULL DEFAULT 'USER' COMMENT '회원 권한',
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '회원 삭제 여부, 0: 삭제 안됨 / 1: 삭제',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시점',
    modified_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시점',

    PRIMARY KEY (user_id),

    UNIQUE KEY uk_users_nickname (nickname),
    UNIQUE KEY uk_users_email (email)

) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;