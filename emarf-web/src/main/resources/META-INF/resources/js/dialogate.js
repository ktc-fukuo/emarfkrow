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
 * dialog制御
 *
 * @author golorp
 */

$(function() {

    console.info('Dialogate init.');

    // 親画面に複数のフォームがある場合に、IDが重複しないよう、ID項目にフォーム名を接頭
    $('[id]').each(function() {
        if (this.tagName == 'INPUT' || this.tagName == 'TEXTAREA') {
            let $me = $(this);
            let $form = $me.closest('form');
            let id = $form.prop('name') + '.' + this.id;
            $me.prop('id', id);
            //labelのfor属性も更新
            let labels = $me.prevAll('LABEL');
            if (labels.length > 0) {
                $(labels[0]).prop('for', id);
            }
        }
        if (this.tagName == 'A' || this.tagName == 'FIELDSET') {
            let $me = $(this);
            let $form = $me.closest('form');
            let id = $form.prop('name') + '.' + this.id;
            $me.prop('id', id);
        }
    });

    /*
     * [target=dialog]のリンクから、ダイアログのhtmlを画面に追加
     */
    $('a[target="dialog"], div[data-href]').each(function() {
        Dialogate.enable(this, "  ", $(this).hasClass('refer'));
    });

    /*
     * [target=dialog]のアンカー押下でダイアログ表示
     */
    $(document).on('click', 'a[target="dialog"]', function(event) {

        console.info('Dialogate ancher on click.');

        // イベントキャンセル
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        // リンクを取得
        let $clicked = $(this);

        // 表示するダイアログを取得
        let href = $clicked.attr('href');
        let dialogId = href.replace(/(^.+\/|\.html(\?.+)?$)/g, '') + 'Dialog';
        let $dialogDiv = $('div[id="' + dialogId + '"]');

        //選択サブなら制約条件を表示
        if ($clicked.hasClass('correct')) {
            $dialogDiv.find('div.stint').show();
        }

        // 参照ダイアログの場合、リンクのIDとダイアログ内の項目のIDを比較し、呼び出し元での接頭辞を評価
        let prefix = '';
        let isRefer = $clicked.hasClass('refer');
        if (isRefer) {
            let linkId = $clicked.prop('id');
            let linkIds = linkId.split('.');
            let linkItemName = linkIds[linkIds.length - 1];                            // betsuSanshoId
            $dialogDiv.find('input').each(function() {
                let inputName = this.name;
                let inputNames = inputName.split('.');
                let inputItemName = inputNames[inputNames.length - 1];
                if (linkItemName.match(Casing.toPascal(inputItemName) + '$')) {
                    prefix = linkItemName.replace(Casing.toPascal(inputItemName), ''); // betsu
                }
            });
        }

        // 呼び出し元の入力項目を取得
        let $form = $clicked.closest('form');
        let $sendInputs;
        if (isRefer) {
            // 参照ダイアログなら接頭辞に合致する項目を取得（接頭辞がなければ全てを取得する）
            $sendInputs = $form.find('input[type="text"][name*=".' + prefix + '"]');
        } else {
            // 単票ダイアログなら主キー項目だけを取得
            $sendInputs = $form.find('input.primaryKey');
        }

        // 呼び出し元の入力項目でループしてダイアログ内に反映
        $sendInputs.each(function() {
            Dialogate.reflect2Dialog($dialogDiv, prefix, this.name, $(this).val());
        });

        //呼び出し先の制約項目があれば、呼び出し元から取得
        let stints = $dialogDiv.find('span.stint,input.stint');
        for (let i = 0;i < stints.length;i++) {
            let stint = stints[i];
            let names = stint.id.split('.');
            let name = names[names.length - 1];
            let parentVal = $form.find('[name$="' + name + '"]').val();
            $(stint).text(parentVal);
            $(stint).val([parentVal]);
        }

        // 集約先追加の場合
        let isSummary = $clicked.hasClass('summary');
        if (isSummary) {

            // グリッド取得
            let $gridDiv = $form.find('[id$="Grid"]');
            let gridId = $gridDiv.prop('id');
            let grid = Gridate.grids[gridId];
            if (grid.getSelectedRows().length == 0) {
                alert(Messages['error.notexist.selectedRows']);
                return false;
            }

            // グリッド列から主キーのIDとカラム名をCSVで取得（？）（単独キーしかsummaryにしてないはず）
            let columns = grid.getColumns();
            let id = '';
            let field = '';
            for (let i = 0;i < columns.length;i++) {
                let column = columns[i];
                let isPK = column.cssClass != null && column.cssClass.indexOf('primaryKey') >= 0;
                if (isPK) {
                    if (id != '') {
                        id += ', ';
                        field += ', ';
                    }
                    id += column.id;
                    field += column.field;
                }
            }

            // グリッドデータ
            let view = grid.getData();
            let items = view.getItems();

            // 主キーの値もCSVで取得してダイアログに反映
            let vals = '';
            for (let i in grid.getSelectedRows()) {
                let r = grid.getSelectedRows()[i];
                let item = items[r];
                if (vals != '') {
                    vals += ', ';
                }
                vals += item[field];
            }
            $dialogDiv.find('[id$="' + id + '"]').text(vals);
            $dialogDiv.find('[name$="' + id + '"]').val(vals);

            // 全ての選択行で一致するカラム名と値を取得
            let summarySufs = null;
            if (Messages['column.summary.suffixs']) {
                summarySufs = Messages['column.summary.suffixs'].split(',');
            }

            let eqs = {};
            for (let i in grid.getSelectedRows()) {
                let r = grid.getSelectedRows()[i];
                let item = items[r];
                for (let colName in item) {
                    if (colName != 'ROW_NUM' && colName != 'id' &&
                        !colName.match(new RegExp('^' + Messages['column.start'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.until'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.insert.timestamp'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.insert.id'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.insert.mei'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.update.timestamp'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.update.id'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.update.mei'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.delete'] + '$', 'i')) &&
                        !colName.match(new RegExp('^' + Messages['column.status'] + '$', 'i'))) {
                        // メタ情報以外の項目を親画面に反映

                        let val = item[colName];

                        let isSummary = false;
                        for (let i in summarySufs) {
                            let summarySuf = summarySufs[i];
                            if (colName.endsWith(summarySuf.toLowerCase()) || colName.endsWith(summarySuf.toUpperCase())) {
                                isSummary = true;
                                break;
                            }
                        }

                        if (isSummary) {
                            if (!eqs[colName]) {
                                eqs[colName] = 0;
                            }
                            eqs[colName] += val;
                        } else {
                            if (!eqs[colName] && eqs[colName] != '') {
                                eqs[colName] = val;
                            } else if (eqs[colName] != val) {
                                eqs[colName] = ''
                            }
                        }
                    }
                }
            }

            for (let colName in eqs) {
                let eq = eqs[colName];
                if (eq == '') {
                    continue;
                }
                let property = Casing.toCamel(colName);
                $dialogDiv.find('span[id$="' + property + '"]').format(eqs[colName]);
                $dialogDiv.find('[name$="' + property + '"]').val([eqs[colName]]);
            }
        }

        let isDerive = $clicked.hasClass('derive');
        if (isDerive) {
            $clicked.nextAll('input').each(function() {
                Dialogate.reflect2Dialog($dialogDiv, 'derivee', this.name, $(this).val());
            });
        }

        // 呼び出し元を設定
        $dialogDiv.attr('data-caller', $clicked.attr('id'));

        // ダイアログを開く
        $dialogDiv.dialog('open');
    });

});

let Dialogate = {

    loaded: {},

    /*
     * [target=dialog]のリンクからダイアログのdivを画面に追加
     */
    enable: function(link, indent, isRefer) {

        // リンクを取得
        let $link = $(link);

        let isReborner = $link.hasClass('reborner');

        // hrefを取得。なければスキップ。
        let href = $link.attr('href');
        if (href == undefined) {
            href = $link.attr('data-href');
        }
        if (href == undefined) {
            return;
        }
        // URL引数を除去
        href = href.replace(/\?.+$/, '');

        // ロード済みならスキップ
        if (Dialogate.loaded[href]) {
            return;
        }

        console.debug(indent + 'Dialogate load [' + href + '].');
        Dialogate.loaded[href] = 1;

        // hrefからdialogIdを取得。作成済みならスキップ。
        let entity = href.replace(/(^.+\/|\.html(\?.+)?$)/g, '');
        let dialogId = entity + 'Dialog';
        if ($('div[id="' + dialogId + '"]').length > 0) {
            return;
        }

        //ローディング開始
        //Loading.fadeIn();

        // ダイアログ用のDIVを追加して取得
        $('body').append($('<div id="' + dialogId + '"></div>'));
        let $dialogDiv = $('div[id="' + dialogId + '"]');

        // 外部htmlファイルをロード
        $.ajax({
            async: false,
            cache: true,
            datatype: 'html',
            url: href,
        }).fail(function(data) {
            console.error(data);
            alert(Messages['error.network']);
        }).done(function(html) {

            var dialogHtml = $.parseHTML(html);

            // 外部htmlファイルをページ内に反映
            var $articleDiv = $('<div>').append(dialogHtml).find('div.article');
            $dialogDiv.html($articleDiv.prop('outerHTML'));

            // ダイアログ内のid項目について、labelとユニークに紐づくようにidを変更
            $dialogDiv.find('[id]').each(function() {
                if (this.tagName == 'INPUT' || this.tagName == 'TEXTAREA') {
                    let $me = $(this);
                    let $form = $me.closest('form');
                    let id = $dialogDiv.prop('id') + '.' + $form.prop('name') + '.' + this.id;
                    $me.prop('id', id);
                    //$me.prev().prop('for', id);
                    let labels = $me.prevAll('LABEL');
                    if (labels.length > 0) {
                        $(labels[0]).prop('for', id);
                    }
                }
                if (this.tagName == 'A' || this.tagName == 'FIELDSET') {
                    let $me = $(this);
                    let $form = $me.closest('form');
                    let id = $dialogDiv.prop('id') + '.' + $form.prop('name') + '.' + this.id;
                    $me.prop('id', id);
                }
            });

            // 参照ダイアログの場合、ダイアログ内の新規ボタンは非表示
            if ($link.hasClass('refer')) {
                $dialogDiv.find('a.anew').hide();
                $dialogDiv.find('[id$=Grid]').each(function() {
                    let $gridDiv = $(this);
                    $gridDiv.attr('data-addRow', false);
                });
                $dialogDiv.find('button.regist').hide();
            }

            // ダイアログを設定
            $dialogDiv.dialog({
                autoOpen: false,
                modal: true,
                title: $link.attr('title'),
                //width: 'auto',
                width: '99%',

                /*
                 * ダイアログオープン
                 */
                open: function(event) {

                    console.info('Dialogate open.');

                    // ダイアログdiv
                    let $dialogDiv = $(event.target);

                    // 転生先として表示された場合は、再転生のリンクは非表示
                    $dialogDiv.find('a.reborner').show();
                    if (isReborner) {
                        $dialogDiv.find('a.reborner').hide();
                        //$dialogDiv.find('div.submits').hide(); 転生リンクを押したときに転生先の登録が出来なくなるためコメントアウト
                    }

                    // ダイアログ内のグリッド新規ボタンは、呼び出し元の主キーが揃っていなければ非活性
                    $dialogDiv.find('.addChild').button('option', 'disabled', false);
                    let isPrimaryKey = true;
                    let primaryKeys = $dialogDiv.find('input.primaryKey');
                    for (let i = 0;i < primaryKeys.length;i++) {
                        isPrimaryKey &= $(primaryKeys[i]).val() != '';
                    }
                    if (!isPrimaryKey) {
                        $dialogDiv.find('.addChild').button('option', 'disabled', true);
                    }

                    let $searchForm = $dialogDiv.find('[name$="SearchForm"]');
                    if ($searchForm.length > 0) {
                        // ダイアログ上部が検索フォームの場合

                        //クエリストリングに指定があれば、フォームのアクションに設定
                        let href = $link.prop('href');
                        let i = href.indexOf('?');
                        if (i >= 0) {
                            let querystring = href.substring(i + 1);
                            let querystrings = querystring.split('&');
                            for (let i in querystrings) {
                                let getParams = querystrings[i].split('=');
                                if (getParams[0] == 'action') {
                                    $searchForm.prop('action', getParams[1]);
                                    break;
                                }
                            }
                        }

                        // 呼び出し元がグリッドでなく、検索項目が設定されている場合は、検索結果を初期表示
                        if (!$dialogDiv.attr('data-caller').match(/.+Grid.+/)) {
                            let formJson = Jsonate.toValueJson($searchForm);
                            delete formJson['rows'];
                            delete formJson['page'];
                            if (JSON.stringify(formJson) != '{}') {
                                $searchForm.find('button.search').click();
                            }
                        }
                    }

                    let $registForm = $dialogDiv.find('[name$="RegistForm"]');
                    if ($registForm.length > 0) {
                        // ダイアログ内に登録フォームがある場合

                        // ダイアログの場合だけ初期化すればいいのでBase.referRegistForm()から移動
                        $registForm.find('a, input, select, textarea').each(function() {
                            Base.writable(this);
                        });
                        $registForm.find('fieldset a.refer, input[type="button"].gridDelete').show();
                        let gridDivs = $registForm.find('[id$=Grid]');
                        for (let i = 0;i < gridDivs.length;i++) {
                            let gridId = gridDivs[i].id;
                            Gridate.grids[gridId].getOptions()['editable'] = true;
                        }

                        // ダイアログ内の登録フォームに、呼び出し元イベントで値が設定されている場合は、照会結果を初期表示
                        Base.referRegistForm($registForm);
                    }

                    let grids = $dialogDiv.find('[id$=Grid]');
                    for (let i = 0;i < grids.length;i++) {
                        let gridId = grids[i].id;
                        $('[id$="' + gridId + '"]').css('width', $dialogDiv.css('width'))
                    }

                    try {
                        console.info(entity + 'Open($("#' + entity + 'Dialog"));');
                        eval(entity + 'Open($("#' + entity + 'Dialog"))');
                    } catch (e) {
                        console.debug(e.message);
                    }

                    Nextize.first($dialogDiv);
                },

                /*
                 * ダイアログクローズ
                 */
                close: function() {
                    // 表示内容をクリア
                    Jsonate.clearForm($(this).find('form'));
                },
            });

            // 遅延ロードした外部HTMLにダイアログリンクがあれば、再度、外部HTMLを読み込み
            $(dialogHtml).find('a[target="dialog"]').each(function() {
                let dialogLink = this;
                // 転生ならネストしない
                if (!isReborner || !$(dialogLink).hasClass('reborner')) {
                    if (!isRefer || $(dialogLink).hasClass('refer') || $(dialogLink).hasClass('derivee')) {
                        Dialogate.enable(dialogLink, indent + "  ", isRefer || $(dialogLink).hasClass('refer') || $(dialogLink).hasClass('derivee'));
                    }
                }
            });

        }).always(function(data) {

            //ローディング終了
            //Loading.fadeOut();
        });
    },

    refresh: function(event) {
        let dialog = event.srcElement.closest('[id$="Dialog"]');
        if (dialog) {
            let dialogId = dialog.id;
            Dialogate.refreshById(dialogId);
        } else {
            window.location.reload();
        }
    },

    reset: function(event) {
        let searchForm = event.srcElement.closest('form.search');
        $(searchForm).find('input:not([name="rows"]):not([name="page"]),select,textarea').each(function() {
            let $inputs = $(this);
            $inputs.val([$inputs.attr('data-callerVal')]);
            let $referMei = $inputs.siblings('[data-referFor="' + $inputs[0].name + '"]');
            $referMei.text([$referMei.attr('data-callerVal')]);
        });
    },

    refreshById: function(dialogId) {

        let $dialogDiv = $('[id$="' + dialogId + '"]');

        // ダイアログの主キー情報を退避
        let primaryKeys = {};
        $dialogDiv.find('input.primaryKey').each(function() {
            primaryKeys[this.name] = $(this).val();
        });

        // ダイアログを一旦閉じる
        $dialogDiv.dialog('close');

        // ダイアログに主キー情報を復帰して開く
        for (let name in primaryKeys) {
            $dialogDiv.find('[name="' + name + '"]').val(primaryKeys[name]);
        }
        $dialogDiv.dialog('open');
    },

    reflect2Dialog: function($dialogDiv, prefix, sendItemName, sendValue) {

        // let sendItemName = sendInput.name;                                         // TEntity.betsuSansho1Id
        // let sendValue = $(sendInput).val();
        if (sendValue.length == 0) {
            return;
        }

        // そのままの項目名でヒットする場合（詳細リンク、追加リンクの親モデル）
        let $dialogItem = $dialogDiv.find('[name="' + sendItemName + '"]');
        if ($dialogItem.length > 0) {
            $dialogItem.val([sendValue]);
            $dialogDiv.find('span[id="' + sendItemName + '"]').text(sendValue);
            if ($dialogItem.hasClass('primaryKey') && $dialogItem.hasClass('refer')) {
                $dialogItem.attr('data-callerVal', sendValue);
                $dialogDiv.find('span[id="' + sendItemName + '"]').attr('data-callerVal', sendValue);
            }
            // 追加リンクの親モデル用にコメントアウト
            // return;
        }

        // 以下、参照リンク、検索画面の追加リンク、詳細画面の追加リンク

        // 表示するダイアログ内のフォーム名からモデル名を取得
        let $dialogForm = $dialogDiv.find('form');
        let dialogFormName = $dialogForm.prop('name');                                // MSansho1SearchForm
        let dialogFormEntityName = dialogFormName.replace(/(Search|Regist)Form/, ''); // MSansho1

        // 項目名からフィールド名を取得
        let sendItemNames = sendItemName.split('.');
        let sendFieldName = sendItemNames[sendItemNames.length - 1];                  // betsuSansho1Id

        $dialogDiv.find('input').each(function() {
            let dialogInputName = this.name;                                          // MSansho1.sansho1Id
            let dialogInputNames = dialogInputName.split('.');                        // MSansho1, sansho1Id
            let dialogInputEntityName = dialogInputNames[0];                          // MSansho1
            let dialogFieldName = dialogInputNames[1];                                // sansho1Id
            if (dialogFormEntityName == dialogInputEntityName && sendFieldName.match(new RegExp('^' + prefix + dialogFieldName + '$', 'i'))) {
                $(this).val([sendValue]);
                $dialogDiv.find('span[id="' + dialogInputName + '"]').text(sendValue);
                if ($(this).hasClass('primaryKey') && $(this).hasClass('refer')) {
                    $(this).attr('data-callerVal', sendValue);
                    $dialogDiv.find('span[id="' + dialogInputName + '"]').attr('data-callerVal', sendValue);
                    Base.readonly(this);
                    Base.readonly($('a[id="' + this.id + '"]'));
                }
            }
        });
    },

};
