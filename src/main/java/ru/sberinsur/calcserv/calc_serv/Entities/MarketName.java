package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MarketName {
  private String uid;
  private Timestamp created_at;
  private int is_deleted;
  private long id;
  private String name;
}