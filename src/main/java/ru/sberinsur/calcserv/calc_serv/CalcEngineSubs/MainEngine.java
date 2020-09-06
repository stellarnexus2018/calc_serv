package ru.sberinsur.calcserv.calc_serv.CalcEngineSubs;

//region import

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.handler.ExceptionHandlingWebHandler;
import ru.sberinsur.calcserv.calc_serv.Entities.*;
import ru.sberinsur.calcserv.calc_serv.Repositories.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

//endregion

/**
 * Основной управляющий класс проекта
 */
@Slf4j
public class MainEngine {
  //region Поля

  private CalcServiceRep          calc_serv_rep;
  private ErrorProtocolSub        err_prot_sub;
  private CalcPolicySub           calc_pol_sub;
  private CalculationProtocol     calc_prot_item;
  private List<ErrorProtocol>     lst_err_prot;
  private List<CalculationPolicy> lst_calc_pol;
  private List<CalculationResult> lst_calc_result;
  private SimpleDateFormat        format_full;
  private SimpleDateFormat        format_short;
  private String                  service_type_code;
  private int                     big_decimal_scale;

  //endregion

  //region Инициализация

  public MainEngine(CalculationProtocol _calc_prot_item,
                    CalcServiceRep      _calc_serv_rep,
                    String              _service_type_code,
                    int                 _big_decimal_scale
  ) {
    big_decimal_scale = _big_decimal_scale;
    service_type_code = _service_type_code;
    format_full       = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    format_short      = new SimpleDateFormat("yyyy-MM-dd");
    calc_serv_rep     = _calc_serv_rep;
    calc_prot_item    = _calc_prot_item;
    err_prot_sub      = new ErrorProtocolSub(_calc_prot_item);
    calc_pol_sub      = new CalcPolicySub(_calc_prot_item);
  }

  public void Init() {
    try{
      // Тянем список с типами ошибок
      List<ErrorType> lst_err_types = calc_serv_rep.GetErrorTypesLst();
      if (lst_err_types != null && lst_err_types.size() > 0){
        err_prot_sub.Init(lst_err_types);
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }//(Init)

  //endregion

  //region EnrollProcessSaveDB

  public void EnrollProcessSaveDB(){
    // сохраняем ошибки
    if (lst_err_prot != null&&lst_err_prot.size() > 0){
      for (ErrorProtocol ep_one : lst_err_prot) {
        calc_serv_rep.AddSingleErrorProtocolRecord(ep_one);
      }
    }

    // сохраняем обогащённые полисы
    if (lst_calc_pol != null&&lst_calc_pol.size() > 0) {
      for (CalculationPolicy cp_one : lst_calc_pol) {
        calc_serv_rep.AddSingleCalcPolicyRecord(cp_one);
      }
    }

    // сохраняем результаты расчётов
    if (lst_calc_result != null&&lst_calc_result.size() > 0) {
      for (CalculationResult cr_one : lst_calc_result) {
        calc_serv_rep.AddSingleCalcResultRecord(cr_one);
      }
    }

    calc_prot_item.setEnd_calc(Timestamp.valueOf(LocalDateTime.now()));

    // сохраняем протокол
    calc_serv_rep.AddSingleCalcProtocolRecord(calc_prot_item);
  }//(EnrollProcessSaveDB)

  //endregion

  //region EnrollProcessCalculation

  public void EnrollProcessCalculation(){
    String current_ois_upload_session_id = calc_prot_item.getUpload_session_id();

    // Запускаем перичный анализ на RAW-данных из ОИС
    boolean is_analyze_done = ProcessRawRecordsForCalcPolicy(current_ois_upload_session_id);
    //boolean is_analyze_done = true; // для отладки

    // Проверка качества анализа
    if(!is_analyze_done){
      // По результатам нечего рассчитывать
      calc_prot_item.setIs_completed(0);
    } else {
      // Запускаем расчёт на основе обогащённых данных по полисам
      boolean is_calc_done = ProcessCalcPolicyData(current_ois_upload_session_id);

      if(!is_calc_done){
        calc_prot_item.setIs_completed(0);
      } else {
        calc_prot_item.setIs_completed(1);
      }
    }

    // запускаем сохранение рассчитанных данных в БД
    EnrollProcessSaveDB();
  }//(EnrollProcessCalculation)

  //endregion

  //region ProcessRawRecordsForCalcPolicy

  /**
   * Запуск предварительного анализа выгруженных данных из ОИС,
   * подготовка данных к расчёту
   * @param _ois_upload_session_id ID сессии загрузки из ОИС
   * @return Флаг результата анализа (=true - преходим к процессу расчёта, =false - выход из процесса)
   */
  public boolean ProcessRawRecordsForCalcPolicy(String _ois_upload_session_id) {
    boolean result = false;

    ErrorProtocol ep = null;
    List<String> dublicate_dict_series;
    List<String> non_found_dict_series;

    //region Шаг 1

    // Читаем список полисов выгруженных из ОИС для расчёта
    //List<OISRawPolicyItem> lst_ois_raw_pol = calc_serv_rep.GetRawOISPolicyLst(_ois_upload_session_id);
    List<OISRawPolicyItem> lst_ois_raw_pol = calc_serv_rep.GetRawOISPolicyLst1(_ois_upload_session_id);

    int raw_policy_count = lst_ois_raw_pol.size();

    calc_prot_item.setOis_policy_count(raw_policy_count);

    if(raw_policy_count < 1) {
      lst_err_prot = new ArrayList<>(1);
      ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoRawRecordsUpload);
      lst_err_prot.add(ep);

      if (ErrorProtocolSub.isStopFactor(ep)){
        return false;
      }
    } else {
      lst_err_prot = new ArrayList<>(raw_policy_count);
      lst_calc_pol = new ArrayList<>(raw_policy_count);
    }

    //endregion Шаг 1

    //region Шаг 2

    // Читаем связаные серии полисов из справочника ИСЖ и ИСЖК, кэшируем выбранные справочники, проверяем на отсутствие серий, дубликаты
    List<String> lst_raw_pol_series = lst_ois_raw_pol
        .stream()
        .map(m -> m.getSproduct_series().trim().toUpperCase())
        .distinct()
        .collect(Collectors.toList());

    String series_names_raw = lst_raw_pol_series.stream().map(Utils::GetQuotedString).collect(Collectors.joining(", "));

    // Список из life_insurance_series по тем сериям, которые в выборке полисов distinct
    List<LifeInsuranceSeries> lst_ins_pol_series_dict = calc_serv_rep.GetLifeInsuranceSeriesCustom(series_names_raw);

    // В выборку попадают серии с описанием серии: 'ИСЖ Доходный курс плюс' и 'Доходный курс'
    if(lst_ins_pol_series_dict.size() < 1) {
      ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoRecordsUpload);
      lst_err_prot.add(ep);

      if (ErrorProtocolSub.isStopFactor(ep)){
        return false;
      }
    }

    // получение списка ID для имён серий полисов
    List<PolicySeries> lst_pol_series_dict = calc_serv_rep.GetPolicySeriesCustom(series_names_raw);

    dublicate_dict_series = new ArrayList<>(lst_raw_pol_series.size());
    non_found_dict_series = new ArrayList<>(lst_raw_pol_series.size());

    // Составляем распределение имён серий на ID (может быть полис с серией не проставленной в справочнике)
    HashMap<String, Long> polser_to_id = new HashMap<>(lst_raw_pol_series.size());
    HashMap<String, Long> polser_to_id_final = new HashMap<>(lst_raw_pol_series.size());

    for (String rps_one : lst_raw_pol_series) {
      Optional<PolicySeries> pol_ser = lst_pol_series_dict
          .stream()
          .filter(m -> m.getName().toUpperCase().equals(rps_one.toUpperCase()))
          .findFirst();
      if(!pol_ser.isPresent()) {
        // Нет серии полиса в словаре
        non_found_dict_series.add(rps_one);
      } else {
        PolicySeries ps = pol_ser.get();
        polser_to_id.put(rps_one, ps.getId());
      }
    }

    // Исследуем справочник ИСЖ и ИСЖК на предмет отсутствия или дубликатов записей
    for (Map.Entry<String, Long> polserid_one : polser_to_id.entrySet()) {
      long target_data_cnt = lst_ins_pol_series_dict
          .stream()
          .filter(m -> m.getSeries_id() == polserid_one.getValue())
          .count();

      if(target_data_cnt == 0){
        // В справочнике не представлены данные по серии
        non_found_dict_series.add(polserid_one.getKey());
      }else if(target_data_cnt > 1){
        // В справочнике найдены дубликаты данных по серии
        dublicate_dict_series.add(polserid_one.getKey());
      }else{
        // Всё в порядке
        polser_to_id_final.put(polserid_one.getKey(), polserid_one.getValue());
      }
    }

    //endregion Шаг 2

    //region Шаг 3

    // Читаем наименования ID серий полисов по наименованиям серий полисов
    Map<String, Long> mp_ser_name_id = calc_serv_rep
        .GetPolicySeriesCustom(series_names_raw)
        .stream()
        .collect(Collectors.toMap(m -> m.getName(), m -> m.getId()));

    // Сейчас сценарий, когда имеем:
    // список отсутствующих значений
    // список дубликатов
    // список нормальных значений
    // Двигаемся по коллекции полисов, формируем промежуточный результат
    CalculationPolicy cp;
    int is_valid = 1;

    for (OISRawPolicyItem ois_raw_pol_one : lst_ois_raw_pol) {
      //log.info("ois_raw_pol_one.getSproduct_series() {}", ois_raw_pol_one.getSproduct_series());

      try{
        long r = mp_ser_name_id.get(ois_raw_pol_one.getSproduct_series());
      }catch (Exception ex){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoDictPolSerieFound, ois_raw_pol_one.getSproduct_series());
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      String pol_serie = ois_raw_pol_one.getSproduct_series();
      DataParamCont odms = new DataParamCont(ois_raw_pol_one.getNsync(),
                                             mp_ser_name_id.get(ois_raw_pol_one.getSproduct_series()),
                                             ois_raw_pol_one.getSproduct_series(),
                                             ois_raw_pol_one.getSpolicy_number());
      odms.ResetContent();
      odms.AddParamData("POLICY_NUMBER", ois_raw_pol_one.getSpolicy_number());

      // Отлавливаем отсутствующих
      if(non_found_dict_series.stream().anyMatch(m -> m.toUpperCase().equals(pol_serie.toUpperCase()))){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoPolicySerieFound, odms);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // Отлавливаем дубликаты
      if(dublicate_dict_series.stream().anyMatch(m -> m.toUpperCase().equals(pol_serie.toUpperCase()))){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.DublicatePolicySerieFound, odms);//21
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // здесь считаем количество полисов по группе серий
      calc_prot_item.UpCompleteCnt();

      long pol_ser_id = polser_to_id_final.get(pol_serie);
      LifeInsuranceSeries lis = null;
      Optional<LifeInsuranceSeries> opt_lis = lst_ins_pol_series_dict
          .stream()
          .filter(m -> m.getSeries_id()==pol_ser_id).findFirst();
      if(opt_lis.isPresent()){
        lis = opt_lis.get();
      }

      //log.info("lis {}", lis != null);

      // Значение "Инвестиционный фонд", равно 0
      if(Utils.isZeroBigDecimal(lis.getInvestment_fund())){
        is_valid = 0;
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoInvestmentFundVal, odms);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      UnderlyingAsset ua = null;
      List<UnderlyingAsset> lst_ua = calc_serv_rep.GetUnderlyingAssetCustom(ois_raw_pol_one.getSinvest_cov_baseact(), format_full.format(ois_raw_pol_one.getDdate_()));
      int lst_ua_size = lst_ua.size();
      if(lst_ua_size == 1){
        Optional<UnderlyingAsset> opt_ua = lst_ua.stream().findFirst();
        ua = opt_ua.get();
      }else{
        is_valid = 0;
        if (lst_ua_size < 1){
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoUnderLyingAsset, odms);
        }else{
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.DublicateUnderLyingAsset, odms);
        }
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      //log.info("ua {}", ua != null);

      ProductFund pf = null;
      List<ProductFund> lst_pf = calc_serv_rep.GetProductFundCustom(ua.getId(), format_full.format(ois_raw_pol_one.getDdate_()), ois_raw_pol_one.getNduration());
      if(lst_pf.size() == 1){
        Optional<ProductFund> opt_pf = lst_pf.stream().findFirst();
        pf = opt_pf.get();
      }else{
        is_valid = 0;
        odms.AddParamData("UNDERLYING_ASSET_INDEX", ua.getMarket_index_id());
        if (lst_ua_size < 1) {
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoProductFund, odms);
        } else {
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.DublicateProductFund, odms);
        }
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      //log.info("pf {}", pf != null);

      //log.info("ois_raw_pol_one {}", ois_raw_pol_one != null);
      //log.info("lis {}", lis == null);
      //log.info("ua {}", ua == null);
      //log.info("pf {}", pf == null);

      // Запись с обогащёнными данными по полису
      cp = CalcPolicySub.GetCalcPolicyItem(ois_raw_pol_one, lis, ua, pf, is_valid);
      lst_calc_pol.add(cp);
    }

    //endregion Шаг 3

    // Количество полисов по условию групп серий полисов
    long selected_policy_count = lst_calc_pol.size();
    calc_prot_item.setCalc_policy_count(selected_policy_count);

    // Проверяем количество обогащённых полисов, готовых для дальнейшего расчёта
    long verified_policy_count = lst_calc_pol.stream().filter(m -> m.getIs_verified() == 1).count();
    if (verified_policy_count > 0) {
      result = true;
    }

    return result;
  }//(ProcessRawRecordsForCalcPolicy)

  //endregion

  //region ProcessCalcPolicyData

  public boolean ProcessCalcPolicyData(String _ois_upload_session_id) {
    boolean result = false;

    // Скрываем глобальный объект для тестирования
    //lst_calc_pol = calc_serv_rep.GetCalcPolicyListCustom();
    //List<ErrorProtocol> lst_err_prot = new ArrayList<>(10000);

    // region Прогружаем оперативные справочники

    List<Long> l_lst_pol_ser_id = lst_calc_pol
        .stream()
        .map(m -> m.getSeries_id())
        .distinct()
        .collect(Collectors.toList());

    List<Long> l_lst_pol_mname_id = lst_calc_pol
        .stream()
        .map(m -> m.getMarket_name_id())
        .distinct()
        .collect(Collectors.toList());

    List<String> s_lst_policy_isins = lst_calc_pol
        .stream()
        .map(m -> m.getIsin().trim())
        .distinct()
        .collect(Collectors.toList());

    List<String> s_lst_ua_indx = lst_calc_pol
        .stream()
        .map(m -> m.getUnderl_asset_index().trim())
        .distinct()
        .collect(Collectors.toList());

    // Все ISIN полисов
    String s_policy_isins = s_lst_policy_isins
        .stream()
        .map(Utils::GetQuotedString)
        .collect(Collectors.joining(", "));

    // Все Индексы БА полисов
    String s_ua_indx = s_lst_ua_indx
        .stream()
        .map(Utils::GetQuotedString)
        .collect(Collectors.joining(", "));

    // Все ID серий полисов
    String s_pol_ser_id = l_lst_pol_ser_id
        .stream()
        .map(m -> m.toString())
        .collect(Collectors.joining(", "));

    // Все ID маркетинговых наименований
    String s_pol_mname_id = l_lst_pol_mname_id
        .stream()
        .map(m -> m.toString())
        .collect(Collectors.joining(", "));

    // Читаем котировки ЦБ на бирже по всем ISIN в выборке
    List<SecurityQuote> lst_sec_quotes = calc_serv_rep.GetSecurityQuoteListCustom(s_policy_isins);

    // Читаем сделки по всем ISIN в выборке
    List<Deal> lst_deals = calc_serv_rep.GetDealListCustom(s_policy_isins);

    // Читаем котировки индексов по всем "Индексам БА" в выборке
    List<MarketIndexRate> lst_mir = calc_serv_rep.GetMarketIndexRateListCustom(s_ua_indx);

    // Читаем активы по всем ISIN
    List<Assets> lst_asset = calc_serv_rep.GetAssetsListCustom(s_policy_isins);

    // Читаем граничные условия по всем ID маркетинговых наименований для целевого сервиса
    List<BorderCondition> lst_border_cond = calc_serv_rep.GetBorderConditionListCustom(service_type_code, ErrorProtocolSub.GetAvailableErrorTypeCodes().stream().map(m -> m.toString()).collect(Collectors.joining(", ")));

    // Читаем наименования серий полисов по всем ID
    Map<Long, String> mp_ser_names = calc_serv_rep
        .GetPolicySeriesListCustom(s_pol_ser_id)
        .stream()
        .collect(Collectors.toMap(m -> m.getId(), m -> m.getName()));

    // Читаем маркетинговые наименования по всем ID
    Map<Long, String> mp_market_names = calc_serv_rep
        .GetMarketNamesListCustom(s_pol_mname_id)
        .stream()
        .collect(Collectors.toMap(m -> m.getId(), m -> m.getName()));

    // endregion Прогружаем оперативные справочники

    lst_calc_result = new ArrayList<>(lst_calc_pol.size());

    CalculationResult cr = null;
    DataParamCont pd_cont = null;
    ErrorProtocol ep = null;

    // Рассчитываем каждую действующую запись из списка обогащённых полисов
    for (CalculationPolicy calc_pol_one:lst_calc_pol) {
      cr = new CalculationResult();
      pd_cont = new DataParamCont(calc_pol_one.getPolicy_id(),
                               calc_pol_one.getSeries_id(),
                               mp_ser_names.get(calc_pol_one.getSeries_id()),
                               calc_pol_one.getPolicy_number());
      pd_cont.ResetContent();
      pd_cont.AddParamData("POLICY_NUMBER", calc_pol_one.getPolicy_number());
      pd_cont.AddParamData("ISIN", calc_pol_one.getIsin());
      pd_cont.AddParamData("UNDERLYING_ASSET_INDEX", calc_pol_one.getUnderl_asset_index());

      // region Не расчётные поля

      cr.setCalc_protocol_id(calc_prot_item.getId());                                 // ID протокола расчёта
      cr.setPolicy_id(calc_pol_one.getPolicy_id());                                   // ID полиса
      cr.setPolicy_number(calc_pol_one.getPolicy_number());                           // номер полиса
      //cr.setPolicy_series_id(calc_pol_one.getSeries_id());                          // серия полиса
      cr.setPolicy_series_name(mp_ser_names.get(calc_pol_one.getSeries_id()));        // серия полиса
      //cr.setMarket_name_id(calc_pol_one.getMarket_name_id());                       // маркетинговое наименование
      cr.setMarket_name(mp_market_names.get(calc_pol_one.getMarket_name_id()));       // маркетинговое наименование
      cr.setInsurance_start_date(calc_pol_one.getInsurance_start_date());             // дата начала страхования
      cr.setInsurance_end_date(calc_pol_one.getInsurance_end_date());                 // дата окончания страхования
      cr.setInsurance_premium(calc_pol_one.getInsurance_premium());                   // страховая премия
      cr.setPolicy_currency_code(calc_pol_one.getCurrency_code());                    // валюта полиса
      cr.setInvesting_start_date(calc_pol_one.getTranche_date_planned());             // дата инвестирования
      cr.setAsset_nominal_value(calc_pol_one.getInsurance_premium());                 // номинал полиса
      cr.setZ_factor(calc_pol_one.getZ_factor());                                     // Коэффициент Z
      cr.setIndex_ba(calc_pol_one.getUnderl_asset_index());                           // Индекс БА
      cr.setFund_isin(calc_pol_one.getIsin());                                        // ISIN фонда
      cr.setFund_currency_code(calc_pol_one.getFund_currency_code());                 // Валюта фонда
      cr.setRf_share(calc_pol_one.getInvestment_fund());                              // Доля РФ

      // endregion Не расчётные поля

      // region Дата расчёта

      LocalDate dt_now = LocalDate.now(); // задаём по алгоритму
      LocalDate dt_min = LocalDate.MIN;   // минимальная дата для обоих выборок

      // читаем сделки по isin
      List<Deal> lst_deals_by_isin = lst_deals
          .stream()
          .filter(m -> m.getIsin().equals(calc_pol_one.getIsin()))
          .collect(Collectors.toList());

      // читаем котировки индексов по индексу БА
      List<MarketIndexRate> lst_mir_by_uaindex = lst_mir
          .stream()
          .filter(n -> n.getMarket_index_id().equals(calc_pol_one.getUnderl_asset_index()))
          .collect(Collectors.toList());

      if (lst_deals_by_isin.size() == 0) {
        // Не найдена сделка по ISIN
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoDealByISIN, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }
      if (lst_mir_by_uaindex.size() == 0) {
        // Не найдена биржевая котировка по индексу БА
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoMarketIndexRateByUAIndx, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // вычисляем минимальную дату, на которую есть данные в выборке сделок
      LocalDate d_min_deals = lst_deals_by_isin.stream().map(m -> m.getQuotation_date().toLocalDate()).min(LocalDate::compareTo).get();

      // вычисляем минимальную дату, на которую есть данные в выборке котировок индексов
      LocalDate d_min_mir = lst_mir_by_uaindex.stream().map(m -> m.getQuotation_date().toLocalDate()).min(LocalDate::compareTo).get();

      if (d_min_deals.isAfter(d_min_mir)){
        dt_min = d_min_deals;
      }else if(d_min_mir.isAfter(d_min_deals)){
        dt_min = d_min_mir;
      }else if(d_min_mir.isEqual(d_min_deals)){
        dt_min = d_min_mir;
      }

      boolean is_calc_at = false;
      while (!dt_now.isBefore(dt_min)){
        LocalDate finalDt_now = dt_now;

        boolean is_deal_found = lst_deals_by_isin.stream().anyMatch(m -> m.getQuotation_date().toLocalDate().equals(finalDt_now));
        boolean is_mir_found = lst_mir_by_uaindex.stream().anyMatch(m -> m.getQuotation_date().toLocalDate().equals(finalDt_now));

        if (is_deal_found&&is_mir_found){
          cr.setCalculated_at(java.sql.Date.valueOf(dt_now));
          log.info("cr.setCalculated_at {}", java.sql.Date.valueOf(dt_now));
          is_calc_at = true;
          break;
        }else{
          dt_now = dt_now.minusDays(1);
        }
      }

      if (!is_calc_at){
        // не определена дата расчёта
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoCalcDateFound, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      if(Period.between(dt_now, LocalDate.now()).getDays() > 365 ){
        // дата расчёта на период больше года
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.LessByYearCalcDateFound, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // endregion Дата расчёта

      // region Котировка БА на дату расчёта

      LocalDate calc_at = cr.getCalculated_at().toLocalDate();
      Optional<MarketIndexRate> opt_mir_uai = lst_mir_by_uaindex
          .stream()
          .filter(n -> n.getQuotation_date().toLocalDate().isEqual(calc_at))
          .filter(m -> m.getStock_exchange_id()==calc_pol_one.getStock_exchange_id())
          .findFirst();
      if (opt_mir_uai.isPresent()){
        MarketIndexRate mir = opt_mir_uai.get();
        cr.setRate_ba_calc(mir.getClose_price());
        log.info("cr.setRate_ba_calc {}", mir.getClose_price());
      } else {
        // генерим ошибку, не найден Котировка БА на дату расчёта (динамика ба зависит)
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteUA, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      if(Period.between(calc_at, LocalDate.now()).getDays() > 10 ){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteUAActual, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // endregion Котировка БА на дату расчёта

      // region Котировка БА на дату начала инвестирования

      LocalDate finalInvesting_start_date = cr.getInvesting_start_date().toLocalDate();
      Optional<MarketIndexRate> opt_mir_uai_inv = lst_mir_by_uaindex
          .stream()
          .filter(n -> n.getQuotation_date().toLocalDate().isEqual(finalInvesting_start_date))
          .filter(m -> m.getStock_exchange_id()==calc_pol_one.getStock_exchange_id())
          .findFirst();
      if (opt_mir_uai_inv.isPresent()){
        MarketIndexRate mir = opt_mir_uai_inv.get();
        cr.setRate_ba_invest(mir.getClose_price());
        log.info("cr.setRate_ba_invest {}", mir.getClose_price());
      } else {
        // генерим ошибку не найден  Котировка БА на дату начала инвестирования (динамика ба зависит)
        // потом не сможем посчитать динамику ба, (деление на 0)
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteUA, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)) {
          continue;
        }
      }

      if(Period.between(finalInvesting_start_date, LocalDate.now()).getDays() > 10 ){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteUAActual, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // endregion Котировка БА на дату начала инвестирования

      // region Стоимость актива в день покупки

      Optional<Deal> opt_deals_by_isin = lst_deals_by_isin
          .stream()
          .filter(m -> m.getQuotation_date().toLocalDate().isEqual(d_min_deals))
          .findFirst();
      if (opt_deals_by_isin.isPresent()) {
        Deal deal = opt_deals_by_isin.get();
        cr.setAsset_buy_price(deal.getPrice());
        log.info("cr.setAsset_buy_price {}", deal.getPrice());
      } else {
        // генерим ошибку
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoFundISINFound, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)) {
          continue;
        }
      }

      if(Period.between(d_min_deals, LocalDate.now()).getDays() > 10 ){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteFundActual, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // endregion Стоимость актива в день покупки

      // region Стоимость актива

      BigDecimal asset_nominal_value = BigDecimal.ZERO;

      List<Assets> lst_assets_by_isin = lst_asset
          .stream()
          .filter(m -> m.getIsin().equals(calc_pol_one.getIsin()))
          .collect(Collectors.toList());
      if (lst_assets_by_isin.size() == 1) {
        Optional<Assets> opt_asset_by_isin = lst_assets_by_isin.stream().findFirst();
        if (opt_asset_by_isin.isPresent()) {
          Assets asset = opt_asset_by_isin.get();
          asset_nominal_value = asset.getNominal_value();
          BigDecimal val = cr.getAsset_buy_price().divide(asset.getNominal_value(), big_decimal_scale, RoundingMode.HALF_UP);
          cr.setAsset_price(val);
          log.info("cr.setAsset_price {}", val);
        }
      } else {
        // потом не получится посчитать коэффициент участия (деление на 0)
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoFundISINFound, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // endregion Стоимость актива

      if(Period.between(d_min_deals, LocalDate.now()).getDays() > 10 ) {
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoQuoteFundActual, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      if(d_min_deals.isAfter(cr.getInvesting_start_date().toLocalDate().plusDays(2))
          || d_min_deals.isBefore(cr.getInvesting_start_date().toLocalDate().minusDays(2))){
        ep = err_prot_sub.GetErrorProtItem(ErrorTypes.DCalcAssetPriceDInvDiffMuch, pd_cont);
        lst_err_prot.add(ep);
        if (ErrorProtocolSub.isStopFactor(ep)){
          continue;
        }
      }

      // region Динамика БА

      BigDecimal dynamics_ba = cr
          .getRate_ba_calc()
          .divide(cr.getRate_ba_invest(), big_decimal_scale, RoundingMode.HALF_UP)
          .subtract(new BigDecimal("1"));
      cr.setDynamics_ba(dynamics_ba);
      log.info("cr.setDynamics_ba {}", dynamics_ba);

      // endregion Динамика БА

      // region КУ - коэффициент участия (Participation_rate)

      BigDecimal val = cr.getRf_share().divide(cr.getAsset_price(), big_decimal_scale, RoundingMode.HALF_UP);
      cr.setParticipation_rate(val);
      log.info("cr.setParticipation_rate {}", val);

      // endregion КУ - коэффициент участия (Participation_rate)

      // region Курс ЦБ (для расчёта)

      BigDecimal fund_currency_rate_by_calc_date  = BigDecimal.ZERO;
      BigDecimal fund_currency_rate_by_inv_date   = BigDecimal.ZERO;
      BigDecimal pol_currency_rate_by_calc_date   = BigDecimal.ZERO;
      BigDecimal pol_currency_rate_by_inv_date    = BigDecimal.ZERO;
      String calculated_at = format_short.format(cr.getCalculated_at());
      String investing_start_date = format_short.format(cr.getInvesting_start_date());

      BigDecimal cbr = BigDecimal.ZERO;
      String cpol = cr.getPolicy_currency_code().trim().toUpperCase();
      String cfund = cr.getFund_currency_code().trim().toUpperCase();

      if (cpol.equals("RUB")&&cfund.equals("RUB")){
        cbr = new BigDecimal("1");

        log.info("cpol.equals(\"RUB\")&&cfund.equals(\"RUB\") {}", cbr);

      } else if (cpol.equals("RUB")&&!cfund.equals("RUB")) {
        // region Курс ЦБ валюты фонда на дату расчёта(calculated_at) (для расчёта).
        List<CurrencyRate> lst_fund_c_rate_by_calc_date = calc_serv_rep.GetCurrencyRateListCustom(cfund, calculated_at);
        if(lst_fund_c_rate_by_calc_date.size() != 1) {
          // нет валюты фонда или дубликаты, ошибка для полиса на дату расчёта
          pd_cont.AddParamData("TARGET_DATE", calculated_at);
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoCurrencyDictOnDate, pd_cont);
          lst_err_prot.add(ep);
          if (ErrorProtocolSub.isStopFactor(ep)){
            continue;
          }
        } else {
          fund_currency_rate_by_calc_date = lst_fund_c_rate_by_calc_date.get(0).getRate();

          log.info("fund_currency_rate_by_calc_date {}:{}", fund_currency_rate_by_calc_date, calculated_at);

          // проверяем значение валюты фонда на 0, ошибка для полиса на дату расчёта
          if(Utils.isZeroBigDecimal(fund_currency_rate_by_calc_date)) {
            pd_cont.AddParamData("TARGET_DATE", calculated_at);
            ep = err_prot_sub.GetErrorProtItem(ErrorTypes.ZeroCurrencyDictOnDate, pd_cont);
            lst_err_prot.add(ep);
            if (ErrorProtocolSub.isStopFactor(ep)){
              continue;
            }
          }
        }
        // endregion

        // region Курс ЦБ валюты фонда на дату начала инвестирования (investing_start_date) (для расчёта).
        List<CurrencyRate> lst_fund_c_rate_by_inv_date = calc_serv_rep.GetCurrencyRateListCustom(cfund, investing_start_date);
        if(lst_fund_c_rate_by_inv_date.size() != 1) {
          // нет валюты фонда или дубликаты, ошибка для полиса на дату начала инвестирования
          pd_cont.AddParamData("TARGET_DATE", investing_start_date);
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoCurrencyDictOnDate, pd_cont);
          lst_err_prot.add(ep);
          if (ErrorProtocolSub.isStopFactor(ep)){
            continue;
          }
        } else {
          fund_currency_rate_by_inv_date = lst_fund_c_rate_by_inv_date.get(0).getRate();

          log.info("fund_currency_rate_by_inv_date {}:{}", fund_currency_rate_by_inv_date, investing_start_date);

          // проверяем значение валюты фонда на 0, ошибка для полиса на дату начала инвестирования
          if(Utils.isZeroBigDecimal(fund_currency_rate_by_inv_date)) {
            pd_cont.AddParamData("TARGET_DATE", investing_start_date);
            ep = err_prot_sub.GetErrorProtItem(ErrorTypes.ZeroCurrencyDictOnDate, pd_cont);
            lst_err_prot.add(ep);
            if (ErrorProtocolSub.isStopFactor(ep)){
              continue;
            }
          }
        }
        // endregion

        cbr = fund_currency_rate_by_calc_date.divide(fund_currency_rate_by_inv_date, big_decimal_scale, RoundingMode.HALF_UP);

        log.info("cpol.equals(\"RUB\")&&!cfund.equals(\"RUB\") {}", cbr);

      } else if (!cpol.equals("RUB")&&cfund.equals("RUB")) {

        // region Курс ЦБ валюты полиса на дату расчёта(calculated_at) (для расчёта).
        List<CurrencyRate> lst_pol_c_rate_by_calc_date = calc_serv_rep.GetCurrencyRateListCustom(cpol, calculated_at);
        if(lst_pol_c_rate_by_calc_date.size() != 1) {
          // нет валюты полиса или дубликаты, ошибка для полиса на дату расчёта
          pd_cont.AddParamData("TARGET_DATE", calculated_at);
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoCurrencyDictOnDate, pd_cont);
          lst_err_prot.add(ep);
          if (ErrorProtocolSub.isStopFactor(ep)){
            continue;
          }
        } else {
          pol_currency_rate_by_calc_date = lst_pol_c_rate_by_calc_date.get(0).getRate();

          log.info("pol_currency_rate_by_calc_date {}:{}", pol_currency_rate_by_calc_date, calculated_at);

          // проверяем значение валюты полиса на 0, ошибка для полиса на дату расчёта
          if(Utils.isZeroBigDecimal(pol_currency_rate_by_calc_date)) {
            pd_cont.AddParamData("TARGET_DATE", calculated_at);
            ep = err_prot_sub.GetErrorProtItem(ErrorTypes.ZeroCurrencyDictOnDate, pd_cont);
            lst_err_prot.add(ep);
            if (ErrorProtocolSub.isStopFactor(ep)){
              continue;
            }
          }
        }
        // endregion

        // region Курс ЦБ валюты полиса на дату начала инвестирования (investing_start_date) (для расчёта).
        List<CurrencyRate> lst_pol_c_rate_by_inv_date = calc_serv_rep.GetCurrencyRateListCustom(cpol, investing_start_date);
        if(lst_pol_c_rate_by_inv_date.size() != 1) {
          // нет валюты полиса или дубликаты, ошибка для полиса на дату начала инвестирования
          pd_cont.AddParamData("TARGET_DATE", investing_start_date);
          ep = err_prot_sub.GetErrorProtItem(ErrorTypes.NoCurrencyDictOnDate, pd_cont);
          lst_err_prot.add(ep);
          if (ErrorProtocolSub.isStopFactor(ep)){
            continue;
          }
        } else {
          pol_currency_rate_by_inv_date = lst_pol_c_rate_by_inv_date.get(0).getRate();

          log.info("pol_currency_rate_by_inv_date {}:{}", pol_currency_rate_by_inv_date, investing_start_date);

          // проверяем значение валюты полиса на 0, ошибка для полиса на дату начала инвестирования
          if(Utils.isZeroBigDecimal(pol_currency_rate_by_inv_date)) {
            pd_cont.AddParamData("TARGET_DATE", investing_start_date);
            ep = err_prot_sub.GetErrorProtItem(ErrorTypes.ZeroCurrencyDictOnDate, pd_cont);
            lst_err_prot.add(ep);
            if (ErrorProtocolSub.isStopFactor(ep)){
              continue;
            }
          }
        }
        // endregion

        cbr = pol_currency_rate_by_inv_date.divide(pol_currency_rate_by_calc_date, big_decimal_scale, RoundingMode.HALF_UP);

        log.info("!cpol.equals(\"RUB\")&&cfund.equals(\"RUB\") {}", cbr);

      }

      // endregion Курс ЦБ (для расчёта)

      // region Плата за страхование(для расчёта)

      BigDecimal ins_for_payment = cr.getAsset_nominal_value().multiply(cr.getZ_factor());
      log.info("ins_for_payment {}", ins_for_payment);

      // endregion Плата за страхование(для расчёта)

      // region Котировка фонда на дату расчета(для расчёта)

      BigDecimal seq_quote = BigDecimal.ZERO;
      // читаем котировки фонда по isin
      LocalDate finalCalc_at = cr.getCalculated_at().toLocalDate();
      Optional<SecurityQuote> opt_sec_quote_by_isin = lst_sec_quotes.stream()
          .filter(m -> m.getIsin().equals(calc_pol_one.getIsin()))
          .filter(n -> n.getQuotation_date().toLocalDate().isEqual(finalCalc_at))
          .filter(l -> l.getStock_exchange_id() == calc_pol_one.getStock_exchange_id()).findFirst();
      if(opt_sec_quote_by_isin.isPresent()){
        seq_quote = opt_sec_quote_by_isin.get().getRecognized_quotation();
        log.info("seq_quote {}", seq_quote);
      }

      // endregion Котировка фонда на дату расчета(для расчёта)

      // region РФ, количество номинала (rf_shares_quantity)

      BigDecimal crate_rf_shares_quantity = BigDecimal.ZERO;
      if (cpol.equals("RUB")&&cfund.equals("RUB")){
        crate_rf_shares_quantity = new BigDecimal("1");
      } else if (cpol.equals("RUB")&&!cfund.equals("RUB")) {
        crate_rf_shares_quantity = fund_currency_rate_by_inv_date;
      } else if (!cpol.equals("RUB")&&cfund.equals("RUB")) {
        if(!Utils.isZeroBigDecimal(fund_currency_rate_by_inv_date)){
          crate_rf_shares_quantity =  new BigDecimal("1").divide(fund_currency_rate_by_inv_date, big_decimal_scale, RoundingMode.HALF_UP);
        }
      }

      BigDecimal rf_shares_quantity = cr.getAsset_nominal_value()
          .multiply(cr.getRf_share())
          .divide(cr.getAsset_price(), big_decimal_scale, RoundingMode.HALF_UP)
          .divide(crate_rf_shares_quantity, big_decimal_scale, RoundingMode.HALF_UP);
      cr.setRf_shares_quantity(rf_shares_quantity);
      log.info("cr.setRf_shares_quantity {}", rf_shares_quantity);

      // endregion РФ, количество номинала (rf_shares_quantity)

      // region РФ, количество (notes) (rf_notes_quantity)

      BigDecimal rf_notes_quantity = cr.getRf_shares_quantity().divide(asset_nominal_value, big_decimal_scale, RoundingMode.HALF_UP);
      cr.setRf_notes_quantity(rf_notes_quantity);
      log.info("cr.setRf_notes_quantity {}", rf_notes_quantity);

      // endregion РФ, количество (notes) (rf_notes_quantity)

      // region РФ на дату (в валюте фонда) (rf_in_fund_currency)

      BigDecimal rf_in_fund_currency = cr.getRf_shares_quantity().multiply(seq_quote);
      cr.setRf_in_fund_currency(rf_in_fund_currency);
      log.info("cr.setRf_in_fund_currency {}", rf_in_fund_currency);

      // endregion РФ на дату (в валюте фонда) (rf_in_fund_currency)

      // region ДИД (setDid)

      BigDecimal did = (cr.getAsset_nominal_value()
          .multiply(cr.getDynamics_ba())
          .multiply(cr.getParticipation_rate())
          .multiply(cbr))
          .subtract(ins_for_payment);

      if (BigDecimal.ZERO.compareTo(did) >= 0){
        cr.setDid(BigDecimal.ZERO);
      } else {
        cr.setDid(did);
        log.info("cr.setDid {}", did);
      }

      // endregion ДИД (setDid)

      // region ИДД (setIdd)

      cr.setIdd(cr.getDid());
      log.info("cr.setIdd {}", cr.getDid());

      // endregion ИДД (setIdd)

      lst_calc_result.add(cr);
    }

    if(lst_calc_result.size()>0){
      result = true;
    }

    return result;
  }//(ProcessCalcPolicyData)

  // endregion
}//(MainEngine)