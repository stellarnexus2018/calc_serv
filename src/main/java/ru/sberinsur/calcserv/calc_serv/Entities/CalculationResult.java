package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
//import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CalculationResult {
  //private String uid;                     // Идентификатор записи в таблице
  //private Timestamp created_at;           // Дата-время добавления записи в таблицу
  private String uid = UUID.randomUUID().toString();                              // Идентификатор записи в таблице
  private Timestamp created_at = new Timestamp(new java.util.Date().getTime());   // Дата-время добавления записи в таблицу
  private int is_deleted=0;               // Признак удаления записи
  private String calc_protocol_id;        // Идентификатор расчёта
  private long policy_id;                 // Идентификатор полиса
  private String policy_number;           // Номер полиса
  //private long policy_series_id;          // Серия полиса
  private String policy_series_name;          // Серия полиса









  private Date insurance_start_date;      // Дата начала страхования
  private Date insurance_end_date;        // Дата окончания страхования
  private Date investing_start_date;      // Дата начала инвестирования
  //private long market_name_id;            // Маркетинговое наименование

  private String market_name;            // Маркетинговое наименование

  private String investment_conditions = "";   // Условия инвестирования
  private BigDecimal insurance_premium;   // Страховая премия
  private String fund_isin;               // ISIN фонда
  private String policy_currency_code;    // Валюта полиса
  private String fund_currency_code;      // Валюта фонда
  private BigDecimal idd;                 // ИДД
  private String index_ba;                // Индекс БА
  private BigDecimal did;                 // ДИД
  private BigDecimal z_factor;            // Коэффициент Z
  private BigDecimal dynamics_ba;         // Динамика БА
  private BigDecimal rate_ba_calc;        // Котировка БА на дату расчёта
  private BigDecimal rate_ba_invest;      // Котировка БА на дату инвестирования
  private BigDecimal asset_price;         // Стоимость актива
  private BigDecimal asset_buy_price;     // Стоимость актива в день покупки
  private BigDecimal asset_nominal_value; // Номинал актива
  private BigDecimal rf_share;            // Доля РФ
  private BigDecimal participation_rate;  // КУ - коэффициент участия
  private BigDecimal rf_shares_quantity;  // РФ кол-во номинала
  private BigDecimal rf_in_fund_currency; // РФ на дату в валюте фонда
  private BigDecimal rf_notes_quantity;   // РФ, количество (notes)
  private BigDecimal did_coupon=BigDecimal.ZERO;          // ДИД купон
  private BigDecimal guaranteed_coupon=BigDecimal.ZERO;   // Гарантированный купон
  private Date calculated_at;             // Дата расчёта
  //private String calc_initiator;          // Инициатор расчёта
}