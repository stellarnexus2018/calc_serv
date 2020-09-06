package ru.sberinsur.calcserv.calc_serv.CalcEngineSubs;

/**
 * Типы ошибок расчёта
 */
public enum ErrorTypes {
  NoRawRecordsUpload(0),                  // Неполная выборка договоров из ОИС. Расчет по данному полису прекращается
  NoRecordsUpload(1),                     // Неполная выборка договоров из ОИС. Расчет по данному полису прекращается
  NoPolicySerieFound(2),                  // Полис {POLICY_NUMBER} не может быть рассчитан в связи отсутствием серии в справочнике. Расчет по данному полису прекращается
  NoInvestmentFundVal(4),                 // РФ для полиса {POLICY_NUMBER} равен нулю. Расчет по данному полису прекращается
  NoUnderLyingAsset(5),                   // Индекс БА не найден по полису {POLICY_NUMBER}. Расчет по данному полису прекращается
  NoCurrencyDictOnDate(6),                // Расчёт полиса {POLICY_NUMBER} не выполнен. Справочник курсов валют на дату {TARGET_DATE} не загружен. Расчет по данному полису прекращается
  ZeroCurrencyDictOnDate(7),              // Расчёт полиса {POLICY_NUMBER} не выполнен. Курс на {TARGET_DATE} равен 0. Расчет по данному полису прекращается
  NoQuoteUA(8),                           // Котировка БА {UNDERLYING_ASSET_INDEX} для полиса {POLICY_NUMBER} не определена. Расчет по данному полису прекращается
  NoFundISINFound(9),                     // Котировка фонда {ISIN} для полиса {POLICY_NUMBER} не определена. Расчет по данному полису прекращается
  NoQuoteUAActual(10),                    // Котировка БА {UNDERLYING_ASSET_INDEX} для полиса {POLICY_NUMBER} не актуальна. Расчет по данному полису продолжается
  NoQuoteFundActual(11),                  // Котировка фонда {ISIN} для полиса {POLICY_NUMBER} не актуальна. Расчет по данному полису продолжается
  DCalcAssetPriceDInvDiffMuch(12),        // Дата расчета стоимости актива полиса {POLICY_NUMBER} значительно отличается от даты инвестирования. Расчет по данному полису продолжается

  DIDCalcPrevDiff(13),                    // Для полиса {POLICY_NUMBER} возможна ошибка расчета ДИД. Найдены отличия от предыдущих  ДИД. Расчет по данному полису продолжается
  DIDCalcPrevMuchDiff(14),                // Для полиса {POLICY_NUMBER} произошла ошибка расчета ДИД. Найдены значительные отличия от предыдущих ДИД. Расчет по данному полису продолжается
  DIDCalcNoPrevUAFound(15),               // Для полиса {POLICY_NUMBER} произошла ошибка расчета ДИД. Не найдены предыдущие БА. Расчет по данному полису продолжается
  DIDCalcPrevUADiffFound(16),             // Для полиса {POLICY_NUMBER} возможна ошибка расчета ДИД. Найдены отличия от предыдущих котировок БА. Расчет по данному полису продолжается
  DIDCalcPrevUAMuchDiffFound(17),         // Для полиса {POLICY_NUMBER} произошла ошибка расчета ДИД. Найдены значительные отличия от предыдущих котировок БА. Расчет по данному полису продолжается
  DIDCalcNoPrevFundQuoteFound(18),        // Для полиса {POLICY_NUMBER} произошла ошибка расчета РФ. Не найдены предыдущие котировки фонда. Расчет по данному полису продолжается
  DIDCalcPrevFundQuoteDiffFound(19),      // Для полиса {POLICY_NUMBER} возможна ошибка расчета РФ. Найдены отличия от предыдущих котировок фонда. Расчет по данному полису продолжается
  DIDCalcPrevFundQuoteMuchDiffFound(20),  // Для полиса {POLICY_NUMBER} произошла ошибка расчета РФ. Найдены значительные отличия от предыдущих котировок фонда. Расчет по данному полису продолжается

  DublicatePolicySerieFound(21),          // Полис {POLICY_NUMBER} не может быть рассчитан в связи с дубликатами серии в справочнике. Расчет по данному полису прекращается
  NoDealByISIN(22),                       // Для полиса {POLICY_NUMBER}, не найдена сделка по целевому ISIN: {ISIN}. Расчет по данному полису прекращается
  NoMarketIndexRateByUAIndx(23),          // Для полиса {POLICY_NUMBER}, не найдена биржевая котировка по индексу БА: {UNDERLYING_ASSET_INDEX}
  DublicateUnderLyingAsset(24),           // Дубликат Индекс БА найден по полису {POLICY_NUMBER}. Расчет по данному полису прекращается
  DublicateProductFund(25),               // Найден дубликат фондов продуктов для полиса {POLICY_NUMBER}. Расчет по данному полису прекращается
  NoCalcDateFound(26),                    // Не найдена дата расчёта для полиса: {POLICY_NUMBER}, Индекс БА: {UNDERLYING_ASSET_INDEX}, ISIN: {ISIN}. Расчет по данному полису прекращается
  NoAssetByISINFound(27),                 // Не найден Актив, или найдены дубликаты Активов, для полиса: {POLICY_NUMBER}, по ISIN: {ISIN}. Расчет по данному полису прекращается
  NoProductFund(28),                      // Для полиса {POLICY_NUMBER} не найден фонд продуктов. Расчет по данному полису прекращается
  NoDictPolSerieFound(29),                // Не найдено справочное значение ID серии для имени серии полиса {POLICY_SERIE_NAME}. Расчет невозможен. Расчет по данному полису прекращается
  LessByYearCalcDateFound(36);            // Для полиса {POLICY_NUMBER} дата расчета меньше текущей даты на год. Расчет невозможен. Расчет по данному полису прекращается

  private int error_code;
  ErrorTypes(int _error_code) {
    this.error_code = _error_code;
  }
  public int getErrCode(){ return error_code;}
}