---
id: business-profile
title: Business Profile Setup
sidebar_position: 3
---

# Business Profile Setup

Before you can file 1099s, you need to set up your business profile. This information appears on every form you submit to the IRS.

## Required information

### Business details
- **Legal business name** -- as registered with the IRS
- **EIN (Employer Identification Number)** -- format: XX-XXXXXXX
- **Business type** -- sole proprietorship, LLC, corporation, etc.
- **Business phone number**

### Physical address
Your business must have a physical street address on file. **PO Boxes are not accepted** by the IRS for this purpose.

- Street address
- City, State, ZIP code

### Mailing address (optional)
If your mailing address is different from your physical address, check the box and enter it separately.

## Setting up your profile

1. After logging in, you'll see a yellow banner: **"Complete your business profile"**
2. Click on it, or go to **Business Profile** from the dashboard
3. Fill in all required fields (marked with *)
4. Click **Complete Setup**

## EIN security

Your EIN is **encrypted with AES-256** before being stored. It is never displayed in full -- you'll only see a masked version like `**-***6789`.

When editing your profile, you'll need to re-enter your EIN since we don't store or display it in plain text.

## IRIS credentials (optional)

If you already have an IRS IRIS Transmitter Control Code (TCC), you can enter it here:

- **TCC** -- 5-character code starting with 'D' (e.g., DTEST)
- **IRIS API Client ID** -- from your IRS API Client ID Application

If you don't have these yet, you can skip this section. See [IRS IRIS Overview](/irs-overview) for how to apply.

## Next step

Once your profile is set up, you're ready to [file your first 1099-NEC](/filing-1099-nec).
