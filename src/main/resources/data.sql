-- 1. departments (부서)
INSERT INTO departments (name, description, established_date) VALUES
                                                                  ('경영지원팀', '인사, 회계 및 총무 업무를 담당합니다.', '2020-01-01'),
                                                                  ('기술개발팀', '플랫폼 서비스의 설계 및 개발을 담당합니다.', '2020-01-01'),
                                                                  ('디자인팀', 'UI/UX 및 브랜드 디자인을 담당합니다.', '2021-03-15'),
                                                                  ('마케팅팀', '콘텐츠 마케팅 및 퍼포먼스 광고를 담당합니다.', '2022-05-20');

-- 2. binary_contents (파일: 프로필 이미지 및 백업용)
INSERT INTO binary_contents (file_name, content_type, size, file_path) VALUES
                                                                           ('profile_kim.png', 'image/png', 102450, '/uploads/profiles/2026/03/kim.png'),
                                                                           ('profile_lee.jpg', 'image/jpeg', 204800, '/uploads/profiles/2026/03/lee.jpg'),
                                                                           ('profile_park.png', 'image/png', 153600, '/uploads/profiles/2026/03/park.png'),
                                                                           ('db_backup_20260313.zip', 'application/zip', 52428800, '/backups/db/20260313.zip');

-- 3. employees (직원)
-- 부서 ID와 프로필 ID는 앞서 생성된 PK 값에 의존합니다. (1, 2, 3...)
INSERT INTO employees (employee_number, name, email, department_id, position, hire_date, status, profile_image_id) VALUES
                                                                                                                       ('EMP2020001', '김철수', 'chulsu.kim@company.com', 1, '팀장', '2020-01-01', 'ACTIVE', 1),
                                                                                                                       ('EMP2021015', '이영희', 'younghee.lee@company.com', 2, '시니어 개발자', '2021-02-10', 'ACTIVE', 2),
                                                                                                                       ('EMP2022033', '박디자인', 'park.dsgn@company.com', 3, '디자이너', '2022-06-01', 'ON_LEAVE', 3),
                                                                                                                       ('EMP2023005', '최개발', 'dev.choi@company.com', 2, '주니어 개발자', '2023-01-15', 'ACTIVE', NULL),
                                                                                                                       ('EMP2024012', '정마케', 'mkt.jung@company.com', 4, '매니저', '2024-03-20', 'RESIGNED', NULL);

-- 4. employee_histories (변경 이력)
INSERT INTO employee_histories (type, employee_number, diff_json, memo, ip_address) VALUES
                                                                                        ('CREATED', 'EMP2020001', '{"name": "김철수", "dept": "경영지원팀"}', '신규 입사 처리', '192.168.0.10'),
                                                                                        ('UPDATED', 'EMP2022033', '{"status": "ON_LEAVE"}', '개인 사정으로 인한 휴직', '192.168.0.15'),
                                                                                        ('CREATED', 'EMP2024012', '{"name": "정마케", "dept": "마케팅팀"}', '신규 입사 처리', '127.0.0.1');

-- 5. backups (백업 이력)
INSERT INTO backups (worker, started_at, ended_at, status, file_id) VALUES
                                                                        ('system', '2026-03-12 02:00:00', '2026-03-12 02:05:00', 'COMPLETED', 4),
                                                                        ('192.168.0.10', '2026-03-13 09:00:00', NULL, 'IN_PROGRESS', NULL),
                                                                        ('system', '2026-03-11 02:00:00', '2026-03-11 02:01:00', 'FAILED', NULL);