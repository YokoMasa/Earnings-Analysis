package com.masalab.earnings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.masalab.earnings.cik.CikResolver;
import com.masalab.earnings.exception.AppException;
import com.masalab.earnings.persistent.entity.DSubmission;
import com.masalab.earnings.persistent.service.ParseErrorLogService;
import com.masalab.earnings.persistent.service.XbrlService;
import com.masalab.earnings.submission.Submission;
import com.masalab.earnings.submission.SubmissionRepository;
import com.masalab.earnings.submission.SubmittedFile;
import com.masalab.earnings.submission.SubmittedFileRepository;
import com.masalab.earnings.xbrl.Context;
import com.masalab.earnings.xbrl.Fact;
import com.masalab.earnings.xbrl.StandardFacts;
import com.masalab.earnings.xbrl.XbrlInstance;
import com.masalab.earnings.xbrl.XbrlInstanceParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class EarningsApplication implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(EarningsApplication.class);

	@Autowired
	private CikResolver cikResolver;

	@Autowired
	private SubmissionRepository submissionRepository;

	@Autowired
	private SubmittedFileRepository submittedFileRepository;

	@Autowired
	private XbrlInstanceParser xbrlInstanceParser2;

	@Autowired
	private XbrlService xbrlService;

	@Autowired
	private ParseErrorLogService parseErrorLogService;

	public static void main(String[] args) {
		SpringApplication.run(EarningsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Options options = new Options();
		options.addRequiredOption("a", "action", true, "Specify one of the following options: list_all_facts, list_facts, show_soi_calctree, analyze_and_save");
		options.addOption("t", "ticker", true, "Specify ticker symbol. Symbol can be upper cased or lower cased.");
		options.addOption("f", "fact-name", true, "Specify fact name. This option will be ignored unless the action is \"list_facts\"");

		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			String action = cmd.getOptionValue("a");
			String ticker = "";
			if (cmd.hasOption("t")) {
				ticker = cmd.getOptionValue("t");
			}
			switch (action) {
				case "list_all_facts":
					listFacts(ticker, null);
					break;
				case "list_facts":
					String factName = cmd.getOptionValue("f");
					listFacts(ticker, factName);
					break;
				case "show_soi_calctree":
					showSoiCalcTree(ticker);
					break;
				case "analyze_and_save":
					analyzeAndSaveSp500();
					break;
				default:
					logger.error("Unknown action.");
			}
		} catch (ParseException pe) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("earnings cli", options);
		}
	}

	private void listFacts(String ticker, String factName) throws AppException {
		String cik = cikResolver.getCik(ticker);
		List<Submission> submissions = submissionRepository.getSubmissions(cik, SubmissionRepository.FORM_10K);
		if (submissions.size() == 0) {
			logger.debug("No submission found.");
			return;
		}

		Submission submission = submissions.get(0); // latest submission
		SubmittedFile xbrlInstanceFile = submittedFileRepository.getXbrlInstance(submission);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		if (xbrlInstanceFile != null) {
			logger.debug("instance file: " + xbrlInstanceFile.url);
			XbrlInstance instance = xbrlInstanceParser2.parse(xbrlInstanceFile.url);

			List<Fact> factList = null;
			if (StringUtils.hasText(factName)) {
				if (!instance.getFactSet().hasFact(factName)) {
					logger.error("No facts found for name: " + factName);
					return;
				}
				factList = instance.getFactSet().getFacts(factName);
			} else {
				factList = instance.getFactSet().getAllFacts();
			}

			for (Fact f: factList) {
				String periodString = "";
				if (f.context.periodType == Context.PeriodType.INSTANT) {
					periodString = df.format(f.context.instant);
				} else {
					periodString = df.format(f.context.startDate) + " ~ " + df.format(f.context.endDate); 
				}

				String segmentInfo = StringUtils.hasText(f.context.segmentContent) ? ", is segment info" : "";
				String scenarioInfo = StringUtils.hasText(f.context.scenarioContent) ? ", is scenario info" : "";

				logger.debug(f.ns + ", " + f.name + ", " + f.value + ", " + periodString + segmentInfo + scenarioInfo);
			}
		} else {
			logger.warn("xbrlInstance is null!!!");
		}
	}

	private void showSoiCalcTree(String ticker) throws AppException {
		String cik = cikResolver.getCik(ticker);
		logger.debug("ticker: " + ticker + ", cik: " + cik);
		List<Submission> submissions = submissionRepository.getSubmissions(cik, SubmissionRepository.FORM_10K);
		if (submissions.size() == 0) {
			logger.debug("No submission found.");
			return;
		}

		Submission submission = submissions.get(0);
		logger.debug("accession number: " + submission.accessionNumber);

		SubmittedFile xbrlInstanceFile = submittedFileRepository.getXbrlInstance(submission);
		if (xbrlInstanceFile != null) {
			logger.debug("instance file: " + xbrlInstanceFile.url);
			XbrlInstance instance = xbrlInstanceParser2.parse(xbrlInstanceFile.url);
			StandardFacts standardFacts = instance.getStandardFacts();
			if (standardFacts.rootFact != null) {
				System.out.println(standardFacts.rootFact.getTreeString(5));
			} else {
				logger.error("Soi calculation tree not found.");
			}
		} else {
			logger.error("Xbrl instance file not found.");
		}
	}

	private void analyzeAndSaveSp500() throws InterruptedException {
		String sp500TickerFilePath = "C:\\Users\\masato\\Dropbox\\Earnings Analysis\\earnings_retriever\\src\\main\\resources\\sp500tickers.txt";
		try (BufferedReader reader = new BufferedReader(new FileReader(sp500TickerFilePath))) {
			String ticker = reader.readLine();
			while (ticker != null) {
				Thread.sleep(700);
				analyzeAndSave(ticker);
				ticker = reader.readLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void analyzeAndSave(String ticker) {
		String accessionNumber = "";
		String cik = "";
		int year = 0;

		try {
			cik = cikResolver.getCik(ticker);
			logger.debug("ticker: " + ticker + ", cik: " + cik);
			if (!StringUtils.hasText(cik)) {
				return;
			}
			
			List<Submission> submissions = submissionRepository.getSubmissions(cik, SubmissionRepository.FORM_10K);
			if (submissions.size() == 0) {
				logger.debug("No submission found.");
				return;
			}

			Submission submission = submissions.get(0);

			accessionNumber = submission.accessionNumber;
			if (xbrlService.isImported(accessionNumber)) {
				logger.debug("Already imported");
				return;
			}

			year = toIntYear(submission.reportDate);
			xbrlService.saveSubmission(submission, ticker);

			logger.debug("accession number: " + submission.accessionNumber);
			SubmittedFile xbrlInstanceFile = submittedFileRepository.getXbrlInstance(submission);
			if (xbrlInstanceFile != null) {
				System.out.println(xbrlInstanceFile.name + " " + xbrlInstanceFile.url);
				XbrlInstance instance = xbrlInstanceParser2.parse(xbrlInstanceFile.url);
				StandardFacts standardFacts = instance.getStandardFacts();
				System.out.println(standardFacts.toString());
				if (standardFacts.rootFact != null) {
					System.out.println("---------------------------------------------------------");
					System.out.println(standardFacts.rootFact.getTreeString(5));
					System.out.println("---------------------------------------------------------");
				}
				xbrlService.saveFacts(standardFacts, submission, ticker);
				xbrlService.updateSubmissionStatus(accessionNumber, DSubmission.IMPORTED);
			} else {
				logger.warn("xbrlInstance is null!!!");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			parseErrorLogService.logError(accessionNumber, cik, ticker, year, e.getMessage(), e);
			xbrlService.updateSubmissionStatus(accessionNumber, DSubmission.ERROR_ON_IMPORT);
		}

	}

	private int toIntYear(Date date) {
		Calendar calendar = new Calendar.Builder()
			.setInstant(date)
			.build();
		return calendar.get(Calendar.YEAR);
	}

}
