-- Drop existing objects
drop security policy if exists TenantAccessPolicy;
go
drop function if exists TenantAccessPredicate;
go
drop table if exists orders;
go
drop sequence if exists orders_seq;
go

-- Create function for row level security predicate
CREATE FUNCTION TenantAccessPredicate( @tenant_id bigint )  
	RETURNS TABLE  WITH SCHEMABINDING AS  
		RETURN  SELECT 1 AS TenantAccessPredicateResult  WHERE @tenant_id = CAST(SESSION_CONTEXT(N'TenantId') AS bigint);
go

-- Create table
create table orders (
	order_id bigint primary key, 
	order_number varchar(50), 
	tenant_id bigint not null, 
	order_date date, 
	order_status varchar(50), 
	customername varchar(100)
);
go

-- Create index on tenant_id
CREATE NONCLUSTERED INDEX [NonClusteredIndex-TenantId] ON orders
(
	[tenant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

-- Create sequence
CREATE SEQUENCE orders_seq
 AS [bigint]
 START WITH 1
 INCREMENT BY 50
 MINVALUE 0
 MAXVALUE 9223372036854775807
 CACHE 
GO

-- Create security policy for Row Level Security (filter for SELECT, block for INSERT/UPDATE/DELETE)
CREATE SECURITY POLICY TenantAccessPolicy
	ADD FILTER PREDICATE TenantAccessPredicate(tenant_id) ON orders ,
	ADD BLOCK  PREDICATE TenantAccessPredicate(tenant_id) ON orders; 
go

-- Insert test data
-- Set session variables and insert data for tenant 1
EXEC sys.sp_set_session_context @key = N'TenantId', @value = 1;  
insert into orders values(1, 'ORD-001', 1, '2025-01-15', 'PENDING', 'Socrates');
insert into orders values(2, 'ORD-002', 1, '2025-02-10', 'COMPLETED', 'Plato');
insert into orders values(3, 'ORD-003', 1, '2025-03-05', 'SHIPPED', 'Aristotle');

-- Set session variables and insert data for tenant 2
EXEC sys.sp_set_session_context @key = N'TenantId', @value = 2;  
insert into orders values(4, 'ORD-004', 2, '2025-01-20', 'PENDING', 'Kant');
insert into orders values(5, 'ORD-005', 2, '2025-02-15', 'COMPLETED', 'Hegel');
insert into orders values(6, 'ORD-006', 2, '2025-03-10', 'CANCELLED', 'Nietzsche');

-- Set session variables and insert data for tenant 3
EXEC sys.sp_set_session_context @key = N'TenantId', @value = 3;  
insert into orders values(7, 'ORD-007', 3, '2025-01-25', 'PENDING', 'Descartes');
insert into orders values(8, 'ORD-008', 3, '2025-02-20', 'SHIPPED', 'Spinoza');

-- Test query
EXEC sys.sp_set_session_context @key = N'TenantId', @value = 1;  
select * from orders;
