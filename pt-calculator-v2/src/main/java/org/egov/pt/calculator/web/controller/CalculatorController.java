package org.egov.pt.calculator.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.validation.Valid;

import org.egov.pt.calculator.service.DemandService;
import org.egov.pt.calculator.service.EstimationService;
import org.egov.pt.calculator.service.PayService;
import org.egov.pt.calculator.util.CalculatorConstants;
import org.egov.pt.calculator.web.models.Calculation;
import org.egov.pt.calculator.web.models.CalculationReq;
import org.egov.pt.calculator.web.models.CalculationRes;
import org.egov.pt.calculator.web.models.GetBillCriteria;
import org.egov.pt.calculator.web.models.MutationCalculatorReq;
import org.egov.pt.calculator.web.models.demand.BillResponse;
import org.egov.pt.calculator.web.models.demand.DemandRequest;
import org.egov.pt.calculator.web.models.demand.DemandResponse;
import org.egov.pt.calculator.web.models.property.RequestInfoWrapper;
import org.egov.pt.calculator.web.models.propertyV2.PropertyRequestV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/propertytax")
public class CalculatorController {

	@Autowired
	private DemandService demandService;

	@Autowired
	private EstimationService calculatorService;
	
	@Autowired
	private PayService payService;

	@PostMapping("/_estimate")
	public ResponseEntity<CalculationRes> getTaxEstimation(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(calculatorService.getTaxCalculation(calculationReq), HttpStatus.OK);
	}

	@PostMapping("/_calculate")
	public ResponseEntity<Map<String, Calculation>> generateDemands(@RequestBody @Valid CalculationReq calculationReq) {
		return new ResponseEntity<>(calculatorService.calculateAndCreateDemand(calculationReq), HttpStatus.OK);
	}
	
	@PostMapping("/_getbill")
	public ResponseEntity<BillResponse> getBill(@RequestBody @Valid RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute @Valid GetBillCriteria getBillCriteria) {
		return new ResponseEntity<>(demandService.getBill(getBillCriteria, requestInfoWrapper), HttpStatus.OK);
	}

	@PostMapping("/_updatedemand")
	public ResponseEntity<DemandResponse> updateDemand(@RequestBody @Valid RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute @Valid GetBillCriteria getBillCriteria) {
		return new ResponseEntity<>(demandService.updateDemands(getBillCriteria, requestInfoWrapper), HttpStatus.OK);
	}
	
	@PostMapping("/mutation/_calculate")
	public ResponseEntity<Map<String, Calculation>> mutationCalculator(@RequestBody @Valid PropertyRequestV2 request) {
		return new ResponseEntity<>(calculatorService.mutationCalculator(request.getProperty(), request.getRequestInfo()), HttpStatus.OK);
	}
	
	@PostMapping("/demand/_create")
	public ResponseEntity<DemandResponse> createPTDemand(@RequestBody @Valid DemandRequest request) {
		return new ResponseEntity<>(demandService.createPTDemands(request.getDemands(), request.getRequestInfo()),
				HttpStatus.CREATED);
	}

	@PostMapping("/demand/_update")
	public ResponseEntity<DemandResponse> updatePTDemand(@RequestBody @Valid DemandRequest request) {
		return new ResponseEntity<>(demandService.updatePTDemands(request.getDemands(), request.getRequestInfo()),
				HttpStatus.OK);
	}
	
	@GetMapping("/_updateSytemTimeVar")
	public ResponseEntity<Long> _updateSytemTimeVar(@RequestParam String time ) {
		CalculatorConstants.systemTimeInMillisecEnv = Long.valueOf(time);
		return new ResponseEntity<>(CalculatorConstants.systemTimeInMillisecEnv , HttpStatus.OK);
	}
	
	@GetMapping("/_getSytemTimeVar")
	public ResponseEntity<String> _getSytemTimeVar() {
		String date="";
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		date = sdf.format(new Date(CalculatorConstants.systemTimeInMillisecEnv));
		return new ResponseEntity<>(date , HttpStatus.OK);
	}

}
