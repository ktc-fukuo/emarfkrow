/**
 * MST_TOTAL_COST_REG_LINEグリッド定義
 */

let MstTotalCostRegLineGridColumns = [
    Column.cell('PRO_GROUP_NO', Messages['MstTotalCostRegLineGrid.proGroupNo'], 20, 'primaryKey', null),
    Column.cell('HINBAN', Messages['MstTotalCostRegLineGrid.hinban'], 250, 'primaryKey', null),
    Column.cell('ROUTING', Messages['MstTotalCostRegLineGrid.routing'], 20, 'primaryKey', null),
    Column.refer('WC_CODE', Messages['MstTotalCostRegLineGrid.wcCode'], 30, '', 'WC_NAME'),
    Column.refer('SUP_CODE', Messages['MstTotalCostRegLineGrid.supCode'], 60, '', 'SUP_NAME'),
    Column.text('OPE_DETAIL', Messages['MstTotalCostRegLineGrid.opeDetail'], 100, '', null),
    Column.select('LAST_ROUTING_KBN', Messages['MstTotalCostRegLineGrid.lastRoutingKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.check('TOTAL_COST_TARGET_FLAG', Messages['MstTotalCostRegLineGrid.totalCostTargetFlag'], 10, ''),
    Column.select('COST_RATE_KBN', Messages['MstTotalCostRegLineGrid.costRateKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('UNIT_COST_FIRST', Messages['MstTotalCostRegLineGrid.unitCostFirst'], 90, '', null),
    Column.text('UNIT_COST_SECOND', Messages['MstTotalCostRegLineGrid.unitCostSecond'], 90, '', null),
    Column.text('TOTAL_UNIT_COST', Messages['MstTotalCostRegLineGrid.totalUnitCost'], 90, '', null),
    Column.text('SUM_COST_FIRST', Messages['MstTotalCostRegLineGrid.sumCostFirst'], 90, '', null),
    Column.text('SUM_COST_SECOND', Messages['MstTotalCostRegLineGrid.sumCostSecond'], 90, '', null),
    Column.text('TOTAL_SUM_COST', Messages['MstTotalCostRegLineGrid.totalSumCost'], 90, '', null),
    Column.text('OPE_COUNTS', Messages['MstTotalCostRegLineGrid.opeCounts'], 50, '', null),
    Column.text('OPE_AMOUNTS', Messages['MstTotalCostRegLineGrid.opeAmounts'], 120, '', null),
    Column.text('UNIT_WEIGHT', Messages['MstTotalCostRegLineGrid.unitWeight'], 110, '', null),
    Column.text('OPE_TIME', Messages['MstTotalCostRegLineGrid.opeTime'], 90, '', null),
    Column.text('HUM_PRE_TIME', Messages['MstTotalCostRegLineGrid.humPreTime'], 90, '', null),
    Column.text('HUM_ACT_TIME', Messages['MstTotalCostRegLineGrid.humActTime'], 90, '', null),
    Column.text('MAC_PRE_TIME', Messages['MstTotalCostRegLineGrid.macPreTime'], 90, '', null),
    Column.text('MAC_ACT_TIME', Messages['MstTotalCostRegLineGrid.macActTime'], 90, '', null),
    Column.text('SUM_UNIT_SUPPLY', Messages['MstTotalCostRegLineGrid.sumUnitSupply'], 100, '', null),
    Column.text('MATERIALS_COST', Messages['MstTotalCostRegLineGrid.materialsCost'], 90, '', null),
    Column.text('HUM_LABOR_COST_FIRST', Messages['MstTotalCostRegLineGrid.humLaborCostFirst'], 90, '', null),
    Column.text('HUM_EXPENSES_FIRST', Messages['MstTotalCostRegLineGrid.humExpensesFirst'], 90, '', null),
    Column.text('HUM_LABOR_COST_SECOND', Messages['MstTotalCostRegLineGrid.humLaborCostSecond'], 90, '', null),
    Column.text('HUM_EXPENSES_SECOND', Messages['MstTotalCostRegLineGrid.humExpensesSecond'], 90, '', null),
    Column.text('MAC_LABOR_COST_FIRST', Messages['MstTotalCostRegLineGrid.macLaborCostFirst'], 90, '', null),
    Column.text('MAC_EXPENSES_FIRST', Messages['MstTotalCostRegLineGrid.macExpensesFirst'], 90, '', null),
    Column.text('MAC_LABOR_COST_SECOND', Messages['MstTotalCostRegLineGrid.macLaborCostSecond'], 90, '', null),
    Column.text('MAC_EXPENSES_SECOND', Messages['MstTotalCostRegLineGrid.macExpensesSecond'], 90, '', null),
    Column.cell('TIME_STAMP_CREATE', Messages['MstTotalCostRegLineGrid.timeStampCreate'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('TIME_STAMP_CHANGE', Messages['MstTotalCostRegLineGrid.timeStampChange'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('USER_ID_CREATE', Messages['MstTotalCostRegLineGrid.userIdCreate'], 100, 'metaInfo', null),
    Column.cell('USER_ID_CHANGE', Messages['MstTotalCostRegLineGrid.userIdChange'], 100, 'metaInfo', null),
    Column.text('GROSS_SUM_COST_FIRST', Messages['MstTotalCostRegLineGrid.grossSumCostFirst'], 90, '', null),
    Column.text('GROSS_SUM_COST_SECOND', Messages['MstTotalCostRegLineGrid.grossSumCostSecond'], 90, '', null),
    Column.text('TOTAL_GROSS_SUM_COST', Messages['MstTotalCostRegLineGrid.totalGrossSumCost'], 90, '', null),
    Column.text('GROSS_SUM_UNIT_SUPPLY', Messages['MstTotalCostRegLineGrid.grossSumUnitSupply'], 100, '', null),
];