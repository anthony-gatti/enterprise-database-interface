# Rental Company Interface
Author: Anthony Gatti

## Overview
This program is intended to simulate all necessary interfaces for an apartment rental company. It includes four interfaces: tenant, property manager, company manager, and financial manager.
* The tenant interface is intended to be used by a tenant who is a current resident of one of the company's apartments. In this interface, the tenat has the ability to check if they have any payments due, make payments, manage opt-in amenities, and view their payment history. Managing the opt-in amenities in includes the ability to view, add, and cancel subscriptions to the public amenities that the property offers.
* The property manager interface is intended to be used by a property manager employed by the company. It has the ability to record visit data, record lease data, record a move-out, add a person or pet to a lease, charge rent, generate property reports, and get data on a specific apartment. Recording visit data involves choosing or creating a new prospective tenant and entering the details of the tour they went on. Recording lease data is intended for adding a new lease to an apartment once the current lease expires. When apartments are created they all have leases with null end dates, so once the first tenant moves in, the end date is set. The rest of the menu option actions are clear from their names. Adding a pet is only possible if the building is pet friendly. 
* The company manager interface is intended to be used by the manager of the company. This interface has the ability to view properties, add new properties, view all property reports, and view a full company report. The 'view properties' option then allows the user to select one of the properties to generate a report on it. Adding new properties generates an amount of apartments specified by the user, and fills data into all related tables. Viewing all property reports prints a property report for each property held by the company. A full company report provides aggregate data about the whole company. 
* The financial manager interface is used to generate aggreate financial data about the company. It has the ability to view tenants with payments due, generate financial reports by property, generate a financial report for the entire company, and get payment history.

## Assumptions
* The minimum rent is 500 and the maximum is 2000
* Pets can only be up to 120 pounds
* A property cannot have less than 3 or more than 20 apartments
* Apartments can have 1-4 bedrooms
* Apartments can have 1-3.5 bathrooms
* All lease end dates are set 6 months from the day a tenant is added to the lease
* There is one lease per apartment, not one lease per tenant. Rent is evenly divided amongst the tenants
* The security deposit is automatically added as a payment due for the first tenant added to a least. It is up to the tenants to divide that deposit evenly. 
* All utilities are included in rent

## Sample Ids
Most menus list acceptable responses, but I will provide some examples.
* Tenants 20 and 23 have amenities registed to them
* Tenant 19 has payments to view in payment history
* Tenants 13 and 80 have payments due. 
* Properties 33, 35, 37, and 39 are pet friendly
* Tenants 73 and 23 have pets

### Compilation Instructions

Run commands:
javac src/proj/pack/*.java
jar cfmv Project.jar Manifest.txt -C src .
java -jar Project.jar