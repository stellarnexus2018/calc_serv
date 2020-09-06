package ru.sberinsur.calcserv.calc_serv.Controllers;

//region import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.sberinsur.calcserv.calc_serv.CalcEngineSubs.MainEngine;
import ru.sberinsur.calcserv.calc_serv.Entities.CalculationProtocol;
import ru.sberinsur.calcserv.calc_serv.Repositories.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

//endregion

@RestController
public class CalculateController {
  //region Поля

  @Autowired
  private CalcServiceRep calc_serv_rep;

  String service_type_code = "DID_CLASSIC_1";
  String upload_session_id = "b4988f2c-64a6-49ac-ad30-9a2b5e63635f";
  //String upload_session_id = "f25906d7-de5f-46c4-80d0-5c7445468855";
  String initiator_person = "Igoryasha";
  int big_decimal_scale = 4;

  //endregion Поля

  @PostMapping(value = "/calculate")
  public String GetCalculationResult(/*@RequestParam(value = "inputParam") String inputParam*/) {
    // Формирование айтема протокола
    CalculationProtocol calc_prot_item = new CalculationProtocol();
    calc_prot_item.setCalculation_id(1);
    calc_prot_item.setInitiator_sign(1);
    calc_prot_item.setInitiator_person(initiator_person);
    calc_prot_item.setSchedule_calc(Timestamp.valueOf(LocalDateTime.now()));
    calc_prot_item.setUpload_session_id(upload_session_id);

    MainEngine main_engine = new MainEngine(calc_prot_item, calc_serv_rep, service_type_code, big_decimal_scale);
    main_engine.Init();
    main_engine.EnrollProcessCalculation();

    return "OK";
  }
}