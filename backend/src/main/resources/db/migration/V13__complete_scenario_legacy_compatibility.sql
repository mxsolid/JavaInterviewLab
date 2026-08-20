-- 旧基线中的 difficulty 没有默认值；V0.3 场景包未携带该展示字段，统一使用 HARD。
ALTER TABLE scenario ALTER COLUMN difficulty SET DEFAULT 'HARD';
