package ru.sberinsur.calcserv.calc_serv.CalcEngineSubs;

import java.math.BigDecimal;

public class Utils {
  public static String GetQuotedString(String _s){
    return String.format("'%s'", _s.trim());
  }
  public static String GetLongAsString(long _l){
    return String.format("%d",  _l);
  }
  public static boolean isZeroBigDecimal(BigDecimal _comparing_val){
    return (BigDecimal.ZERO.compareTo(_comparing_val) == 0);
  }
}