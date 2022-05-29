/**
 * MFG_TOTAL_COST_REG_LINE_I_Qグリッド定義
 */

let MfgTotalCostRegLineIQGridColumns = [
    Column.cell('YY', Messages['MfgTotalCostRegLineIQGrid.yy'], 40, 'primaryKey', null),
    Column.cell('MM', Messages['MfgTotalCostRegLineIQGrid.mm'], 20, 'primaryKey', null),
    Column.cell('PRO_GROUP_NO', Messages['MfgTotalCostRegLineIQGrid.proGroupNo'], 20, 'primaryKey', null),
    Column.cell('HINBAN', Messages['MfgTotalCostRegLineIQGrid.hinban'], 250, 'primaryKey', null),
    Column.cell('ROUTING', Messages['MfgTotalCostRegLineIQGrid.routing'], 20, 'primaryKey', null),
    Column.refer('WC_CODE', Messages['MfgTotalCostRegLineIQGrid.wcCode'], 30, '', 'WC_NAME'),
    Column.refer('SUP_CODE', Messages['MfgTotalCostRegLineIQGrid.supCode'], 60, '', 'SUP_NAME'),
    Column.text('OPE_DETAIL', Messages['MfgTotalCostRegLineIQGrid.opeDetail'], 100, '', null),
    Column.select('LAST_ROUTING_KBN', Messages['MfgTotalCostRegLineIQGrid.lastRoutingKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.check('TOTAL_COST_TARGET_FLAG', Messages['MfgTotalCostRegLineIQGrid.totalCostTargetFlag'], 10, ''),
    Column.select('COST_RATE_KBN', Messages['MfgTotalCostRegLineIQGrid.costRateKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.text('UNIT_COST_FIRST', Messages['MfgTotalCostRegLineIQGrid.unitCostFirst'], 90, '', null),
    Column.text('UNIT_COST_SECOND', Messages['MfgTotalCostRegLineIQGrid.unitCostSecond'], 90, '', null),
    Column.text('TOTAL_UNIT_COST', Messages['MfgTotalCostRegLineIQGrid.totalUnitCost'], 90, '', null),
    Column.text('SUM_COST_FIRST', Messages['MfgTotalCostRegLineIQGrid.sumCostFirst'], 90, '', null),
    Column.text('SUM_COST_SECOND', Messages['MfgTotalCostRegLineIQGrid.sumCostSecond'], 90, '', null),
    Column.text('TOTAL_SUM_COST', Messages['MfgTotalCostRegLineIQGrid.totalSumCost'], 90, '', null),
    Column.text('OPE_COUNTS', Messages['MfgTotalCostRegLineIQGrid.opeCounts'], 50, '', null),
    Column.text('OPE_AMOUNTS', Messages['MfgTotalCostRegLineIQGrid.opeAmounts'], 120, '', null),
    Column.text('UNIT_WEIGHT', Messages['MfgTotalCostRegLineIQGrid.unitWeight'], 110, '', null),
    Column.text('OPE_TIME', Messages['MfgTotalCostRegLineIQGrid.opeTime'], 90, '', null),
    Column.text('HUM_PRE_TIME', Messages['MfgTotalCostRegLineIQGrid.humPreTime'], 90, '', null),
    Column.text('HUM_ACT_TIME', Messages['MfgTotalCostRegLineIQGrid.humActTime'], 90, '', null),
    Column.text('MAC_PRE_TIME', Messages['MfgTotalCostRegLineIQGrid.macPreTime'], 90, '', null),
    Column.text('MAC_ACT_TIME', Messages['MfgTotalCostRegLineIQGrid.macActTime'], 90, '', null),
    Column.text('SUM_UNIT_SUPPLY', Messages['MfgTotalCostRegLineIQGrid.sumUnitSupply'], 100, '', null),
    Column.text('MATERIALS_COST', Messages['MfgTotalCostRegLineIQGrid.materialsCost'], 90, '', null),
    Column.text('HUM_LABOR_COST_FIRST', Messages['MfgTotalCostRegLineIQGrid.humLaborCostFirst'], 90, '', null),
    Column.text('HUM_EXPENSES_FIRST', Messages['MfgTotalCostRegLineIQGrid.humExpensesFirst'], 90, '', null),
    Column.text('HUM_LABOR_COST_SECOND', Messages['MfgTotalCostRegLineIQGrid.humLaborCostSecond'], 90, '', null),
    Column.text('HUM_EXPENSES_SECOND', Messages['MfgTotalCostRegLineIQGrid.humExpensesSecond'], 90, '', null),
    Column.text('MAC_LABOR_COST_FIRST', Messages['MfgTotalCostRegLineIQGrid.macLaborCostFirst'], 90, '', null),
    Column.text('MAC_EXPENSES_FIRST', Messages['MfgTotalCostRegLineIQGrid.macExpensesFirst'], 90, '', null),
    Column.text('MAC_LABOR_COST_SECOND', Messages['MfgTotalCostRegLineIQGrid.macLaborCostSecond'], 90, '', null),
    Column.text('MAC_EXPENSES_SECOND', Messages['MfgTotalCostRegLineIQGrid.macExpensesSecond'], 90, '', null),
    Column.select('DATA_SOURCE_KBN', Messages['MfgTotalCostRegLineIQGrid.dataSourceKbn'], 10, '', { json: 'MstCodeValueSearch.json', paramkey: 'code_nm', value: 'CODE_VALUE', label: 'CODE_VALUE_MEI' }),
    Column.cell('TIME_STAMP_CREATE', Messages['MfgTotalCostRegLineIQGrid.timeStampCreate'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('TIME_STAMP_CHANGE', Messages['MfgTotalCostRegLineIQGrid.timeStampChange'], 70, 'metaInfo', Slick.Formatters.Extends.DateTime),
    Column.cell('USER_ID_CREATE', Messages['MfgTotalCostRegLineIQGrid.userIdCreate'], 100, 'metaInfo', null),
    Column.cell('USER_ID_CHANGE', Messages['MfgTotalCostRegLineIQGrid.userIdChange'], 100, 'metaInfo', null),
];