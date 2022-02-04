package com.masalab.earnings.persistent.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import com.masalab.earnings.persistent.entity.DSoiFact;
import com.masalab.earnings.persistent.entity.DStandardFacts;
import com.masalab.earnings.persistent.entity.DSubmission;
import com.masalab.earnings.persistent.repository.DSoiFactRepository;
import com.masalab.earnings.persistent.repository.DStandardFactsRepository;
import com.masalab.earnings.persistent.repository.DSubmissionRepository;
import com.masalab.earnings.submission.Submission;
import com.masalab.earnings.xbrl.StandardFacts;
import com.masalab.earnings.xbrl.calc.CalculatedFact;
import com.masalab.earnings.xbrl.calc.CalculationItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class XbrlServiceImpl implements XbrlService {

    @Autowired
    private DSoiFactRepository dSoiFactRepository;

	@Autowired
	private DStandardFactsRepository dStandardFactsRepository;

	@Autowired
	private DSubmissionRepository dSubmissionRepository;

    @Override
    @Transactional
    public void saveFacts(StandardFacts standardFacts, Submission submission, String ticker) {
		if (standardFacts.rootFact != null && StringUtils.hasText(standardFacts.soiRole)) {
        	saveSoiFact(submission, ticker, standardFacts.rootFact, standardFacts.soiRole, 0, 0, new int[] {0});
		}
		saveStandardFact(standardFacts, submission, ticker);
    }

	private void saveStandardFact(StandardFacts standardFacts, Submission submission, String ticker) {
		DStandardFacts facts = new DStandardFacts();
		facts.setAccessionNumber(submission.accessionNumber);
		facts.setCik(submission.cik);
		facts.setTicker(ticker);
		facts.setYear(toIntYear(submission.reportDate));
		facts.setRevenue(standardFacts.revenue);
		facts.setCostOfRevenue(standardFacts.costOfRevenue);
		facts.setGrossProfit(standardFacts.grossProfit);
		facts.setOperatingExpenses(standardFacts.operatingExpenses);
		facts.setOperatingIncome(standardFacts.operatingIncome);
		facts.setOtherOperatingIncomeExpense(standardFacts.otherOperatingIncomeExpense);
		facts.setPretaxIncome(standardFacts.pretaxIncome);
		dStandardFactsRepository.save(facts);
	}

    private void saveSoiFact(Submission submission, String ticker, CalculatedFact calculatedFact, String role, double weight, int level, int[] order) {
        DSoiFact fact = new DSoiFact();
		fact.setAccessionNumber(submission.accessionNumber);
		fact.setName(calculatedFact.getName());
		fact.setCik(submission.cik);
		fact.setLevelInSoi(level);
		fact.setOrderInSoi(order[0]);
		fact.setTicker(ticker);
		fact.setValue(calculatedFact.getVal());
		fact.setYear(toIntYear(submission.reportDate));
		if (weight == -1) {
			fact.setWeight(-1);
		} else if (weight == 1) {
			fact.setWeight(1);
		} else {
			fact.setWeight(0);
		}

		dSoiFactRepository.save(fact);

		if (!calculatedFact.hasChildren(role)) {
			return;
		}
		
		List<CalculationItem<CalculatedFact>> children = calculatedFact.getChildren(role);
		for (int i = 0; i < children.size(); i++) {
			order[0]++;
			CalculationItem<CalculatedFact> calcItem = children.get(i);
			saveSoiFact(submission, ticker, calcItem.getItem(), role, calcItem.getWeight() ,level+1, order);
		}
	}

	@Override
	public void saveSubmission(Submission submission, String ticker) {
		DSubmission dSub = new DSubmission();
		dSub.setAccessionNumber(submission.accessionNumber);
		dSub.setCik(submission.cik);
		dSub.setFilingdate(submission.filingDate);
		dSub.setReportdate(submission.reportDate);
		dSub.setForm(submission.form);
		dSub.setTicker(ticker);
		dSub.setYear(toIntYear(submission.reportDate));
		dSub.setImportStatus(DSubmission.NOT_IMPORTED);
		dSubmissionRepository.save(dSub);
	}

	@Override
	public void updateSubmissionStatus(String accessionNumber, int status) {
		Optional<DSubmission> dSubOptional = dSubmissionRepository.findById(accessionNumber);
		if (dSubOptional.isPresent()) {
			DSubmission dSub = dSubOptional.get();
			dSub.setImportStatus(status);
			dSubmissionRepository.save(dSub);
		}
	}

	private int toIntYear(Date date) {
		Calendar calendar = new Calendar.Builder()
			.setInstant(date)
			.build();
		return calendar.get(Calendar.YEAR);
	}

	@Override
	public boolean isImported(String accessionNumber) {
		Optional<DSubmission> dSub = dSubmissionRepository.findById(accessionNumber);
		if (dSub.isPresent()) {
			return dSub.get().getImportStatus() == DSubmission.IMPORTED;
		} else {
			return false;
		}
	}

}
