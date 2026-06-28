---
id: filing-1099-nec
title: Filing 1099-NEC
sidebar_position: 4
---

# Filing 1099-NEC

Form 1099-NEC (Nonemployee Compensation) is used to report payments of $600 or more made to non-employees, such as independent contractors, freelancers, and self-employed individuals.

## When to file

- **To the IRS**: By **March 31** (electronic filing)
- **To recipients**: By **January 31**

## Step 1: Create a new filing

1. From your dashboard, click **New Filing**
2. Select **1099-NEC** as the form type
3. Choose the **Tax Year** (e.g., 2025)
4. Click **Create Submission**

This creates a draft submission where you'll add your recipient records.

## Step 2: Add recipients

Click **Add Recipient** to add a 1099-NEC record. You'll need to enter:

### Payer information (your business)
- **Payer's EIN** -- format: XX-XXXXXXX
- **Payer's name** -- your business name
- **Payer's address** -- street, city, state, ZIP

### Recipient information (the person/business you paid)
- **TIN type** -- SSN, EIN, ITIN, or ATIN
- **Recipient's TIN** -- their tax identification number
- **Name** -- first name and last name (for individuals) or business name
- **Address** -- street, city, state, ZIP
- **Account number** (optional) -- your internal reference number

### 1099-NEC amounts

| Box | Description | Required? |
|-----|-------------|-----------|
| **Box 1** | Nonemployee compensation | Yes |
| **Box 2** | Payer made direct sales of $5,000+ | No (checkbox) |
| **Box 4** | Federal income tax withheld | No |
| **Box 5** | State tax withheld | No |
| **Box 6** | State/payer's state number | No |
| **Box 7** | State income | No |

:::info
**Box 1** is the main field -- enter the total amount you paid to this recipient during the tax year. At least Box 1 or Box 4 must have a value.
:::

## Step 3: Review your records

After adding recipients, review the list on the submission page. Each record shows:
- Recipient name and masked TIN
- Address
- Box 1 amount and any tax withheld

You can **delete** records that have errors and re-add them.

## Step 4: Validate

Click **Validate** to check your submission before sending. The system checks:
- Your business profile is complete
- TCC is configured
- All required fields are filled
- EIN/TIN formats are correct

Fix any errors shown before proceeding.

## Step 5: Submit to IRS

Click **Submit to IRS** to file your 1099s. The system will:
1. Generate the IRS-formatted XML
2. Authenticate with the IRS system
3. Submit your transmission
4. Return a **Receipt ID** -- save this for your records

## Step 6: Track status

After submission, click **Check Status** to see if the IRS has processed your filing:

| Status | Meaning |
|--------|---------|
| **Processing** | IRS is reviewing your submission |
| **Accepted** | All records accepted successfully |
| **Accepted with Errors** | Some records accepted, some had issues |
| **Rejected** | Submission was rejected -- check error details |

## IRS name rules

The IRS has strict rules for names in 1099 forms:
- Person names can only contain **letters, numbers, spaces, and hyphens**
- No apostrophes (O'Malley must be entered as OMalley)
- No special characters (#, @, &, etc.)

Our system automatically cleans names to meet IRS requirements.

## Next step

Learn about [payment options](/payments) to complete your filing.
