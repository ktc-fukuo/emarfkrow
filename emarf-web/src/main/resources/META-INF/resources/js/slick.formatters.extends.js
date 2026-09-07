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
 * SlickGridのformatter拡張
 *
 * @author golorp
 */

(function($) {

    $.extend(true, window, {
        "Slick": {
            "Formatters": {
                "Extends": {
                    "Button": ButtonFormatter,
                    "Choose": ChooseButtonFormatter,
                    "Delete": DeleteButtonFormatter,
                    "Month": MonthFormatter,
                    "Date": DateFormatter,
                    "DateTime": DateTimeFormatter,
                    "Timestamp": TimestampFormatter,
                    "Link": LinkFormatter,
                    "Select": SelectFormatter,
                    "Checkbox": CheckboxFormatter,
                    "Dec0": Dec0Formatter,
                    "Dec1": Dec1Formatter,
                    "Dec2": Dec2Formatter,
                    "Dec3": Dec3Formatter,
                    "Refer": ReferFormatter,
                    "Dec0Refer": Dec0ReferFormatter,
                    "Dec1Refer": Dec1ReferFormatter,
                    "Dec2Refer": Dec2ReferFormatter,
                    "Dec3Refer": Dec3ReferFormatter,
                }
            }
        }
    });

    function ButtonFormatter(row, cell, value, columnDef, dataContext) {
        if (columnDef.label) {
            return '<input type="button" value="' + columnDef.label + '" class="gridButton" />';
        }
        return '<input type="button" value="' + columnDef.name + '" class="gridButton" />';
    }

    /* 選択ボタン列 */
    function ChooseButtonFormatter(row, cell, value, columnDef, dataContext) {
        // 登録日時がなければ選択ボタン非表示
        if (!dataContext[columnRegistTs.toLowerCase()] && !dataContext[columnRegistTs.toUpperCase()]) {
            return null;
        }
        if (columnDef.label) {
            return '<input type="button" value="' + columnDef.label + '" class="gridButton gridChoose" />';
        }
        return '<input type="button" value="' + columnDef.name + '" class="gridButton gridChoose" />';
    }

    /* 削除ボタン列 */
    function DeleteButtonFormatter(row, cell, value, columnDef, dataContext) {
        // 削除フラグがあれば削除ボタン非表示
        if (columnDelete.toLowerCase() in dataContext || columnDelete.toUpperCase() in dataContext) {
            return null;
        }
        // 有効期間終了日があれば削除ボタン非表示
        if (columnUntil.toLowerCase() in dataContext || columnUntil.toUpperCase() in dataContext) {
            return null;
        }
        // ステータスがあれば削除ボタン非表示
        if (dataContext[columnStatus.toLowerCase()] || dataContext[columnStatus.toUpperCase()]) {
            return null;
        }
        if (columnDef.label) {
            return '<input type="button" value="' + columnDef.label + '" class="gridButton gridDelete" />';
        }
        return '<input type="button" value="' + columnDef.name + '" class="gridButton gridDelete" />';
    }

    function MonthFormatter(row, cell, value, columnDef, dataContext) {
        if (!value) {
            return null;
        }
        return Formatter.Ym(value);
    }

    function DateFormatter(row, cell, value, columnDef, dataContext) {
        if (!value) {
            return null;
        }
        return Formatter.Ymd(new Date(value));
    }

    function DateTimeFormatter(row, cell, value, columnDef, dataContext) {
        if (!value) {
            return null;
        }
        let v = Formatter.YmdHms(new Date(value));
        dataContext[columnDef.field] = v;
        return v;
    }

    function TimestampFormatter(row, cell, value, columnDef, dataContext) {
        if (!value) {
            return null;
        }
        return Formatter.YmdHmsS(new Date(value));
    }

    function LinkFormatter(row, cell, value, columnDef, dataContext) {
        if ((columnRegistTs != undefined && !dataContext[columnRegistTs.toLowerCase()] && !dataContext[columnRegistTs.toUpperCase()]) &&
            columnDetail != undefined && !dataContext[columnDetail.toLowerCase()] && !dataContext[columnDetail.toUpperCase()]) {
            return null;
        }
        if (value) {
            return '<a id=\"' + columnDef.id + '\" href="" class="gridLink" target="blank">' + Messages['common.download'] + '</a>';
        }
        return '<a href="" class="gridLink">' + columnDef.name + '</a>';
    }

    function SelectFormatter(row, cell, value, columnDef, dataContext) {
        return columnDef.options[value];
    }

    function CheckboxFormatter(row, cell, value, columnDef, dataContext) {
        return '<img class="slick-edit-preclick" src="../images/' + (value == 1 ? "CheckboxY" : "CheckboxN") + '.png">';
    }

    function Dec0Formatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec0(value);
    }

    function Dec1Formatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec1(value);
    }

    function Dec2Formatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec2(value);
    }

    function Dec3Formatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec3(value);
    }

    function ReferFormatter(row, cell, value, columnDef, dataContext) {
        if (dataContext[columnDef.referField]) {
            return value + '：' + dataContext[columnDef.referField];
        }
        return value;
    }

    function Dec0ReferFormatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec0(value);
    }

    function Dec1ReferFormatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec1(value);
    }

    function Dec2ReferFormatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec2(value);
    }

    function Dec3ReferFormatter(row, cell, value, columnDef, dataContext) {
        return Formatter.dec3(value);
    }

})(jQuery);
