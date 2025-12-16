-- PostgreSQL cleanup script
-- TRUNCATE with CASCADE automatically handles foreign key constraints
-- RESTART IDENTITY resets the auto-increment counters

TRUNCATE TABLE 
    rma_audit_log, 
    rma_item, 
    rma_inward_gatepass, 
    rma_outward_gatepass, 
    depot_dispatch, 
    rma_request 
RESTART IDENTITY CASCADE;

-- Output confirmation (optional, depending on client)
SELECT 'RMA Tables Cleaned Successfully' AS status;
