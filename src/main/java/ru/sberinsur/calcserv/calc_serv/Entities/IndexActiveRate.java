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
public class IndexActiveRate {
  private long trading_system_id;
  private Date rate_date;
  private String uid;
  private String index_active_id;
  private Timestamp created_at;
  private BigDecimal rate;
}