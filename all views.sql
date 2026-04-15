
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."STAFF_HL_APPENDIX_26" ("WINAME", "DAYOF", "MONTHOF", "YEAROF", "LOANAVLBRANCHCITY", "NAME_OF_THE_BORROWER", "AGE", "BORROWER_FATHER_NAME", "DESIGNATION", "PRESESNT_WORKING_BRANCH", "NAME_OF_CO_BORROWER", "CO_BORROWER_AGE", "CO_BORROWER_FATHER_NAME", "CO_BORROWER_ADDRESS", "SANCTIONLOANAMT", "SANCTIONLOANAMTINWORDS", "LOANACCNUM", "PROPERTYADD", "SANCTIONDATE", "LOAN_REQUESTED", "LOAN_REQUESTED_WORDS", "LOAN_SANCTIONED_WORDS", "LOAN_SANCTIONED", "APP_REF_NO", "STAFFNUMBER", "PRESENTWORKINGBRANCH", "EMPNAME", "EMPFATHERNAME", "PRESENTADDEMP", "APPLREFNO", "NAMECOBORROWER", "AGECOBORROWER", "FATHERCOBORROWER", "PERMADDCOBORROWER", "SL_NO", "DATE_OF", "DESCRIPTION_OF", "AMOUNT", "DESCRIPTION", "STAFF_HL", "MARGIN", "ROI", "APPROXIMATE_VALUE", "LOCATION", "BOUNDRIES", "NATURE_OF_PROPERTY", "EXTENT", "PROPERTY_DECS", "LOAN_AVL_BRANCH") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  select distinct sst.winame as winame,
TO_CHAR(SYSDATE, 'DD') ||
CASE
    WHEN TO_CHAR(SYSDATE, 'DD') IN ('11','12','13') THEN 'th'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '1' THEN 'st'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '2' THEN 'nd'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '3' THEN 'rd'
    ELSE 'th'
END AS DAYOF,
    TO_CHAR(SYSDATE, 'Month') AS MONTHOF,

    TO_CHAR(SYSDATE, 'YYYY') AS YEAROF,
    (select STATE from los_m_branch where trim(BRANCHNAME)=trim(ssht.BRANCH_NAME)) AS LOANAVLBRANCHCITY,
    nvl(ltc.CUSTOMERFIRSTNAME,'') || ' ' || nvl(ltc.CUSTOMERMIDDLENAME,'') || ' ' || nvl(ltc.CUSTOMERLASTNAME,'') AS NAME_OF_THE_BORROWER,
    ssht.age AS AGE,
    ltc.FATHERNAME AS BORROWER_FATHER_NAME,
    ssht.designation AS DESIGNATION,
     sst.current_branch AS PRESESNT_WORKING_BRANCH,

    CASE WHEN lnbi.applicanttype = 'CB' THEN lnbi.fullname ELSE u'' END AS NAME_OF_CO_BORROWER,

     NVL(lnbii.age,'') AS CO_BORROWER_AGE,
     NVL(lnbii.fathername,'') AS CO_BORROWER_FATHER_NAME,
      nvl(lna.line1,'') || ' ' || nvl(lna.line2,'') || ' '  ||nvl(lna.line3,'') AS CO_BORROWER_ADDRESS,
     -- NVL(lnbi.permaddress1||' '||lnbi.permaddress2||' '||lnbi.permaddress3 AS CO_BORROWER_ADDRESS,
     sst.app_loan_amt_vl AS SANCTIONLOANAMT,
     convert_to_indian_words(sst.app_loan_amt_vl) AS SANCTIONLOANAMTINWORDS,
     stld.loan_accountno AS LOANACCNUM,
    'Survey Number- '||lccd.SURVEY_MUNICIPAL_NUMBER||', '||'Plot Number- '||lccd.plot_number||', '|| lccd.line1||', '||lccd.line2||', '||lccd.district||', '||(select STATE_NAME from los_mst_state where STATE_CODE=lccd.state)||', '||lccd.ZIPCODE as PROPERTYADD,
     stl.SANCTION_DATE AS SANCTIONDATE,
     sst.app_loan_amt_vl as LOAN_REQUESTED,
     convert_to_indian_words(sst.app_loan_amt_vl) as  LOAN_REQUESTED_WORDS,
     convert_to_indian_words(stld.sanction_amount) as  LOAN_SANCTIONED_WORDS,
      stld.sanction_amount as LOAN_SANCTIONED,
     lwt.Application_No as  APP_REF_NO,
     --'' AS FIRST_INSTAL_PERIOD
'' as STAFFNUMBER,
'' as PRESENTWORKINGBRANCH,
'' as EMPNAME,
'' as EMPFATHERNAME,
'' as PRESENTADDEMP,
'' as APPLREFNO,
'' as NAMECOBORROWER,
'' as AGECOBORROWER,
'' as FATHERCOBORROWER,
'' as PERMADDCOBORROWER,
'' as SL_NO,
'' as DATE_OF,
'' as DESCRIPTION_OF,
'' as AMOUNT,
'' as "DESCRIPTION",
ssht.hl_product as STAFF_HL,
TO_CHAR((lccd.project_cost-sst.app_loan_amt_vl),'FM999999990.00') as MARGIN,
'Presently @ 5.50 % simple up to Rs.40,00,000/- and @ 6.00% simple beyond Rs.40,00,000/-' as ROI,
lccd.marketvalueofplot as APPROXIMATE_VALUE,
'Latitude-' || lccd.latitude || ', ' || 'Longitude-' || lccd.longitude as "LOCATION",
'North-' || lccd.boundarynorth || ', ' || 'South-' || lccd.boundarysouth || ', ' ||
'East-' || lccd.boundaryeast || ', ' || 'West-' || lccd.boundarywest  as BOUNDRIES,
lccd.type_of_house as NATURE_OF_PROPERTY,
lccd.carpetarea as "EXTENT",
lccd.BUILDING_WING || ', ' || lccd.line1 || ', ' || lccd.line2 || ', ' ||
    lccd.LINE3 || ', ' || lccd.NEARESTLANDMARK || ', ' || lccd.ZIPCODE as PROPERTY_DECS,
ssht.branch_name as LOAN_AVL_BRANCH


FROM slos_staff_trn sst
JOIN slos_trn_loansummary stl  ON stl.winame = sst.winame
join slos_trn_loandetails stld on stld.pid = sst.winame
join slos_staff_home_trn ssht on ssht.winame=sst.winame
join los_trn_customersummary ltc  on ltc.winame=sst.winame
join LOS_NL_BASIC_INFO lnbi on lnbi.pid= sst.winame  AND lnbi.Applicanttype<> 'B'
join los_wireference_table lwt on lwt.winame=sst.winame
LEFT JOIN LOS_NL_ADDRESS lna on lna.f_key=lnbi.f_key and lna.addresstype='P'
LEFT JOIN los_cam_collateral_details lccd ON ssht.winame = lccd.pid
LEFT JOIN los_m_hl_collateral u ON lccd.asset_subcategory = u.COLLATERAL_ID
LEFT JOIN LOS_L_BASIC_INFO_I lnbii on lnbii.f_key=lnbi.f_key
LEFT JOIN los_m_branch pwb
    ON pwb.BRANCHCODE = LPAD(sst.PRESENT_WORKING_DP_CODE, 5, '0') where sst.winame like '%SLOS%';

----------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_APPENDIX_10" ("WINAME", "LOAN_AVAIL_BRANCH", "SANCTION_DATE", "STAFF_NAME", "STAFF_FATHER_NAME", "ADDRESS", "SANCTION_AMT", "APPLICATION_NO") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  select ssht.winame as winame ,ssht.branch_name as loanAvailBranch,
stl.sanction_date,

nvl(ltc.CUSTOMERFIRSTNAME,'') || nvl(ltc.CUSTOMERMIDDLENAME,'')

|| nvl(ltc.CUSTOMERLASTNAME,''),ltc.FATHERNAME,

nvl(ltc.PERMADDRESS1,'') ||  nvl(ltc.PERMADDRESS2,'') || nvl(ltc.PERMADDRESS3,''),

sst.APP_LOAN_AMT_VL,

lwt.APPLICATION_NO

from slos_staff_home_trn ssht

join slos_staff_trn sst on sst.winame=ssht.winame

join los_trn_customerSummary ltc on ltc.winame=ssht.winame

join slos_trn_loanSummary stl on stl.winame=ssht.winame

join los_wireference_table lwt on lwt.winame=sst.winame

LEFT JOIN los_m_user m
    ON TO_CHAR(m.employee_id) = TO_CHAR(sst.ro_sanction_id)

/* Branch master */
LEFT JOIN los_m_branch lmb
    ON TO_CHAR(lmb.branchcode) = TO_CHAR(m.office_code);

-------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_APPENDIX_18" ("WINAME", "NAME", "AGE", "FATHER_NAME", "DESIGNATION", "ADDRESS", "LOAN_AVAIL_BRANCH", "LOAN_ACC_NO", "SANCTION_NUMBER", "SANCTION_DATE", "SANCTION_AMT", "SANCTION_AMT_WORD", "MARGIN", "ROI", "DAY", "MONTH", "YEAR", "CO_BORROWER_NAME", "CO_BORROWER_AGE", "CO_BORROWER_FATHER_NAME", "CO_BORROWER_ADDRESS", "EMI", "EMI_WORD", "EMI_FIRST_DATE", "PRINCIPAL_TENURE", "INTEREST_TENURE") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT

          ssht.winame AS winame,

          (NVL(ltc.CUSTOMERFIRSTNAME,'') ||' '||NVL(ltc.CUSTOMERMIDDLENAME,' ') ||' '|| NVL(ltc.CUSTOMERLASTNAME,'')) AS name,

          ssht.AGE AS age,

          ltc.FATHERNAME AS father_name,

          ssht.designation AS designation,

          (NVL(ltc.PERMADDRESS1,'') || NVL(ltc.PERMADDRESS2,'') || NVL(ltc.PERMADDRESS3,'')) AS address,

          ssht.branch_name AS loan_avail_branch,

          '_______________________' as loan_acc_no,

          '_______________________' as sanction_number,

          stl.SANCTION_DATE AS sanction_date,

          sst.APP_LOAN_AMT_VL AS sanction_amt,
          convert_to_indian_words(sst.APP_LOAN_AMT_VL) AS SANCTION_AMT_WORD,
          TO_CHAR(e.project_cost-sst.recommended_loan_amt) AS margin,

          'Presently @ 5.50 % simple up to Rs.40,00,000/- and @ 6.00% simple beyond Rs.40,00,000/-' AS roi,

           TO_CHAR(SYSDATE, 'DD') ||

            CASE
          WHEN TO_CHAR(SYSDATE, 'DD') IN ('11','12','13') THEN 'th'
          WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '1' THEN 'st'
          WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '2' THEN 'nd'
          WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '3' THEN 'rd'
          ELSE 'th'
          END AS day,

          INITCAP(TO_CHAR (SYSDATE, 'MONTH')) AS month,

          TO_CHAR(SYSDATE, 'YYYY') AS year,

          lnbi.FULLNAME AS co_borrower_name,

          lnbii.age AS co_borrower_age,

          lnbii.fathername AS co_borrower_father_name,

          nvl(lna.line1,'') || ' ' || nvl(lna.line2,'') || ' '  ||nvl(lna.line3,'') AS co_borrower_address,

          sst.mi_rec_vl as EMI,
          convert_to_indian_words(sst.mi_rec_vl) as EMI_WORD,

          (select TO_CHAR(TO_DATE(STARTDATE, 'YYYY-MM-DD HH24:MI:SS'), 'DD-MM-YYYY') from LOS_STG_CBS_AMM_SCH_DETAILS where PROCESSINSTANCEID=ssht.winame and PRINCIPAL <> 0 ORDER BY TO_NUMBER(INSTALLMENTNO) asc fetch first 1 row only
) AS EMI_FIRST_DATE,
          ssve.prin_repayment_tenure AS PRINCIPAL_TENURE,
          ssve.int_repayment_tenure AS INTEREST_TENURE

      FROM slos_staff_home_trn ssht

      LEFT JOIN slos_staff_trn sst ON sst.winame = ssht.winame

      LEFT JOIN SLOS_STAFF_VL_ELIGIBILITY ssve on ssve.winame=ssht.winame

      LEFT JOIN los_trn_customersummary ltc ON ssht.winame = ltc.winame

      LEFT JOIN slos_trn_loansummary stl ON stl.winame = sst.winame

      LEFT JOIN los_cam_collateral_details e ON e.pid = ssht.winame

      LEFT JOIN los_nl_basic_info lnbi ON lnbi.pid = ssht.winame  AND lnbi.Applicanttype<> 'B'

      LEFT JOIN LOS_L_BASIC_INFO_I lnbii on lnbii.f_key=lnbi.f_key

      LEFT JOIN LOS_NL_ADDRESS lna on lna.f_key=lnbi.f_key and lna.addresstype='P'

      LEFT JOIN los_m_branch lmb ON lmb.branchcode = LPAD(ssht.PRESENT_WORKING_DP_CODE, 5, '0');

-------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_APPENDIX_25" ("WINAME", "PRESENT_YEAR", "DAY", "MONTH", "STAFF_NAME", "AGE", "FATHER_NAME", "DESIGNATION", "ADDRESS", "CO_BORROWER_NAME", "CO_BORROWER_AGE", "CO_BORROWER_FATHER_NAME", "CO_BORROWER_ADDRESS", "BUILDER_NAME", "BUILDER_ADDRESS", "LOAN_AVAIL_BRANCH", "SANCTION_AMT", "PURCHASE_OF", "MANAGING_DIRECTOR", "PREMISES_NO", "POA_DATE", "BUILDER_BY", "PURCHASE", "DOC_NO", "DOC_NO_OF", "THE_BUILDER", "AGREEMENT_DATE", "SELL_SHARE", "PAID_BUILDER", "INTEREST", "EQUITABLE_MORTGAGE", "REGISTERED_SALEDEED", "SHAREOF", "ADVANCE_PAID", "ADVANCE_DATE", "BUILDER_AGREEMENT", "PLACE", "SANCTION_DATE") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT

    ssht.winame AS WINAME,
    EXTRACT(YEAR FROM SYSDATE) AS PRESENT_YEAR,
      TO_CHAR(SYSDATE, 'DD') ||

      CASE
    WHEN TO_CHAR(SYSDATE, 'DD') IN ('11','12','13') THEN 'th'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '1' THEN 'st'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '2' THEN 'nd'
    WHEN SUBSTR(TO_CHAR(SYSDATE, 'DD'), -1) = '3' THEN 'rd'
    ELSE 'th'
    END AS DAY,


    INITCAP(TO_CHAR(SYSDATE, 'MONTH')) AS MONTH,



    /* STAFF DETAILS */

   (NVL(ltc.CUSTOMERFIRSTNAME,'') ||' '||NVL(ltc.CUSTOMERMIDDLENAME,' ') ||' '|| NVL(ltc.CUSTOMERLASTNAME,'')) AS  STAFF_NAME,

    ssht.AGE as age,

    ltc.FATHERNAME as father_name,

    ssht.DESIGNATION as designation,

    NVL(ltc.PERMADDRESS1,'') || NVL(ltc.PERMADDRESS2,'') || NVL(ltc.PERMADDRESS3,'') AS ADDRESS,



    /* CO-BORROWER DETAILS */

    lnbi.FULLNAME AS CO_BORROWER_NAME,

      lnbii.age  AS CO_BORROWER_AGE,

    lnbii.fathername AS CO_BORROWER_FATHER_NAME,

    nvl(lna.line1,'') || ' ' || nvl(lna.line2,'') || ' '  ||nvl(lna.line3,'') AS CO_BORROWER_ADDRESS,



    /* BUILDER DETAILS (currently blank as per your query) */

    '_______________________' AS BUILDER_NAME,

    '_______________________' AS BUILDER_ADDRESS,



    /* LOAN DETAILS */
    ssht.branch_name AS LOAN_AVAIL_BRANCH,
    --lmb.branchname AS LOAN_AVAIL_BRANCH,

    sst.APP_LOAN_AMT_VL AS SANCTION_AMT,
    '_______________________' as PURCHASE_OF,
'_______________________' as MANAGING_DIRECTOR,
'_______________________' as PREMISES_NO,
'_______________________' as POA_DATE,
'_______________________' as BUILDER_BY,
'_______________________' as PURCHASE,
'_______________________' as DOC_NO,
'_______________________' as DOC_NO_OF,
'_______________________' as THE_BUILDER,
'_______________________' as AGREEMENT_DATE,
'_______________________' as SELL_SHARE,
'_______________________' as PAID_BUILDER,
'_______________________' as INTEREST,
'_______________________' as EQUITABLE_MORTGAGE,
'_______________________' as REGISTERED_SALEDEED,
'_______________________' as SHAREOF,
'_______________________' as ADVANCE_PAID,
'_______________________' as ADVANCE_DATE,
'_______________________' as BUILDER_AGREEMENT,
'_______________________' as PLACE,
stl.sanction_date as SANCTION_DATE



FROM slos_staff_home_trn ssht

LEFT JOIN slos_staff_trn sst

    ON sst.winame = ssht.winame

LEFT JOIN los_trn_customerSummary ltc

    ON ltc.winame = ssht.winame

LEFT JOIN los_nl_basic_info lnbi

    ON lnbi.pid = ssht.winame AND lnbi.Applicanttype<> 'B'

LEFT JOIN LOS_L_BASIC_INFO_I lnbii

     on lnbii.f_key=lnbi.f_key

LEFT JOIN LOS_NL_ADDRESS lna

     on lna.f_key=lnbi.f_key and lna.addresstype='P'

LEFT JOIN slos_trn_loanSummary stl

    ON stl.winame = ssht.winame

LEFT JOIN los_m_branch lmb

    ON lmb.branchcode = LPAD(sst.RO_SANCTION_ID, 5, '0');

-------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_APPENDIX_4" ("WINAME", "NAME_OF_THE_BORROWER", "AGE", "BORROWER_FATHER_NAME", "ADDRESS_OF_THE_BORROWER", "DESIGNATION", "PRESESNT_WORKING_BRANCH", "SANCTION_AMT", "ROI_SCHEDULED_CODE", "MONTHOFSANCTION", "DAYOFSANCTION", "YEAROFSANCTION", "NAME_OF_THE_CO_BORROWER", "AGE_CO_BORROWER", "FATHER_NAME_CO_BORROWER", "PREM_ADDRESS_CO_BORROWER", "SANCTION_DATE", "LOAN_PURPOSE", "LOAN_AVAIL_DATE", "APPLICATION_NO", "MARGIN", "LOCATION_BOUNDARIES", "PROP_DESC", "EXTENT_PROP", "APPROX_VAL", "STATE_NAME", "DESC_OF_PROP", "C_MONTH", "C_DAY", "C_YEAR", "LOAN_CITY", "NATURE_PROP", "LOAN_AVAIL_BRANCH_NAME") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT
     distinct ssht.winame AS winame,
     (NVL(ltc.CUSTOMERFIRSTNAME,'') ||' '|| NVL(ltc.CUSTOMERMIDDLENAME,'')||' '|| NVL(ltc.CUSTOMERLASTNAME,'')) AS name_of_the_borrower,
    NVL(ssht.AGE ,'_____') AS age,
     ltc.FATHERNAME AS borrower_father_name,
     (NVL(ltc.permaddress1,'') ||' '|| NVL(ltc.permaddress2,'')||' '|| NVL(ltc.permaddress3,'')) AS address_of_the_borrower,
     ssht.DESIGNATION AS designation,
     lmb.BRANCHNAME AS presesnt_working_branch,
     NVL(to_char(sst.APP_LOAN_AMT_VL), '______________') AS sanction_amt,
     --NVL(ssht.roi, '______') AS roi_scheduled_code,
     '5.50% for loan amt <=Rs.40 lakhs of overall EHL availed & 6.00%for loan amount >Rs.40 lakhs of overall EHL availed' AS roi_scheduled_code,
     NVL (TO_CHAR(SYSDATE,'MM'),'_______') AS monthofsanction,
     TO_CHAR(SYSDATE,'DD') AS dayofsanction,
     TO_CHAR(SYSDATE,'YY') AS yearofsanction,

     CASE WHEN lnbi.applicanttype='CB' THEN lnbi.fullname ELSE unistr('\004E\0041') END AS name_of_the_co_borrower,
     CASE WHEN lnbi.applicanttype='CB' THEN i.age END AS age_co_borrower,
     CASE WHEN lnbi.applicanttype='CB' THEN i.fathername ELSE unistr('\004E\0041') END AS father_name_co_borrower,

     NVL(
         NVL(b.line1,'') || NVL(b.line2,'') || NVL(b.line3,''),
         '_______________________'
     ) AS prem_address_co_borrower,

     TO_CHAR(SYSDATE,'DD-MM-YYYY') AS sanction_date,
     ssht.hl_purpose AS loan_purpose,
     TO_CHAR(wf.createddatetime,'DD-MM-YYYY') AS loan_avail_date,
     lwt.application_no AS application_no,
      TO_CHAR(lccd.project_cost-sst.recommended_loan_amt) AS margin,

     'LATITUDE- ' || lccd.latitude || ' LONGITUDE- ' || lccd.longitude ||
     ' N- ' || lccd.boundarynorth || ' S- ' || lccd.boundarysouth ||
     ' E- ' || lccd.boundaryeast || ' W- ' || lccd.boundarywest AS LOCATION_BOUNDARIES,

        U.COLLATERAL_SUBTYPE||', '||
    'Survey Number- '||lccd.SURVEY_MUNICIPAL_NUMBER||', '||lccd.BUILDING_WING||', '|| lccd.line1||', '||lccd.line2||', '||lccd.LINE3||', '||lccd.NEARESTLANDMARK||', '||lccd.ZIPCODE||', '||
    'BUILD-UP AREA - ' || lccd.builduparea || ', CARPET AREA - ' || lccd.carpetarea||', '||'LONGITUDE-' ||lccd.LONGITUDE||', LATITUDE-'||lccd.LATITUDE as PROP_DESC,
     lccd.builduparea AS EXTENT_PROP,
     lccd.marketvalueofplot AS APPROX_VAL,
     lmb.state AS state_name,
           U.COLLATERAL_SUBTYPE||', '||
    'Survey Number- '||lccd.SURVEY_MUNICIPAL_NUMBER||', '||lccd.BUILDING_WING||', '|| lccd.line1||', '||lccd.line2||', '||lccd.LINE3||', '||lccd.NEARESTLANDMARK||', '||lccd.ZIPCODE||', '||
    'BUILD-UP AREA - ' || lccd.builduparea || ', CARPET AREA - ' || lccd.carpetarea||', '||'LONGITUDE-' ||lccd.LONGITUDE||', LATITUDE-'||lccd.LATITUDE as DESC_OF_PROP,

    NVL(TO_CHAR(SYSDATE, 'Month'), '_______') AS C_MONTH,
    TO_CHAR(SYSDATE, 'FMDDth')                AS C_DAY,
    TO_CHAR(SYSDATE, 'YYYY')                  AS C_YEAR,

     (select CITY from los_m_branch where trim(branchname)=trim(ssht.branch_name))  AS loan_city,
     lccd.type_of_house AS nature_prop,
     ssht.branch_name AS LOAN_AVAIL_BRANCH_NAME

FROM slos_staff_home_trn ssht
LEFT JOIN slos_staff_trn sst ON sst.winame = ssht.winame
LEFT JOIN los_trn_customersummary ltc ON ssht.winame = ltc.winame
LEFT JOIN los_nl_basic_info lnbi ON lnbi.pid = ssht.winame
LEFT JOIN los_cam_collateral_details lccd ON ssht.winame = lccd.pid
LEFT JOIN wfinstrumenttable wf ON ssht.winame = wf.processinstanceid
LEFT JOIN los_wireference_table lwt ON lwt.winame = ltc.winame
LEFT JOIN los_l_basic_info_i i ON lnbi.f_key = i.f_key
LEFT JOIN los_m_hl_collateral u ON lccd.asset_subcategory = u.COLLATERAL_ID
LEFT JOIN los_nl_address b
    ON lnbi.f_key = b.f_key
   AND b.addresstype = 'P'
LEFT JOIN los_m_user m
    ON TO_CHAR(m.employee_id) = TO_CHAR(sst.ro_sanction_id)

/* Branch master */
LEFT JOIN los_m_branch lmb
    ON TO_CHAR(lmb.branchcode) = TO_CHAR(m.office_code)
WHERE ssht.winame LIKE '%SLOS%';

-----------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_ATT_EVD_COMMON" ("WINAME", "BORROWER_NAME", "BORROWER_DESIGNATON", "PROCESSING_BRANCH", "SANCTION_DATE", "GUARANTOR", "WITNESS_NAME", "WITNESS_ADDRESS", "HOME_BRANCH", "STAFF_ID", "PRODUCT_NAME", "APPLICANT_NAME") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  select
a.winame as WINAME,
a.name as BORROWER_NAME,
d.DESIGNATION as BORROWER_DESIGNATON,
--m.PRODUCTname as PRODUCT_NAME,
  CASE

        WHEN x.CPC_BRN_FUNC = 'End to End' THEN x.RAH_NAME

        ELSE x.BRANCHNAME

    END as PROCESSING_BRANCH,

TO_CHAR(SYSDATE,'DD-MM-YYYY') as SANCTION_DATE,
'_________________' as GUARANTOR,
'' as WITNESS_NAME,
'' as WITNESS_ADDRESS,
lmb.city AS HOME_BRANCH,
ssht.staff_number AS STAFF_ID,
ssht.hl_product as product_name,
ssht.name as APPLICANT_NAME
from slos_staff_home_trn a
left join los_nl_basic_info lnbi on a.winame=lnbi.pid
left join LOS_NL_DISBURSEMENT b on a.winame=b.pid
left join los_nl_occupation_info d on lnbi.f_Key=D.f_Key
LEFT JOIN Los_l_Sourcinginfo Q ON a.winame=Q.PID
left join los_nl_proposed_facility j on a.winame=j.pid
LEFT JOIN LOS_M_BRANCH X ON J.PROCESSING_BRANCH_CODE=X.BRANCHCODE
LEFT JOIN LOS_M_PRODUCT  m ON j.product=m.productcode
left join SLOS_STAFF_HOME_TRN ssht on ssht.winame=a.winame
left join LOS_M_BRANCH lmb on ssht.branch_name=lmb.branchname
where a.winame like '%SLOS%';

-------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_BRANCH_SANCTION" ("WINAME", "CUSTOMERID", "CUSTOMER_ID", "HL_PRODUCT", "HL_PRODUCT_CODE", "HL_PRODUCT_CODE_NAME", "HL_PURPOSE", "SANCTION_AMT", "MARGIN", "ROI", "TENURE", "PRIC_REPAY_MONTH", "INT_REPAY_MONTH", "SANCTION_AUTH_NAME", "SANCTION_AMT_ID", "SANCTION_AUTH_DESIGNATION", "DATE_OF_SANCTION", "STARTDATE", "MARGINPERCENTAGE", "FIRST_MONTHLY_INSTAL", "PURPOSE", "SANCTION_AMT_WORDS", "SECURITY_LOAN", "GUARANTEE_BY_USER", "REPAYMENT_HOLIDAY", "SCALE_DESC", "EMPLOYEE_ID", "REGIONAL_OFFICE", "APPLICATION_NO", "C_DATE", "APPLICANT_NAME", "PRODUCT_NAME", "APP_INT_TENURE_RO", "APP_PRIN_TENURE_RO", "APP_LOAN_TENURE_VL", "RATE_OF_INTEREST", "LAONAVAIL") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT

      distinct ssht.winame as winame ,
       ltc.CUSTOMERID as customerID,
        ltc.CUSTOMERID as customer_ID,
       ssht.HL_PRODUCT as HL_PRODUCT,
       ssht.HL_PRODUCT as  HL_PRODUCT_CODE,
       ---ssht.HL_PURPOSE as HL_PRODUCT_CODE_NAME,
       '' as HL_PRODUCT_CODE_NAME,
        ssht.HL_PURPOSE as HL_PURPOSE,
       SST.APP_LOAN_AMT_VL as sanction_amt,
        TO_CHAR(((lccd.project_cost-sst.recommended_loan_amt)/lccd.project_cost)*100,'FM999999990.00') || '%' as MARGIN,
       sve.rate_of_interest || '%' as ROI,
       sst.app_loan_tenure_vl as TENURE,
       sve.app_prin_tenure_ro as PRIC_REPAY_MONTH,
       sve.app_int_tenure_ro as INT_REPAY_MONTH,
       NVL(lmu.EMPLOYEE_FNAME,'') || NVL(lmu.EMPLOYEE_LNAME,'') AS sanction_auth_name,
       lmu.EMPLOYEE_ID as SANCTION_AMT_ID ,
       lmu.SCALE_DESC aS SANCTION_AUTH_DESIGNATION,
       TO_CHAR(SYSDATE,'DD-MM-YYYY') AS date_of_sanction,
       lcd.STARTDATE,
        TO_CHAR(((lccd.project_cost-sst.recommended_loan_amt)/lccd.project_cost)*100,'99.99') || '%' AS marginpercentage,
       sst.mi_rec_vl AS first_monthly_instal,
       ssht.hl_purpose AS purpose,
       convert_to_indian_words(sst.app_loan_amt_vl) AS sanction_amt_words,
          LCCD.FLATNO_HOUSENO || ','  || LCCD.FLOORNO || ','  ||LCCD.LINE1  ||','  || LCCD.LINE2  || ','  ||LCCD.LINE3  || ','  ||LCCD.ZIPCODE ||','  || LCCD.CITYTOWNVILLAGE
           ||','  || LCCD.DISTRICT  || ','  ||LCCD.STATE  AS security_loan,
       '' AS GUARANTEE_BY_USER,
 --convert_to_indian_words(sst.app_loan_amt_vl) AS SANCTION_AMT_WORDS,
       ssht.moratoriam AS repayment_holiday,
       '' AS SCALE_DESC,
       lmu.EMPLOYEE_ID AS EMPLOYEE_ID,
       lmb.BRANCHNAME  as REGIONAL_OFFICE,
       wf.APPLICATION_NO as APPLICATION_NO,
       TO_CHAR(sysdate,'DD-MM-YYYY') as C_DATE,
       ssht.name as APPLICANT_NAME,
      ssht.HL_PRODUCT as PRODUCT_NAME,
       sve.app_int_tenure_ro AS APP_INT_TENURE_RO,
       sve.app_prin_tenure_ro AS APP_PRIN_TENURE_RO,
       sst.app_loan_tenure_vl AS APP_LOAN_TENURE_VL,
       sst.roi AS RATE_OF_INTEREST,
       ssht.branch_name AS LAONAVAIL
  --ssht.HL_PURPOSE AS PURPOSE

 FROM slos_staff_home_trn ssht
 Left  JOIN  slos_staff_trn sst ON sst.winame = ssht.winame
 Left JOIN  los_cam_collateral_details lccd  ON lccd.pid = sst.winame
 Left JOIN los_trn_customersummary ltc ON ltc.winame = ssht.winame
 Left JOIN SLOS_STAFF_VL_ELIGIBILITY sve ON sve.winame = ssht.winame
 left join LOS_WIREFERENCE_TABLE wf on wf.winame=ssht.winame
 LEFT JOIN los_nl_basic_info i ON ssht.winame = i.pid
 LEFT JOIN los_cam_collateral_details lccd   ON ssht.winame = lccd.pid
 left JOIN  LOS_STG_CBS_AMM_SCH_DETAILS lcd ON lcd.ProcessInstanceId = ssht.winame
  AND lcd.stagenumber = '1' AND lcd.installmentno = '1'
 LEFT JOIN los_m_user lmu ON lmu.employee_id = sst.RO_SANCTION_ID
 ---LEFT JOIN los_m_branch lmb ON lmb.branchcode = LPAD(ssht.PRESENT_WORKING_DP_CODE, 5, '0')
 LEFT JOIN los_m_user m
    ON TO_CHAR(m.employee_id) = TO_CHAR(sst.ro_sanction_id)

/* Branch master */
LEFT JOIN los_m_branch lmb
    ON TO_CHAR(lmb.branchcode) = TO_CHAR(m.office_code)
 LEFT JOIN staff_hl_prod_des_matrix spdm ON spdm.SUB_PRODUCT = ssht.hl_product
  AND spdm.loan_purpose = ssht.hl_purpose
 AND spdm.designation = ssht.designation;

--------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_CUSTOMER_SANCTION_LETTER" ("WINAME", "APPLICANT_NAME", "SANCTION_DATE", "PRODUCT", "PRODUCT_CODE_NAME", "SANCTION_AMT", "SANCTION_AMT_WORD", "PURPOSE", "CUSTOMER_ID", "SECURITY", "LTV", "ROI", "TYPE_OF_ROI", "PRINCIPAL_REPAYMENT_PERIOD", "INT_REPAY_PERIOD", "EMI", "FIRST_INST_DATE", "EMI_WITH", "NO_OF_PRIN_MNT", "NO_OF_INT_MNT", "REPAYMENT_PERIOD_HOLIDAY", "TOTAL_FEE", "DATE_OF_SANCTION", "SURETY_AND_ADDRESS", "APPLICATION_NO", "C_DATE", "APPLICANT_TYPE", "REGIONAL_OFFICE") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT

    distinct ssht.winame AS winame,
    ssht.name as APPLICANT_NAME,
    stl.sanction_date AS sanction_date,
    SSHT.HL_PRODUCT AS product,
    ssht.hl_product AS product_code_name,
    sst.APP_LOAN_AMT_VL AS sanction_amt,
     convert_to_indian_words(sst.APP_LOAN_AMT_VL) AS sanction_amt_word,
    ssht.hl_purpose AS purpose,
    ltc.customerid as CUSTOMER_ID,
    LCCD.FLATNO_HOUSENO || ','  || LCCD.FLOORNO || ','  ||LCCD.LINE1  ||','  || LCCD.LINE2  || ','  ||LCCD.LINE3  || ','  ||LCCD.ZIPCODE ||','  || LCCD.CITYTOWNVILLAGE  ||','  || LCCD.DISTRICT  || ','  ||LCCD.STATE  AS security,
    TO_CHAR(((lccd.project_cost-sst.recommended_loan_amt)/lccd.project_cost)*100,'FM999999990.00') || '%' as LTV,
    ssve.rate_of_interest || '%' AS roi,
    'Floating' AS type_of_roi,
    ssve.APP_PRIN_TENURE_RO AS principal_repayment_period,
    ssve.APP_INT_TENURE_RO AS int_repay_period,
    sst.mi_rec_vl AS emi,
    --TO_CHAR((SELECT sch.startdate FROM los_stg_cbs_amm_sch_details sch WHERE sch.installmentno = '7' AND sch.processinstanceid = ssht.winame  AND ROWNUM = 1),'DD-MM-YYYY') AS first_inst_date,
     --(SELECT sch.startdate FROM los_stg_cbs_amm_sch_details sch WHERE sch.installmentno = SSHT.MORATORIAM + 1 AND sch.processinstanceid = ssht.winame  AND ROWNUM = 1) AS first_inst_date,
    TO_CHAR(TO_DATE(lcd.startdate,'YYYY-MM-DD HH24:MI:SS'),'DD-MM-YYYY') AS first_inst_date,
     '' AS emi_with,
    sst.APP_LOAN_TENURE_VL AS no_of_prin_mnt,
   (SELECT sch.principal FROM los_stg_cbs_amm_sch_details sch WHERE sch.installmentno = '1' AND sch.processinstanceid = ssht.winame AND ROWNUM = 1) AS no_of_int_mnt,
    SSHT.MORATORIAM AS repayment_period_holiday,
    '0.00' AS total_fee,
    stl.sanction_date AS date_of_sanction,
 -- case when (l.applicanttype='G') then l.fullname else 'NA' end as SURETY_AND_ADDRESS,
  '' as SURETY_AND_ADDRESS,
    lwt.application_no as APPLICATION_NO,
    TO_CHAR(sysdate,'DD-MM-YYYY') aS C_DATE,
   l.applicanttype as APPLICANT_TYPE,
    --lmb.BRANCHNAME  as REGIONAL_OFFICE
    ssht.branch_name as REGIONAL_OFFICE


FROM slos_staff_home_trn ssht
left JOIN SLOS_STAFF_VL_ELIGIBILITY ssve ON ssve.winame = ssht.winame
left JOIN los_wireference_table lwt ON lwt.winame = ssht.winame
left join los_nl_basic_info l on ssht.winame=l.pid
left JOIN slos_trn_loanSummary stl ON stl.winame = ssht.winame
left JOIN los_trn_customersummary ltc ON stl.winame = ssht.winame
left join los_cam_collateral_details lccd on ssht.winame=lccd.pid
left JOIN slos_staff_trn sst ON sst.winame = ssht.winame
LEFT JOIN los_m_branch lmb
    ON lmb.branchcode = LPAD(ssht.PRESENT_WORKING_DP_CODE, 5, '0')
LEFT JOIN LOS_STG_CBS_AMM_SCH_DETAILS lcd ON lcd.ProcessInstanceId = sst.winame AND lcd.installmentno = TO_CHAR(TO_NUMBER(ssht.moratoriam)+1)
where ssht.winame like 'SLOS%' and l.applicanttype='B';

---------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_KFS" ("WINAME", "APPLICATIONDATE", "CUSTOMERNAME", "CO_APPLICANT", "LOANREFERENCENUMBER", "ROI", "RLLR", "VARIANCE", "FINALRLLR", "PROCESSFEE", "TOTALCIC", "LOAN_AMOUNT", "EMI", "TENURE", "ROITYPE", "TOTAL_INTEREST_AMOUNT", "PAYFEE", "PAYREFEE", "PAYTPFEE", "NETDISBURSED", "TOTAL_AMOUNT_TO_BE_PAID", "APR", "FIRST_REPAYMENT_DATE", "EPI", "CIC_CHARGE", "NESL", "PRODUCT_NAME", "INSURANCE_CHARGE_REB", "INSP_CHARGE_REA", "LEGAL_CHARGE_REB", "CHARGE_SWITCHING", "DOCUMENTATION_CHARGE", "VALUATION_CHARGE", "MORTGAGE_CHARGE", "VETTING_CHARGE", "REPAYMENT_START_DATE", "STAFFNODALOFFICERNAME", "STAFFNODALOFFICERDESIGNATION", "STAFFNODALOFFICERCONTACTNO", "STAFFNODALOFFICEREMAIL") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT DISTINCT
   hm.winame AS winame,
       TO_CHAR(wf.INTRODUCTIONDATETIME, 'DD-MM-YYYY') AS applicationdate,

       hm.name AS customername,



       -- Co-Applicant Name

       (SELECT fullname

          FROM los_nl_basic_info

         WHERE applicanttype = 'CB'

           AND pid = b.pid

           AND ROWNUM = 1) AS co_applicant,



       i.applicationnumber AS loanreferencenumber,
        'Presently @ 5.50 % simple up to Rs.40,00,000/- and @ 6.00% simple beyond Rs.40,00,000/-' AS roi,
      -- g.rate_of_interest AS roi,

       '5.5' AS rllr,

       '0.5' AS variance,

       g.rate_of_interest AS finalrllr,



       'NA' AS processfee,



       -- Total CIC

       ROUND((SELECT SUM(amount)

                FROM los_nl_fee_charges

               WHERE (fees_description LIKE '%Cibil%'

                      OR fees_description LIKE '%Experian%'

                      OR fees_description LIKE '%Equifax%'

                      OR fees_description LIKE '%High%')

                 AND pid = b.pid), 2) AS totalcic,



       TO_CHAR(ROUND(o.app_loan_amt_vl, 2), '99,99,99,99,99,99,99,999') AS loan_amount,

       o.mi_rec_vl AS emi,



       o.app_loan_tenure_vl AS tenure,

       UPPER(g.roi_type) AS roitype,



       TO_CHAR(ROUND(getASTotInterest(b.pid), 2), '99,99,99,99,99,99,99,999') AS total_interest_amount,



       -- Payfee

       TO_CHAR(

         ROUND((SELECT SUM(amount)

                  FROM los_nl_fee_charges

                 WHERE (fees_description LIKE '%Cibil%'

                        OR fees_description LIKE '%Experian%'

                        OR fees_description LIKE '%Equifax%'

                        OR fees_description LIKE '%Crif%')

                   AND pid = b.pid), 2),

         '99,99,99,99,99,999'

       ) AS payfee,



        -- Payrefee

       '0.00' AS payrefee,



       -- Paytpfee

       TO_CHAR(

         ROUND((SELECT SUM(amount)

                  FROM los_nl_fee_charges

                 WHERE (fees_description LIKE '%Cibil%'

                        OR fees_description LIKE '%Experian%'

                        OR fees_description LIKE '%Equifax%'

                        OR fees_description LIKE '%Crif%')

                   AND pid = b.pid), 2),

         '99,99,99,99,99,99,99,999'

       ) AS paytpfee,



       -- Net Disbursed

       TO_CHAR(

         ROUND(

           o.app_loan_amt_vl -

           NVL((SELECT SUM(amount)

                  FROM los_nl_fee_charges

                 WHERE (fees_description LIKE '%Cibil%'

                        OR fees_description LIKE '%Experian%'

                        OR fees_description LIKE '%Equifax%'

                        OR fees_description LIKE '%Crif%')

                   AND pid = b.pid), 0),

           2

         ),

         '99,99,99,99,99,99,999'

       ) AS netdisbursed,



       TO_CHAR(

         ROUND(o.app_loan_amt_vl + ROUND(getASTotInterest(b.pid), 2), 2),

         '99,99,99,99,99,99,99,999'

       ) AS total_amount_to_be_paid,

      -- '' as APR,
      g.rate_of_interest as APR,

    --   TO_CHAR(TO_DATE(GETDATEFIRSTINSTALLMENT(b.pid), 'YYYYMMDD'), 'DD-MM-YYYY') AS first_repayment_date,

    TO_CHAR(TO_DATE(lcd.startdate,'YYYY-MM-DD HH24:MI:SS'),'DD-MM-YYYY') AS first_repayment_date,



       TO_CHAR(ROUND(FUN_GETEPICALC(b.pid), 2), '99,99,99,99,99,99,999') AS epi,



       -- CIC Charge

       ROUND((SELECT SUM(amount)

                FROM los_nl_fee_charges

               WHERE (fees_description LIKE '%Cibil%'

                      OR fees_description LIKE '%Experian%'

                      OR fees_description LIKE '%Equifax%'

                      OR fees_description LIKE '%High%')

                 AND pid = b.pid), 2) AS cic_charge,



       '0.00' AS nesl,
      hm.hl_product  as PRODUCT_NAME,

       '0.00' AS insurance_charge_reb,

       '0.00' AS insp_charge_rea,

       '0.00' AS legal_charge_reb,

       '0.00' AS charge_switching,

       '0.00' AS documentation_charge,

       '0.00' AS valuation_charge,

       '0.00' AS mortgage_charge,

       '0.00' AS vetting_charge,



       -- Repayment Start Date

  --     TO_CHAR(

  --       TO_DATE(

  --         (SELECT sch.startdate

 --             FROM los_stg_cbs_amm_sch_details sch

  --           WHERE sch.installmentno = '1'

  --             AND sch.processinstanceid = b.pid

  --             AND ROWNUM = 1),

  --         'YYYYMMDD'

  --       ),

  --       'DD-MM-YYYY'

  --     ) AS repayment_start_date,

  TO_CHAR(TO_DATE(lcd.startdate,'YYYY-MM-DD HH24:MI:SS'),'DD-MM-YYYY') AS repayment_start_date,

(select CONSTVALUE from los_mst_constants where consttype='STAFFLOAN' and CONSTNAME='NODALNAME')as STAFFNODALOFFICERNAME,
(select CONSTVALUE from los_mst_constants where consttype='STAFFLOAN' and CONSTNAME='NODALDESIGNATION')as STAFFNODALOFFICERDESIGNATION,
(select CONSTVALUE from los_mst_constants where consttype='STAFFLOAN' and CONSTNAME='NODALCONTACT')as STAFFNODALOFFICERCONTACTNO,
(select CONSTVALUE from los_mst_constants where consttype='STAFFLOAN' and CONSTNAME='NODALEMAIL')as STAFFNODALOFFICEREMAIL



FROM SLOS_STAFF_HOME_TRN hm

LEFT JOIN los_nl_basic_info b         ON hm.winame = b.pid

LEFT JOIN slos_staff_vl_eligibility g ON hm.winame = g.winame

LEFT JOIN los_ext_table i             ON hm.winame = i.pid

LEFT JOIN slos_staff_trn o            ON hm.winame = o.winame

LEFT JOIN wfinstrumenttable wf        ON wf.PROCESSINSTANCEID = o.winame
LEFT JOIN LOS_STG_CBS_AMM_SCH_DETAILS lcd ON lcd.ProcessInstanceId = hm.winame AND lcd.installmentno = TO_CHAR(TO_NUMBER(hm.moratoriam)+1)



WHERE hm.winame like 'SLOS%';

--------------------------------------------------------------------------------
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_NF_523" ("WINAME", "NAME_OF_APPLICANT", "DESIGNATION", "STAFF_NO", "FATHER_NAME", "DOB", "DATE_OF_JOIN", "WORKING_BRANCH", "PURPOSE", "REQ_AMT", "COLLATERAL_DETAILS", "APPLICATION_NO", "SANCTION_AMOUNT", "LOAN_AVAIL_BRANCH_CITY", "C_DATE", "EX_SERVICE", "JOINT_RIGHT", "JOINT_RIGHT_DETAILS", "HL_AVAILED_EARLIER", "HL_AVAILED_EARLIER_DETAILS", "NO_DPN_AVAILED", "DPN_AVAILED", "PRESENT_LIAB_AMT", "LIAB_CLEAR", "GROSS_SALARY", "TOTAL_DEDUCTION", "NTH_SALARY", "PERCENTAGE_NTH", "PLOT_SURVEY", "SURVEY_NO", "STREET", "VILLAGE", "DISTRICT", "EXTENT_PLOT", "FLAT_NO", "PURPOSE_ADDITIONAL", "SANCTION_AMT", "COLLATERAL_DET", "PROJECT_COST", "LOAN_AVAIL_BRANCH", "LOAN_RECOMMENDED") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  select distinct(ltc.winame) as winame,
  CASE
      WHEN sht.EX_STAFF_ID IS NULL THEN sht.NAME
     ELSE sht.EX_STAFF_NAME
  END as name_of_applicant,
   CASE
      WHEN sht.EX_STAFF_ID IS NULL THEN sht.designation
     ELSE sht.EX_STAFF_DESIGNATION
  END as designation,
  CASE
      WHEN sht.EX_STAFF_ID IS NULL THEN sht.STAFF_NUMBER
     ELSE sht.EX_STAFF_ID
 END as staff_no,
 ltc.FATHERNAME as father_name,
 TO_CHAR(sht.date_of_birth, 'DD-MM-YYYY') AS dob,
 TO_CHAR(sht.JOINING_DATE, 'DD-MM-YYYY') AS date_of_join,
 pwb.BRANCHNAME AS working_branch,
 hl_purpose as purpose,
 req_amt_tot_pld as REQ_AMT,
 cam.location_of_property as collateral_details,
 APPLICATION_NO as application_no,
 sve.final_elg_vl as sanction_amount,
 sht.availed_ehl as loan_avail_branch_city,
 TO_CHAR(sysdate,'DD-MM-YYYY') as C_DATE,
 SHT.EX_SERVICEMAN as EX_SERVICE,
 '' as JOINT_RIGHT,
 '' as JOINT_RIGHT_DETAILS,
 '' as HL_AVAILED_EARLIER,
 '' as HL_AVAILED_EARLIER_DETAILS,
 (SELECT COUNT(*) FROM SLOS_ALL_ACTIVE_PRODUCT sap WHERE sap.productcode = '701' AND sap.winame = sht.winame) AS  NO_DPN_AVAILED,
   CASE  WHEN EXISTS ( SELECT 1  FROM SLOS_ALL_ACTIVE_PRODUCT sap WHERE sap.winame = sht.winame AND sap.productcode = '701' ) THEN 'YES'  ELSE 'NO'
 END AS DPN_AVAILED,
 (select sum(OUTSTANDING_BALANCE) from SLOS_ALL_ACTIVE_PRODUCT where WINAME=sht.winame and PRODUCTCODE='701') as PRESENT_LIAB_AMT,
 '' as LIAB_CLEAR,
 sht.gross_salary as GROSS_SALARY,
 sht.total_ded as TOTAL_DEDUCTION,
 (sht.gross_salary-sht.total_ded)as NTH_SALARY,
 TO_CHAR((((sht.gross_salary-sht.total_ded)/sht.gross_salary)*100),99.99) as PERCENTAGE_NTH,
 cam.plot_number || '     ' as PLOT_SURVEY,
 cam.survey_municipal_number as SURVEY_NO,
 cam.street_name as STREET,
 cam.citytownvillage as VILLAGE,
 cam.district as DISTRICT,
 cam.builduparea as EXTENT_PLOT,
 cam.flatno_houseno as FLAT_NO,
 '' as PURPOSE_ADDITIONAL,
 sve.final_elg_vl as SANCTION_AMT,
 cam.line1 ||' '|| cam.line2 ||' '|| cam.line3 ||' '|| cam.district ||' '|| cam.state AS COLLATERAL_DET,
 cam.project_cost AS PROJECT_COST,
 sht.branch_name as LOAN_AVAIL_BRANCH,
  trn.recommended_loan_amt AS LOAN_RECOMMENDED
 from SLOS_STAFF_HOME_TRN sht
 LEFT JOIN LOS_CAM_COLLATERAL_DETAILS cam on sht.winame=cam.pid
 right join los_trn_customersummary ltc on ltc.winame =sht.winame
 left join slos_staff_trn trn on sht.winame=trn.winame
 right join LOS_CAM_COLLATERAL_DETAILS lcd on lcd.pid=sht.winame
 right join los_wireference_table lwt on lwt.winame=sht.winame
 right join SLOS_STAFF_VL_ELIGIBILITY sve on sve.winame=sht.winame
 left join SLOS_ALL_ACTIVE_PRODUCT saap on sht.winame=saap.winame
 LEFT JOIN los_m_branch pwb ON pwb.BRANCHCODE = LPAD(sht.present_working_dp_code, 5, '0') where sht.winame like 'SLOS%';

-------------------------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "CNBDLPAGRDEV"."VIW_DOCGEN_STAFF_HL_PROCESS_NOTE" ("WINAME", "NAME", "DOB", "AGE", "PRESENT_ADDRESS", "PREM_ADDRESS", "MOBILE_NO", "CUSTOMER_ID", "PRODUCT_CODE_AND_NAME", "APPLICATION_REF", "EMP_ID", "DESIGNAION", "DATE_OF_JOIN", "COMPLETED_YEARS", "DATE_OF_RETAIR", "REMAIN_SERVICE", "PRESENT_WORKING_BRANCH", "HR_DETAILS", "GROSS_SALARY", "TOTAL_DED", "LOAN_DEDUCTION", "MONTHLY_NET_SALARY", "PURPOSE_OF_LOAN", "ROI", "ROI_TYPE", "TOTAL_TENURE", "NTH_AMT", "MARGIN_AMT", "QUANTUM_LOAN", "ELG_LOAN_AMT", "APP_TEN", "APP_INT_TEN", "EMI_COMMENCE_DATE", "EMI_FIRST_AMT", "OCCUPATION", "EMAIL", "HOMEBRANCH", "PROCESS_NOTE_REF_NO", "SANCTION_DATE", "AVAIL_BRANCH", "NET_WORTH", "PROP_ADDRESS", "NAME_PANEL_ADV", "DATE_HANDLING_OVER", "DATE_RECEIPT", "ADV_REMARKS", "LEGAL_OPINION_DATE", "NAMEOFPANELVALUER", "TOTALPROPERTYVALUE", "VALUATION_DATE", "VALUATION_REMARKS", "PROP_INSPECTED_BY", "INSPECTION_DATE", "PROP_INSP_REMARKS", "DATEOFAGREEMENTSALEDEED", "CONSTRUCTION_YEAR", "RES_LIFE_PROP", "WHETHER_APPROVED_PLAN", "NAMEOFBUILDER_DEVELOPER", "BUILDER_APPROVAL", "STATUTORYCLEARANCES", "FACILITY", "LAND_COST", "CONSTRUCTION_COST", "REGISTRATION_CHARGE", "STAMP_DUTY_CHARGE", "DOCUMENT_CHARGE", "ADDITIONAL_COST_FIXTURE", "TOTAL_COST", "HOUSE_NO", "NTH_PERCENTAGE", "SECURITY_DETAILS", "PROPOSAL_TYPE", "FLAT_NO", "FLOOR_NO", "ADD_SECURITY", "ADDRESS_PROP", "BOUNDARYNORTH", "BOUNDARYSOUTH", "BOUNDARYEAST", "BOUNDARYWEST", "BUILD_UP_AREA", "MORTGAGE_REMARKS", "REMARKS", "NAME_OF_THE_RO", "RO_DESIGNATION", "RO_MOBILE", "SANCTION_AUTHORITY", "SANCTION_DESIGNATION", "SANCTION_MOBILE", "SANCTION_AUTHORITY_BRANCH", "PROPERTY_TYPE", "VALUATION_REMARKSPROP_INSPECTED_BY", "LEGAL_OPNION_DATE", "INCOME_PROOF", "PROJECT_COST", "SANCTION_AUTH_COMMENTS", "PRODUCT_CODE", "TOTAL_TEN", "PRODUCT_NAME", "SANCTION_AMT", "ROW_SPAN", "ELIGIBLE_AMT", "MORATORIAM", "QUANTUM_MARGIN", "MORATORIUM", "AVAILABLE_BALANCE", "SALARY_ACCOUNT_NUMBER", "BRANCH_DETAIL", "PAN", "AADHAR", "PASSPORT") DEFAULT COLLATION "USING_NLS_COMP"  AS 
  SELECT DISTINCT

    ssht.winame AS winame,

    NVL(ltcs.CUSTOMERFIRSTNAME,'') || ' ' ||

    NVL(ltcs.CUSTOMERMIDDLENAME,'') || ' ' ||

    NVL(ltcs.CUSTOMERLASTNAME,'') AS name,

    TO_CHAR(ssht.date_of_birth, 'DD-MM-YYYY') AS DOB,

    ssht.AGE AS age,

    NVL(ltcs.PERMADDRESS1,'') ||

    NVL(ltcs.PERMADDRESS2,'') ||

    NVL(ltcs.PERMADDRESS3,'') AS present_address,

    NVL(ltcs.PERMADDRESS1,'') ||

    NVL(ltcs.PERMADDRESS2,'') ||

    NVL(ltcs.PERMADDRESS3,'') AS prem_address,

    ltcs.MOBILENUMBER AS mobile_no,

    ltcs.CUSTOMERID AS customer_id,

    /* ---- Product Code + Name ---- */

    TRIM(shps.SUB_PRODUCT_CODE) || '-' || ssht.HL_PRODUCT AS product_code_and_name,

    lwt.APPLICATION_NO AS application_ref,

    ssht.STAFF_NUMBER AS emp_id,

    ssht.DESIGNATION AS designaion,

    TO_CHAR(ssht.JOINING_DATE,'DD-MM-YYYY') AS date_of_join,

    ssht.completed_years AS completed_years,

    TO_CHAR(ssht.RETIREMENT_DATE,'DD-MM-YYYY') AS date_of_retair,

    sst.remaining_service AS remain_service,

    lmb.BRANCHNAME AS present_working_branch,

    'YES' AS HR_DETAILS,

    ssht.GROSS_SALARY AS GROSS_SALARY,

    ssht.statutory_deductions AS TOTAL_DED,
    ssht.loan_deductions AS LOAN_DEDUCTION,

    TO_CHAR(ssht.NET_SALARY,'FM999999990.00') AS MONTHLY_NET_SALARY,

    ssht.HL_PURPOSE AS purpose_of_loan,

   'Presently @ 5.50 % simple up to Rs.40,00,000/- and @ 6.00% simple beyond Rs.40,00,000/-' AS roi,
    --ssve.rate_of_interest AS roi,

    'Floating'  AS roi_type,
    --ssht.ROI_TYPE AS roi_type,

    ---sst.app_loan_tenure_vl AS total_tenure,
    ssve.max_tenure AS TOTAL_TENURE,

    TO_CHAR((ssht.NET_SALARY-lcd.principal),'FM999999990.00') AS nth_amt,
  -- SSVE.ELG_PER_NTH AS nth_amt,

   TO_CHAR(((e.project_cost-sst.App_Loan_Amt_Vl)/e.project_cost)*100,'FM999999990.00') || '%' AS margin_amt,

   e.project_cost - sst.app_loan_amt_vl AS quantum_loan,

   --ssve.FINAL_ELG_VL AS elg_loan_amt,

 LEAST(TO_NUMBER(sst.recommended_loan_amt) , TO_NUMBER(SSVE.ELG_PER_NTH) ) AS elg_loan_amt,

    ssve.PRIN_REPAYMENT_TENURE AS app_ten,

   nvl(sst.app_loan_tenure_vl- ssht.moratoriam - ssve.prin_repayment_tenure, '') AS app_int_ten,

   -- TO_CHAR(TO_DATE(g.REPAYMENTDATE,'YYYYMMDD'),'DD-MM-YYYY') AS EMI_COMMENCE_DATE,
   TO_CHAR(TO_DATE(lcd.startdate,'YYYY-MM-DD HH24:MI:SS'),'DD-MM-YYYY') AS EMI_COMMENCE_DATE,

    ssve.prin_mon_inst AS emi_first_amt,

   'Canara Staff' AS OCCUPATION,

    ltcs.emailid AS EMAIL,

    --'' AS HR_DETAILS,

    lmb.BRANCHNAME AS HOMEBRANCH,

    ssht.winame AS process_note_ref_no,

    TO_CHAR(SYSDATE,'DD-MM-YYYY') AS sanction_date,

    ssht.branch_name AS avail_branch,

    nt.totoutstanding AS net_worth,

     e.BUILDING_WING || ', ' || e.line1 || ', ' || e.line2 || ', ' ||

    e.LINE3 || ', ' || e.NEARESTLANDMARK || ', ' || e.ZIPCODE AS prop_address,

    --prop.valuername AS name_panel_adv,
    legal.name AS name_panel_adv,

    legal.dateofhandingover AS date_handling_over,

    prop.completed_on AS date_receipt,

    '' AS adv_remarks,

    TO_CHAR(legal.dateoflegalopinion,'DD-MM-YYYY') AS legal_opinion_date,

   prop.valuername AS nameofpanelvaluer,

    e.totalpropertyvalue AS totalpropertyvalue,

    TO_CHAR(prop.dateofvaluation,'DD-MM-YYYY') AS valuation_date,

    prop.initiator_remarks AS valuation_remarks,

    '' AS prop_inspected_by,

    '' AS inspection_date,

    '' AS prop_insp_remarks,

   -- e.dateofagreement_saledeed AS dateofagreementsaledeed,
   TO_CHAR(TO_DATE(e.dateofagreement_saledeed,'YYYY-MM-DD HH24:MI:SS'),'DD-MM-YYYY') AS dateofagreementsaledeed,

    '' AS construction_year,

    e.RESIDUALLIFEOFPROPERTY AS RES_LIFE_PROP,

    e.APPROVEDPLAN_TAXRECEIPT AS WHETHER_APPROVED_PLAN,

    '' AS nameofbuilder_developer,

    '' AS builder_approval,

    '' AS statutoryclearances,

    ssht.hl_product AS facility,

    e.PLOT_COST AS LAND_COST,

    e.CONSTRUCTION_COST AS CONSTRUCTION_COST,

    '0.00' AS registration_charge,

    '0.00' AS stamp_duty_charge,

    '0.00' AS document_charge,

    '' AS additional_cost_fixture,

    e.project_cost AS total_cost,

    CASE when ssht.prop_under_ehl='0' THEN TO_CHAR('1')
      ELSE  TO_CHAR('2')
        END

    AS house_no,

    sst.nth_roc_vl AS nth_percentage,

    e.BUILDING_WING || ', ' || e.line1 || ', ' || e.line2 || ', ' ||

    e.LINE3 || ', ' || e.NEARESTLANDMARK || ', ' || e.ZIPCODE AS security_details,

    'NO' AS proposal_type,

    e.flatno_houseno AS FLAT_NO,

    e.floorno AS FLOOR_NO,

    '' AS add_security,

    e.BUILDING_WING || ', ' || e.line1 || ', ' || e.line2 || ', ' ||

    e.LINE3 || ', ' || e.NEARESTLANDMARK || ', ' || e.ZIPCODE AS ADDRESS_PROP,

    e.boundarynorth,

    e.boundarysouth,

    e.boundaryeast,

    e.boundarywest,

    e.builduparea AS BUILD_UP_AREA,

    '' AS mortgage_remarks,

    '' AS remarks,

  (select EMPLOYEE_FNAME from los_m_user where trim(EMPLOYEE_ID)=trim(nvl(sst.ro_maker_id, sst.co_maker_id))) AS name_of_the_ro,

    (select SCALE_DESC from los_m_user where trim(EMPLOYEE_ID)=trim(nvl(sst.ro_maker_id, sst.co_maker_id))) AS ro_designation,

    nvl(sst.ro_maker_id, sst.co_maker_id) AS ro_mobile,

    (select EMPLOYEE_FNAME from los_m_user where trim(EMPLOYEE_ID)=trim(nvl(sst.ro_sanction_id, sst.co_sanction_id))) AS sanction_authority,

    (select SCALE_DESC from los_m_user where trim(EMPLOYEE_ID)=trim(nvl(sst.ro_sanction_id, sst.co_sanction_id))) AS SANCTION_DESIGNATION,

    nvl(sst.ro_sanction_id, sst.co_sanction_id) AS SANCTION_MOBILE,

    '' AS sanction_authority_branch,

    e.asset_category AS property_type,

    ' ' AS valuation_remarksprop_inspected_by,

    legal.dateofhandingover AS legal_opnion_date,

    CASE

        WHEN ssht.ex_serviceman = 'No'

        THEN 'Salary Slip'

        ELSE 'Pension Order'

    END AS income_proof,

    e.project_cost AS project_cost,

    '' AS sanction_auth_comments,

     TRIM(shps.SUB_PRODUCT_CODE) AS PRODUCT_CODE,

   sst.app_loan_tenure_vl -ssht.moratoriam AS total_ten,

    ssht.HL_PRODUCT AS PRODUCT_NAME,

    sst.app_loan_amt_vl AS SANCTION_AMT,
          (SELECT COUNT(*) + 1 FROM slos_staff_home_trn x WHERE x.winame =lwt.winame) AS ROW_SPAN,
e.eligible_amount as ELIGIBLE_AMT,
ssht.moratoriam AS MORATORIAM,
TO_CHAR((e.project_cost-sst.app_loan_amt_vl),'FM999999990.00') AS QUANTUM_MARGIN,

ssht.moratoriam AS MORATORIUM,
nvl(ltcs.availablebalanace, '') as AVAILABLE_BALANCE,
nvl(ssht.salary_acc_number, '') as SALARY_ACCOUNT_NUMBER,
nvl(ltcas.homebranch, '') as BRANCH_DETAIL,
nvl(ltcs.pannumber, '') AS PAN,
nvl(ltcs.aadharrefno, '') as AADHAR,
nvl(ltcs.passportno, '') as PASSPORT

FROM slos_staff_home_trn ssht

LEFT JOIN los_trn_customersummary ltcs

    ON ssht.winame = ltcs.winame

LEFT JOIN LOS_T_CUSTOMER_ACCOUNT_SUMMARY ltcas

    ON ssht.winame = ltcas.winame

LEFT JOIN los_wireference_table lwt

    ON lwt.winame = ssht.winame

LEFT JOIN slos_staff_trn sst

    ON sst.winame = ssht.winame

LEFT JOIN los_nl_al_networth nt

    ON nt.pid = ssht.winame

LEFT JOIN SLOS_STAFF_VL_ELIGIBILITY ssve

    ON ssve.winame = ssht.winame

LEFT JOIN los_cam_collateral_details e

    ON e.pid = ssht.winame

LEFT JOIN los_nl_property_valuation prop

    ON prop.pid = ssht.winame

LEFT JOIN los_nl_agency_legal legal

    ON legal.pid = ssht.winame

--left join los_m_user lmu on lmu.employee_id =sst.RO_SANCTION_ID

LEFT JOIN los_m_branch lmb
 ON lmb.branchcode = LPAD(ssht.PRESENT_WORKING_DP_CODE, 5, '0')

--LEFT JOIN LOS_STG_CBS_AMM_SCH_DETAILS g

    --ON g.INSTALLMENTNO = '1'

   --AND g.PROCESSINSTANCEID = sst.winame

   LEFT JOIN LOS_STG_CBS_AMM_SCH_DETAILS lcd ON lcd.ProcessInstanceId = sst.winame AND lcd.installmentno = TO_CHAR(TO_NUMBER(ssht.moratoriam)+1)

LEFT JOIN SLOS_HOME_PRODUCT_SHEET shps

    ON TRIM(shps.SUB_PRODUCT) = TRIM(ssht.HL_PRODUCT);

----------------------------------------------------------------

