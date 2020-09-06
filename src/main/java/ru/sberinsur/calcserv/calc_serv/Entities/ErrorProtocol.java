package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ErrorProtocol {
  private int error_stop_factor;
  private int is_deleted = 0;
  private int error_type;
  private long policy_id;
  private long series_id;
  private String calc_protocol_id;
  private String calc_initiator="";
  private String uid = UUID.randomUUID().toString();
  private String policy_series_name="";
  private String policy_number="";
  private String error_message;
  private Timestamp created_at = new Timestamp(new java.util.Date().getTime());
}