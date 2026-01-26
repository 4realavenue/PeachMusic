ALTER TABLE artists
    ADD COLUMN profile_image VARCHAR(2048) NULL COMMENT '아티스트 프로필 이미지',
    ADD COLUMN country VARCHAR(100) NULL COMMENT '활동 국가',
    ADD COLUMN artist_type VARCHAR(20) NULL COMMENT '아티스트 유형',
    ADD COLUMN debut_date DATE NULL COMMENT '데뷔일',
    ADD COLUMN bio VARCHAR(500) NULL COMMENT '소개글';
