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
 * json/form制御
 *
 * @author golorp
 */

let Jsonate = {

    /**
     * 入力項目のセレクタ
     */
    inputSelector: 'input[type!=button][type!=checkbox][type!=radio][type!=reset][type!=submit]:enabled, '
        + 'input[type=checkbox]:checked:enabled, '
        + 'input[type=radio]:checked:enabled, '
        + 'select:enabled, '
        + 'textarea:enabled',

    /**
     * フォームをJSON化
     */
    toJson: function($form, isSelectRow, isForceGrid) {

        let formJson = {};

        // フォーム内容
        $form.find(Jsonate.inputSelector).each(function() {
            let $input = $(this);
            let k = $input.prop('name');
            let v = $input.val();
            if (v != '' && $input.prop('type') == 'month') {
                v = v.replace(/(\d{4})-(\d{2})/, '$1$2');
            }
            if (formJson[k]) {
                if (!Array.isArray(formJson[k])) {
                    formJson[k] = [formJson[k]];
                }
                formJson[k].push(v);
            } else {
                formJson[k] = v;
            }
        });

        // グリッド内容
        $form.find('[id$=Grid]').each(function() {
            let gridId = this.id;
            let grid = Gridate.grids[gridId];
            let gridData = grid.getData().getItems();

            let postData = [];

            if (isSelectRow) {
                // 行選択指定の場合は、選択している行のみ

                for (let r in gridData) {

                    // 選択有無のチェック
                    let selected = false;
                    for (let i in grid.getSelectedRows()) {
                        if (r == grid.getSelectedRows()[i]) {
                            selected = true;
                            postData.push(gridData[r]);
                            break;
                        }
                    }

                    // 選択なしなら空配列を追加（nullだとvalidatorが利かなくなる）
                    if (!selected) {
                        postData.push({});
                    }
                }

            } else {
                // 行選択指定でない場合は、変更している行のみ

                for (let r in gridData) {

                    let gridRow = gridData[r];

                    let contents = '';
                    for (let c in gridRow) {
                        if (c != 'id') {
                            contents += gridRow[c];
                        }
                    }
                    if (contents.replace(/0/g, '') == '') {
                        continue;
                    }

                    // 変更有無のチェック
                    let updated = false;
                    if (isForceGrid) {
                        // 変更有無を無視する場合
                        updated = true;
                        postData.push(gridRow);
                    } else if (!grid.orgData || !grid.orgData[r]) {
                        // 元データがない場合
                        updated = true;
                        postData.push(gridRow);
                    } else if (JSON.stringify(gridRow) != JSON.stringify(grid.orgData[r])) {
                        // 変更があった場合
                        updated = true;
                        postData.push(gridRow);
                    } else if (gridRow[columnRegistTs.toLowerCase()] == null && gridRow[columnRegistTs.toUpperCase()] == null) {
                        // INSERT_TSがNULLの場合（転生時など）
                        updated = true;
                        postData.push(gridRow);
                    }

                    // 変更なしなら空配列を追加（nullだとvalidatorが利かなくなる）
                    if (!updated) {
                        postData.push({});
                    }
                }
            }

            let gridIds = gridId.split('.');
            let gridName = gridIds[gridIds.length - 1];
            formJson[gridName] = postData;
        });

        return formJson;
    },

    /**
     * 値のある項目のみJSON化
     * @param $form フォーム
     */
    toValueJson: function($form) {

        let formJson = {};

        /*
         * フォーム内容
         */
        $form.find(Jsonate.inputSelector).each(function() {

            // 入力項目
            let $input = $(this);

            // JSONキー
            let k = $input.prop('name');

            // JSON値
            let v = $input.val();
            if (v != '' && $input.prop('type') == 'month') {
                v = v.replace(/(\d{4})-(\d{2})/, '$1$2');
            }

            if (v != '') {
                // JSON値が取れた場合

                if (formJson[k]) {
                    // 取得済みのキーだった場合

                    // JSON値が配列でない場合は配列化
                    if (!Array.isArray(formJson[k])) {
                        formJson[k] = [formJson[k]];
                    }

                    // 配列の末尾に追加
                    formJson[k].push(v);

                } else {

                    // JSON値を設定
                    formJson[k] = v;
                }
            }
        });

        /*
         * グリッド内容
         */
        $form.find('[id$=Grid]').each(function() {

            // グリッドID
            let gridId = this.id;

            // グリッド
            let grid = Gridate.grids[gridId];
            if (grid) {

                // グリッドデータ
                let gridData = grid.getData().getItems();

                if (gridData.length > 0) {
                    // グリッドデータが１件以上ある場合

                    // グリッドIDからグリッド名を取得
                    let gridIds = gridId.split('.');
                    let gridName = gridIds[gridIds.length - 1];

                    // グリッド名をキーにグリッド内容を取得
                    formJson[gridName] = gridData;
                }
            }
        });

        return formJson;
    },

    /**
     * JSONをフォームに反映
     * @param data { entityName, { k: v}}
     * @param $form フォーム
     */
    toForm: function(data, $form) {

        // JSONのキー（エンティティ名）でループ
        for (let entityName in data) {

            // エンティティのJSONを取得
            let entity = data[entityName];

            // 「valueがobject」かつ「value配列でない」なら子モデルを再帰（子モデルは配列になるので、多分ここはもう動かない）
            for (let k in entity) {
                let v = entity[k];
                if (v != null && typeof (v) == 'object' && !Array.isArray(v)) {
                    let nestData = {};
                    nestData[k] = v;
                    Jsonate.toForm(nestData, $form);
                }
            }

            // 入力項目でループ
            $form
                // TODO 転生元IDが空になるためコメントアウト。代替案必要。
                // form内の親モデル・兄弟モデルに、更新日時など、同じメンバ名があると誤爆するため
                // .find('[name*="' + entityName + '."]')
                .find(Jsonate.inputSelector + ', input[type=checkbox]:enabled, input[type=radio]:enabled')
                .each(function() {

                    // 画面上の入力項目
                    let $input = $(this);

                    // 入力項目のname
                    let inputName = $input.prop('name');

                    // チェックボックス・ラジオボタン用の対応（nameにdialogIdが接頭する時？陳腐化？）
                    if (inputName.lastIndexOf(entityName) > 0) {
                        inputName = inputName.substr(inputName.lastIndexOf(entityName));
                    }

                    let inputNames = inputName.split('.');
                    if (inputNames.length == 2) {
                        // データの項目名にエンティティ名の指定がある場合
                        let pagename = window.location.pathname.split('/').pop().replace('.html', '');
                        if (pagename != entityName) {
                            // ページ名とエンティティ名が違う場合
                            if (entityName != inputNames[0]) {
                                // 結果セットのエンティティ名とデータ項目のエンティティ名が異なる場合
                                return;
                            }
                        }
                    }

                    // 入力項目のentity.propertyで値を取得してみる
                    let v = entity[inputName];
                    if (v == null) {
                        // UPPER_CASEでも取得してみる
                        v = entity[Casing.toUpper(inputName)];
                        if (v == null) {
                            // プロパティ名だけで取得してみる
                            let i = inputName.lastIndexOf('.');
                            let property = inputName.substr(i + 1);
                            v = entity[property];
                            if (v == null) {
                                // プロパティ名のUPPER_CASEでも取得してみる
                                v = entity[Casing.toUpper(property)];
                                if (v == null) {
                                    // プロパティ名のsnake_caseでも取得してみる
                                    v = entity[Casing.toSnake(property)];
                                    if (v == null) {
                                        // プロパティ名のkebab-caseでも取得してみる
                                        v = entity[Casing.toKebab(property)];
                                        if (v == null) {
                                            // プロパティ名のUPPER-KEBAB-CASEでも取得してみる
                                            v = entity[Casing.toUpperKebab(property)];
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ファイルタグならリンクを一旦非表示
                    if ($input.attr('type') == 'file') {
                        let $link = $('a[id="' + this.id + '"]');
                        $link.hide();
                    }

                    // 値があれば反映
                    if (v != null) {

                        console.debug('#' + $(this).prop('id') + ' [' + inputName + ' = ' + v + ']');

                        if ($input.attr('type') == 'file') {
                            // ファイルタグならリンクに変換

                            //                        // ファイルタグを非表示
                            //                        $input.prop('disabled', true).hide();

                            // ダウンロードリンクを表示
                            let $link = $('a[id="' + this.id + '"]');
                            $link.show();

                            // ダウンロードリンクのhrefを設定
                            let $form = $input.closest('form');
                            let entityName = $form.prop('name').replace(/(Search|Regist)Form/, '');

                            let $fieldset = $input.closest('fieldset');
                            let inputName = $input.prop('name');
                            let inputNames = inputName.split('.');
                            let params = '?name=' + inputNames[inputNames.length - 1];

                            let $primaryKeys = $fieldset.find('input.primaryKey');
                            for (let i = 0;i < $primaryKeys.length;i++) {
                                let $primaryKey = $($primaryKeys[i]);
                                let name = $primaryKey.prop('name');
                                let names = name.split('.');
                                let val = $primaryKey.val();
                                params += '&' + names[names.length - 1] + '=' + val;
                            }
                            let href = entityName + 'Download.link' + params;
                            $link.prop('href', href);
                            if (v.endsWith('.bmp') || v.endsWith('.gif') || v.endsWith('.jpg') || v.endsWith('.jpeg') || v.endsWith('.png')) {
                                $link.html('<img id="' + this.id + 'Img" class="imageLink" src="' + href + '&' + Date.now() + '" />');
                            } else {
                                $link.html(v.split('|')[0]);
                            }

                            // ファイルパスのhiddenタグを活性
                            $('input[type="hidden"][id="' + this.id + '"]').prop("disabled", false).val([v]);

                        } else if ($input.attr('type') == 'month') {
                            // 年月ならフォーマットして設定
                            v = v.replace(/(\d{4})(\d{2})/, '$1-$2');
                            $input.val(v);
                        } else if ($input[0].type == 'datetime-local') {
                            // 日時ならフォーマット
                            // $input.val([v.toISOString().slice(0, -1)]);
                            v = v.replace('%20', 'T');
                            v = v.replace(/(\d+-\d+-\d+ +\d+:\d+)(:\d+)/, '$1');
                            $input.val(v);
                        } else {
                            $input.val([v]);
                            $form.find('span[id="' + inputName + '"]').format(v);
                        }
                        if ($input.hasClass('bit')) {
                            $form.find('[name="' + inputName + 'F"]').val([v.toString(2)]);
                        }
                    }
                });

            // 表示項目でループ
            $form.find('span[id*="' + entityName + '."]').each(function() {

                let $span = $(this);

                // 入力項目のidで値を取得してみる
                let spanId = $span.prop('id');
                let v = entity[spanId];

                // entityNameなしでも取得してみる
                if (!v) {
                    let property = spanId.replace(entityName + '.', '');
                    v = entity[property];
                    // UPPER_CASEでも取得してみる
                    if (!v) {
                        v = entity[Casing.toUpper(property)];
                    }
                }

                // 値があれば反映
                if (v) {
                    console.debug('#' + $(this).prop('id') + ' [' + spanId + ' = ' + v + ']');
                    $span.format(v);
                }
            });

            // グリッド内容
            $form.find('[id$="Grid"]').each(function() {
                let gridId = this.id;                         // TSosenDialog.TOyaGrid
                let gridIds = gridId.split(".");              // TSosenDialog, TOyaGrid
                let lastId = gridIds[gridIds.length - 1];     // TOyaGrid
                let fieldName = lastId.replace(/Grid$/, 's'); // TOyas
                let childList = entity[fieldName];
                if (childList) {
                    let gridData = [];
                    for (let i in childList) {
                        let child = childList[i];
                        let gridRow = {};
                        for (let fieldName in child) {
                            //gridRow[Casing.toUpper(fieldName)] = child[fieldName];
                            gridRow[fieldName] = child[fieldName];
                        }
                        gridData.push(gridRow);
                    }
                    Gridate.refresh(gridId, gridData);
                }
            });
        }
    },

    clearForm: function($form) {

        // 入力項目のエラースタイル解除
        let $inputs = $form.find(':input');
        $inputs.removeClass('error').prop('title', '');//.removeAttr('readonly').removeClass('readonly').removeAttr('tabindex');
        //$inputs.next('a').show();
        $inputs.each(function() {
            let $input = $(this);
            // input
            $('label[for="' + $input.attr('id') + '"]').removeClass('error').prop('title', '');
            // check,radio
            $input.closest('fieldset').find('legend').removeClass('error').prop('title', '');
            // select
            $input.prev('label').removeClass('error').prop('title', '');
        });

        // 選択項目のエラースタイル解除
        let $label = $inputs.parent('label');
        $label.removeClass('error').prop('title', '');

        // ファイルタグ再表示・リンク非表示
        let $files = $form.find('[type="file"]');
        $files.each(function() {
            let $file = $(this);
            $file.prop('disabled', false).show();
            $('a[id="' + this.id + '"]').hide();
            $('input[type="hidden"][id="' + this.id + '"]').prop('disabled', true);
        });

        // フォーム内容
        $form.find(Jsonate.inputSelector).each(function() {
            let $input = $(this);
            if (this.name != 'rows' && this.name != 'page') {
                $input.val(['']);
            }
        });

        // スパン内容
        $form.find('span[id]').each(function() {
            let $span = $(this);
            $span.text('');
        });

        // グリッド内容
        $form.find('[id$=Grid]').each(function() {
            let gridId = this.id;
            Gridate.refresh(gridId, []);
        });
    },

}
