package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CalculationPolicy {
  private String uid = UUID.randomUUID().toString();                              // Идентификатор записи в таблице
  private Timestamp created_at = new Timestamp(new java.util.Date().getTime());   // Дата-время добавления записи в таблицу
  private int is_deleted = 0;                                                     // Признак удаления объекта
  private long policy_id;                                                         // Идентификатор полиса
  private long series_id;                                                         // Наименование серии полисов (policy_series.id)
  private String policy_number;                                                   // Номер полиса
  private Date insurance_start_date;                                              // Дата начала страхования
  private Date insurance_end_date;                                                // Дата окончания страхования
  private BigDecimal insurance_premium;                                           // Страховая премия в валюте договора
  private String contract_status_name;                                            // Последний статус договора
  private String currency_code;                                                   // Валюта (буквенный код валюты alfa-3: RUB, USD, CHF...)
  private String fund_currency_code;                                              // Валюта фонда (буквенный код валюты alfa-3: RUB, USD, CHF...)
  private long market_name_id;                                                    // ID Маркетингового наименования
  private Date tranche_date_planned;                                              // Дата транша план
  private Date tranche_date_in_fact;                                              // Дата транша факт
  private int urgency;                                                            // Срочнось
  private long basket_id=0;                                                       // Идентификатор корзины
  private String underl_asset_index;                                              // Индекс БА
  private long underl_asset_id;                                                   // Код БА
  private long stock_exchange_id;                                                 // Биржа фонда
  private long base_active_exchange_id;                                           // Биржа БА
  private BigDecimal z_factor;                                                    // Коэффициент Z
  private BigDecimal guarantee_fund;                                              // Гарантийный фонд
  private BigDecimal investment_fund;                                             // Инвестиционный фонд
  private int is_verified;                                                        // Проверка пройдена
  private String calculation_protocol_id;                                         // ID протокола расчета
  private String isin;                                                            // ISIN
}