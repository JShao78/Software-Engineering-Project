BEGIN;

INSERT INTO customers (id, first_name, last_name, address, email, phone)
VALUES
    ('0ea251d8-4c0e-48cf-afdc-df3830099fe6', 'Max', 'Mustermann', 'Musterstrasse 1, 10115 Berlin', 'max.mustermann@example.com', '+49-30-1234567'),
    ('59fef4d9-e955-419d-b1d3-85d5af93d14b', 'Erika', 'Musterfrau', 'Hauptstrasse 5, 04109 Leipzig', 'erika.musterfrau@example.com', '+49-341-7654321')
ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, product_name, product_number, price, stock)
VALUES
    ('4d5808ce-c3bc-4e15-8600-94db29545f49', 'Wireless Mouse', 'P-1001', 29.99, 200),
    ('f6df79b1-1a6d-4957-8e1d-9fa9329d2f47', 'Mechanical Keyboard', 'P-1002', 89.90, 120)
ON CONFLICT (product_number) DO NOTHING;

INSERT INTO orders (id, customer_id, total_cost, order_status, shipping_status)
VALUES
    ('42f6e557-b3f8-44b1-95e9-210f0c7bf46b', '0ea251d8-4c0e-48cf-afdc-df3830099fe6', 149.88, 'paid', 'processing')
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price)
VALUES
    ('68cf139f-a6df-46a8-bf5b-59ae0fa10b00', '42f6e557-b3f8-44b1-95e9-210f0c7bf46b', '4d5808ce-c3bc-4e15-8600-94db29545f49', 2, 29.99),
    ('407f649d-fcf5-4bc3-99e2-113fa01b1200', '42f6e557-b3f8-44b1-95e9-210f0c7bf46b', 'f6df79b1-1a6d-4957-8e1d-9fa9329d2f47', 1, 89.90)
ON CONFLICT (order_id, product_id) DO NOTHING;

COMMIT;
