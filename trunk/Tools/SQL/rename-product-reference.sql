-- Example: rename product.reference starting with 'M' to strting with 'A'

-- use this SQL to check first
select * from products where substr(products.reference,1,1)='M'

-- use this SQL to do the renaming
update products set reference='A'||substr(reference,2) where substr(reference,1,1)='M'
