/*
Copyright 2022 golorp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * SlickGrid制御
 *
 * @author golorp
 */

// Messagesから取得する項目
let Messages = {};
let optionsSizeSearch = 0;
let optionsSizeDetail = 0;
let columnRegistTs = null;
let columnDelete = null;
let columnUntil = null;
let columnStatus = null;
let columnDetail = null;
let columnReason = null;

$(function() {

    console.info('Gridate init.');

    // グリッド列名取得のためここでMessagesを取得する
    console.info('    Gridate get Messages.');
    $.ajax({
        async: false,
        url: 'messages.json',
        dataType: 'text',
        success: function(data) {
            Messages = JSON.parse(data.substr(15));
        }
    });
    console.debug(Messages);

    optionsSizeSearch = Messages['options.size.search'];
    optionsSizeDetail = Messages['options.size.detail'];
    columnRegistTs = Messages['column.update.timestamp'];
    columnDelete = Messages['column.delete'];
    columnUntil = Messages['column.until'];
    columnStatus = Messages['column.status'];
    columnDetail = Messages['view.detail'];
    columnReason = Messages['column.reason'];

    // グリッドからフォーカスアウト時はグリッドを確定
    $(document).on('click', '*', function(event) {

        // クリックした要素を取得
        var $target = $(event.target);

        // slick-pane内でなければ各グリッドを確定
        if ($target.closest('.slick-pane').length == 0) {
            Slick.GlobalEditorLock.commitCurrentEdit();
        }

        //        // datepicker操作時は何もしない
        //        if ($target.closest('.ui-datepicker-header').length > 0) {
        //        }
    });

    if (columnDelete != null) {

        let camel = Casing.toCamel(columnDelete);
        let upper = columnDelete.toUpperCase();

        /* 削除フラグの反映 */
        $(document).on('change', '.article>form.regist>fieldset>div#' + camel + ' [name$="' + camel + '"]', function() {

            let checked = $(this).prop('checked');

            let $form = $(this).closest('form');

            if ($form.find('[name$="' + camel + '"]')[0] == this) {
                //一番目の項目の場合

                //兄弟モデルに反映
                $form.find('[name$="' + camel + '"]').prop('checked', checked);

                //子モデルに反映
                let $grids = $form.children('[id$="Grid"]');
                for (let i = 0;i < $grids.length;i++) {
                    if ($($grids[i]).prop('id')) {
                        let gridId = $($grids[i]).prop('id');
                        let grid = Gridate.grids[gridId];
                        var dataView = grid.getData();
                        let data = dataView.getItems();
                        for (let r in data) {
                            let row = data[r];
                            for (let columnName in row) {
                                if (columnName == upper) {
                                    row[upper] = checked * 1;
                                }
                            }
                        }
                        dataView.beginUpdate();
                        dataView.setItems(data);
                        dataView.endUpdate();
                        grid.invalidate();
                    }
                }
            } else {
                //二番目以降の項目の場合

                //外した場合は一番目も外す
                if (!checked) {
                    $($form.find('[name$="' + camel + '"]')[0]).prop('checked', checked);
                }
            }
        });
    }

    Base.loaded(function() {

        // グリッドごとにループ
        $('[id$=Grid]').each(function() {

            // 列定義を取得
            let gridColumns = eval(this.id + 'Columns');

            // グリッドdivを取得
            let $gridDiv = $(this);

            let isDialog = $gridDiv.closest('[role=dialog]').length > 0;

            let form = $(this).closest('form[name]')[0];

            /*
             * options
             */

            let options = $.extend(true, {}, Gridate.options);

            // 編集可否
            let editable = $gridDiv.attr('data-editable');
            if (editable) {
                options.editable = eval(editable);
            }

            // 新規行有無
            let addRow = $gridDiv.attr('data-addRow');
            if (addRow && Base.getAuthz(form.action + '?anew') == '') {
                options.enableAddRow = eval(addRow);
            }

            // グリッドdiv直後のページャを取得
            let $pager = $gridDiv.find('~[id$=Pager]');

            // グリッドdivのidを、一旦グリッドIDとして退避
            let gridId = this.id;

            let $dialog = $gridDiv.parents('[role="dialog"]');
            if ($dialog.length > 0) {
                // ダイアログ内のグリッドの場合

                // 「ダイアログID.グリッドID」を正式なグリッドIDとして取得
                gridId = $dialog.attr('aria-describedby') + '.' + this.id;

                // グリッドを配置したフォーム名から検索フォーム名に変換し、検索ボタンが取れれば、検索ボタンの対象グリッドIDも変更
                let $gridForm = $gridDiv.parent('form');
                let gridFormName = $gridForm.attr('name');
                let searchFormName = gridFormName.replace(/SRegist/, 'Search');
                let $searchForm = $dialog.find('form[name="' + searchFormName + '"]');
                let $searchButton = $searchForm.find('[data-gridId="' + this.id + '"]');
                $searchButton.attr('data-gridId', gridId);

                // ページャIDも「ダイアログID.ページャID」に変換
                let pagerId = gridId.replace(/Grid$/, 'Pager');
                $pager.attr('id', pagerId);

                // グリッドIDを更新
                this.id = gridId
            }

            // divの属性からselectionModeを取得
            let selectionMode = $gridDiv.attr('data-selectionMode');

            /*
             * columnsのjsをget
             */

            //カラム定義を取得（パスが跨る時に動かないため廃止）
            //$.getScript('./' + divId + 'Columns.js', function() {

            var columns = $.extend(true, [], gridColumns);

            // 新規行を打てなくなるのでコメントアウト
            //			//主キー列かユニーク列ならエディタをクリア
            //			for (let i in columns) {
            //				let column = columns[i];
            //				if (column.cssClass) {
            //					if (column.cssClass.indexOf('primaryKey') >= 0 || column.cssClass.indexOf('uniqueKey') >= 0) {
            //						column.editor = null;
            //					}
            //				}
            //			}

            //登録権限なしなら列のエディタをクリア
            if (Base.getAuthz(form.action) != '') {
                for (let i in columns) {
                    let column = columns[i];
                    column.editor = null;
                }
            }

            let frozenColumnAdd = 0;

            let checkboxSelectColumn;

            if (selectionMode == 'checkbox') {
                // チェックボックスでの行選択の場合（検索画面の場合）

                if (isDialog) {

                    let gridChooseTitle = Messages['common.grid.choose.title'];
                    let gridChooseTitleWidth = 0;
                    if (gridChooseTitle) {
                        gridChooseTitleWidth = new Blob([gridChooseTitle]).size * 10;
                    }

                    // ダイアログなら選択ボタンを追加
                    columns.unshift({
                        id: 'choose',
                        name: gridChooseTitle,
                        field: 'field',
                        sortable: true,
                        width: gridChooseTitleWidth,
                        label: Messages['common.grid.choose.label'],
                        formatter: Slick.Formatters.Extends.Choose
                    });
                    ++frozenColumnAdd;

                    $dialog.find('button').each(function() {
                        if ($(this).hasClass('selectRows')) {
                            $(this).hide();
                        }
                    });

                } else {
                    // ダイアログでない場合（親画面の場合）

                    let gridLinkTitle = Messages['common.grid.link.title'];
                    let gridLinkTitleWidth = 0;
                    if (gridLinkTitle) {
                        gridLinkTitleWidth = new Blob([gridLinkTitle]).size * 7;
                    }

                    //                    // 固定列がある場合（主キーが１つ以上ある場合）
                    //                    if ($gridDiv.attr('data-frozenColumn') * 1 >= 0) {
                    // 詳細リンク列を追加
                    columns.unshift({
                        id: 'link',
                        name: gridLinkTitle,
                        field: 'field',
                        sortable: true,
                        width: gridLinkTitleWidth,
                        label: Messages['common.grid.link.label'],
                        formatter: Slick.Formatters.Extends.Link
                    });
                    ++frozenColumnAdd;
                    //                    }
                }

                // checkbox指定で、ダイアログ内でないなら、最左列にチェックボックス列を追加
                if (!isDialog && $gridDiv.attr('data-frozenColumn') * 1 >= 0) {

                    // メイングリッドにsubmitボタンがある場合は、行選択用のチェックボックス列を追加
                    if ($('body>.article .regist .buttons button[type="submit"]').length > 0) {
                        checkboxSelectColumn = new Slick.CheckboxSelectColumn({
                            selectableOverride: function(row, dataContext, grid) {
                                let dataItem = dataContext;
                                if (dataItem[columnRegistTs.toLowerCase()] || dataItem[columnRegistTs.toUpperCase()]) {
                                    var UID = Math.round(10000000 * Math.random()) + row;
                                    return dataContext
                                        ? "<input id='selector" + UID + "' type='checkbox' checked='checked'><label for='selector" + UID + "'></label>"
                                        : "<input id='selector" + UID + "' type='checkbox'><label for='selector" + UID + "'></label>";
                                }
                            }
                        });
                        columns.unshift(checkboxSelectColumn.getColumnDefinition());
                        ++frozenColumnAdd;
                    }

                    // 削除権限なら最左列に削除ボタン列を追加
                    if (options.editable) {
                        if (Base.getAuthz(form.action.replace('Regist', 'Delete')) == '') {

                            let gridDeleteTitle = Messages['common.grid.delete.title'];
                            let gridDeleteTitleWidth = 0;
                            if (gridDeleteTitle) {
                                gridDeleteTitleWidth = new Blob([gridDeleteTitle]).size * 7;
                            }

                            let noDelete = false;
                            for (let i in gridColumns) {
                                let col = gridColumns[i];
                                if (col.field.match(new RegExp(columnDelete, 'i')) || col.field.match(new RegExp(columnUntil, 'i'))) {
                                    noDelete = true;
                                    break;
                                }
                            }
                            if (!noDelete) {
                                columns.unshift({
                                    id: 'delete',
                                    name: gridDeleteTitle,
                                    field: 'field',
                                    sortable: true,
                                    width: gridDeleteTitleWidth,
                                    label: Messages['common.grid.delete.label'],
                                    formatter: Slick.Formatters.Extends.Delete
                                });
                                ++frozenColumnAdd;
                            }
                        }
                    }
                }

            } else if (selectionMode == 'link') {
                // リンクでの行選択の場合（詳細画面の場合）

                //最左列に詳細リンク列を追加
                columns.unshift({
                    id: 'link',
                    name: Messages['common.grid.link.title'],
                    field: 'field',
                    sortable: true,
                    width: new Blob([Messages['common.grid.link.title']]).size * 10,
                    label: Messages['common.grid.link.label'],
                    formatter: Slick.Formatters.Extends.Link
                });
                ++frozenColumnAdd;

                // 削除権限なら最左列に削除ボタン列を追加
                if (options.editable) {
                    if (Base.getAuthz(form.action.replace('Regist', 'Delete')) == '') {

                        let noDelete = false;
                        for (let i in gridColumns) {
                            let col = gridColumns[i];
                            if (col.field.match(new RegExp(columnDelete, 'i')) || col.field.match(new RegExp(columnUntil, 'i'))) {
                                noDelete = true;
                                break;
                            }
                        }
                        if (!noDelete) {
                            columns.unshift({
                                id: 'delete',
                                name: Messages['common.grid.delete.title'],
                                field: 'field',
                                sortable: true,
                                width: new Blob([Messages['common.grid.delete.title']]).size * 7,
                                label: Messages['common.grid.delete.label'],
                                formatter: Slick.Formatters.Extends.Delete
                            });
                            ++frozenColumnAdd;
                        }
                    }
                }
            }

            // 固定列（ダイアログ内で固定すると崩れる）
            let frozenColumn = $gridDiv.attr('data-frozenColumn');
            if (frozenColumn) {
                if (!isDialog) {
                    options.frozenColumn = (frozenColumn * 1) + frozenColumnAdd;
                }
            }

            let dataView = new Slick.Data.DataView();
            let grid = new Slick.Grid($gridDiv, dataView, columns, options);
            Gridate.grids[gridId] = grid;

            //            new Slick.Controls.Pager(dataView, grid, $pager, {
            //                showAllText: Messages['common.grid.showAllText'],
            //                showPageText: Messages['common.grid.showPageText'],
            //                showCountText: Messages['common.grid.showCountText']
            //            });

            dataView.onRowCountChanged.subscribe(function(/*e, args*/) {
                grid.updateRowCount();
                grid.render();
            });

            dataView.onRowsChanged.subscribe(function(e, args) {
                grid.invalidateRows(args.rows);
                grid.render();
            });

            /*
             * selectionMode
             */

            if (selectionMode == 'checkbox' && !isDialog && $gridDiv.attr('data-frozenColumn') * 1 >= 0) {
                // 行選択チェックボックス
                if (checkboxSelectColumn) {
                    grid.registerPlugin(checkboxSelectColumn);
                }
                grid.setSelectionModel(new Slick.RowSelectionModel({ selectActiveRow: false }));
            } else if (selectionMode == 'row') {
                // 行選択モード
                grid.setSelectionModel(new Slick.RowSelectionModel());
            } else if (!isDialog) {
                // セル範囲選択モード
                grid.setSelectionModel(new Slick.CellSelectionModel());
            }

            /*
             * subscribe
             */

            /* セル移動 */
            grid.onActiveCellChanged.subscribe(function(e, args) {
                let r = args.row;
                let c = args.cell;
                let g = args.grid;
                console.debug('Gridate active cell change.[r:' + r + ', c:' + c + ']');
                if (c) {
                    //参照モデルなら隠しリンク押下
                    let column = g.getColumns()[c];
                    if (/*column.referField*/column.formatter && column.formatter.name.match('ReferFormatter$') && g.getOptions().editable == true) {
                        let gridNode = g.getContainerNode();
                        let gridId = gridNode.id;
                        console.debug('gridId: ' + gridId);
                        console.debug(column);
                        let $dialog = $(gridNode).closest('[id$="Dialog"]');
                        let $form = $(gridNode).closest('form');
                        let modelNm = gridId.replace(/^.+\.|Grid$/g, '');
                        let id = '';
                        if ($dialog.length) {
                            id = $dialog[0].id + '.';
                        }
                        id += $form.prop('name') + '.' + modelNm + 'Grid.' + column.id;
                        let $link = $('[id="' + id + '"]');
                        $link.click();
                    }
                }
            });
            grid.onActiveCellPositionChanged.subscribe(function() {
                console.debug('Gridate active cell position changed.');
            });
            grid.onAddNewRow.subscribe(function(e, args) {
                //				let data = args.grid.getData();
                //				data.push(args.item);
                //				args.grid.invalidateRow(data.length - 1);
                //				args.grid.updateRowCount();
                //				args.grid.render();

                //新規行追加
                var dataView = grid.getData();
                let data = dataView.getItems();
                args.item['id'] = data.length + 1;
                //                args.item['isNew'] = true; 初期表示の１行目や選択サブで効かなくなる
                data.push(args.item);
                dataView.beginUpdate();
                dataView.setItems(data);
                dataView.endUpdate();

                let g = args.grid;
                let r = g.getActiveCell().row;
                let c = g.getActiveCell().cell;
                let column = g.getColumns()[c]
                let columnName = Casing.toPascal(column.id);
                try {
                    eval(columnName + 'OnChange(g, r, c)');
                } catch (e) {
                    console.debug(e.message);
                }
            });
            //				grid.onAutosizeColumns.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeAppendCell.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onBeforeCellEditorDestroy.subscribe(function(e, args) {
                console.debug('Gridate before cell editor destroy.');
                let editor = args.editor;
                let g = args.grid;
            });
            //				grid.onBeforeColumnsResize.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeDestroy.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onBeforeEditCell.subscribe(function(e, args) {
                console.debug('Gridate before edit cell.');
                //                let $clicked = $(e.target);
                let r = args.row;
                let c = args.cell;
                let g = args.grid;
                let $clicked = $(g.getActiveCellNode());
                let dataItem = g.getDataItem(r);
                if (Gridate.isReadonly($clicked, dataItem, grid, r, c)) {
                    return false;
                }
            });
            //				grid.onBeforeFooterRowCellDestroy.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeHeaderCellDestroy.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeHeaderRowCellDestroy.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeSetColumns.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onBeforeSort.subscribe(function(a, b, c, d, e, f, g) { });
            /* セル値変更 */
            grid.onCellChange.subscribe(function(e, args) {
                console.debug('Gridate on cell change.');
                let column = args.column;
                if (!column) {
                    column = args.grid.getColumns()[args.cell];
                }
                let command = args.command;
                let g = args.grid;
                let r = args.row; // evalで使うため必要
                let c = args.cell; // evalで使うため必要
                let field = column.field;
                //削除フラグオフの親伝播
                if (columnDelete != null) {
                    let camel = Casing.toCamel(columnDelete);
                    let upper = columnDelete.toUpperCase();
                    if (field.toUpperCase() == upper) {
                        let val = args.item[upper];
                        if (!val) {
                            $($(g.getActiveCellNode()).closest('form').find('[name$="' + camel + '"]')[0]).prop('checked', false);
                        }
                    }
                }
                let productSuffixs = Messages['span.product.suffixs'].split(',');
                for (let i in productSuffixs) {
                    if (!productSuffixs[i]) {
                        continue;
                    }
                    let productSuffix = productSuffixs[i].split(':'); // ['_am', '_qt*_pr.2']
                    let spanSuffix = productSuffix[0]; // _am
                    let definition = productSuffix[1].split('.'); // ['_qt*_pr', '2']
                    let formula = definition[0].split('*'); // ['_qt', '_pr']
                    let digits = definition[1]; // 2
                    let leftSuffix = formula[0]; // _qt
                    let rightSuffix = formula[1]; // _pr

                    if (field.match(new RegExp('(' + leftSuffix + '|' + rightSuffix + ')$', 'i'))) {

                        let leftName = null;
                        let rightName = null;
                        let spanName = null;

                        // 接頭辞で一致する場合
                        let prefix = field.replace(new RegExp('(' + leftSuffix + '|' + rightSuffix + ')$', 'i'), ''); // SURYO
                        let leftField = prefix + leftSuffix;
                        let rightField = prefix + rightSuffix;
                        let spanField = prefix + spanSuffix;
                        for (let k in grid.getColumns()) {
                            let c = grid.getColumns()[k];
                            if (c.field.match(new RegExp(leftField + '$', 'i'))) {
                                leftName = c.field;
                            }
                            if (c.field.match(new RegExp(rightField + '$', 'i'))) {
                                rightName = c.field;
                            }
                            if (c.field.match(new RegExp(spanField + '$', 'i'))) {
                                spanName = c.field;
                            }
                        }
                        if (leftName && rightName && spanName) {
                            if (item[leftName] && item[rightName]) {
                                item[spanName] = Math.round(item[leftName] * item[rightName] * digits) / digits;
                                grid.invalidate();
                                break;
                            }
                        }

                        // 接頭辞で一致しない場合
                        for (let k in grid.getColumns()) {
                            let c = grid.getColumns()[k];
                            if (c.field.match(new RegExp(leftSuffix + '$', 'i'))) {
                                leftName = c.field;
                            }
                            if (c.field.match(new RegExp(rightSuffix + '$', 'i'))) {
                                rightName = c.field;
                            }
                            if (c.field.match(new RegExp(spanSuffix + '', 'i'))) {
                                spanName = c.field;
                            }
                        }
                        if (leftName && rightName && spanName) {
                            let item = grid.getDataItem(r);
                            if (item[leftName] && item[rightName]) {
                                item[spanName] = Math.round(item[leftName] * item[rightName] * digits) / digits;
                                grid.invalidate();
                                break;
                            }
                        }
                    }
                }
                //フック済みならイベント発火
                let columnName = Casing.toPascal(column.id);
                try {
                    eval(columnName + 'OnChange(g, r, c)');
                } catch (e) {
                    console.debug(e.message);
                }
            });
            //				grid.onCellCssStylesChanged.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onClick.subscribe(function(e, args) {

                console.debug('Gridate on click cell.');

                let $clicked = $(e.target);

                let r = args.row;
                let c = args.cell;
                let g = args.grid;

                let dataItem = g.getDataItem(r);

                if (Gridate.isReadonly($clicked, dataItem, grid, r, c)) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                }

                if ($clicked.hasClass('gridButton')) {
                    // グリッド行ボタン押下時

                    if ($clicked.hasClass('gridChoose')) {
                        // グリッド行選択ボタン押下時

                        console.info('Gridate gridChoose click.');

                        // 呼び出し元ダイアログIDを取得
                        let $dialog = $clicked.closest('[role="dialog"]');
                        let $dialogDiv = $dialog.find('[id$=Dialog]');
                        let caller = $dialogDiv.attr('data-caller');             // TEntityDialog.TEntityRegistForm.TEntity.betsuSansho1Id
                        console.debug('caller: ' + caller);

                        let callerDialogId = caller.match(/.+Dialog/);           // TEntityDialog
                        let callerFormName = caller.match(/[^\.]+Form/);         // TEntityRegistForm
                        let callerGridName = caller.match(/[^\.]+Grid/);         // TEntityGrid
                        let callerItemName = caller.match(/[^\.]+$/).toString(); // betsuSansho1Id

                        // 項目名の接頭辞を取得
                        let prefix = '';
                        for (let columnName in dataItem) {

                            // DataView用の「id」列ならスキップ
                            if (columnName == 'id') {
                                continue;
                            }

                            // 呼び出し元項目名が当該列名で終わる場合は、接頭辞を取得
                            let pascal = Casing.toPascal(columnName);
                            if (callerItemName.match(pascal + '$')) {                          //      Sansho1Id$
                                prefix = callerItemName.replace(new RegExp(pascal + '$'), ''); // betsu
                                break;
                            }
                        }

                        // 返却先のセレクタを取得
                        let parentSelector = 'body>div.article';
                        if (callerDialogId != undefined && callerDialogId != '') {
                            parentSelector = 'div[id="' + callerDialogId + '"] form[name="' + callerFormName + '"]';
                        } else if (callerFormName != undefined && callerFormName != '') {
                            parentSelector = 'form[name="' + callerFormName + '"]';
                        }

                        let callerGrid = null;
                        let callerR = 0;
                        let callerC = 0;
                        //                        let callerC = 0;
                        if (callerGridName != undefined && callerGridName != '') {
                            let gridId = callerGridName.toString();
                            if (callerDialogId != undefined && callerDialogId != '') {
                                gridId = callerDialogId + '.' + callerGridName;
                            }
                            console.debug(gridId);
                            callerGrid = Gridate.grids[gridId];
                            if (callerGrid.getActiveCell() != null) {
                                callerR = callerGrid.getActiveCell().row;
                                callerC = callerGrid.getActiveCell().cell;
                            } else {
                                callerR = callerGrid.getDataLength();
                            }
                        }

                        for (let columnName in dataItem) {
                            if (columnName != 'ROW_NUM' && columnName != 'id' &&
                                !columnName.match(new RegExp('^' + Messages['column.start'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.until'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.insert.timestamp'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.insert.id'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.insert.mei'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.update.timestamp'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.update.id'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.update.mei'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.delete'] + '$', 'i')) &&
                                !columnName.match(new RegExp('^' + Messages['column.status'] + '$', 'i'))) {
                                // メタ情報以外の項目を親画面に反映

                                let camel = Casing.toCamel(prefix + columnName);
                                if (callerGridName != undefined && callerGridName != '') {
                                    //グリッドの場合
                                    var dataView = callerGrid.getData();
                                    let data = dataView.getItems();
                                    if (!data[callerR]) {
                                        data[callerR] = { id: callerR + 1 };
                                    }
                                    let callerColumnName = Casing.toUpper(camel);
                                    data[callerR][callerColumnName] = dataItem[columnName];
                                    dataView.beginUpdate();
                                    dataView.setItems(data);
                                    dataView.endUpdate();
                                    callerGrid.invalidate();

                                } else {
                                    //フォームの場合
                                    if ($(parentSelector + ' [name$="' + camel + '"]:not([readonly])').length > 0) {
                                        console.debug(parentSelector + ' [name$="' + camel + '"]:not([readonly])');
                                        $(parentSelector + ' [name$="' + camel + '"]:not([readonly])').val([dataItem[columnName]]).trigger('change');
                                    }
                                    if ($(parentSelector + ' [name$="' + camel + '"].forceReadonly').length > 0) {
                                        console.debug(parentSelector + ' [name$="' + camel + '"].forceReadonly');
                                        $(parentSelector + ' [name$="' + camel + '"].forceReadonly').val([dataItem[columnName]]).trigger('change');
                                        Base.readonly($(parentSelector + ' [name$="' + camel + '"].forceReadonly'));
                                    }
                                    if ($(parentSelector + ' span[id$="' + camel + '"]').length > 0) {
                                        console.debug(parentSelector + ' span[id$="' + camel + '"]');
                                        $(parentSelector + ' span[id$="' + camel + '"]').format(dataItem[columnName]);
                                    }
                                }
                            }
                        }

                        //グリッドの場合
                        if (callerGridName != undefined && callerGridName != '') {
                            callerGrid.onCellChange.notify({
                                row: callerR,
                                cell: callerC,
                                item: grid.getDataItem(callerR),
                                grid: callerGrid
                            });
                        }

                        // 一旦、検索条件を開いておく
                        let $searchForm = $dialogDiv.find('[name$="SearchForm"]');
                        let $h2 = $searchForm.prev('h2');
                        let $h2Toggle = $h2.find('[id="h2Toggle"]');
                        if ($h2Toggle.hasClass('ui-icon-triangle-1-e')) {
                            $h2.click();
                        }

                        $dialogDiv.dialog('close');

                    } else if ($clicked.hasClass('gridDelete')) {

                        console.info('Gridate gridDelete click.');

                        if (confirm(Messages['confirm.delete'])) {

                            // グリッドの属性からエンティティ名を取得
                            let entityName = $gridDiv.attr('data-href').replace(/(^.+\/|\.html$)/g, '');

                            // 対象エンティティに削除権限がないならエラー
                            let errorId = Base.getAuthz(form.action.replace('Regist', 'Delete'));
                            if (errorId != '') {
                                e.preventDefault();
                                e.stopPropagation();
                                e.stopImmediatePropagation();
                                alert(Messages[errorId]);
                                return;
                            }

                            let dataView = g.getData();
                            // if (dataItem.isNew) { 初期表示の１行目や選択サブで効かなくなる
                            if (!dataItem[columnRegistTs.toLowerCase()] && !dataItem[columnRegistTs.toUpperCase()]) {
                                //未登録なら画面上で消去
                                dataView.deleteItem(dataItem.id);
                            } else {
                                //登録済みなら物理削除
                                let postJson = {};
                                for (let i in g.getColumns()) {
                                    let column = g.getColumns()[i];
                                    let colCss = column.cssClass;
                                    // 削除フォームを登録フォーム化すると主キー以外の必須チェックに引っかかるためコメントアウト
                                    // if (!colCss || (colCss.indexOf('primaryKey') < 0 && colCss.indexOf('optLock') < 0)) {
                                    //     continue;
                                    // }
                                    let v = dataItem[column.field];
                                    postJson[column.id] = v;
                                }

                                Ajaxize.ajaxPost(entityName + 'Delete.ajax', postJson, function(data) {
                                    let dataItems = dataView.getItems();
                                    dataItems.splice(r, 1);
                                    //g.invalidate();
                                    dataView.beginUpdate();
                                    dataView.setItems(dataItems);
                                    dataView.endUpdate();
                                });
                            }
                        }

                    } else {
                        alert('not implemented.');
                    }

                } else if ($clicked.hasClass('gridLink')) {
                    // グリッド行選択リンク押下時

                    console.info('Gridate gridLink click.');

                    // 対象エンティティに参照権限がないならエラー
                    let authzMsg = Base.getAuthz($gridDiv.attr('data-href'));
                    if (authzMsg != '') {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        alert(Messages[authzMsg]);
                        return;
                    }

                    // グリッドの属性からエンティティ名を取得
                    //let entityName = gridId.replace(/[^\.]+\./, '').replace(/Grid$/, '');
                    let entityName = $gridDiv.attr('data-href').replace(/(^.+\/|\.html$)/g, '');

                    if (e.target.id) {
                        // ファイル列の場合

                        // ダウンロードURL
                        let href = entityName + 'Download.link?name=' + e.target.id;

                        //let dataItem = g.getDataItem(r);

                        // グリッド列定義でループして、主キー列のみURL引数に設定
                        for (let i in g.getColumns()) {
                            let column = g.getColumns()[i];
                            if (!column.cssClass || column.cssClass.indexOf('primaryKey') < 0) {
                                continue;
                            }
                            let v = dataItem[column.field];
                            href += '&' + column.id + '=' + v;
                        }

                        e.target.href = href;

                    } else {
                        // 詳細リンクの場合

                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();

                        Gridate.openDetail($gridDiv.prop('id'), entityName, g.getColumns(), g.getDataItem(r));
                    }
                }
            });
            //				grid.onColumnsDrag.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onColumnsReordered.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onColumnsResizeDblClick.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onColumnsResized.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onCompositeEditorChange.subscribe(function() {
                console.debug('Gridate on composite editor change.');
            });
            //				grid.onContextMenu.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onDblClick.subscribe(function(e, args) {

                console.debug('Gridate on double click cell.');

                let $clicked = $(e.target);

                let r = args.row;
                let c = args.cell;
                let g = args.grid;

                let dataItem = g.getDataItem(r);

                if (Gridate.isReadonly($clicked, dataItem, grid, r, c)) {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                }
            });
            //				grid.onDrag.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onDragEnd.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onDragInit.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onDragStart.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onFooterClick.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onFooterContextMenu.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onFooterRowCellRendered.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderCellRendered.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderClick.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderContextMenu.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderMouseEnter.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderMouseLeave.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderRowCellRendered.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderRowMouseEnter.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onHeaderRowMouseLeave.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onKeyDown.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onMouseEnter.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onMouseLeave.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onRendered.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onScroll.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onSelectedRowsChanged.subscribe(function(e, args) {
            //					console.debug(e);
            //					console.debug(args);
            //				});
            //				grid.onSetOptions.subscribe(function(a, b, c, d, e, f, g) { });
            grid.onSort.subscribe(function(e, args) {
                var cols = args.sortCols;
                args.grid.getData().sort(function(dataRow1, dataRow2) {
                    for (var i = 0, l = cols.length;i < l;i++) {
                        var field = cols[i].sortCol.field;
                        var sign = cols[i].sortAsc ? 1 : -1;
                        var value1 = dataRow1[field], value2 = dataRow2[field];
                        var result = (value1 == value2 ? 0 : (value1 > value2 ? 1 : -1)) * sign;
                        if (result != 0) {
                            return result;
                        }
                    }
                    return 0;
                });
                grid.invalidate();
            });
            //				grid.onValidationError.subscribe(function(a, b, c, d, e, f, g) { });
            //				grid.onViewportChanged.subscribe(function(a, b, c, d, e, f, g) { });

            // plugin
            let ExternalCopyOption = {
                //copyCellStyle               :コピーされたセルに使用されるcss classNameを設定します。デフォルト："copied"
                //copyCellStyleLayerKey       :コピーされたセルのcss値を設定するためのレイヤーキーを設定します。デフォルト： "copy-manager"
                //dataItemColumnValueExtractor:カスタム列値抽出関数を指定するオプション
                //dataItemColumnValueSetter   :カスタム列値セッター関数を指定するオプション
                /*
                 * 貼り付けアクションのカスタムハンドラーを指定するオプション
                 */
                clipboardCommandHandler: function(clipCommand) {

                    // ペースト実行
                    clipCommand.execute();

                    // 表示専用列を元の値に戻す
                    var r = clipCommand.activeRow;
                    var c = clipCommand.activeCell;
                    var h = clipCommand.h;
                    var w = clipCommand.w;
                    var columns = grid.getColumns();

                    var data = grid.getData();

                    // DataViewの場合
                    if (data.getItems) {
                        data = data.getItems();
                    }

                    // 固定列（editorなし）なら元の値に書き戻す
                    for (var i = 0;i < w;i++) {
                        var column = columns[i + c];
                        if (column && !column.editor) {
                            for (var j = 0;j < h;j++) {
                                data[j + r][column.field] = clipCommand.oldValues[j][i];
                            }
                        }
                    }

                    grid.invalidate();

                    // 選択列をクリアする
                    if (grid.getSelectionModel().pluginName == 'RowSelectionModel') {
                        grid.setSelectedRows([]);
                    }

                    // コールバックを実行
                    try {
                        eval(Gridate.getGridName(grid) + 'Paste(grid, r, c, h, w)');
                    } catch (e) {
                        // 未実装ならコンソールに通知
                        console.info(e.message);
                    }
                },
                //includeHeaderWhenCopying    :trueに設定すると、プラグインは各列からnameプロパティ（通常はヘッダーに表示されるもの）を取得し、それをクリップボードにコピーされるテキストの最初の行として配置します
                //bodyElement                 :非表示のテキストボックスに追加されるカスタムDOM要素を指定するオプション。グリッドがモーダルダイアログ内にある場合に便利です。
                /*
                 * コピーアクションの初期化時に実行するオプションのハンドラー
                 */
                onCopyInit: function() {
                    //if (grid.getSelectionModel().pluginName == 'RowSelectionModel') {
                    //    alert('チェックボックスで選択した行をコピーします。');
                    //} else if (grid.getSelectionModel().pluginName == 'CellSelectionModel') {
                    //    alert('セルの値をコピーします。');
                    //}
                },
                //onCopySuccess               :コピーアクションが完了したときに実行するオプションのハンドラー
                /**
                 * この関数が提供されない場合、貼り付けがテーブルの下部からオーバーフローした場合にテーブルに行を追加する関数。新しい行は無視されます。
                 * @param {any} count
                 */
                newRowCreator: function(count) {
                    for (var i = 0;i < count;i++) {
                        var item = {
                            id: grid.getData().getLength()
                        }
                        grid.getData().addItem(item);
                    }
                },
                readOnlyMode: false,
                //headerColumnValueExtractor  :カスタム列ヘッダー値抽出関数を指定するオプション
            };

            if (grid.getSelectionModel()) {
                // グリッドに外部コピープラグインを設定（Ctrl+C、Ctrl+Vを有効化）
                grid.registerPlugin(new Slick.CellExternalCopyManager(ExternalCopyOption));
            }
        });
    });
});

var Gridate = {

    grids: {},

    /** グリッドオプション */
    options: {

        /** グリッド自体の編集可否 */
        editable: true,

        /** セル内文字列選択可否 */
        enableTextSelectionOnCells: true,

        /** 複数列ソート可否 */
        multiColumnSort: true,

        // ヘッダーの上
        //createPreHeaderPanel: true,
        //showPreHeaderPanel: true,
        //preHeaderPanelHeight: 23,
        //explicitInitialization: true,

        // グリッド内の１行目
        //showHeaderRow: true,

        // headerRowと明細の間
        //showTopPanel: true,

        //createFooterRow: true,
        //showFooterRow: true,
    },

    refresh: function(gridId, data, totalRows, currentPage) {
        if (totalRows) {
            let rows = $('[name="rows"]').val();
            let maxPage = Math.ceil(totalRows / rows);

            let pagerId = gridId.replace(/Grid$/, 'Pager');
            let $pager = $('[id="' + pagerId + '"]');
            let html = '';
            for (let i = 1;i <= maxPage;i++) {
                if (i == currentPage) {
                    html += '<a>' + i + '</a> ';
                } else {
                    html += '<a href="javascript:void(0);" onclick="Gridate.paginate(\'' + gridId + '\',' + i + ')">' + i + '</a> ';
                }
            }
            $pager.html(html);
        }
        let grid = Gridate.grids[gridId];
        if (grid) {
            if (!data) {
                data = [];
            }
            // id列がなければ、子モデルのためrows指定が無かったという事なので、連番を付与
            for (let i in data) {
                let item = data[i];
                if (item['id']) {
                    break;
                }
                item['id'] = i;
            }
            grid['orgData'] = JSON.parse(JSON.stringify(data));
            //		if (data.length > 0 && data[0]['id']) {
            var dataView = grid.getData();
            dataView.beginUpdate();
            dataView.setItems(data);
            dataView.endUpdate();
            //		} else {
            //			grid.setData(data);
            //			grid.invalidate();
            //		}
            if (grid.getSelectionModel()) {
                grid.setSelectedRows([]);
            }
        }
    },

    paginate: function(gridId, page) {
        let $button = $('[data-gridid="' + gridId + '"]');
        let $form = $button.closest('form');
        let $page = $form.find('[name="page"]');
        $page.val(page);
        $button.click();
        $page.val(0);
    },

    /**
     * 詳細画面起動
     * レコード内に「TABLE_NAME」列があればサブウィンドウ
     * なければentityNameのダイアログ
     * @param gridId     グリッドID
     * @param entityName ダイアログ用のエンティティ名
     * @param columns    グリッド列定義
     * @param item       グリッド行
     */
    openDetail: function(gridId, entityName, columns, item) {

        console.info('Gridate.openDetail ' + gridId + ', ' + entityName);
        console.debug('Gridate.openDetail columns = ' + JSON.stringify(columns));
        console.debug('Gridate.openDetail item = ' + JSON.stringify(item));

        // TABLE_NAME列の検査
        let tableName = null;
        for (let columnName in item) {
            if (columnName.toUpperCase() == columnDetail.toUpperCase()) {
                tableName = item[columnName];
                break;
            }
        }

        if (tableName == null) {

            // グリッドIDからダイアログIDを取得
            let dialogId = entityName + 'Dialog';

            // グリッド列定義でループ
            for (let i in columns) {
                let column = columns[i];

                // 主キー列でなければスキップ
                //			if (column.cssClass != 'primaryKey') {
                //				continue;
                //			}

                // 反映先の項目名を取得
                let camel = Casing.toCamel(column.field);

                // 反映値を取得
                let v = item[column.field];

                let $item = $('[id="' + dialogId + '"] [name="' + camel + '"]');
                if ($item.length > 0) {

                    for (let i = 0;i < $item.length;i++) {
                        if ($item[i].type == 'file') {
                            continue;
                        } else if ($item[i].type == 'month') {
                            $('[id="' + dialogId + '"] span[id="' + camel + '"]').format(v);
                            v = v.replace(/(\d{4})(\d{2})/, '$1-$2');
                            $($item[i]).val([v]);
                        } else {
                            $($item[i]).val([v]);
                            $('[id="' + dialogId + '"] span[id="' + camel + '"]').format(v);
                        }
                    }

                } else {

                    $item = $('[id="' + dialogId + '"] [name="' + entityName + '.' + camel + '"]');
                    if ($item.length > 0) {

                        for (let i = 0;i < $item.length;i++) {
                            if ($item[i].type == 'file') {
                                continue;
                            } else if ($item[i].type == 'month') {
                                if (v) {
                                    $('[id="' + dialogId + '"] span[id="' + entityName + '.' + camel + '"]').format(v);
                                    v = v.replace(/(\d{4})(\d{2})/, '$1-$2');
                                    $($item[i]).val([v]);
                                }
                            } else {
                                $($item[i]).val([v]);
                                $('[id="' + dialogId + '"] span[id="' + entityName + '.' + camel + '"]').format(v);
                            }
                        }
                    }
                }

                //if ($item.hasClass('primaryKey')) {
                //$item.attr('readonly', true).attr('tabindex', '-1').addClass('readonly');
                //$item.next('a').hide();
                //}
            }

            let $dialogDiv = $('div[id="' + dialogId + '"]');

            // 呼び出し元
            $dialogDiv.attr('data-caller', gridId);

            $dialogDiv.dialog('open');

        } else {
            //TABLE_NAME列があった場合（VIEWの場合）

            let primaryKey = '';
            let queryString = '';

            // グリッド列定義でループ
            for (let i in columns) {
                let column = columns[i];
                let k = Casing.toCamel(column.field);
                let v = item[column.field];

                if (v == null) {
                    continue;
                }

                // windowName用に主キーを退避
                if (column.cssClass != null && column.cssClass.indexOf('primaryKey') >= 0) {
                    if (primaryKey == '') {
                        primaryKey += '?';
                    } else {
                        primaryKey += '&';
                    }
                    primaryKey += k + '=' + v;
                }

                // URL引数用に各列値を退避
                if (queryString == '') {
                    queryString += '?';
                } else {
                    queryString += '&';
                }
                queryString += k + '=' + v;
            }

            queryString += '&isSilent=true';

            entityName = Casing.toPascal(tableName);

            //別ウィンドウにするためwidth/heightを指定
            let width = window.outerWidth / 3 * 2;
            let height = window.outerHeight / 3 * 2;
            let top = window.screenTop + (window.innerHeight / 2) - (height / 2);
            let left = window.screenLeft + (window.innerWidth / 2) - (width / 2);
            window.open('./' + entityName + '.html' + queryString, entityName + primaryKey, 'width=' + width + 'px,height=' + height + 'px,top=' + top + 'px,left=' + left + 'px');
        }
    },

    /**
     * 読み取り専用セルか評価
     * @param $clicked
     * @param dataItem
     * @param grid
     * @param r
     * @param c
     */
    isReadonly: function($clicked, dataItem, grid, r, c) {

        //ステータス区分列なら非活性
        if (grid.getColumns()[c].field == columnStatus.toLowerCase() || grid.getColumns()[c].field == columnStatus.toUpperCase()) {
            return true;
        }

        //        if (!dataItem || dataItem.isNew) { 初期表示の１行目や選択サブで効かなくなる
        if (!dataItem || (!dataItem[columnRegistTs.toLowerCase()] && !dataItem[columnRegistTs.toUpperCase()])) {
            //新規行の場合

            //採番キーなら非活性
            if ($clicked.hasClass('numbering')) {
                return true;
            }

        } else {
            //既存データの場合

            //主キーとユニークキーは非活性
            if ($clicked.hasClass('primaryKey') || $clicked.hasClass('uniqueKey')) {
                return true;
            }

            //ステータス区分が「1」以上なら非活性
            if (dataItem[columnStatus.toLowerCase()] >= 1 || dataItem[columnStatus.toUpperCase()] >= 1) {
                return true;
            }
        }

        return false;
    },

    /**
     * グリッドの編集状態確定
     */
    fix: function() {

        // 現在のスコープの全SlickGridを確定する
        Slick.GlobalEditorLock.commitCurrentEdit();

        // SlickGridに設定済みのvalidatorがエラーでなければ、編集状態が確定され、isActiveがfalseになる
        return Slick.GlobalEditorLock.isActive() != true;
    },

};
