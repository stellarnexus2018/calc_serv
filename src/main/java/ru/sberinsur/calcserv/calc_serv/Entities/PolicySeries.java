package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PolicySeries {
  private int is_deleted;
  private long id;
  private String uid;
  private String name;
  private Timestamp created_at;
}