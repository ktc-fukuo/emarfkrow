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
 * ajax通信
 *
 * @author golorp
 */

$(function() {

    console.info('Ajaxize init.');

    /*
     * 「.ajax」のフォーム送信を横取り
     */
    $(document).on('submit', 'form', function(event) {

        // 押下したボタンを取得
        let $button = $(event.originalEvent.submitter);

        // 削除ボタンだった場合は実行確認
        if ($button.hasClass('delete')) {
            if (!confirm(Messages['confirm.delete'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 登録ボタンだった場合は実行確認
        if ($button.hasClass('regist')) {
            if (!confirm(Messages['confirm.regist'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 申請ボタンだった場合は実行確認
        if ($button.hasClass('apply')) {
            if (!confirm(Messages['confirm.apply'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 取消ボタンだった場合は実行確認
        if ($button.hasClass('cancel')) {
            if (!confirm(Messages['confirm.cancel'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 承認ボタンだった場合は実行確認
        if ($button.hasClass('permit')) {
            if (!confirm(Messages['confirm.permit'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 否認ボタンだった場合は実行確認
        if ($button.hasClass('forbid')) {
            if (!confirm(Messages['confirm.forbid'])) {
                event.preventDefault();
                event.stopPropagation();
                event.stopImmediatePropagation();
                return false;
            }
        }

        // 送信先を取得
        let $form = $(this);
        let action = $form.prop('action');

        // ボタンに送信先の指定があれば上書き
        if ($button.attr('data-action')) {
            action = $button.attr('data-action');
        }

        // 送信先が「.ajax」でなければ終了（ブラウザが送信する）
        if (action.match(/\.ajax/g) == null) {
            return;
        }

        // イベントキャンセル
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();

        // フォーム内の入力項目のエラー表示をクリア
        let $inputs = $form.find(':input');
        $inputs.removeClass('error').prop('title', '');
        $inputs.each(function() {
            let $input = $(this);
            // input
            $('label[for="' + $input.attr('id') + '"]').removeClass('error').prop('title', '');
            // check,radio
            $input.closest('fieldset').find('legend').removeClass('error').prop('title', '');
            // select
            $input.prev('label').removeClass('error').prop('title', '');
        });
        let $label = $inputs.parent('label');
        $label.removeClass('error').prop('title', '');

        // グリッドのエラースタイルを解除
        for (let gridId in Gridate.grids) {
            let grid = Gridate.grids[gridId];
            grid.removeCellCssStyles('error');
        }

        // formdataを取得
        let formData = new FormData(this);

        // フォーム内容をjsonに取得して、グリッドデータもformdataに追加
        let isSelectRow = $button.hasClass('selectRows');
        let formJson = Jsonate.toJson($form, isSelectRow);

        // 削除ボタンなら変更が無くてもグリッドを含む
        if ($button.hasClass('delete')) {
            formJson = Jsonate.toJson($form, isSelectRow, true);
        }

        for (let k in formJson) {
            let itemJson = formJson[k];
            if (Array.isArray(itemJson) && itemJson.length > 0 && k.match(/Grid$/)) {

                // table_nameでの振り分けはサーバ側で行う（トランザクション内で行ごとにtable_nameで振り分けたい）
                //                // 【VIEW対応】グリッドデータでテーブル名指定があった場合は送信先とグリッド名を変更する
                //                if (columnDetail && columnDetail != '') {
                //                    let tableNameColumn = itemJson[0][columnDetail.toUpperCase()];
                //                    if (!tableNameColumn) {
                //                        tableNameColumn = itemJson[0][columnDetail.toLowerCase()];
                //                    }
                //                    if (tableNameColumn) {
                //                        let entityName = Casing.toPascal(tableNameColumn);
                //                        action = './' + entityName + 'SRegist.ajax';
                //                        k = entityName + 'Grid';
                //                    }
                //                }

                formData.append(k, JSON.stringify(itemJson));
            }
        }

        let callback;

        let gridId = $button.attr('data-gridId');
        let graphId = $button.attr('data-graphId');
        let ganttId = $button.attr('data-ganttId');
        if (gridId != undefined) {
            // 送信したボタンに反映先のグリッドID指定がある場合（検索ボタンの場合）

            Gridate.refresh(gridId, []);

            callback = function(data) {
                for (let dataName in data) {
                    if (Array.isArray(data[dataName])) {
                        Gridate.refresh(gridId, data[dataName], data['totalRows'], data['currentPage']);

                        let $form = $button.closest('form');
                        let $h2 = $form.prev('h2');
                        let $h2Toggle = $h2.find('[id="h2Toggle"]');
                        if ($h2Toggle.hasClass('ui-icon-triangle-1-s')) {
                            $h2.click();
                        }

                        break;
                    }
                }
                if (gridId.indexOf('Dialog') < 0) {
                    Base.resizeNav();
                }
            };

        } else if (graphId != undefined) {
            // 送信したボタンに反映先のグラフID指定がある場合（検索ボタンの場合）

            callback = function(json) {
                for (let dataName in json) {
                    if (Array.isArray(json[dataName])) {

                        //                        let datasets = [];
                        //                        let dataset = json[dataName];
                        //                        for (let i = 0;i < dataset.length;i++) {
                        //
                        //                            let newData = {};
                        //                            let data = dataset[i];
                        //                            for (let key in data) {
                        //                                if (key == 'DATA') {
                        //                                    newData['data'] = data[key].split(',').map(Number);
                        //                                } else {
                        //                                    newData[key.toLowerCase()] = data[key];
                        //                                }
                        //                            }
                        //
                        //                            datasets.push(newData);
                        //                        }

                        let datasets = transformDataJson(json[dataName]);

                        const data = {
                            labels: datasets[0].labels.split(','),
                            datasets: datasets
                        };

                        const config = {
                            data: data,
                            options: {
                                scales: {
                                    x: {
                                        stacked: true,
                                    },
                                    y: {
                                        stacked: true,
                                    },
                                },
                                responsive: true,
                                plugins: {
                                    legend: {
                                        position: 'right',
                                    },
                                    title: {
                                        display: false,
                                        text: '表タイトル'
                                    }
                                }
                            }
                        };

                        if (Chart.getChart(graphId)) {
                            Chart.getChart(graphId).destroy();
                        }
                        const canvas = document.getElementById(graphId);
                        new Chart(canvas, config);

                        break;
                    }
                }
            };

        } else if (ganttId != undefined) {
            // 送信したボタンに反映先のガントID指定がある場合（検索ボタンの場合）

            Ganttate.refresh(ganttId, []);

            callback = function(data) {
                for (let dataName in data) {
                    if (Array.isArray(data[dataName])) {
                        Ganttate.refresh(ganttId, data[dataName]);

                        let $form = $button.closest('form');
                        let $h2 = $form.prev('h2');
                        let $h2Toggle = $h2.find('[id="h2Toggle"]');
                        if ($h2Toggle.hasClass('ui-icon-triangle-1-s')) {
                            $h2.click();
                        }

                        break;
                    }
                }
                if (ganttId.indexOf('Dialog') < 0) {
                    Base.resizeNav();
                }
            };

        } else {
            // 送信したボタンに反映先のグリッドID指定がない場合（登録ボタンの場合）

            let $dialogDiv = $button.closest('[id$="Dialog"]');
            if ($dialogDiv.length == 0) {

                // 親画面の場合、検索ボタンがあれば押下、なければ再描画
                let $articleDiv = $('body>div.article');
                let $searchButton = $articleDiv.find('button[id^="Search"]');
                if ($searchButton.length > 0) {
                    callback = function() { $searchButton.click(); };
                } else {
                    if (window.opener) {
                        let $articleDiv = $(window.opener.document).find('body>div.article');
                        let $searchButton = $articleDiv.find('button[id^="Search"]');
                        if ($searchButton.length > 0) {
                            callback = function() { $searchButton.click(); window.close(); };
                        } else {
                            callback = function() { window.document.location.reload(); };
                        }
                    } else {
                        callback = function() { window.document.location.reload(); };
                    }
                }

            } else {
                // ダイアログの場合

                let $searchButton = $dialogDiv.find('button[id^="Search"]');
                if ($searchButton.length > 0) {
                    // ダイアログ内に検索ボタンがある場合は再検索
                    callback = function() { $searchButton.click(); };
                } else {
                    // ダイアログ内に検索ボタンがない場合

                    let caller = $dialogDiv.attr('data-caller');
                    let parentDialogId = caller.match(/.+Dialog/);

                    if (parentDialogId == null) {
                        // １階層目のダイアログの場合

                        let $articleDiv = $('body>div.article');
                        let $searchButton = $articleDiv.find('button[id^="Search"]');
                        if ($searchButton.length > 0) {
                            // 親画面に検索ボタンがあれば、ダイアログを閉じて再検索
                            callback = function() {
                                $dialogDiv.dialog('close');
                                $searchButton.click();
                            };
                        } else {
                            // 親画面に検索ボタンがなければ何もしない
                        }

                    } else {
                        // ２階層以降のダイアログの場合

                        let $parentDiv = $('div[id="' + parentDialogId + '"]');
                        let $searchButton = $parentDiv.find('button[id^="Search"]');
                        if ($searchButton.length > 0) {
                            // 親ダイアログに検索ボタンがあれば、ダイアログを閉じて再検索
                            callback = function() {
                                $dialogDiv.dialog('close');
                                $searchButton.click();
                            };
                        } else {
                            // 親画面に検索ボタンがなければ、ダイアログを閉じて親ダイアログを再描画
                            callback = function() {
                                $dialogDiv.dialog('close');
                                Dialogate.refreshById(parentDialogId);
                            };
                        }
                    }
                }
            }
        }

        Ajaxize.ajaxPost(action, formData, callback);
    });
});

function transformDataJson(items) {

    // 1. ユニークな日付のリストを取得して昇順ソート
    const labelsSet = new Set(items.map(item => item.LABELS));
    const labels = Array.from(labelsSet).sort();

    // 日付の結合文字列（例: "'20260601','20260602',..."）
    const labelsString = labels.map(d => `${d}`).join(',');

    // 2. LABELごとのデータを集約
    const labelMap = new Map();

    for (const item of items) {
        const label = item.LABEL.trim();

        if (!labelMap.has(label)) {
            labelMap.set(label, {
                TYPE: item.TYPE,
                STACK: item.STACK,
                dataByDate: new Map()
            });
        }

        labelMap.get(label).dataByDate.set(item.LABELS, item.DATA);
    }

    // 3. 変換後のJSON構造（配列）を作成
    const result = [];

    for (const [label, info] of labelMap.entries()) {

        // 各日付のDATA値を順番に連結（存在しない場合は0）
        //const data = labels.map(d => info.dataByDate.get(d) ?? 0);
        var data = labels.map(function(d) {
            var val = info.dataByDate.get(d);
            return (val !== undefined && val !== null) ? val : 0;
        });

        result.push({
            labels: labelsString,
            type: info.TYPE,
            stack: info.STACK,
            label: label,
            data: data
        });
    }

    return result;
}

let Ajaxize = {

    ajaxPost: function(action, formJson, callback, noLoading, isQuiet) {
        Ajaxize.jaxPost(action, formJson, callback, noLoading, isQuiet, true);
    },

    sjaxPost: function(action, formJson, callback, noLoading, isQuiet) {
        Ajaxize.jaxPost(action, formJson, callback, noLoading, isQuiet, false);
    },

    /**
     * ajax送信
     */
    jaxPost: function(action, formJson, callback, noLoading, isQuiet, isAsync) {

        if (typeof Loading != 'undefined' && noLoading != true) {
            Loading.fadeIn(action);
        }

        // ajaxオプション
        let options = {
            async: isAsync,   // 非同期通信フラグ
            cache: false,     // キャッシュフラグ
            dataType: 'json', // 通信結果取得のデータ型
            headers: {
                "X-CSRF-TOKEN": formJson['_csrf']
            },
            type: 'post',
            url: action,
        };

        let logJson = formJson;
        if (formJson instanceof FormData) {
            // 送信値がformdataの場合

            options['contentType'] = false; // contentTypeをfalseに指定
            options['data'] = formJson;
            options['processData'] = false; // Ajaxがdataを整形しない指定

            logJson = {};
            formJson.forEach(function(value, key) {
                logJson[key] = value;
            });

        } else {
            // 送信値がjsonの場合

            options['contentType'] = 'application/json; charset=UTF-8';
            options['data'] = JSON.stringify(formJson);
        }

        $.ajax(options).fail(function(data) {
            console.error(options);
            console.error(logJson);
            console.error(data);
            if (data.status == 200) {
                alert(Messages['error.session']);
                if (window.document.location.href.indexOf(Messages['loginfilter.login.page']) < 0) {
                    window.document.location.href = '../';
                }
            } else if (data.status == 500) {
                alert(Messages['fatal']);
            } else {
                alert(Messages['error.network']);
            }
        }).done(function(data) {

            console.debug('    action: ' + action);
            console.debug(logJson);
            console.debug(data);
            //console.info('--------------------------------------------------');

            // システムエラー
            if (data && data.FATAL) {
                alert(data.FATAL);
                return;
            }

            // アプリケーションエラー
            if (data && data.ERROR && isQuiet != true) {
                alert(data.ERROR);
                Ajaxize.errorStyle(data.errors);
                return;
            }

            // 警告表示
            if (data && data.WARN) {
                alert(data.WARN);
            }

            // 情報通知
            if (data && data.INFO && isQuiet != true) {
                alert(data.INFO);
            }

            try {
                callback(data)
            } catch (e) {
                console.error(e);
                alert(e);
            }
        }).always(function() {
            if (typeof Loading != 'undefined' && noLoading != true) {
                // Base.loaded内で Base.referMei をコール時に Loading が一瞬切れるため少し遅らせる
                setTimeout(function() {
                    Loading.fadeOut();
                }, 300);
            }
        });
    },

    errorStyle: function(errors) {

        var gridStyles = {};

        for (let k in errors) {

            if (!k.match(/\[[0-9]+\]\./)) {
                // form項目の場合（添え字なしの場合）

                let keys = k.split('.');
                let formName = keys[0];
                let entityName = keys[keys.length - 2].replace('RegistForm', '');
                let fieldName = keys[keys.length - 1];
                let $input = $('[name="' + formName + '"]').find('[name="' + fieldName + '"]');
                if ($input.length == 0) {
                    $input = $('[name="' + formName + '"]').find('[name="' + entityName + '.' + fieldName + '"]');
                }
                $input.addClass('error').attr('title', errors[k]);
                $('label[for="' + $input.attr('id') + '"]').addClass('error').prop('title', errors[k]);

                // 選択項目ならラベルにもエラー表示
                if ($input.parent('label').length > 0) {
                    // input
                    $input.parent('label').addClass('error').attr('title', errors[k]);
                    // check,radio
                    $input.closest('fieldset').find('legend').addClass('error').attr('title', errors[k]);
                }
                // select
                $input.prev('label').addClass('error').attr('title', errors[k]);

            } else {
                // grid項目の場合

                var names = k.split('\.');               // D0001RegistForm, D0001Grid[0], entityMei
                var rowName = names[1];                  // D0001Grid[0]
                var fieldName = names[2];                // entityMei

                // 長兄モデルの際の、兄弟モデルの対応
                if (names.length > 3) {
                    fieldName = fieldName.replace(/RegistForm$/, '') + '.' + names[3];
                }

                var rowNames = rowName.split(/[\[\]]/g); // D0001Grid, 0
                var gridId = rowNames[0];                // D0001Grid
                var rowIndex = rowNames[1];              // 0

                let cell;

                // グリッド取得
                var grid = Gridate.grids[gridId];
                if (!grid) {
                    for (var id in Gridate.grids) {
                        if (id.endsWith(gridId)) {
                            grid = Gridate.grids[id];
                            break;
                        }
                    }
                }

                if (grid) {

                    // グリッドのエラースタイルを作成
                    var gridColumns = grid.getColumns();
                    for (var colIndex in gridColumns) {
                        var column = gridColumns[colIndex];
                        if (column['id'] == fieldName) {

                            let rowStyles = gridStyles[gridId];
                            if (!rowStyles) {
                                rowStyles = {};
                            }

                            var cellStyles = rowStyles[rowIndex];
                            if (!cellStyles) {
                                cellStyles = {};
                            }

                            cellStyles[fieldName] = 'error';
                            rowStyles[rowIndex] = cellStyles;
                            gridStyles[gridId] = rowStyles;

                            cell = colIndex;

                            break;
                        }
                    }

                    try {
                        grid.getCellNode(rowIndex, cell).title = errors[k];
                    } catch (e) {
                        // グリッドが横に長すぎるとcellnodeが取れない時がある
                        console.error(e);
                    }
                }
            }
        }

        // グリッドにエラースタイルを設定
        for (let gridId in gridStyles) {
            var grid = Gridate.grids[gridId];
            if (!grid) {
                for (var id in Gridate.grids) {
                    if (id.endsWith(gridId)) {
                        grid = Gridate.grids[id];
                        break;
                    }
                }
            }
            grid.setCellCssStyles('error', gridStyles[gridId]);
        }
    },

};
