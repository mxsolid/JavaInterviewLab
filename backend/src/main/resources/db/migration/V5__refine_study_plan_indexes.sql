-- UNIQUE (study_plan_id, day_number) 已自动创建唯一索引，重复普通索引只会增加路线同步时的写维护成本。
DROP INDEX IF EXISTS idx_study_plan_day_plan_day;

-- 仅 active = TRUE 的部分唯一索引已覆盖当前路线查询；(profile_id, active) 是重复索引。
DROP INDEX IF EXISTS idx_profile_plan_active;
