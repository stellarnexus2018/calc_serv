package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Currency {
  private String uid;
  private Timestamp created_at;
  private String code;
  private String name;
}
