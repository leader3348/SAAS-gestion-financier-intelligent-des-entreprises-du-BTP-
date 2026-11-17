-- MizanFlow : schéma initial. Base partagée, isolation par tenant_id (= companies.id).
-- Montants : NUMERIC(14,2), quantités NUMERIC(14,3). Jamais de float.
CREATE TABLE plans (
  code VARCHAR(20) PRIMARY KEY, name VARCHAR(60) NOT NULL,
  price_monthly NUMERIC(10,2) NOT NULL CHECK (price_monthly >= 0),
  price_yearly NUMERIC(10,2) NOT NULL CHECK (price_yearly >= 0),
  max_users INT NOT NULL CHECK (max_users > 0), max_active_projects INT NOT NULL CHECK (max_active_projects > 0),
  features TEXT NOT NULL DEFAULT '', active BOOLEAN NOT NULL DEFAULT TRUE);

CREATE TABLE companies (
  id UUID PRIMARY KEY, name VARCHAR(160) NOT NULL, legal_name VARCHAR(200), ice VARCHAR(20), rc VARCHAR(40), tax_id VARCHAR(40),
  address VARCHAR(300), city VARCHAR(80), phone VARCHAR(30), email VARCHAR(160),
  currency CHAR(3) NOT NULL DEFAULT 'MAD', language CHAR(2) NOT NULL DEFAULT 'fr',
  status VARCHAR(12) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE users (
  id UUID PRIMARY KEY, tenant_id UUID REFERENCES companies(id),
  email VARCHAR(160) NOT NULL, password_hash VARCHAR(100) NOT NULL, full_name VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('SUPER_ADMIN','COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT','FIELD_WORKER','CLIENT')),
  active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((role = 'SUPER_ADMIN' AND tenant_id IS NULL) OR (role <> 'SUPER_ADMIN' AND tenant_id IS NOT NULL)));
CREATE UNIQUE INDEX ux_users_email ON users (lower(email));
CREATE INDEX ix_users_tenant ON users (tenant_id);

CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash CHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL, revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX ix_rt_user ON refresh_tokens (user_id);

CREATE TABLE subscriptions (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), plan_code VARCHAR(20) NOT NULL REFERENCES plans(code),
  billing_cycle VARCHAR(10) NOT NULL DEFAULT 'MONTHLY' CHECK (billing_cycle IN ('MONTHLY','YEARLY')),
  status VARCHAR(12) NOT NULL CHECK (status IN ('TRIAL','ACTIVE','EXPIRED','SUSPENDED','CANCELLED')),
  trial_ends_at TIMESTAMPTZ, period_start TIMESTAMPTZ, period_end TIMESTAMPTZ,
  payment_proof_ref VARCHAR(200), validated_by UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), version BIGINT NOT NULL DEFAULT 0);
CREATE INDEX ix_sub_tenant ON subscriptions (tenant_id, created_at DESC);

-- Macro pour les tables métier : id, tenant_id, created_at, updated_at, version
CREATE TABLE customers (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), name VARCHAR(160) NOT NULL, ice VARCHAR(20),
  email VARCHAR(160), phone VARCHAR(30), address VARCHAR(300), city VARCHAR(80), notes TEXT,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0, UNIQUE (id, tenant_id));
CREATE INDEX ix_customers_tenant_name ON customers (tenant_id, lower(name));

CREATE TABLE suppliers (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), name VARCHAR(160) NOT NULL, ice VARCHAR(20),
  email VARCHAR(160), phone VARCHAR(30), address VARCHAR(300), city VARCHAR(80),
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0, UNIQUE (id, tenant_id));

CREATE TABLE projects (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), reference VARCHAR(30) NOT NULL, name VARCHAR(200) NOT NULL,
  customer_id UUID NOT NULL, description TEXT, site_address VARCHAR(300), manager_id UUID REFERENCES users(id),
  start_date DATE, planned_end_date DATE, budget NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (budget >= 0),
  status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PLANNED','IN_PROGRESS','ON_HOLD','COMPLETED','CANCELLED')),
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, reference), UNIQUE (id, tenant_id),
  FOREIGN KEY (customer_id, tenant_id) REFERENCES customers (id, tenant_id), -- FK composite : pas de lien inter-tenant possible
  CHECK (planned_end_date IS NULL OR start_date IS NULL OR planned_end_date >= start_date));
CREATE INDEX ix_projects_tenant_status ON projects (tenant_id, status);

CREATE TABLE work_logs (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), project_id UUID NOT NULL, author_id UUID NOT NULL REFERENCES users(id),
  work_date DATE NOT NULL, service_type VARCHAR(100) NOT NULL, description TEXT, quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0),
  unit VARCHAR(10) NOT NULL CHECK (unit IN ('M2','M3','ML','UNIT','HOUR','DAY')), unit_price NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
  invoiced_quantity NUMERIC(14,3) NOT NULL DEFAULT 0 CHECK (invoiced_quantity >= 0),
  status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','INVOICED')),
  rejection_reason TEXT, created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY (project_id, tenant_id) REFERENCES projects (id, tenant_id),
  CHECK (invoiced_quantity <= quantity)); -- une quantité ne peut jamais être facturée deux fois
CREATE INDEX ix_wl_tenant_project ON work_logs (tenant_id, project_id, status);

CREATE TABLE work_approvals (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), work_log_id UUID NOT NULL REFERENCES work_logs(id),
  actor_id UUID NOT NULL REFERENCES users(id), from_status VARCHAR(12) NOT NULL, to_status VARCHAR(12) NOT NULL, comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX ix_wa_wl ON work_approvals (work_log_id);

CREATE TABLE work_evidence (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), work_log_id UUID NOT NULL REFERENCES work_logs(id),
  storage_key VARCHAR(300) NOT NULL, original_name VARCHAR(200) NOT NULL, mime_type VARCHAR(100) NOT NULL, size_bytes BIGINT NOT NULL,
  sha256 CHAR(64) NOT NULL, uploaded_by UUID NOT NULL REFERENCES users(id), uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(), comment TEXT);

CREATE TABLE quotations (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), number VARCHAR(30) NOT NULL, customer_id UUID NOT NULL,
  project_id UUID, status VARCHAR(10) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','SENT','ACCEPTED','REJECTED','EXPIRED')),
  valid_until DATE, payment_terms TEXT, total_ht NUMERIC(14,2) NOT NULL DEFAULT 0, total_vat NUMERIC(14,2) NOT NULL DEFAULT 0, total_ttc NUMERIC(14,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, number), FOREIGN KEY (customer_id, tenant_id) REFERENCES customers (id, tenant_id));
CREATE TABLE quotation_lines (
  id UUID PRIMARY KEY, quotation_id UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE, position INT NOT NULL, label VARCHAR(300) NOT NULL,
  quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0), unit VARCHAR(10) NOT NULL, unit_price NUMERIC(14,2) NOT NULL CHECK (unit_price >= 0),
  discount_pct NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (discount_pct BETWEEN 0 AND 100), vat_rate NUMERIC(5,2) NOT NULL DEFAULT 20 CHECK (vat_rate >= 0));

CREATE TABLE work_statements (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), project_id UUID NOT NULL, number VARCHAR(30) NOT NULL, sequence_no INT NOT NULL,
  period_start DATE NOT NULL, period_end DATE NOT NULL, status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','VALIDATED','INVOICED','CANCELLED')),
  total_ht NUMERIC(14,2) NOT NULL DEFAULT 0, previously_invoiced NUMERIC(14,2) NOT NULL DEFAULT 0, net_to_invoice NUMERIC(14,2) NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, number), UNIQUE (project_id, sequence_no), FOREIGN KEY (project_id, tenant_id) REFERENCES projects (id, tenant_id));
CREATE TABLE work_statement_lines (
  id UUID PRIMARY KEY, statement_id UUID NOT NULL REFERENCES work_statements(id) ON DELETE CASCADE, work_log_id UUID NOT NULL REFERENCES work_logs(id),
  quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0), unit_price NUMERIC(14,2) NOT NULL, amount NUMERIC(14,2) NOT NULL);

CREATE TABLE invoices (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), number VARCHAR(30), customer_id UUID NOT NULL, project_id UUID,
  statement_id UUID REFERENCES work_statements(id), quotation_id UUID REFERENCES quotations(id),
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','ISSUED','PARTIALLY_PAID','PAID','CANCELLED')),
  issue_date DATE, due_date DATE, payment_terms TEXT,
  total_ht NUMERIC(14,2) NOT NULL DEFAULT 0, total_vat NUMERIC(14,2) NOT NULL DEFAULT 0, total_ttc NUMERIC(14,2) NOT NULL DEFAULT 0,
  paid_amount NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0), cancel_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  UNIQUE (tenant_id, number), UNIQUE (id, tenant_id), FOREIGN KEY (customer_id, tenant_id) REFERENCES customers (id, tenant_id),
  CHECK (paid_amount <= total_ttc)); -- OVERDUE n'est pas stocké : calculé (échéance dépassée ET solde > 0)
CREATE INDEX ix_inv_tenant_status ON invoices (tenant_id, status, due_date);
CREATE TABLE invoice_lines (
  id UUID PRIMARY KEY, invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE, position INT NOT NULL, label VARCHAR(300) NOT NULL,
  quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0), unit VARCHAR(10) NOT NULL, unit_price NUMERIC(14,2) NOT NULL CHECK (unit_price >= 0),
  discount_pct NUMERIC(5,2) NOT NULL DEFAULT 0, vat_rate NUMERIC(5,2) NOT NULL DEFAULT 20, work_log_id UUID REFERENCES work_logs(id));

CREATE TABLE payments (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), invoice_id UUID NOT NULL, amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
  paid_on DATE NOT NULL, method VARCHAR(12) NOT NULL CHECK (method IN ('TRANSFER','CASH','CHECK','CARD','OTHER')), reference VARCHAR(100),
  recorded_by UUID NOT NULL REFERENCES users(id), created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY (invoice_id, tenant_id) REFERENCES invoices (id, tenant_id));
CREATE INDEX ix_pay_invoice ON payments (invoice_id);

CREATE TABLE expenses (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), project_id UUID, supplier_id UUID, category VARCHAR(60) NOT NULL, description TEXT,
  amount_ht NUMERIC(14,2) NOT NULL CHECK (amount_ht >= 0), vat_rate NUMERIC(5,2) NOT NULL DEFAULT 20, amount_ttc NUMERIC(14,2) NOT NULL,
  expense_date DATE NOT NULL, status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')),
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0);
CREATE TABLE materials (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), reference VARCHAR(40) NOT NULL, name VARCHAR(200) NOT NULL, unit VARCHAR(10) NOT NULL,
  unit_price NUMERIC(14,2) NOT NULL DEFAULT 0, stock_qty NUMERIC(14,3) NOT NULL DEFAULT 0 CHECK (stock_qty >= 0), min_stock NUMERIC(14,3) NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL DEFAULT 0, UNIQUE (tenant_id, reference));
CREATE TABLE stock_movements (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), material_id UUID NOT NULL REFERENCES materials(id), project_id UUID,
  kind VARCHAR(4) NOT NULL CHECK (kind IN ('IN','OUT')), quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0), created_by UUID NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE notifications (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), user_id UUID NOT NULL REFERENCES users(id), channel VARCHAR(10) NOT NULL DEFAULT 'IN_APP',
  title VARCHAR(200) NOT NULL, body TEXT, dedup_key VARCHAR(200) NOT NULL, read_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, channel, dedup_key)); -- idempotence : même événement = une seule notification
CREATE TABLE automation_rules (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), name VARCHAR(120) NOT NULL, trigger_event VARCHAR(40) NOT NULL,
  conditions JSONB NOT NULL DEFAULT '{}', action VARCHAR(30) NOT NULL, recipient_role VARCHAR(20), enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE automation_executions (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), rule_id UUID NOT NULL REFERENCES automation_rules(id), dedup_key VARCHAR(200) NOT NULL,
  status VARCHAR(10) NOT NULL, error TEXT, executed_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE (rule_id, dedup_key));
CREATE TABLE documents (
  id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES companies(id), project_id UUID, customer_id UUID, category VARCHAR(30) NOT NULL, name VARCHAR(200) NOT NULL,
  storage_key VARCHAR(300) NOT NULL, mime_type VARCHAR(100) NOT NULL, size_bytes BIGINT NOT NULL, version_no INT NOT NULL DEFAULT 1, uploaded_by UUID REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY, tenant_id UUID, actor_id UUID, action VARCHAR(60) NOT NULL, entity_type VARCHAR(40), entity_id UUID, details TEXT, ip VARCHAR(45),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE INDEX ix_audit_tenant ON audit_logs (tenant_id, created_at DESC);
