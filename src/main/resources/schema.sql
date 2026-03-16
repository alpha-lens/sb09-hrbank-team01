CREATE TABLE IF NOT EXISTS departments (
    id                BIGSERIAL    PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    description       VARCHAR(500),
    established_date  DATE         NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uk_department_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS binary_contents (
    id            BIGSERIAL    PRIMARY KEY,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size          BIGINT       NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS employees (
    id                BIGSERIAL    PRIMARY KEY,
    employee_number   VARCHAR(20)  NOT NULL,
    name              VARCHAR(50)  NOT NULL,
    email             VARCHAR(100) NOT NULL,
    department_id     BIGINT       NOT NULL,            -- FK는 BIGINT (BIGSERIAL X)
    position          VARCHAR(50)  NOT NULL,
    hire_date         DATE         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    profile_image_id  BIGINT,                           -- 선택적이므로 NULL 허용
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uk_employee_number UNIQUE (employee_number),
    CONSTRAINT uk_employee_email  UNIQUE (email),
    CONSTRAINT chk_employee_status CHECK (status IN ('ACTIVE', 'ON_LEAVE', 'RESIGNED')),

    CONSTRAINT fk_employee_department
    FOREIGN KEY (department_id)
    REFERENCES departments (id)
    ON DELETE RESTRICT,

    CONSTRAINT fk_employee_profile_image
    FOREIGN KEY (profile_image_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
    );

CREATE INDEX IF NOT EXISTS idx_employee_department ON employees (department_id);
CREATE INDEX IF NOT EXISTS idx_employee_status     ON employees (status);
CREATE INDEX IF NOT EXISTS idx_employee_hire_date  ON employees (hire_date);
CREATE INDEX IF NOT EXISTS idx_employee_name       ON employees (name);

CREATE TABLE IF NOT EXISTS employee_histories (
    id               BIGSERIAL    PRIMARY KEY,
    type             VARCHAR(20)  NOT NULL,
    employee_number  VARCHAR(20)  NOT NULL,
    diff_json        TEXT,                              -- 변경 상세 JSON (목록 조회 시 제외)
    memo             VARCHAR(500),                      -- 선택적이므로 NULL 허용
    ip_address       VARCHAR(45)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT chk_history_type CHECK (type IN ('CREATED', 'UPDATED', 'DELETED'))
    );

CREATE INDEX IF NOT EXISTS idx_history_employee_number ON employee_histories (employee_number);
CREATE INDEX IF NOT EXISTS idx_history_type            ON employee_histories (type);
CREATE INDEX IF NOT EXISTS idx_history_created_at      ON employee_histories (created_at);
CREATE INDEX IF NOT EXISTS idx_history_ip_address      ON employee_histories (ip_address);

-- 5. backup (데이터 백업 이력)
CREATE TABLE IF NOT EXISTS backups (
    id          BIGSERIAL    PRIMARY KEY,
    worker      VARCHAR(50)  NOT NULL,                  -- IP 주소 또는 "system"
    started_at  TIMESTAMP    NOT NULL DEFAULT now(),
    ended_at    TIMESTAMP,                              -- 진행중이면 NULL
    status      VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    file_id     BIGINT,                                 -- 완료/실패 시 파일 연결

    CONSTRAINT chk_backup_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED', 'SKIPPED')),
    CONSTRAINT fk_backup_file
    FOREIGN KEY (file_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
    );
