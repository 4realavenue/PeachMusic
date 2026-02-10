ALTER TABLE songs
    ADD COLUMN streaming_status tinyint(1) DEFAULT '0' COMMENT '스트리밍 가능 여부, 0: 스트리밍 불가능 / 1: 스트리밍 가능'