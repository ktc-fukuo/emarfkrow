/**
 * MST_HINBANグリッド定義
 */

let MstHinbanGridColumns = [
    Column.cell('HINBAN', Messages['MstHinbanGrid.hinban'], 250, 'primaryKey', null),
    Column.select('HINBAN_CODE', Messages['MstHinbanGrid.hinbanCode'], 100, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('ITEM', Messages['MstHinbanGrid.item'], 300, '', null),
    Column.select('ITEM_KBN', Messages['MstHinbanGrid.itemKbn'], 20, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.select('SHIKAKE_KBN', Messages['MstHinbanGrid.shikakeKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('LOT_SIZE', Messages['MstHinbanGrid.lotSize'], 60, '', null),
    Column.text('COUNTS', Messages['MstHinbanGrid.counts'], 60, '', null),
    Column.refer('UNIT_CODE', Messages['MstHinbanGrid.unitCode'], 20, '', 'UNIT_NAME'),
    Column.refer('ACCOUNT_CODE', Messages['MstHinbanGrid.accountCode'], 50, '', 'ACCOUNT_NAME'),
    Column.text('SUPPLY_CHANGE', Messages['MstHinbanGrid.supplyChange'], 10, '', null),
    Column.text('SUPPLY_PERMISSION', Messages['MstHinbanGrid.supplyPermission'], 10, '', null),
    Column.text('OFF_POST_NO', Messages['MstHinbanGrid.offPostNo'], 30, '', null),
    Column.text('ISUUE_POST_NO', Messages['MstHinbanGrid.isuuePostNo'], 40, '', null),
    Column.text('STORE_NO', Messages['MstHinbanGrid.storeNo'], 100, '', null),
    Column.select('STOCKS_KIND_KBN', Messages['MstHinbanGrid.stocksKindKbn'], 20, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.select('HINGUN_KBN', Messages['MstHinbanGrid.hingunKbn'], 30, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('ELEC_INV_CONTROL', Messages['MstHinbanGrid.elecInvControl'], 10, '', null),
    Column.select('REST_ARTICLE_KBN', Messages['MstHinbanGrid.restArticleKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('REST_ARTICLE_MAX', Messages['MstHinbanGrid.restArticleMax'], 110, '', null),
    Column.select('COST_AMOUNT_KBN', Messages['MstHinbanGrid.costAmountKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.select('F_COST_AMOUNT_KBN', Messages['MstHinbanGrid.fCostAmountKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.select('S_COST_AMOUNT_KBN', Messages['MstHinbanGrid.sCostAmountKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.refer('HINBAN_OPE_CODE', Messages['MstHinbanGrid.hinbanOpeCode'], 100, '', 'HINBAN_OPE_NAME'),
    Column.check('PERMISSION_FLAG', Messages['MstHinbanGrid.permissionFlag'], 10, ''),
    Column.text('VERSION', Messages['MstHinbanGrid.version'], 50, '', null),
    Column.select('PLAN_CODE', Messages['MstHinbanGrid.planCode'], 300, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.select('POISON_KBN', Messages['MstHinbanGrid.poisonKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('MATERIAL', Messages['MstHinbanGrid.material'], 50, '', null),
    Column.text('MATERIAL_SIZE', Messages['MstHinbanGrid.materialSize'], 300, '', null),
    Column.text('MAKER', Messages['MstHinbanGrid.maker'], 300, '', null),
    Column.text('MATERIAL_RECYCLE', Messages['MstHinbanGrid.materialRecycle'], 50, '', null),
    Column.text('MAKER_SIMUKE', Messages['MstHinbanGrid.makerSimuke'], 30, '', null),
    Column.text('MAKER_WEIGHT', Messages['MstHinbanGrid.makerWeight'], 70, '', null),
    Column.text('ACT_INV_FIRST', Messages['MstHinbanGrid.actInvFirst'], 10, '', null),
    Column.text('ACT_INV_LAST', Messages['MstHinbanGrid.actInvLast'], 10, '', null),
    Column.text('SUP_UNIT', Messages['MstHinbanGrid.supUnit'], 90, '', null),
    Column.text('ROUTING_DEF_GROUP', Messages['MstHinbanGrid.routingDefGroup'], 20, '', null),
    Column.text('COST_CALCULATE_GROUP', Messages['MstHinbanGrid.costCalculateGroup'], 20, '', null),
    Column.text('KBN1', Messages['MstHinbanGrid.kbn1'], 50, '', null),
    Column.text('KBN2', Messages['MstHinbanGrid.kbn2'], 40, '', null),
    Column.text('KBN3', Messages['MstHinbanGrid.kbn3'], 50, '', null),
    Column.text('STOCK_STATUS', Messages['MstHinbanGrid.stockStatus'], 10, '', null),
    Column.date('STOCK_STATUS_DATE', Messages['MstHinbanGrid.stockStatusDate'], 80, '', null),
    Column.check('PRINT_STRUCTURE_FLG', Messages['MstHinbanGrid.printStructureFlg'], 10, ''),
    Column.check('PRINT_WORK_FLOW_FLG', Messages['MstHinbanGrid.printWorkFlowFlg'], 10, ''),
    Column.check('PRINT_WORK_RESULT_FLG', Messages['MstHinbanGrid.printWorkResultFlg'], 10, ''),
    Column.check('PRINT_SHIP_SHEET_FLG', Messages['MstHinbanGrid.printShipSheetFlg'], 10, ''),
    Column.check('PRINT_INSPECT_FLG', Messages['MstHinbanGrid.printInspectFlg'], 10, ''),
    Column.check('PRINT_PRODUCT_BOX_FLG', Messages['MstHinbanGrid.printProductBoxFlg'], 10, ''),
    Column.check('PRINT_HINBAN_STRUCT_FLG', Messages['MstHinbanGrid.printHinbanStructFlg'], 10, ''),
    Column.check('SEMI_PRODUCT_FLG', Messages['MstHinbanGrid.semiProductFlg'], 10, ''),
    Column.text('STOCK_MANAGEMENT_SECTION', Messages['MstHinbanGrid.stockManagementSection'], 10, '', null),
    Column.cell('TIME_STAMP_CREATE', Messages['MstHinbanGrid.timeStampCreate'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('TIME_STAMP_CHANGE', Messages['MstHinbanGrid.timeStampChange'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('USER_ID_CREATE', Messages['MstHinbanGrid.userIdCreate'], 100, 'metaInfo', null),
    Column.cell('USER_ID_CHANGE', Messages['MstHinbanGrid.userIdChange'], 100, 'metaInfo', null),
    Column.text('TOTAL_LT', Messages['MstHinbanGrid.totalLt'], 70, '', null),
    Column.select('COMPANY_DIV_CODE', Messages['MstHinbanGrid.companyDivCode'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('KBN4', Messages['MstHinbanGrid.kbn4'], 100, '', null),
    Column.text('KBN5', Messages['MstHinbanGrid.kbn5'], 100, '', null),
    Column.text('KBN6', Messages['MstHinbanGrid.kbn6'], 100, '', null),
    Column.text('ROHS', Messages['MstHinbanGrid.rohs'], 10, '', null),
    Column.text('TANTAI_LT', Messages['MstHinbanGrid.tantaiLt'], 70, '', null),
    Column.text('TSUKESU', Messages['MstHinbanGrid.tsukesu'], 30, '', null),
    Column.text('EX_TAX_RATE', Messages['MstHinbanGrid.exTaxRate'], 20, '', null),
    Column.longText('MEMO', Messages['MstHinbanGrid.memo'], 300, '', null),
    Column.select('HAIBAN_KBN', Messages['MstHinbanGrid.haibanKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.month('HAIBAN_YM', Messages['MstHinbanGrid.haibanYm'], 60, '', null),
];