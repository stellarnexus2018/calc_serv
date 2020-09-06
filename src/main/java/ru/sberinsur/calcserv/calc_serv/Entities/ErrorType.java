package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ErrorType {
  private int       error_type_val;
  private int       error_stop_factor;
  private int       is_enabled;
  private int       error_type;
  private String    uid = UUID.randomUUID().toString();
  private String    error_type_desc;
  private String    error_type_message;
  private Timestamp created_at = new Timestamp(new java.util.Date().getTime());
}