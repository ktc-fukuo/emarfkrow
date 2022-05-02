SELECT
      a.*
    , (SELECT r1.SOSEN_MEI FROM t_sosen r1 WHERE r1.SOSEN_ID = a.SOSEN_ID) AS SOSEN_MEI
FROM
    t_tenpu_file a 
WHERE
    1 = 1 
    AND a.sosen_id = :sosen_id 
    AND a.oya_sn = :oya_sn 
    AND a.entity_sn = :entity_sn 
    AND a.tenpu_file_sn = :tenpu_file_sn 
    AND a.tenpu_file_mei LIKE CONCAT ('%', :tenpu_file_mei, '%') 
    AND a.tenpu_file = :tenpu_file 
    AND a.insert_dt = :insert_dt 
    AND a.insert_dt >= :insert_dt_1 
    AND a.insert_dt <= :insert_dt_2 
    AND a.insert_by = :insert_by 
    AND a.update_dt = :update_dt 
    AND a.update_dt >= :update_dt_1 
    AND a.update_dt <= :update_dt_2 
    AND a.update_by = :update_by 
    AND CASE WHEN a.delete_f IS NULL THEN '0' ELSE a.delete_f END IN (:delete_f) 
ORDER BY
    a.SOSEN_ID, a.OYA_SN, a.ENTITY_SN, a.TENPU_FILE_SN