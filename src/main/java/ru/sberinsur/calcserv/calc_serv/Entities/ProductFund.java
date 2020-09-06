package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ProductFund {
  //private int duration;
  private BigDecimal duration;
  private int is_deleted=0;
  private long code;
  private long stock_exchange_id;
  private long issuer_id;
  private long underlying_asset_id;
  private Date tranche_date;
  private String fund_currency;
  private String fund_description;
  private String description;
  private String uid;
  private String isin;
  private Timestamp created_at;
}


























