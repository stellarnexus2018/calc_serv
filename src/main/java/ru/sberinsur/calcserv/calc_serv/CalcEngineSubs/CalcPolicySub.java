package ru.sberinsur.calcserv.calc_serv.CalcEngineSubs;

import ru.sberinsur.calcserv.calc_serv.Entities.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalcPolicySub {
  static CalculationProtocol calc_prot;

  public CalcPolicySub(CalculationProtocol _calc_prot){
    calc_prot = _calc_prot;
  }

  public static CalculationPolicy GetCalcPolicyItem(OISRawPolicyItem _rpi,
                                                    LifeInsuranceSeries _lis,
                                                    UnderlyingAsset _ua,
                                                    ProductFund _pf,
                                                    int _is_verified){
    CalculationPolicy cp = new CalculationPolicy();     // Полис для расчёта
    cp.setCalculation_protocol_id(calc_prot.getId());   // UID расчёта
    cp.setIs_verified(_is_verified);                    // Признак валидации
    if(_is_verified == 1){
      //calc_prot.UpCompleteCnt();
    }

    // _rpi
    cp.setPolicy_id(_rpi.getNsync());                           // ID полиса nsync
    cp.setPolicy_number(_rpi.getSpolicy_number());              // Номер полиса spolicy_number
    cp.setInsurance_start_date(_rpi.getDstart_date());          // Дата начала страхования dstart_date
    cp.setInsurance_end_date(_rpi.getDterm_date());             // Дата окончания страхования dterm_date (???Если в выгрузке ОИС заполнено поле DEND_DATE, то значение этого поля, если нет - значение поля DTERM_DATE???)
    cp.setCurrency_code(_rpi.getScurrency());                   // Валюта полиса scurrency
    cp.setTranche_date_planned(_rpi.getDdate_());               // Дата транша план ddate_
    cp.setTranche_date_in_fact(_rpi.getDinvest_invest_date());  // Дата транша факт dinvest_invest_date
    cp.setUrgency(_rpi.getNduration());                         // Срочность nduration
    switch (_rpi.getScurrency().toUpperCase()){
      case "RUB":
        cp.setInsurance_premium((_rpi.getNsum_prem_det_main_rub()));
        break;
      case "USD":
        cp.setInsurance_premium((_rpi.getNsum_prem_det_main_usd()));
        break;
      default:
        cp.setInsurance_premium((_rpi.getNsum_prem_det_main_rub()));
        break;
    }

    // _lis
    if(_lis != null){
      cp.setSeries_id(_lis.getSeries_id());                     // series_id
      cp.setZ_factor(_lis.getZ_factor());                       // z_factor
      cp.setGuarantee_fund(_lis.getGuarantee_fund());           // guarantee_fund
      //cp.setInvestment_fund(_lis.getInvestment_fund());         // investment_fund
      cp.setInvestment_fund(_lis.getInvestment_fund().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));         // костыльно корректируем значение, переводим в доли процентов
    } else {
      cp.setSeries_id(0l);
      cp.setZ_factor(BigDecimal.ZERO);
      cp.setGuarantee_fund(BigDecimal.ZERO);
      cp.setInvestment_fund(BigDecimal.ZERO);
    }

    // _ua
    if(_ua != null) {
      cp.setMarket_name_id(_ua.getMarket_name_id());              // market_name_id маркетинговое наименование
      cp.setUnderl_asset_index(_ua.getMarket_index_id());         // market_index_id индекс БА
      cp.setBase_active_exchange_id(_ua.getStock_exchange_id());  // биржа ба
      cp.setUnderl_asset_id(_ua.getId());                         // код ба
    } else {
      cp.setMarket_name_id(0l);
      cp.setUnderl_asset_index("");
      cp.setBase_active_exchange_id(0l);  // биржа ба
      cp.setUnderl_asset_id(0l);                         // код ба
    }

    // _pf
    if(_pf != null) {
      cp.setIsin(_pf.getIsin());                            // isin
      cp.setStock_exchange_id(_pf.getStock_exchange_id());  // Биржа фонда
      cp.setFund_currency_code(_pf.getFund_currency());     // Валюта фонда (буквенный код валюты alfa-3: RUB, USD, CHF...)
    } else {
      cp.setIsin("");
      cp.setStock_exchange_id(0l);  // Биржа фонда
      cp.setFund_currency_code("");     // Валюта фонда (буквенный код валюты alfa-3: RUB, USD, CHF...)
    }

    cp.setContract_status_name("valid");

    return cp;
  }//(GetCalcPolicyItem)
}