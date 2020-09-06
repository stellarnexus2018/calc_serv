package ru.sberinsur.calcserv.calc_serv.Entities;

//import lombok.*;
import java.util.HashMap;
import java.util.Map;

public class DataParamCont {
  private long   policy_id;
  private long   series_id;
  private String policy_series_name;
  private String policy_number;
  private Map<String, String> map_vals;

  public long getPolicy_id() {
    return policy_id;
  }

  public void setPolicy_id(long policy_id) {
    this.policy_id = policy_id;
  }

  public long getSeries_id() {
    return series_id;
  }

  public void setSeries_id(long series_id) {
    this.series_id = series_id;
  }

  public String getPolicy_series_name() {
    return policy_series_name;
  }

  public void setPolicy_series_name(String policy_series_name) {
    this.policy_series_name = policy_series_name;
  }

  public String getPolicy_number() {
    return policy_number;
  }

  public void setPolicy_number(String policy_number) {
    this.policy_number = policy_number;
  }

  public DataParamCont(long policy_id, long series_id, String policy_series_name, String policy_number) {
    this.policy_id = policy_id;
    this.series_id = series_id;
    this.policy_series_name = policy_series_name;
    this.policy_number = policy_number;
    map_vals = new HashMap<String, String>();
  }

  public void ResetContent(){
    map_vals.clear();
  }

  public void AddParamData(String _subst_name, String _subst_val){
    if(map_vals.containsKey(_subst_name)){
      map_vals.replace(_subst_name, _subst_val);
    } else {
      map_vals.put(_subst_name, _subst_val);
    }
  }

  public String GetParamData(String _subst_name){
    return map_vals.get(_subst_name);
  }

  public String ProcessStrData(String _templ_str){
    String result = _templ_str;
    for(Map.Entry<String, String> item_one : map_vals.entrySet()){
      result = result.replace( String.format("{%s}", item_one.getKey()), item_one.getValue());
    }
    return result;
  }
}