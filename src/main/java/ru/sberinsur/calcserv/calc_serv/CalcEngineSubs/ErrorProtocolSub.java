package ru.sberinsur.calcserv.calc_serv.CalcEngineSubs;

//region import

import ru.sberinsur.calcserv.calc_serv.Entities.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

//endregion import

/**
 * Полсистема формирования сообщений об ошибках расчёта
 */
public class ErrorProtocolSub {
  //region Поля

  CalculationProtocol calc_prot;
  private static List<ErrorType> lst_error_types;
  private HashMap<Integer, ErrorType> hmp_error_types;

  //endregion

  //region Инициализация

  public ErrorProtocolSub(CalculationProtocol _calc_prot){
    calc_prot = _calc_prot;
  }

  public void Init(List<ErrorType> _lst_error_types){
    lst_error_types = _lst_error_types;
    hmp_error_types = new HashMap<Integer, ErrorType>(lst_error_types.size());
    for ( ErrorType et_one: lst_error_types) {
      hmp_error_types.put(et_one.getError_type_val(), et_one);
    }
  }

  //endregion Инициализация

  //region GetAvailableErrorTypeCodes

  public static List<Integer> GetAvailableErrorTypeCodes(){
    List<Integer> l_error_type_codes = lst_error_types
        .stream()
        .map(m -> m.getError_type_val())
        .distinct()
        .collect(Collectors.toList());
    return l_error_type_codes;
  }

  //endregion

  //region GetErrorProtItem

  public ErrorProtocol GetErrorProtItem(ErrorTypes _error_type){
    return GetErrorProtItemInternal(_error_type, null, "");
  }

  public ErrorProtocol GetErrorProtItem(ErrorTypes _error_type, DataParamCont _odms){
    return GetErrorProtItemInternal(_error_type, _odms, "");
  }

  public ErrorProtocol GetErrorProtItem(ErrorTypes _error_type, String _param_str){
    return GetErrorProtItemInternal(_error_type, null, _param_str);
  }

  private ErrorProtocol GetErrorProtItemInternal(ErrorTypes _error_type, DataParamCont _odms, String _param_str){
    ErrorProtocol result = new ErrorProtocol();
    ErrorType et = hmp_error_types.get(_error_type.getErrCode());

/*
    switch (_error_type.getErrCode()){
      case 0:
      case 1:
        result.setError_message(et.getError_type_message());
        break;
      case 2:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      case 28:
      case 36:
        result.setError_message(_odms.ProcessStrData(et.getError_type_message()));
        break;
      default:
        break;
    }
*/

    if(_odms != null){
      result.setPolicy_id(_odms.getPolicy_id());
      result.setPolicy_series_name(_odms.getPolicy_series_name());
      result.setPolicy_number(_odms.getPolicy_number());
      result.setError_message(_odms.ProcessStrData(et.getError_type_message()));
    } else {
      result.setError_message(et.getError_type_message().replace("{#}", _param_str));
    }

    result.setCalc_protocol_id(calc_prot.getId());
    result.setError_type(et.getError_type_val());
    result.setError_stop_factor(et.getError_stop_factor());

    calc_prot.UpErrorCnt();

    return result;
  }

  //endregion

  // region isStopFactor

  public static boolean isStopFactor(ErrorProtocol _ep){
    return (_ep.getError_stop_factor() == 1);
  }

  // endregion isStopFactor
}//(ErrorProtocolSub)