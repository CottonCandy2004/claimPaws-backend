CREATE TABLE resources (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL COMMENT 'MEETING_ROOM or WORKSTATION',
    floor       VARCHAR(20),
    building    VARCHAR(50),
    capacity    INT,
    description TEXT,
    active      BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     BOOLEAN      DEFAULT FALSE
);

CREATE TABLE reservation_policies (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id              BIGINT       NOT NULL,
    slot_minutes             INT          NOT NULL DEFAULT 30,
    advance_days             INT          NOT NULL DEFAULT 30,
    min_duration_minutes     INT          NOT NULL DEFAULT 30,
    max_duration_minutes     INT          NOT NULL DEFAULT 480,
    cancel_deadline_minutes  INT          NOT NULL DEFAULT 60,
    check_in_window_minutes  INT          NOT NULL DEFAULT 15,
    requires_approval        BOOLEAN      DEFAULT FALSE,
    approval_level           INT          DEFAULT 0,
    active                   BOOLEAN      DEFAULT TRUE,
    created_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted                  BOOLEAN      DEFAULT FALSE
);
