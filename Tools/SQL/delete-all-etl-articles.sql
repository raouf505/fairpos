-- delete articles loaded by ETL
-- DEFINITION: id is shorter than 30 -> no UUID -> loaded by ETL
DELETE FROM STOCKCURRENT;
DELETE FROM PRODUCTS WHERE length(PRODUCTS_CAT.product) < 36;
-- delete all non manually created catergories 
DELETE FROM PRODUCTS_CAT WHERE length(PRODUCTS_CAT.product) < 36;
-- delete combined product (deposit, hamper)
DELETE FROM PRODUCTS_COM;
