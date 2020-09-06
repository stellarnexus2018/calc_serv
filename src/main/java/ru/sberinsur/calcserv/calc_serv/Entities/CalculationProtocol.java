package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CalculationProtocol {
  private int initiator_sign;
  private int is_completed;
  private int is_deleted = 0;
  private long calculation_id;
  private long ois_policy_count;
  private long calc_policy_count;
  private long calc_error_count;
  private String initiator_person;
  private String uid = UUID.randomUUID().toString();
  private String id = UUID.randomUUID().toString();
  private String upload_session_id;
  private Timestamp schedule_calc;
  private Timestamp created_at = new Timestamp(new java.util.Date().getTime());
  private Timestamp begin_calc = new Timestamp(new java.util.Date().getTime());
  private Timestamp end_calc;
  public void UpErrorCnt(){calc_error_count++;};
  public void UpCompleteCnt(){calc_policy_count++;};
}