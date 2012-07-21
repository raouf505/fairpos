-- delete articles loaded by ETL
-- DEFINITION: id is shorter than 30 -> no UUID -> loaded by ETL
-- delete combined product (deposit, hamper)
DELETE FROM PRODUCTS_COM;
-- delete all non manually created categories 
DELETE FROM PRODUCTS_CAT WHERE length(PRODUCTS_CAT.product) < 36;
-- delete all non manually created articles 
DELETE FROM PRODUCTS WHERE length(PRODUCTS.id) < 36;
