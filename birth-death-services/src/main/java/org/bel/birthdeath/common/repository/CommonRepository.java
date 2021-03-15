package org.bel.birthdeath.common.repository;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.EgBirthFatherInfo;
import org.bel.birthdeath.birth.model.EgBirthMotherInfo;
import org.bel.birthdeath.birth.model.EgBirthPermaddr;
import org.bel.birthdeath.birth.model.EgBirthPresentaddr;
import org.bel.birthdeath.birth.validator.BirthValidator;
import org.bel.birthdeath.common.contract.BirthResponse;
import org.bel.birthdeath.common.contract.DeathResponse;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.common.model.EgHospitalDtl;
import org.bel.birthdeath.common.repository.builder.CommonQueryBuilder;
import org.bel.birthdeath.common.repository.rowmapper.CommonRowMapper;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.EgDeathFatherInfo;
import org.bel.birthdeath.death.model.EgDeathMotherInfo;
import org.bel.birthdeath.death.model.EgDeathPermaddr;
import org.bel.birthdeath.death.model.EgDeathPresentaddr;
import org.bel.birthdeath.death.model.EgDeathSpouseInfo;
import org.bel.birthdeath.death.validator.DeathValidator;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class CommonRepository {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private CommonQueryBuilder queryBuilder;
	
	@Autowired
	private CommonRowMapper rowMapper;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	private CommonUtils commUtils;
	
	@Autowired
	BirthValidator birthValidator;
	
	@Autowired
	DeathValidator deathValidator;
    
	private static final String birthDtlSaveQry="INSERT INTO public.eg_birth_dtls(id, registrationno, hospitalname, dateofreport, "
    		+ "dateofbirth, firstname, middlename, lastname, placeofbirth, informantsname, informantsaddress, "
    		+ "createdtime, createdby, lastmodifiedtime, lastmodifiedby, counter, tenantid, gender, remarks, hospitalid) "
    		+ "VALUES (:id, :registrationno, :hospitalname, :dateofreport, :dateofbirth, :firstname, :middlename, :lastname, "
    		+ ":placeofbirth, :informantsname, :informantsaddress, :createdtime, :createdby, :lastmodifiedtime, "
    		+ ":lastmodifiedby, :counter, :tenantid, :gender, :remarks, :hospitalid); ";
	
	private static final String birthFatherInfoSaveQry="INSERT INTO public.eg_birth_father_info( id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, education, proffession, nationality, religion, createdtime, createdby, lastmodifiedtime, lastmodifiedby, birthdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :education, :proffession, :nationality,"
			+ " :religion, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :birthdtlid);";
	
	private static final String birthMotherInfoSaveQry="INSERT INTO public.eg_birth_mother_info(id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, education, proffession, nationality, religion, createdtime, createdby, lastmodifiedtime, lastmodifiedby, birthdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :education, :proffession, :nationality,"
			+ " :religion, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :birthdtlid);";
	
	private static final String birthPermAddrSaveQry="INSERT INTO public.eg_birth_permaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, birthdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country,"
			+ " :createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :birthdtlid);";
	
	private static final String birthPresentAddrSaveQry="INSERT INTO public.eg_birth_presentaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, birthdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country, "
			+ ":createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :birthdtlid);";
	
	private static final String deathDtlSaveQry="INSERT INTO public.eg_death_dtls(id, registrationno, hospitalname, dateofreport, "
    		+ "dateofdeath, firstname, middlename, lastname, placeofdeath, informantsname, informantsaddress, "
    		+ "createdtime, createdby, lastmodifiedtime, lastmodifiedby, counter, tenantid, gender, remarks, hospitalid, age, eidno, aadharno, nationality, religion, icdcode) "
    		+ "VALUES (:id, :registrationno, :hospitalname, :dateofreport, :dateofdeath, :firstname, :middlename, :lastname, "
    		+ ":placeofdeath, :informantsname, :informantsaddress, :createdtime, :createdby, :lastmodifiedtime, "
    		+ ":lastmodifiedby, :counter, :tenantid, :gender, :remarks, :hospitalid, :age, :eidno, :aadharno, :nationality, :religion, :icdcode); ";
	
	private static final String deathFatherInfoSaveQry="INSERT INTO public.eg_death_father_info( id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, createdtime, createdby, lastmodifiedtime, lastmodifiedby, deathdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :deathdtlid);";
	
	private static final String deathMotherInfoSaveQry="INSERT INTO public.eg_death_mother_info(id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, createdtime, createdby, lastmodifiedtime, lastmodifiedby, deathdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :deathdtlid);";
	
	private static final String deathSpouseInfoSaveQry="INSERT INTO public.eg_death_spouse_info(id, firstname, middlename, lastname, aadharno, "
			+ "emailid, mobileno, createdtime, createdby, lastmodifiedtime, lastmodifiedby, deathdtlid) "
			+ "VALUES (:id, :firstname, :middlename, :lastname, :aadharno, :emailid, :mobileno, :createdtime, :createdby, :lastmodifiedtime, :lastmodifiedby, :deathdtlid);";
	
	private static final String deathPermAddrSaveQry="INSERT INTO public.eg_death_permaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, deathdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country,"
			+ " :createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :deathdtlid);";
	
	private static final String deathPresentAddrSaveQry="INSERT INTO public.eg_death_presentaddr(id, buildingno, houseno, streetname, locality, tehsil, "
			+ "district, city, state, pinno, country, createdby, createdtime, lastmodifiedby, lastmodifiedtime, deathdtlid) "
			+ "VALUES (:id, :buildingno, :houseno, :streetname, :locality, :tehsil, :district, :city, :state, :pinno, :country, "
			+ ":createdby, :createdtime, :lastmodifiedby, :lastmodifiedtime, :deathdtlid);";
	
	public List<EgHospitalDtl> getHospitalDtls(String tenantId) {
		List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getHospitalDtls(tenantId, preparedStmtList);
        List<EgHospitalDtl> hospitalDtls =  jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
        return hospitalDtls;
	}

	public ArrayList<EgBirthDtl> saveBirthImport(BirthResponse response, RequestInfo requestInfo) {
		//ArrayList<EgBirthDtl> birthArrayList = new ArrayList<EgBirthDtl>();
		ArrayList<EgBirthDtl> rejectedbirthArrayList = new ArrayList<EgBirthDtl>();
		Set<EgBirthDtl> duplicateList = new HashSet<EgBirthDtl>();
		try {
		//BirthResponse response= mapper.convertValue(importJSon, BirthResponse.class);
		List<MapSqlParameterSource> birthDtlSource = new ArrayList<>();
		List<MapSqlParameterSource> birthFatherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> birthMotherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> birthPermAddrSource = new ArrayList<>();
		List<MapSqlParameterSource> birthPresentAddrSource = new ArrayList<>();
		Map<String,EgBirthDtl> uniqueList = new HashMap<String, EgBirthDtl>();
		response.getBirthCerts().forEach(bdtl -> {
			if (bdtl.getRegistrationno() != null) {
				if (uniqueList.get(bdtl.getRegistrationno()) == null)
					uniqueList.put(bdtl.getRegistrationno(), bdtl);
				else {
					duplicateList.add(bdtl);
					duplicateList.add(uniqueList.get(bdtl.getRegistrationno()));
				}
			}
		});
		AuditDetails auditDetails = commUtils.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		for (Entry<String, EgBirthDtl> entry : uniqueList.entrySet()) {
			EgBirthDtl birthDtl = entry.getValue();
			if(birthValidator.validateUniqueRegNo(birthDtl) && birthValidator.validateImportFields(birthDtl)){
				birthDtlSource.add(getParametersForBirthDtl(birthDtl, auditDetails));
				birthFatherInfoSource.add(getParametersForFatherInfo(birthDtl, auditDetails));
				birthMotherInfoSource.add(getParametersForMotherInfo(birthDtl, auditDetails));
				birthPermAddrSource.add(getParametersForPermAddr(birthDtl, auditDetails));
				birthPresentAddrSource.add(getParametersForPresentAddr(birthDtl, auditDetails));
				//birthArrayList.add(birthDtl);
			}
			else {
				rejectedbirthArrayList.add(birthDtl);
			}
		}
		//log.info(new Gson().toJson(birthDtlSource));
		namedParameterJdbcTemplate.batchUpdate(birthDtlSaveQry, birthDtlSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthFatherInfoSaveQry, birthFatherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthMotherInfoSaveQry, birthMotherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthPermAddrSaveQry, birthPermAddrSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(birthPresentAddrSaveQry, birthPresentAddrSource.toArray(new MapSqlParameterSource[0]));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		log.info("duplicate "+duplicateList.size());
		duplicateList.forEach(duplicate -> { duplicate.setRejectReason("Duplicate Reg No.");});
		rejectedbirthArrayList.addAll(duplicateList);
		return rejectedbirthArrayList;
	}

	private MapSqlParameterSource getParametersForPresentAddr(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgBirthPresentaddr presentaddr = birthDtl.getBirthPresentaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", presentaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", presentaddr.getHouseno());
		sqlParameterSource.addValue("streetname", presentaddr.getStreetname());
		sqlParameterSource.addValue("locality", presentaddr.getLocality());
		sqlParameterSource.addValue("tehsil", presentaddr.getTehsil());
		sqlParameterSource.addValue("district", presentaddr.getDistrict());
		sqlParameterSource.addValue("city", presentaddr.getCity());
		sqlParameterSource.addValue("state", presentaddr.getState());
		sqlParameterSource.addValue("pinno", presentaddr.getPinno());
		sqlParameterSource.addValue("country", presentaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForPermAddr(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgBirthPermaddr permaddr = birthDtl.getBirthPermaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", permaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", permaddr.getHouseno());
		sqlParameterSource.addValue("streetname", permaddr.getStreetname());
		sqlParameterSource.addValue("locality", permaddr.getLocality());
		sqlParameterSource.addValue("tehsil", permaddr.getTehsil());
		sqlParameterSource.addValue("district", permaddr.getDistrict());
		sqlParameterSource.addValue("city", permaddr.getCity());
		sqlParameterSource.addValue("state", permaddr.getState());
		sqlParameterSource.addValue("pinno", permaddr.getPinno());
		sqlParameterSource.addValue("country", permaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForMotherInfo(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		EgBirthMotherInfo birthMotherInfo = birthDtl.getBirthMotherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", birthMotherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", birthMotherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", birthMotherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", birthMotherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", birthMotherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", birthMotherInfo.getMobileno());
		sqlParameterSource.addValue("education", birthMotherInfo.getEducation());
		sqlParameterSource.addValue("proffession", birthMotherInfo.getProffession());
		sqlParameterSource.addValue("nationality", birthMotherInfo.getNationality());
		sqlParameterSource.addValue("religion", birthMotherInfo.getReligion());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForFatherInfo(EgBirthDtl birthDtl,
			AuditDetails auditDetails) {
		EgBirthFatherInfo birthFatherInfo = birthDtl.getBirthFatherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", birthFatherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", birthFatherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", birthFatherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", birthFatherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", birthFatherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", birthFatherInfo.getMobileno());
		sqlParameterSource.addValue("education", birthFatherInfo.getEducation());
		sqlParameterSource.addValue("proffession", birthFatherInfo.getProffession());
		sqlParameterSource.addValue("nationality", birthFatherInfo.getNationality());
		sqlParameterSource.addValue("religion", birthFatherInfo.getReligion());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("birthdtlid", birthDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForBirthDtl(EgBirthDtl birthDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		String id= UUID.randomUUID().toString();
		sqlParameterSource.addValue("id", id);
		sqlParameterSource.addValue("registrationno", birthDtl.getRegistrationno());
		sqlParameterSource.addValue("hospitalname", birthDtl.getHospitalname());
		sqlParameterSource.addValue("dateofreport", birthDtl.getDateofreport());
		sqlParameterSource.addValue("dateofbirth", birthDtl.getDateofbirth());
		sqlParameterSource.addValue("firstname", birthDtl.getFirstname());
		sqlParameterSource.addValue("middlename", birthDtl.getMiddlename());
		sqlParameterSource.addValue("lastname", birthDtl.getLastname());
		sqlParameterSource.addValue("placeofbirth", birthDtl.getPlaceofbirth());
		sqlParameterSource.addValue("informantsname", birthDtl.getInformantsname());
		sqlParameterSource.addValue("informantsaddress", birthDtl.getInformantsaddress());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("counter", birthDtl.getCounter());
		sqlParameterSource.addValue("tenantid", birthDtl.getTenantid());
		sqlParameterSource.addValue("gender", birthDtl.getGender());
		sqlParameterSource.addValue("remarks", birthDtl.getRemarks());
		sqlParameterSource.addValue("hospitalid", birthDtl.getHospitalid());
		birthDtl.setId(id);
		return sqlParameterSource;

	}
	
	
	public ArrayList<EgDeathDtl> saveDeathImport(DeathResponse response, RequestInfo requestInfo) {
		//ArrayList<EgDeathDtl> deathArrayList = new ArrayList<EgDeathDtl>();
		ArrayList<EgDeathDtl> rejecteddeathArrayList = new ArrayList<EgDeathDtl>();
		Set<EgDeathDtl> duplicateList = new HashSet<EgDeathDtl>();
		try {
		//DeathResponse response= mapper.convertValue(importJSon, DeathResponse.class);
		List<MapSqlParameterSource> deathDtlSource = new ArrayList<>();
		List<MapSqlParameterSource> deathFatherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> deathMotherInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> deathSpouseInfoSource = new ArrayList<>();
		List<MapSqlParameterSource> deathPermAddrSource = new ArrayList<>();
		List<MapSqlParameterSource> deathPresentAddrSource = new ArrayList<>();
		Map<String,EgDeathDtl> uniqueList = new HashMap<String, EgDeathDtl>();
		response.getDeathCerts().forEach(bdtl -> {
			if (bdtl.getRegistrationno() != null) {
				if (uniqueList.get(bdtl.getRegistrationno()) == null)
					uniqueList.put(bdtl.getRegistrationno(), bdtl);
				else {
					duplicateList.add(bdtl);
					duplicateList.add(uniqueList.get(bdtl.getRegistrationno()));
				}
			}
		});
		AuditDetails auditDetails = commUtils.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		for (Entry<String, EgDeathDtl> entry : uniqueList.entrySet()) {
			EgDeathDtl deathDtl = entry.getValue();
			if(deathValidator.validateUniqueRegNo(deathDtl) && deathValidator.validateImportFields(deathDtl)){
				deathDtlSource.add(getParametersForDeathDtl(deathDtl, auditDetails));
				deathFatherInfoSource.add(getParametersForFatherInfo(deathDtl, auditDetails));
				deathMotherInfoSource.add(getParametersForMotherInfo(deathDtl, auditDetails));
				deathSpouseInfoSource.add(getParametersForSpouseInfo(deathDtl, auditDetails));
				deathPermAddrSource.add(getParametersForPermAddr(deathDtl, auditDetails));
				deathPresentAddrSource.add(getParametersForPresentAddr(deathDtl, auditDetails));
				//deathArrayList.add(deathDtl);
			}
			else {
				rejecteddeathArrayList.add(deathDtl);
			}
		}
		//log.info(new Gson().toJson(deathDtlSource));
		namedParameterJdbcTemplate.batchUpdate(deathDtlSaveQry, deathDtlSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(deathFatherInfoSaveQry, deathFatherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(deathMotherInfoSaveQry, deathMotherInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(deathSpouseInfoSaveQry, deathSpouseInfoSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(deathPermAddrSaveQry, deathPermAddrSource.toArray(new MapSqlParameterSource[0]));
		namedParameterJdbcTemplate.batchUpdate(deathPresentAddrSaveQry, deathPresentAddrSource.toArray(new MapSqlParameterSource[0]));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		log.info("duplicate "+duplicateList.size());
		duplicateList.forEach(duplicate -> { duplicate.setRejectReason("Duplicate Reg No.");});
		rejecteddeathArrayList.addAll(duplicateList);
		return rejecteddeathArrayList;
	}

	private MapSqlParameterSource getParametersForPresentAddr(EgDeathDtl deathDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgDeathPresentaddr presentaddr = deathDtl.getDeathPresentaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", presentaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", presentaddr.getHouseno());
		sqlParameterSource.addValue("streetname", presentaddr.getStreetname());
		sqlParameterSource.addValue("locality", presentaddr.getLocality());
		sqlParameterSource.addValue("tehsil", presentaddr.getTehsil());
		sqlParameterSource.addValue("district", presentaddr.getDistrict());
		sqlParameterSource.addValue("city", presentaddr.getCity());
		sqlParameterSource.addValue("state", presentaddr.getState());
		sqlParameterSource.addValue("pinno", presentaddr.getPinno());
		sqlParameterSource.addValue("country", presentaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("deathdtlid", deathDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForPermAddr(EgDeathDtl deathDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		EgDeathPermaddr permaddr = deathDtl.getDeathPermaddr();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("buildingno", permaddr.getBuildingno());
		sqlParameterSource.addValue("houseno", permaddr.getHouseno());
		sqlParameterSource.addValue("streetname", permaddr.getStreetname());
		sqlParameterSource.addValue("locality", permaddr.getLocality());
		sqlParameterSource.addValue("tehsil", permaddr.getTehsil());
		sqlParameterSource.addValue("district", permaddr.getDistrict());
		sqlParameterSource.addValue("city", permaddr.getCity());
		sqlParameterSource.addValue("state", permaddr.getState());
		sqlParameterSource.addValue("pinno", permaddr.getPinno());
		sqlParameterSource.addValue("country", permaddr.getCountry());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("deathdtlid", deathDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForMotherInfo(EgDeathDtl deathDtl, AuditDetails auditDetails) {
		EgDeathMotherInfo deathMotherInfo = deathDtl.getDeathMotherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", deathMotherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", deathMotherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", deathMotherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", deathMotherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", deathMotherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", deathMotherInfo.getMobileno());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("deathdtlid", deathDtl.getId());
		return sqlParameterSource;
	}
	
	private MapSqlParameterSource getParametersForSpouseInfo(EgDeathDtl deathDtl, AuditDetails auditDetails) {
		EgDeathSpouseInfo deathSpouseInfo = deathDtl.getDeathSpouseInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", deathSpouseInfo.getFirstname());
		sqlParameterSource.addValue("middlename", deathSpouseInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", deathSpouseInfo.getLastname());
		sqlParameterSource.addValue("aadharno", deathSpouseInfo.getAadharno());
		sqlParameterSource.addValue("emailid", deathSpouseInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", deathSpouseInfo.getMobileno());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("deathdtlid", deathDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForFatherInfo(EgDeathDtl deathDtl,
			AuditDetails auditDetails) {
		EgDeathFatherInfo deathFatherInfo = deathDtl.getDeathFatherInfo();
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		sqlParameterSource.addValue("id", UUID.randomUUID().toString());
		sqlParameterSource.addValue("firstname", deathFatherInfo.getFirstname());
		sqlParameterSource.addValue("middlename", deathFatherInfo.getMiddlename());
		sqlParameterSource.addValue("lastname", deathFatherInfo.getLastname());
		sqlParameterSource.addValue("aadharno", deathFatherInfo.getAadharno());
		sqlParameterSource.addValue("emailid", deathFatherInfo.getEmailid());
		sqlParameterSource.addValue("mobileno", deathFatherInfo.getMobileno());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("deathdtlid", deathDtl.getId());
		return sqlParameterSource;
	}

	private MapSqlParameterSource getParametersForDeathDtl(EgDeathDtl deathDtl, AuditDetails auditDetails) {
		MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
		String id= UUID.randomUUID().toString();
		sqlParameterSource.addValue("id", id);
		sqlParameterSource.addValue("registrationno", deathDtl.getRegistrationno());
		sqlParameterSource.addValue("hospitalname", deathDtl.getHospitalname());
		sqlParameterSource.addValue("dateofreport", deathDtl.getDateofreport());
		sqlParameterSource.addValue("dateofdeath", deathDtl.getDateofdeath());
		sqlParameterSource.addValue("firstname", deathDtl.getFirstname());
		sqlParameterSource.addValue("middlename", deathDtl.getMiddlename());
		sqlParameterSource.addValue("lastname", deathDtl.getLastname());
		sqlParameterSource.addValue("placeofdeath", deathDtl.getPlaceofdeath());
		sqlParameterSource.addValue("informantsname", deathDtl.getInformantsname());
		sqlParameterSource.addValue("informantsaddress", deathDtl.getInformantsaddress());
		sqlParameterSource.addValue("createdtime", auditDetails.getCreatedTime());
		sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
		sqlParameterSource.addValue("lastmodifiedtime", null);
		sqlParameterSource.addValue("lastmodifiedby", null);
		sqlParameterSource.addValue("counter", deathDtl.getCounter());
		sqlParameterSource.addValue("tenantid", deathDtl.getTenantid());
		sqlParameterSource.addValue("gender", deathDtl.getGender());
		sqlParameterSource.addValue("remarks", deathDtl.getRemarks());
		sqlParameterSource.addValue("hospitalid", deathDtl.getHospitalid());
		sqlParameterSource.addValue("age", deathDtl.getAge() );
		sqlParameterSource.addValue("eidno", deathDtl.getEidno() );
		sqlParameterSource.addValue("aadharno", deathDtl.getAadharno() );
		sqlParameterSource.addValue("nationality", deathDtl.getNationality() );
		sqlParameterSource.addValue("religion", deathDtl.getReligion() );
		sqlParameterSource.addValue("icdcode", deathDtl.getIcdcode() );	
		deathDtl.setId(id);
		return sqlParameterSource;

	}
}
