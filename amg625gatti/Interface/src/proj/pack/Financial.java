package proj.pack;

import java.sql.*;
import java.util.*;

public class Financial {
    /*
     * param: Scanner
     * return: int
     * This method takes in a scanner paramether and uses it to prompt the user to
     * navigate to an operation on the property manager interface
     */
    public static int financialMenu(Scanner scan) {
        while (true) {
            // Prints out the menu to choose an operation
            System.out.println("Please choose what operation you would like to do: ");
            System.out.println("(1) View tenants with payments due");
            System.out.println("(2) Financial report by property");
            System.out.println("(3) Company Financial Report");
            System.out.println("(4) Get payment history"); // IMPLEMENT THIS
            System.out.println("(5) Exit");

            int response;
            // Checks if the response is valid
            if (scan.hasNextInt()) {
                response = scan.nextInt();
                if (response > 0 && response < 6) {
                    return response;
                }
            }
            System.out.println("Please enter an integer between 1 and 6.");
            scan.nextLine();
        }
    }

    public static void viewTenantsWithPayments(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String unpaidTenantQuery = "select name, balance from tenant where balance > 0";
            ResultSet result = stmt.executeQuery(unpaidTenantQuery);
            if (!result.next()) {
                System.out.println("No outstanding payments.");
            } else {
                System.out.println("Tenants with rent due:");
                System.out.printf("%-20s%-15s\n", "Name", "Rent");
                while (result.next()) {
                    String name = result.getString("name");
                    double rent = result.getDouble("balance");
                    System.out.printf("%-20s $%-15.2f\n", name, rent);
                }
                System.out.println("--------------------------------");
                System.out.println("Total outstanding rent: " + getTotalRentDue(conn));
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid tenant id.");
        }
    }

    public static double getTotalRentDue(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select sum(balance) from tenant join lease_tenant on tenant.tenant_id = lease_tenant.tenant_id";
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid property id");
            } else {
                return result.getDouble("sum(balance)");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    public static void generatePropretyReport(Connection conn, int propertyId) {
        System.out.println("------------------------------------");
        System.out.println("Property id: " + propertyId);
        System.out.println("Address: " + PropertyManager.getAddressById(conn, propertyId));
        System.out.println();
        try (Statement stmt = conn.createStatement()) {
            String aptQuery = "select * from apartment where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(aptQuery);
            if (!result.next()) {
                System.out.println("No apartments found");
                System.out.println();
            } else {
                System.out.printf("%-15s %-10s %-12s %-13s %-9s%n", "Room number:", "Area:", "Bedrooms:", "Bathrooms:",
                        "Occupants:");
                do {
                    int roomNum = result.getInt("room_number");
                    double area = result.getDouble("area");
                    int bedrooms = result.getInt("bedrooms");
                    double bathrooms = result.getDouble("bathrooms");
                    int occupants = PropertyManager.getAptOccupants(conn, propertyId, roomNum);
                    System.out.printf("%-15d %-10.1f %-12d %-13.1f %-9d%n", roomNum, area, bedrooms, bathrooms,
                            occupants);
                } while (result.next());
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        try (Statement stmt = conn.createStatement()) {
            String amenQuery = "select public_amenity.name, public_amenity.price, public_amenity.hours from public_amenity join building_amenity on public_amenity.name=building_amenity.name where property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(amenQuery);
            if (!result.next()) {
                System.out.println("No amenities found");
                System.out.println();
            } else {
                System.out.println("Building Amenities:");
                System.out.printf("%-20s %-10s %-15s%n", "Name", "Price", "Hours");
                while (result.next()) {
                    String name = result.getString("name");
                    double price = result.getDouble("price");
                    String hours = result.getString("hours");

                    System.out.printf("%-20s $%-9.2f %-15s%n", name, price, hours);
                }
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        PropertyManager.getAvailableRooms(conn, propertyId);
        try (Statement stmt = conn.createStatement()) {
            String petQuery = "select pet_friendly from property where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(petQuery);
            if (!result.next()) {
                System.out.println("Property not found");
            } else {
                if (result.getInt("pet_friendly") == 1) {
                    System.out.println();
                    System.out.println("Pet friendly");
                    System.out.println();
                } else {
                    System.out.println();
                    System.out.println("Not Pet friendly");
                    System.out.println();
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        try (Statement stmt = conn.createStatement()) {
            String petQuery = "select pet.tenant_id, name, species, weight from pet, resident where pet.tenant_id=resident.tenant_id and property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(petQuery);
            if (!result.next()) {
            } else {
                System.out.println("Building Pets");
                System.out.printf("%-10s %-20s %-15s %-10s%n", "Tenant id:", "Name:", "Species:", "Weight:");
                do {
                    int tenantId = result.getInt("tenant_id");
                    String name = result.getString("name");
                    String species = result.getString("species");
                    double weight = result.getDouble("weight");
                    System.out.printf("%-10d %-20s %-15s %-10.2f%n", tenantId, name, species, weight);
                } while (result.next());
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        try (Statement stmt = conn.createStatement()) {
            String roomQuery = "select count(room_number) from property join apartment on property.property_id = apartment.property_id where property.property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(roomQuery);
            if (!result.next()) {
                System.out.println("No apartments found");
            } else {
                int numApts = result.getInt("count(room_number)");
                System.out.println("Total apartments: " + numApts);
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        try (Statement stmt = conn.createStatement()) {
            String tenantQuery = "select count(tenant_id) as occupants from lease_tenant join lease on lease.lease_id = lease_tenant.lease_id and property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(tenantQuery);
            if (!result.next()) {
                System.out.println("No tenants found");
            } else {
                int numTenants = result.getInt("occupants");
                System.out.println("Total tenants: " + numTenants);
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        double rent = getPropertyRent(conn, propertyId);
        System.out.println("Total income from rent: $" + rent);
        System.out.println();
        double subs = getPropertySubscriptions(conn, propertyId);
        System.out.println("Total income from amenities: $" + subs);
        System.out.println();
        System.out.println("Total revenue: $" + (rent+subs));
        System.out.println("------------------------------------");
    }

    public static void getPaymentHistory(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String paymentQuery = "select * from payment";
            ResultSet result = stmt.executeQuery(paymentQuery);
            if (!result.next()) {
                System.out.println("No past payments.");
            } else {
                System.out.printf("%-15s%-15s%-15s%-15s%-15s%-15s\n", "Payment id:", "Tenant id:", "Lease id:", "Amount:", "Date:", "Type:");
                do {
                    int pId = result.getInt("payment_id");
                    int tId = result.getInt("tenant_id");
                    int lId = result.getInt("lease_id");
                    double amt = result.getDouble("amount");
                    String date = result.getString("payment_date");
                    String type = result.getString("type");

                    System.out.printf("%-15d%-15d%-15d%-15.2f%-15s%-15s\n", pId, tId, lId, amt, date, type);
                } while (result.next());
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
    }

    public static double getPropertyRent(Connection conn, int propertyId){
        double totalRent = 0;
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select rent from lease join lease_tenant on lease.lease_id=lease_tenant.lease_id where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
            } else {
                while (result.next()) {
                    totalRent += result.getDouble("rent");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return totalRent;
    }

    public static double getPropertySubscriptions(Connection conn, int propertyId){
        double totalSubs = 0;
        try (Statement stmt = conn.createStatement()) {
            String subQuery = "select subscription_fee from tenant join resident on tenant.tenant_id=resident.tenant_id where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(subQuery);
            if (!result.next()) {
            } else {
                while (result.next()) {
                    totalSubs += result.getDouble("subscription_fee");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return totalSubs;
    }

    public static void generateCompanyFinancials(Connection conn) {
        System.out.println("------------------------------------");
        int[] totals = Financial.getAptsByProperty(conn);
        System.out.println("Total number or properties " + totals[0]);
        System.out.println("Total number or apartments: " + totals[1]);
        System.out.println("Total number of tenants: " + totals[2]);
        System.out.println("Total company revenue: $" + totals[3]);
        System.out.println("------------------------------------");
    }

    public static int[] getAptsByProperty(Connection conn) {
        int[] totals = new int[4];
        try (Statement stmt = conn.createStatement()) {
            String amenityQuery = "select property_id, count(room_number) as numApts from apartment group by property_id";
            ResultSet result = stmt.executeQuery(amenityQuery);
            if (!result.next()) {
                System.out.println("No properties found.");
            } else {
                System.out.println("Property id:    Number of apartments:    Tenants in Building:   Revenue:");
                do {
                    int propertyId = result.getInt("property_id");
                    totals[0] += 1;
                    int tensInBuilding = Company.getTenantsInBuilding(conn, propertyId);
                    totals[2] += tensInBuilding;
                    int numApts = result.getInt("numApts");
                    totals[1] += numApts;
                    double rent = getPropertyRent(conn, propertyId);
                    double subs = getPropertySubscriptions(conn, propertyId);
                    double revenue = rent + subs;
                    totals[3] += revenue;
                    
                    System.out.printf("%-15s %-25s %-20s %-20s%n", propertyId, numApts, tensInBuilding, revenue);
                } while (result.next());
                System.out.println();
                return totals;
            }
        } catch (SQLException se) {
            System.out.println("No properties found.");
        }
        return totals;
    }

}
