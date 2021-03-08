package org.bel.birthdeath.death.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.EgDeathFatherInfo;
import org.bel.birthdeath.death.model.EgDeathMotherInfo;
import org.bel.birthdeath.death.model.EgDeathSpouseInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DeathDtlsRowMapper implements ResultSetExtractor<List<EgDeathDtl>> {

	@Override
	public List<EgDeathDtl> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, EgDeathDtl> deathDtlMap = new LinkedHashMap<>();
		try {
			while (rs.next()) {
				String deathdtlid = rs.getString("deathdtlid");
				EgDeathDtl deathDtl = deathDtlMap.get(deathdtlid);

				if (deathDtl == null) {
					EgDeathMotherInfo motherInfo = EgDeathMotherInfo.builder().firstname(rs.getString("bmotfn")).middlename(rs.getString("bmotmn")).lastname(rs.getString("bmotln"))
							.build();
					EgDeathFatherInfo fatherInfo = EgDeathFatherInfo.builder().firstname(rs.getString("bfatfn")).middlename(rs.getString("bfatmn")).lastname(rs.getString("bfatln"))
							.build();
					EgDeathSpouseInfo spouseInfo = EgDeathSpouseInfo.builder().firstname(rs.getString("bspsfn")).middlename(rs.getString("bspsmn")).lastname(rs.getString("bspsln"))
							.build();
					deathDtl = EgDeathDtl.builder().id(deathdtlid).registrationno(rs.getString("registrationno")).hospitalname(rs.getString("hospitalname")).gender(rs.getInt("gender"))
							.dateofdeath(rs.getTimestamp("dateofdeath")).counter(rs.getInt("counter")).genderStr(rs.getString("genderstr")).tenantid(rs.getString("tenantid"))
							.firstname(rs.getString("bdtlfn")).middlename(rs.getString("bdtlmn")).lastname(rs.getString("bdtlln"))
							.deathMotherInfo(motherInfo).deathFatherInfo(fatherInfo).deathSpouseInfo(spouseInfo)
							.build();
					deathDtlMap.put(deathdtlid, deathDtl);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException("INVALID INPUT", "Error in fetching data");
		}
		return new ArrayList<>(deathDtlMap.values());
	}

}
