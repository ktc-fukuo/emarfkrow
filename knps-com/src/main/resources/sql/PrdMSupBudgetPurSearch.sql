SELECT
      a.*
    , (SELECT r1.SUP_NAME FROM MST_SUPPLIER r1 WHERE r1.SUP_CODE = a.SUP_CODE) AS SUP_NAME
FROM
    PRD_M_SUP_BUDGET_PUR a 
WHERE
    1 = 1 
    AND TRIM (a.yyyy) = TRIM (:yyyy) 
    AND TRIM (a.mm) = TRIM (:mm) 
    AND TRIM (a.hinban) = TRIM (:hinban) 
    AND TRIM (a.sup_code) IN (:sup_code) 
    AND a.order_counts = :order_counts 
    AND a.order_counts >= :order_counts_1 
    AND a.order_counts <= :order_counts_2 
    AND a.order_unit = :order_unit 
    AND a.order_unit >= :order_unit_1 
    AND a.order_unit <= :order_unit_2 
    AND a.order_amount = :order_amount 
    AND a.order_amount >= :order_amount_1 
    AND a.order_amount <= :order_amount_2 
    AND a.item_kbn IN (:item_kbn) 
    AND a.time_stamp_create = :time_stamp_create 
    AND a.time_stamp_change = :time_stamp_change 
    AND TRIM (a.user_id_create) = TRIM (:user_id_create) 
    AND TRIM (a.user_id_change) = TRIM (:user_id_change) 
ORDER BY
    a.YYYY, a.MM, a.HINBAN, a.SUP_CODE