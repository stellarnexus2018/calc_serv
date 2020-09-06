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
public class OISRawPolicyItem {
  private int nduration;
  private long nsync;
  private Date dstart_date;
  private Date dterm_date;
  private Date dinvest_invest_date;
  private Date ddate_;
  private String sproduct;
  private String sproduct_series;
  private String spolicy_number;
  private String scurrency;
  private String sinvest_cov_baseact;
  private String ois_upload_session;
  private Timestamp ois_upload_date;
  private BigDecimal nsum_prem_det_main_rub;
  private BigDecimal nsum_prem_det_main_usd;
}