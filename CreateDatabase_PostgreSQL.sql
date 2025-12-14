-- Drop existing objects
DROP POLICY IF EXISTS tenant_access_policy ON orders;
DROP FUNCTION IF EXISTS tenant_access_predicate(bigint);
DROP TABLE IF EXISTS orders;
DROP SEQUENCE IF EXISTS orders_seq;

-- Create function for row level security predicate
CREATE OR REPLACE FUNCTION tenant_access_predicate(tenant_id bigint)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN (
        tenant_id = NULLIF(current_setting('app.tenant_id', TRUE), '')::bigint
    );
END;
$$ LANGUAGE plpgsql STABLE;

-- Create table
CREATE TABLE orders (
    order_id bigint PRIMARY KEY,
    order_number varchar(50),
    tenant_id bigint NOT NULL,
    order_date date,
    order_status varchar(50),
    customername varchar(100)
);

-- Create index on tenant_id
CREATE INDEX idx_orders_tenant_id ON orders(tenant_id);

-- Create sequence
CREATE SEQUENCE orders_seq
    AS bigint
    START WITH 1
    INCREMENT BY 50
    MINVALUE 0
    MAXVALUE 9223372036854775807
    CACHE 1;

-- Enable Row Level Security
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- Create policy for SELECT (filter) and INSERT/UPDATE/DELETE (block)
CREATE POLICY tenant_access_policy ON orders
    USING (tenant_access_predicate(tenant_id))
    WITH CHECK (tenant_access_predicate(tenant_id));

-- Insert test data
-- Set session variables and insert data for tenant 1
SET app.tenant_id = '1';
INSERT INTO orders VALUES(1, 'ORD-001', 1, '2025-01-15', 'PENDING', 'Socrates');
INSERT INTO orders VALUES(2, 'ORD-002', 1, '2025-02-10', 'COMPLETED', 'Plato');
INSERT INTO orders VALUES(3, 'ORD-003', 1, '2025-03-05', 'SHIPPED', 'Aristotle');

-- Set session variables and insert data for tenant 2
SET app.tenant_id = '2';
INSERT INTO orders VALUES(4, 'ORD-004', 2, '2025-01-20', 'PENDING', 'Kant');
INSERT INTO orders VALUES(5, 'ORD-005', 2, '2025-02-15', 'COMPLETED', 'Hegel');
INSERT INTO orders VALUES(6, 'ORD-006', 2, '2025-03-10', 'CANCELLED', 'Nietzsche');

-- Set session variables and insert data for tenant 3
SET app.tenant_id = '3';
INSERT INTO orders VALUES(7, 'ORD-007', 3, '2025-01-25', 'PENDING', 'Descartes');
INSERT INTO orders VALUES(8, 'ORD-008', 3, '2025-02-20', 'SHIPPED', 'Spinoza');

-- Test query
SET app.tenant_id = '1';
SELECT * FROM orders;
