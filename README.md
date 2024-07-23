# Enterprise Database Interface

## Overview

This project simulates essential interfaces for an apartment rental company, including tenant, property manager, company manager, and financial manager functionalities.

- **Tenant Interface:** Allows tenants to check and make payments, manage opt-in amenities, and view payment history.
- **Property Manager Interface:** Enables property managers to record visits, leases, move-outs, add people or pets to leases, charge rent, generate reports, and access apartment data.
- **Company Manager Interface:** Provides company managers with the ability to view/add properties, generate property reports, and view comprehensive company data.
- **Financial Manager Interface:** Facilitates financial managers in generating financial reports, viewing tenants with due payments, and accessing payment histories.

## Assumptions

- Rent ranges from $500 to $2000.
- Pets up to 120 pounds are allowed.
- Properties have 3-20 apartments, with 1-4 bedrooms and 1-3.5 bathrooms.
- Lease end dates are set 6 months from tenant addition.
- Each apartment has one lease, with rent divided among tenants.
- Security deposits are added as payments due for the first tenant.
- All utilities are included in the rent.

## Sample IDs

- Tenants 20 and 23 have registered amenities.
- Tenant 19 has payments in their payment history.
- Tenants 13 and 80 have payments due.
- Properties 33, 35, 37, and 39 are pet-friendly.
- Tenants 73 and 23 have pets.

## Compilation Instructions

To compile and run the program:

```bash
javac src/proj/pack/*.java
jar cfmv Project.jar Manifest.txt -C src .
java -jar Project.jar