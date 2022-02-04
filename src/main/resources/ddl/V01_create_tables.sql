create table submission (
    accession_number varchar(25) primary key,
    year integer not null,
    cik varchar(10) not null,
    ticker varchar(10) not null,
    filingdate date,
    reportdate date,
    form varchar(10),
    import_status integer not null default 0
);

comment on column submission.import_status is '0: not imported yet, 1: imported, 2: error on import';

create table soi_fact (
    accession_number varchar(25),
    year integer not null,
    cik varchar(10) not null,
    ticker varchar(10) not null,
    name varchar(200) not null,
    value decimal(16, 2) not null,
    order_in_soi integer,
    level_in_soi integer,
    weight integer,
    primary key (accession_number, name)
);

create table standard_facts (
    accession_number varchar(25) primary key,
    year integer not null,
    cik varchar(10) not null,
    ticker varchar(10) not null,
    revenue bigint,
    cost_of_revenue bigint,
    gross_profit bigint,
    operating_expenses bigint,
    other_operating_income_expense bigint,
    operating_income bigint,
    pretax_income bigint,
    eps_basic double precision,
    eps_diluted double precision
);

comment on column standard_facts.reporting_type is '0: type 1, 1: type 2, 2: type 3';

create table parse_error_log (
    accession_number varchar(25),
    year integer not null,
    cik varchar(10) not null,
    ticker varchar(10) not null,
    content varchar(200),
    stacktrace varchar(400),
    log_timestamp timestamp,
    primary key (accession_number, log_timestamp)
);