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
public class MarketIndexRate {
  private int is_deleted;
  private long stock_exchange_id;
  private Date quotation_date;
  private String market_index_id;
  private String uid;
  private Timestamp created_at;
  private BigDecimal close_price;
}