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
public class SecurityQuote {
  private String uid;
  private Timestamp created_at;
  private int is_deleted;
  private String isin;
  private Date quotation_date;
  private BigDecimal recognized_quotation;
  private long stock_exchange_id;
}