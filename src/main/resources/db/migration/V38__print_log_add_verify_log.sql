-- V38__print_log_add_verify_log.sql
-- mail-service-xw6: Capture verifier IP for signature verification

ALTER TABLE print_log
  ADD COLUMN verify_log VARCHAR(64) DEFAULT NULL AFTER ip_address;

CREATE INDEX idx_print_log_verify_log ON print_log(verify_log);