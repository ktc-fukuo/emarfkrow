SELECT
      a.*
FROM
    M_PKOSE a 
WHERE
    1 = 1 
    AND TRIM (a."HHINBAN") = TRIM (:hhinban) 
    AND TRIM (a."HINMEI") = TRIM (:hinmei) 
    AND TRIM (a."PHINBAN") = TRIM (:phinban) 
    AND TRIM (a."PHINMEI") = TRIM (:phinmei) 
    AND a."TOUROKUBI" = :tourokubi 
    AND TRIM (a."FILLER") = TRIM (:filler) 
ORDER BY
    a."HHINBAN", a."PHINBAN"