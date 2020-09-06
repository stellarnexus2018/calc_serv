package ru.sberinsur.calcserv.calc_serv.Repositories;

//region import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.sberinsur.calcserv.calc_serv.Entities.*;

import java.math.BigDecimal;
import java.util.List;

//endregion

@Repository
public class CalcServiceRep {
  //region Поля

  @Autowired
  private JdbcTemplate calc_serv_jdbc;

  private final String ACTIVE_SCHEMA           = "inSure";
  private final String TNAME_ERROR_PROTOCOL    = "error_protocol";
  private final String TNAME_OIS_RAW_POLICY    = "ois_data";
  private final String TNAME_ERROR_TYPE        = "error_type";
  private final String TNAME_POLICY_SERIES     = "policy_series";
  private final String TNAME_CALC_PROTOCOL     = "calculation_protocol";
  private final String TNAME_CALC_POLICY       = "calculation_policy";
  private final String TNAME_CALC_RESULT       = "calculation_result";
  private final String TNAME_LIFE_INS_SERIES   = "life_insurance_series";
  private final String TNAME_DEAL              = "deal";
  private final String TNAME_MARKET_INDEX_RATE = "market_index_rate";
  private final String TNAME_ASSETS            = "assets";
  private final String TNAME_CURRENCY_RATE     = "currency_rate";
  private final String TNAME_SECURITY_QUOTE    = "security_quote";
  private final String TNAME_MARKET_NAME       = "market_name";
  private final String TNAME_BORDER_CONDITIONS = "border_conditions";

  //endregion

  //region Инициализация

  public CalcServiceRep(JdbcTemplate _calc_serv_jdbc) {
    this.calc_serv_jdbc = _calc_serv_jdbc;
  }

  //endregion

  //region AddSingleErrorProtocolRecord

  /**
   * Сохранение одной записи протокола ошибок в БД
   * @param _ep Объект записи протокола ошибок
   */
  public void AddSingleErrorProtocolRecord(ErrorProtocol _ep){
    SimpleJdbcInsert ins_err_rec = new SimpleJdbcInsert(calc_serv_jdbc);
    ins_err_rec.withTableName(GetQualifiedTableName(TNAME_ERROR_PROTOCOL)).usingColumns(
        "uid",
        "created_at",
        "is_deleted",
        "calc_protocol_id",
        "calc_initiator",
        "error_type",
        "policy_id",
        "series_id",
        "policy_series_name",
        "policy_number",
        "error_message"
    );
    BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(_ep);
    ins_err_rec.execute(param);
  }//(AddSingleErrorProtocolRecord)

  //endregion

  //region AddSingleCalcProtocolRecord

  /**
   * Сохранение одной записи протокола расчёта в БД
   * @param _cp Объект записи протокола расчёта
   */
  public void AddSingleCalcProtocolRecord(CalculationProtocol _cp){
    SimpleJdbcInsert ins_calc_prot_rec = new SimpleJdbcInsert(calc_serv_jdbc);
    ins_calc_prot_rec.withTableName(GetQualifiedTableName(TNAME_CALC_PROTOCOL)).usingColumns(
        "uid",
        "created_at",
        "is_deleted",
        "id",
        "upload_session_id",
        "calculation_id",
        "initiator_sign",
        "initiator_person",
        "schedule_calc",
        "ois_policy_count",
        "calc_policy_count",
        "calc_error_count",
        "is_completed",
        "begin_calc",
        "end_calc"
    );
    BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(_cp);
    ins_calc_prot_rec.execute(param);
  }//(AddSingleCalcProtocolRecord)

  //endregion

  //region AddSingleCalcPolicyRecord

  /**
   * Сохранение одной записи обогащённых данных по полису в БД
   * @param _cp Объект записи обогащённых данных
   */
  public void AddSingleCalcPolicyRecord(CalculationPolicy _cp){
    SimpleJdbcInsert ins_calc_pol = new SimpleJdbcInsert(calc_serv_jdbc);
    ins_calc_pol.withTableName(GetQualifiedTableName(TNAME_CALC_POLICY)).usingColumns(
        "uid",
        "created_at",
        "is_deleted",
        "policy_id",
        "series_id",
        "policy_number",
        "insurance_start_date",
        "insurance_end_date",
        "insurance_premium",
        "contract_status_name",
        "currency_code",
        "fund_currency_code",
        "market_name_id",
        "tranche_date_planned",
        "tranche_date_in_fact",
        "urgency",
        "basket_id",
        "underl_asset_index",
        "stock_exchange_id",
        "base_active_exchange_id",
        "z_factor",
        "guarantee_fund",
        "investment_fund",
        "is_verified",
        "calculation_protocol_id",
        "isin"
    );
    BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(_cp);
    ins_calc_pol.execute(param);
  }//(AddSingleCalcPolicyRecord)

  //endregion

  //region AddSingleCalcResultRecord

  /**
   * Сохранение одной записи результата расчёта данных по полису в БД
   * @param _cr Объект записи результата расчёта
   */
  public void AddSingleCalcResultRecord(CalculationResult _cr){
    SimpleJdbcInsert ins_calc_res = new SimpleJdbcInsert(calc_serv_jdbc);
    ins_calc_res.withTableName(GetQualifiedTableName(TNAME_CALC_RESULT)).usingColumns(
        "uid",
        "created_at",
        "is_deleted",
        "calc_protocol_id",
        "policy_id",
        "policy_number",
        "policy_series_name",
        //"policy_series_id",
        "insurance_start_date",
        "insurance_end_date",
        "investing_start_date",
        "market_name",
        //"market_name_id",
        "investment_conditions",
        "policy_currency_code",
        "insurance_premium",
        "fund_isin",
        "fund_currency_code",
        "idd",
        "index_ba",
        "did",
        "dynamics_ba",
        "rate_ba_calc",
        "rate_ba_invest",
        "asset_price",
        "asset_buy_price",
        "asset_nominal_value",
        "rf_share",
        "participation_rate",
        "rf_shares_quantity",
        "rf_in_fund_currency",
        "rf_notes_quantity",
        "did_coupon",
        "guaranteed_coupon"
    );
    BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(_cr);
    ins_calc_res.execute(param);
  }//(AddSingleCalcResultRecord)

  //endregion

  //region GetQualifiedTableName

  public String GetQualifiedTableName(String _table_name){
    return String.format("%s.%s", ACTIVE_SCHEMA, _table_name);
  }

  //endregion

  //region GetErrorTypesLst

  /**
   * Получение списка доступных типов ошибок
   * @return Целевой список, типы ошибок в включённом состоянии
   */
  public List<ErrorType> GetErrorTypesLst(){
    String sel_sql_str =
        "select "                     +
            "et.uid, "                    +
            "et.created_at, "             +
            "et.id, "                     +
            "et.service_type_uid, "       +
            "et.error_type_val, "         +
            "et.error_stop_factor, "      +
            "et.error_type_message, "     +
            "et.is_enabled, "             +
            "et.error_type_desc "         +
            "from inSure.error_type et "  +
            "any left join inSure.service_type st on et.service_type_uid = st.uid " +
            "where et.is_enabled = 1 "    +
            "and st.type_code = 'DID_CLASSIC_1'";
    List<ErrorType> lst_result = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(ErrorType.class));
    return lst_result;
  }

  //endregion

  //region GetRawOISPolicyLst

  public List<OISRawPolicyItem> GetRawOISPolicyLst(String _ois_upload_session){
    String sel_sql_str = String.format(
        "select "                    +
        "t.ois_upload_session as ois_upload_session, "     +
        "t.ois_upload_date as ois_upload_date, "        +
        "t.sproduct as sproduct, "               +
        "t.nsync as nsync, "                  +
        "t.sproduct_series as sproduct_series, "        +
        "t.spolicy_number as spolicy_number, "         +
        "t.dstart_date as dstart_date, "            +
        "t.dterm_date as dterm_date, "             +
        "t.scurrency as scurrency, "              +
        "t.nsum_prem_det_main_rub as nsum_prem_det_main_rub, " +
        "t.nsum_prem_det_main_usd as nsum_prem_det_main_usd, " +
        "t.sinvest_cov_baseact as sinvest_cov_baseact, "    +
        "t.dinvest_invest_date as dinvest_invest_date, "    +
        "t.ddate_ as ddate_, "                 +
        "t.nduration as nduration "               +
        "from " + GetQualifiedTableName(TNAME_OIS_RAW_POLICY) + " t where t.ois_upload_session = '%s'", _ois_upload_session);
    List<OISRawPolicyItem> lst_raw_policy = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(OISRawPolicyItem.class));
    return lst_raw_policy;
  }//(GetRawOISPolicyLst)

  //endregion

  //region GetRawOISPolicyLst1

  public List<OISRawPolicyItem> GetRawOISPolicyLst1(String _ois_upload_session){
    String sss = "7727845258, 7727845390, 276393625, 4836239686, 979856519, 1071709787, 172571860, 8470225310, 8470225663, 7959287109, 8067366541, 8049100604, 8042661882, 7998743670, 7727845390, 7727845258, 7727845390, 7727845258, 8465913048, 8465906287, 8465909873, 8465912080";

    String sel_sql_str = String.format(
        "select "                    +
            "t.ois_upload_session as ois_upload_session, "     +
            "t.ois_upload_date as ois_upload_date, "        +
            "t.sproduct as sproduct, "               +
            "t.nsync as nsync, "                  +
            "t.sproduct_series as sproduct_series, "        +
            "t.spolicy_number as spolicy_number, "         +
            "t.dstart_date as dstart_date, "            +
            "t.dterm_date as dterm_date, "             +
            "t.scurrency as scurrency, "              +
            "t.nsum_prem_det_main_rub as nsum_prem_det_main_rub, " +
            "t.nsum_prem_det_main_usd as nsum_prem_det_main_usd, " +
            "t.sinvest_cov_baseact as sinvest_cov_baseact, "    +
            "t.dinvest_invest_date as dinvest_invest_date, "    +
            "t.ddate_ as ddate_, "                 +
            "t.nduration as nduration "               +
            "from inSure.ois_data t where t.ois_upload_session = '%s' and t.nsync in (%s)", _ois_upload_session, sss);
    List<OISRawPolicyItem> lst_raw_policy = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(OISRawPolicyItem.class));
    return lst_raw_policy;
  }//(GetRawOISPolicyLst1)

  //endregion

  //region GetLifeInsuranceSeriesCustom

  public List<LifeInsuranceSeries> GetLifeInsuranceSeriesCustom(String _ois_pol_series){
    String sel_sql_str = String.format(
        "select " +
        "lis.uid as uid, " +
        "lis.created_at as created_at, " +
        "lis.is_deleted as is_deleted, " +
        "lis.id as id, " +
        "lis.series_id as series_id, " +
        "lis.series_description_id as series_description_id, " +
        "lis.channel as channel, " +
        "lis.years as years, " +
        "lis.sns_risk as sns_risk, " +
        "lis.guarantee as guarantee, " +
        "lis.guarantee_fund as guarantee_fund, " +
        "lis.investment_fund as investment_fund, " +
        "lis.ic_commission_contract_conclusion_option as ic_commission_contract_conclusion_option, " +
        "lis.bank_commission_contract_conclusion_option as bank_commission_contract_conclusion_option, " +
        "lis.ic_commission_additional_payment_option as ic_commission_additional_payment_option, " +
        "lis.bank_commission_additional_payment_option as bank_commission_additional_payment_option, " +
        "lis.guaranteed_profit as guaranteed_profit, " +
        "lis.planning_profit as planning_profit, " +
        "lis.guarantee_rate as guarantee_rate, " +
        "lis.ic_commission_change_fund_option as ic_commission_change_fund_option, " +
        "lis.ic_commission_attachment_option as ic_commission_attachment_option, " +
        "lis.payment_frequency as payment_frequency, " +
        "lis.coupon_scheme as coupon_scheme, " +
        "lis.product_group as product_group, " +
        "lis.description as description, " +
        "lis.guaranteed_coupon_rate as guaranteed_coupon_rate, " +
        "lis.death_risk_payment_error_type as death_risk_payment_error_type, " +
        "lis.accident_rate as accident_rate, " +
        "lis.z_factor as z_factor, " +
        "lis.currency_code as currency_code, " +
        "lis.series_begin_date as series_begin_date, " +
        "lis.series_end_date as series_end_date " +
        "from inSure.life_insurance_series lis " +
        "any left join inSure.policy_series ps on lis.series_id = ps.id " +
        "any left join inSure.policy_series_description psd on lis.series_description_id = psd.id " +
        "where 1=1 " +
        "and lis.is_deleted = 0 " +
        "and ps.is_deleted = 0 " +
        "and psd.is_deleted = 0 " +
        "and ps.name in (%s) and psd.name in ('ИСЖ Доходный курс плюс', 'Доходный курс')", _ois_pol_series);
    List<LifeInsuranceSeries> lst_life_ins_series = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(LifeInsuranceSeries.class));

    return lst_life_ins_series;
  }//(GetLifeInsuranceSeriesCustom)

  //endregion

  //region GetPolicySeriesCustom

  public List<PolicySeries> GetPolicySeriesCustom(String _pol_series_names){
    String sel_sql_str = String.format(
        /*"select " +
            "t.uid as uid, " +
            "t.created_at as created_at, " +
            "t.is_deleted as is_deleted, " +
            "t.id as id, " +
            "t.name as name " +
        */

        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.id, " +
        "t.name " +
        "from %s t where t.is_deleted = 0 and t.name in (%s)", TNAME_POLICY_SERIES, _pol_series_names);
    List<PolicySeries> lst_policy_series = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(PolicySeries.class));

    return lst_policy_series;
  }//(GetPolicySeriesCustom)

  //endregion

  //region GetUnderlyingAssetCustom

  public List<UnderlyingAsset> GetUnderlyingAssetCustom(String _market_name, String _invest_date_start){
    String sel_sql_str =
        "select " +
        "ua.uid, " +
        "ua.created_at, " +
        "ua.is_deleted, " +
        "ua.id, " +
        "ua.market_name_id, " +
        "ua.market_index_id, " +
        "ua.currency_code, " +
        "ua.begin_date, " +
        "ua.end_date, " +
        "ua.undrl_asset_platform, " +
        "ua.description, " +
        "ua.stock_exchange_id " +
        "from inSure.underlying_asset ua " +
        "any left join inSure.market_name mn on ua.market_name_id = mn.id " +
        "where ua.is_deleted = 0 " +
        "and mn.name = '" + _market_name +"' " +
        "and ua.begin_date <= toDate('" + _invest_date_start + "') " +
        "and ua.end_date >= toDate('" + _invest_date_start + "')";
    List<UnderlyingAsset> lst_underlayng_assets = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(UnderlyingAsset.class));

    return lst_underlayng_assets;
  }//(GetUnderlyingAssetCustom)

  //endregion

  //region GetProductFundCustom

  public List<ProductFund> GetProductFundCustom(long _underl_asset_id, String _tranche_date, int _duration){
    String sel_sql_str =
        "select "                          +
            "pf.uid, "                     +
            "pf.created_at, "              +
            "pf.is_deleted, "              +
            "pf.underlying_asset_id, "     +
            "pf.isin, "                    +
            "pf.code, "                    +
            "pf.tranche_date, "            +
            "pf.duration, "                +
            "pf.fund_currency, "           +
            "pf.fund_description, "        +
            "pf.description, "             +
            "pf.stock_exchange_id, "       +
            "pf.issuer_id "                +
            "from inSure.product_fund pf " +
        "where pf.is_deleted = 0 "         +
        "and pf.underlying_asset_id = " + _underl_asset_id + " " +
        "and pf.tranche_date = toDate('" + _tranche_date + "') " +
        "and pf.duration = " + _duration;
    List<ProductFund> lst_product_fund = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(ProductFund.class));
    return lst_product_fund;
  }//(GetProductFundCustom)

  //endregion

  //region GetCalcPolicyListCustom

  public List<CalculationPolicy> GetCalcPolicyListCustom(){
    String sel_sql_str = String.format(
        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.policy_id, " +
        "t.series_id, " +
        "t.policy_number, " +
        "t.insurance_start_date, " +
        "t.insurance_end_date, " +
        "t.insurance_premium, " +
        "t.contract_status_name, " +
        "t.currency_code, " +
        "t.fund_currency_code, " +
        "t.market_name_id, " +
        "t.tranche_date_planned, " +
        "t.tranche_date_in_fact, " +
        "t.urgency, " +
        "t.basket_id, " +
        "t.underl_asset_index, " +
        "t.stock_exchange_id, " +
        "t.base_active_exchange_id, " +
        "t.z_factor, " +
        "t.guarantee_fund, " +
        "t.investment_fund, " +
        "t.is_verified, " +
        "t.calculation_protocol_id, " +
        "t.isin " +
        "from %s t where t.is_deleted = 0 and t.is_verified = 1 ", GetQualifiedTableName(TNAME_CALC_POLICY));
    List<CalculationPolicy> lst_calc_policy = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(CalculationPolicy.class));

    return lst_calc_policy;
  }//(GetCalcPolicyListCustom)

  //endregion

  //region GetDealListCustom

  public List<Deal> GetDealListCustom(String _isin_str){
    String sel_sql_str = String.format(
        "select " +
            "t.uid, " +
            "t.created_at, " +
            "t.is_deleted, " +
            "t.isin, " +
            "t.quotation_date, " +
            "t.price, " +
            "t.deal_type " +
            "from %s t  " +
            "where t.is_deleted = 0 " +
            "and t.isin in (%s) " +
            "order by t.quotation_date asc",
        GetQualifiedTableName(TNAME_DEAL),
        _isin_str);
    List<Deal> lst_deal = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(Deal.class));

    return lst_deal;
  }//(GetDealListCustom)

  //endregion

  //region GetMarketIndexRateListCustom

  public List<MarketIndexRate> GetMarketIndexRateListCustom(String _underl_asset_indx_str){
    String sel_sql_str = String.format(
        "select " +
            "t.uid, " +
            "t.created_at, " +
            "t.is_deleted, " +
            "t.market_index_id, " +
            "t.quotation_date, " +
            "t.close_price, " +
            "t.stock_exchange_id " +
            "from %s t where t.is_deleted = 0 and t.market_index_id in (%s) order by t.quotation_date asc",
        GetQualifiedTableName(TNAME_MARKET_INDEX_RATE), _underl_asset_indx_str);
    List<MarketIndexRate> lst_deal = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(MarketIndexRate.class));

    return lst_deal;
  }//(GetMarketIndexRateListCustom)

  //endregion

  //region GetAssetsListCustom

  public List<Assets> GetAssetsListCustom(String _isin_str){
    String sel_sql_str = String.format(
        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.isin, " +
        "t.nominal_value, " +
        "t.issuer_id, " +
        "t.currency_code " +
        "from %s t where t.is_deleted = 0 and t.isin in (%s)",
        GetQualifiedTableName(TNAME_ASSETS), _isin_str);
    List<Assets> lst_assets = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(Assets.class));

    return lst_assets;
  }//(GetAssetsListCustom)

  //endregion

  //region GetCurrencyRateListCustom

  public List<CurrencyRate> GetCurrencyRateListCustom(String _currency_code, String _target_date){
    String sel_sql_str = String.format(
        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.currency_code, " +
        "t.rate_date, " +
        "t.rate " +
        "from %s t where t.is_deleted = 0 and t.currency_code = '%s' and t.rate_date = toDate('%s')",
        GetQualifiedTableName(TNAME_CURRENCY_RATE), _currency_code, _target_date);
    List<CurrencyRate> lst_currency_rate = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(CurrencyRate.class));

    return lst_currency_rate;
  }//(GetCurrencyRateListCustom)

  //endregion

  //region GetSecurityQuoteListCustom

  public List<SecurityQuote> GetSecurityQuoteListCustom(String _isin_str_arr){
    String sel_sql_str = String.format(
        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.isin, " +
        "t.quotation_date, " +
        "t.recognized_quotation, " +
        "t.stock_exchange_id " +
        "from %s t where t.is_deleted = 0 and t.isin in (%s)",
        GetQualifiedTableName(TNAME_SECURITY_QUOTE), _isin_str_arr);
    List<SecurityQuote> lst_sec_quote = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(SecurityQuote.class));

    return lst_sec_quote;
  }//(GetSecurityQuoteListCustom)

  //endregion

  //region GetPolicySeriesListCustom

  public List<PolicySeries> GetPolicySeriesListCustom(String _series_id_str){
    String sel_sql_str = String.format(
        "select " +
        "t.uid, " +
        "t.created_at, " +
        "t.is_deleted, " +
        "t.id, " +
        "t.name " +
        "from %s t where t.is_deleted = 0 and t.id in (%s)",
        GetQualifiedTableName(TNAME_POLICY_SERIES), _series_id_str);

    List<PolicySeries> lst_policy_series = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(PolicySeries.class));

    return lst_policy_series;
  }//(GetPolicySeriesListCustom)

  //endregion

  //region GetMarketNamesListCustom

  public List<MarketName> GetMarketNamesListCustom(String _pol_mname_id_str){
    String sel_sql_str = String.format(
        "select " +
            "t.uid, " +
            "t.created_at, " +
            "t.is_deleted, " +
            "t.id, " +
            "t.name " +
            "from %s t where t.is_deleted = 0 and t.id in (%s)",
        GetQualifiedTableName(TNAME_MARKET_NAME), _pol_mname_id_str);
    List<MarketName> lst_market_name = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(MarketName.class));
    return lst_market_name;
  }//(GetMarketNamesListCustom)

  //endregion

  //region GetBorderConditionListCustom

  public List<BorderCondition> GetBorderConditionListCustom(String _service_type_code, String _err_type_ids){
    String sel_sql_str = String.format(
        "select "               +
        "bc.uid, "              +
        "bc.created_at, "       +
        "bc.is_deleted, "       +
        //"bc.id, "             +
        "bc.error_type, "       +
        //"bc.service_type_uid, " +
        "bc.limit_one, "        +
        "bc.limit_two, "        +
        "bc.market_name_id "    +
        "from inSure.border_conditions bc " +
        "any left join inSure.error_type et on bc.error_type = et.error_type_val " +
        "where bc.is_deleted = 0 " +
        "and et.is_enabled = 1");

        //"any left join inSure.service_type st on bc.service_type_uid = st.uid " +
        //"where bc.is_deleted = 0 " +
        //"and st.type_code = '%s'" +
        //"and bc.id in (%s)", _service_type_code, _err_type_ids);
        //"and bc.error_type in (%s)", _service_type_code, _err_type_ids);

    List<BorderCondition> lst_border_cond = calc_serv_jdbc.query(sel_sql_str, BeanPropertyRowMapper.newInstance(BorderCondition.class));
    return lst_border_cond;
  }//(GetBorderConditionListCustom)

  //endregion
}//(CalcServiceRep)