CREATE TABLE subscription_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tenant_id UUID NOT NULL REFERENCES companies(id), plan_code VARCHAR(20) NOT NULL REFERENCES plans(code),
  billing_cycle VARCHAR(10) NOT NULL CHECK (billing_cycle IN ('MONTHLY','YEARLY')), proof_ref VARCHAR(200) NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED')), decided_by UUID REFERENCES users(id), decided_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE UNIQUE INDEX ux_subreq_pending ON subscription_requests (tenant_id) WHERE status = 'PENDING';
