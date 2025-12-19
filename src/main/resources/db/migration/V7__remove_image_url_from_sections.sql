-- V7: sectionsテーブルからimage_urlカラムを削除

-- 画像データがimagesテーブルに移行済みであることを確認
-- （移行が完了し、アプリケーションが新しいimagesテーブルを使用していることを確認後に実行）

ALTER TABLE sections DROP COLUMN IF EXISTS image_url;

COMMENT ON TABLE sections IS 'セクション情報を管理するテーブル（画像は別テーブルimagesで管理）';
