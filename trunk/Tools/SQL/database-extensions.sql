-- Modifications of the default Openbravo POS database
-- for fairpos extensions

-- Column for notes in payments in/out
ALTER TABLE payments ADD COLUMN notes character varying;

-- Column for recommended price, optional from ETL process
ALTER TABLE products ADD COLUMN pricesell_recommended double precision;
-- Column for manual price entry during buy process
ALTER TABLE products ADD COLUMN manual_price boolean;
ALTER TABLE products ALTER COLUMN manual_price SET DEFAULT false;
update products set manual_price=false;
ALTER TABLE products ALTER COLUMN manual_price SET NOT NULL;

-- Make Index pcom_inx_prod NON-unique to add more than one article to hamper/linked articles
DROP INDEX pcom_inx_prod;
CREATE INDEX pcom_inx_prod
  ON products_com
  USING btree
  (product, product2);