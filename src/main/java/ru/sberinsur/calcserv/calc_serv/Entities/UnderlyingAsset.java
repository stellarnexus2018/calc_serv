package ru.sberinsur.calcserv.calc_serv.Entities;

import lombok.*;
import java.sql.Timestamp;
import java.sql.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UnderlyingAsset {
  private int is_deleted;
  private long id;
  private long market_name_id;
  private long stock_exchange_id;
  private Date begin_date;
  private Date end_date;
  private String market_index_id;
  private String uid;
  private String currency_code;
  private String undrl_asset_platform;
  private String description;
  private Timestamp created_at;
}