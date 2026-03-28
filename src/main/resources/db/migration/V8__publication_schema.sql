-- ============================================================
-- V8: Publication (area_publik) module schema
-- ============================================================

-- 1. Tabel jenis_dokumen (lookup)
CREATE TABLE `jenis_dokumen` (
  `id`             INT AUTO_INCREMENT PRIMARY KEY,
  `jenis_dokumen`  VARCHAR(100) NOT NULL,
  `status`         INT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed jenis dokumen
INSERT INTO `jenis_dokumen` (`jenis_dokumen`, `status`) VALUES
  ('Pengumuman', 1),
  ('Peraturan', 1),
  ('Surat Keputusan', 1),
  ('Surat Edaran', 1),
  ('Instruksi', 1),
  ('Lain-lain', 1);

-- 2. Tabel area_publik (fixes C1, C2: VARCHAR status + CHECK constraint)
CREATE TABLE `area_publik` (
  `id`                 INT AUTO_INCREMENT PRIMARY KEY,
  `judul`              VARCHAR(255) NOT NULL,
  `desk`               TEXT,
  `type`               INT,
  `status`             VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  `published_date`     DATETIME,
  `notif_flag`         INT NOT NULL DEFAULT 0,
  `file_name`          VARCHAR(255),
  `file_path`          VARCHAR(500),
  `file_size`          INT,
  `created_by_name`    VARCHAR(100),
  `created_by_title`   VARCHAR(100),
  `created_by_user_id` INT,
  `created_at`         DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_publik_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'DELETED')),
  CONSTRAINT fk_publik_type FOREIGN KEY (`type`) REFERENCES `jenis_dokumen`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Indexes
CREATE INDEX idx_publik_status_date ON area_publik (status, published_date DESC);
CREATE INDEX idx_publik_notif       ON area_publik (notif_flag);

-- 4. Tabel allowed_file_type: whitelist ekstensi per konteks upload (fixes L2, L3)
CREATE TABLE `allowed_file_type` (
  `id`          INT AUTO_INCREMENT PRIMARY KEY,
  `context`     VARCHAR(50)  NOT NULL,
  `extension`   VARCHAR(20)  NOT NULL,
  `max_size_mb` INT          NOT NULL DEFAULT 10,
  `is_active`   TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_ctx_ext (context, extension)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: default allowed types untuk Publication
INSERT INTO `allowed_file_type` (`context`, `extension`, `max_size_mb`) VALUES
  ('PUBLICATION', 'pdf',  10),
  ('PUBLICATION', 'doc',  10),
  ('PUBLICATION', 'docx', 10),
  ('PUBLICATION', 'xls',  10),
  ('PUBLICATION', 'xlsx', 10),
  ('PUBLICATION', 'ppt',  20),
  ('PUBLICATION', 'pptx', 20),
  ('PUBLICATION', 'png',   5),
  ('PUBLICATION', 'jpg',   5),
  ('PUBLICATION', 'jpeg',  5),
  ('PUBLICATION', 'zip',  20),
  ('PUBLICATION', 'rar',  20),
  ('PUBLICATION', '7z',   20),
  ('PUBLICATION', 'tar',  20),
  ('PUBLICATION', 'gz',   20);
