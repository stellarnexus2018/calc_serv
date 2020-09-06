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
public class CurrencyRate {
  private int is_deleted;
  private Date rate_date;
  private String currency_code;
  private String uid;
  private Timestamp created_at;
  private BigDecimal rate;
}