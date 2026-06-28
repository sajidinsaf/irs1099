---
id: irs-overview
title: IRS IRIS Overview
sidebar_position: 7
---

# IRS IRIS Overview

## What is IRIS?

IRIS (Information Returns Intake System) is the IRS system for electronic filing of information returns like 1099 forms. Our platform uses the IRIS **A2A (Application to Application)** channel to submit your forms directly to the IRS.

## Do I need to register with the IRS?

**If you're using our platform**, we handle the technical submission for you. However, to file under your own Transmitter Control Code (TCC), you'll need:

1. **IRS e-Services account** -- register at [IRS.gov](https://www.irs.gov)
2. **IRIS TCC** -- apply through the IRIS Application for TCC
3. **API Client ID** -- for A2A authentication

:::info
The TCC application process can take **up to 45 days**. We recommend applying early.
:::

## Applying for a TCC

1. Go to the IRS IRIS TCC application page
2. Sign in with your IRS e-Services account
3. Select **Individual** on the organization page
4. Click **New Application** and select **IRIS Application for TCC**
5. Complete the application with your business details
6. Wait for approval (up to 45 days)

Your TCC will be a 5-character code starting with **'D'** (e.g., DTEST).

## Supported form types

The IRS IRIS system supports over 30 form types for Tax Year 2025, including:

- All 1099 forms (NEC, MISC, INT, DIV, K, R, and more)
- Forms 1042-S, 1097-BTC, 1098 series
- Forms 3921, 3922, 5498 series
- Form W-2G
- Form 8809 (Extension of Time)

## Important dates

| Form | Due to Recipients | Due to IRS (electronic) |
|------|-------------------|------------------------|
| 1099-NEC | January 31 | January 31 |
| 1099-MISC | January 31 | March 31 |
| 1099-INT | January 31 | March 31 |
| 1099-DIV | January 31 | March 31 |

## Record retention

The IRS requires you to keep copies of information returns for:
- **3 years** from the reporting due date (general rule)
- **4 years** for returns reporting federal withholding
- **4 years** for Form 1099-C (Cancellation of Debt)

## More information

- [IRS IRIS Program webpage](https://www.irs.gov/iris)
- [IRS Publication 5718](https://www.irs.gov/pub/irs-pdf/p5718.pdf) -- IRIS A2A Specifications
- [IRS Publication 4557](https://www.irs.gov/pub/irs-pdf/p4557.pdf) -- Safeguarding Taxpayer Data
