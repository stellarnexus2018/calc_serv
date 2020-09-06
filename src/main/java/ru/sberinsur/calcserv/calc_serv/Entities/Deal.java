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
public class Deal {
  private int is_deleted;
  private Date quotation_date;
  private String uid;
  private String isin;
  private String deal_type;
  private Timestamp created_at;
  private BigDecimal price;
}