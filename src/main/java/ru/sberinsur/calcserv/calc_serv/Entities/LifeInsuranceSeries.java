package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LifeInsuranceSeries {
  private String uid;
  private Timestamp created_at;
  private int is_deleted;
  private long id;
  private long series_id;
  private long series_description_id;
  private String channel;
  private int years;
  private int sns_risk;
  private String guarantee;
  private BigDecimal guarantee_fund;
  private BigDecimal investment_fund;
  private BigDecimal ic_commission_contract_conclusion_option;
  private BigDecimal bank_commission_contract_conclusion_option;
  private BigDecimal ic_commission_additional_payment_option;
  private BigDecimal bank_commission_additional_payment_option;
  private BigDecimal guaranteed_profit;
  private BigDecimal planning_profit;
  private BigDecimal guarantee_rate;
  private BigDecimal ic_commission_change_fund_option;
  private BigDecimal ic_commission_attachment_option;
  private String payment_frequency;
  private String coupon_scheme;
  private String product_group;
  private String description;
  private BigDecimal guaranteed_coupon_rate;
  private String death_risk_payment_error_type;
  private BigDecimal accident_rate;
  private BigDecimal z_factor;
  private String currency_code;
  private Date series_begin_date;
  private Date series_end_date;
}