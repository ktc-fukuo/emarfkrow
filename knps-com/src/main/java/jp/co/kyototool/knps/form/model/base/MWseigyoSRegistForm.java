package jp.co.kyototool.knps.form.model.base;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jp.co.golorp.emarf.process.BaseProcess;
import jp.co.golorp.emarf.validation.IForm;

/**
 * WEB制御マスタ一覧登録フォーム
 *
 * @author emarfkrow
 */
public class MWseigyoSRegistForm implements IForm {

    /** logger */
    private static final Logger LOG = LoggerFactory.getLogger(MWseigyoRegistForm.class);

    /** WEB制御マスタ登録フォームのリスト */
    @Valid
    private List<MWseigyoRegistForm> mWseigyoGrid;

    /**
     * @return WEB制御マスタ登録フォームのリスト
     */
    public List<MWseigyoRegistForm> getMWseigyoGrid() {
        return mWseigyoGrid;
    }

    /**
     * @param p WEB制御マスタ登録フォームのリスト
     */
    public void setMWseigyoGrid(final List<MWseigyoRegistForm> p) {
        this.mWseigyoGrid = p;
    }

    /** 関連チェック */
    @Override
    public void validate(final Map<String, String> errors, final BaseProcess baseProcess) {
        LOG.debug("not overridden in subclasses.");
    }

}