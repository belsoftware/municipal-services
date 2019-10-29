package org.egov.waterConnection.service;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.repository.WaterDao;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.egov.waterConnection.validator.ValidateProperty;
import org.egov.waterConnection.validator.WaterConnectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WaterServiceImpl implements WaterService {

	Logger logger = LoggerFactory.getLogger(WaterServiceImpl.class);

	@Autowired
	WaterDao waterDao;

	@Autowired
	WaterServicesUtil waterServicesUtil;

	@Autowired
	WaterConnectionValidator waterConnectionValidator;

	@Autowired
	ValidateProperty validateProperty;

	@Autowired
	EnrichmentService enrichmentService;

	@Override
	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		List<Property> propertyList;
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, false);
		validateProperty.validatePropertyCriteriaForCreate(waterConnectionRequest);
		if (!validateProperty.isPropertyIdPresent(waterConnectionRequest)) {
			propertyList = waterServicesUtil.propertySearch(waterConnectionRequest);
		} else {
			propertyList = waterServicesUtil.createPropertyRequest(waterConnectionRequest);
		}
		enrichmentService.enrichWaterConnection(waterConnectionRequest, propertyList);
		waterDao.saveWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}

	public List<WaterConnection> search(WaterConnectionSearchCriteria criteria, RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList;
		waterConnectionList = getWaterConnectionsList(criteria, requestInfo);
		enrichmentService.enrichWaterSearch(waterConnectionList, requestInfo);
		return waterConnectionList;
	}

	public List<WaterConnection> getWaterConnectionsList(WaterConnectionSearchCriteria criteria,
			RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList = waterDao.getWaterConnectionList(criteria, requestInfo);
		if (waterConnectionList.isEmpty())
			return Collections.emptyList();
		return waterConnectionList;
	}

	@Override
	public List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, true);
		validateProperty.validatePropertyCriteria(waterConnectionRequest);
		waterDao.updateWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
}
