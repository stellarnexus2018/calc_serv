package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class BorderCondition {
  private String uid;
  private Timestamp created_at;
  //private String service_type_uid;
  //private long id;
  private int error_type;
  private BigDecimal limit_one;
  private BigDecimal limit_two;
  private long market_name_id;
}