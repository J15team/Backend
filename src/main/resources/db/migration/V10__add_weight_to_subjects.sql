-- 題材テーブルに重み（weight）カラムを追加
-- 重みは1〜5の範囲で、学習の優先度を表す

-- weightカラムを追加（デフォルト値は1）
ALTER TABLE subjects ADD COLUMN weight INTEGER NOT NULL DEFAULT 1;

-- CHECK制約を追加（1〜5の範囲）
ALTER TABLE subjects ADD CONSTRAINT chk_weight CHECK (weight >= 1 AND weight <= 5);
