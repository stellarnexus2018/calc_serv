package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Assets {
  private String uid;
  private Timestamp created_at;
  private int is_deleted;
  private String isin;
  private BigDecimal nominal_value;
  private String currency_code;
  private long issuer_id;
}