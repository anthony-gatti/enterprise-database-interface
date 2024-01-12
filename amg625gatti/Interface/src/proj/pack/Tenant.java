package proj.pack;

import java.sql.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tenant {
    /*
     * param: Scanner
     * return: int
     * This method takes in a scanner paramether and uses it to prompt the user to
     * navigate to an operation on the tenant interface
     */
    public static int tenantMenu(Scanner scan) {
        while (true) {
            // Prints out the menu to choose an interface
            System.out.println("Please choose what operation you would like to do: ");
            System.out.println("(1) Check Payment Status");
            System.out.println("(2) Make Rental Payment");
            System.out.println("(3) Manage Amenities");
            System.out.println("(4) View payment history");
            System.out.println("(5) Exit");

            scan.nextLine();
            while (true) {
                System.out.println("Select an option from the menu:");
                try {
                    int choice = scan.nextInt();
                    if (choice == 1) {
                        return 1;
                    } else if (choice == 2) {
                        return 2;
                    } else if (choice == 3) {
                        return 3;
                    } else if (choice == 4) {
                        return 4;
                    } else if (choice == 5) {
                        return 5;
                    }
                } catch (InputMismatchException e) {
                    System.out.println("invalid response.");
                    scan.next();
                } catch (NoSuchElementException e){
                    System.out.println("Have a good day!");
                    System.exit(99);
                }
            }
        }
    }

    /*
     * param: Scanner, Connection
     * return: int
     * This method takes in a scanner and a database connection paramether and them
     * to prompt the user for a tenant id until a valid one is provided.
     */
    public static int getTenantId(Scanner scan, Connection conn) {
        while (true) {
            System.out.println("Please enter your unique tenant id or '0' to exit: ");
            if (scan.hasNextInt()) {
                int response = scan.nextInt();
                if (response == 0) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                // Performs a query to check if the id matches a tenant in the db
                try (Statement stmt = conn.createStatement()) {
                    String idCheckQuery = "select count(*) as tenant from tenant where tenant_id = " + response;
                    ResultSet result = stmt.executeQuery(idCheckQuery);
                    if (result.next()) {
                        if (result.getInt("tenant") == 1) {
                            System.out.println("Tenant found.");
                            return response;
                        }
                    }
                } catch (SQLException se) {
                    System.out.println("Invalid tenant id.");
                    break;
                }
            }
            scan.nextLine();
        }
        return 0;
    }

    /*
     * param: Scanner, Connection, id
     * return: void
     * This method takes in a scanner, a database connection, and a tenant id
     * parameter and uses them
     * to get the outstanding balance of the signed in tenant.
     */
    public static double getTenantBalance(Scanner scan, Connection conn, int id) {
        System.out.print("Current outstanding balance: $");
        // Performs a query to check if the id matches a tenant in the db
        try (Statement stmt = conn.createStatement()) {
            String balanceQuery = "select balance from tenant where tenant_id = " + id;
            ResultSet result = stmt.executeQuery(balanceQuery);
            if (result.next()) {
                double balance = result.getInt("balance");
                System.out.println(balance);
                return balance;
            }
        } catch (SQLException se) {
            System.out.println("Invalid tenant id.");
        }
        return 0;
    }

    /*
     * param: Scanner, Connection
     * return: int
     * This method takes in a scanner and a database connection paramether and them
     * to prompt the user for a tenant id until a valid one is provided.
     */
    public static void makePayment(Scanner scan, Connection conn, int id, double balance) {
        double payment;
        while (true) {
            System.out.println("Enter the amount you would like to pay: ");
            try {
                payment = scan.nextDouble();
                if (payment <= 0 || payment > balance) {
                    System.out.println("Please enter a positive amount less than $" + balance);
                } else {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a positive numeric amount.");
                scan.next();
            }
        }
        int choice;
        while (true) {
            System.out.println("Please select a payment method:");
            System.out.println("(1) Credit");
            System.out.println("(2) Debit");
            System.out.println("(3) Cash");
            System.out.println("(4) Cancel");
            try {
                choice = scan.nextInt();
                if (choice > 0 && choice < 5) {
                    break;
                } else {
                    System.out.println("Please enter an integer between 1 and 4.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter an integer between 1 and 4.");
                scan.next();
            }
        }
        switch (choice) {
            case 1:
                getCardInfo();
                insertPayment(conn, id, payment, "Credit");
                break;
            case 2:
                getCardInfo();
                insertPayment(conn, id, payment, "Debit");
                break;
            case 3:
                System.out.println(
                        "Please bring you payment to the front office and it will be processed in 1-2 business days");
                break;
            case 4:
                break;
        }
    }

    public static void insertPayment(Connection conn, int id, double amount, String type) {
        try (Statement stmt = conn.createStatement()) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String date = currentDateTime.format(formatter);
            int leaseId = getLeaseId(conn, id);
            String insert = "insert into payment (tenant_id, lease_id, amount, payment_date, type) values (" + id + ", " + leaseId + ", " + amount + ", '" + date + "', '" +type
                    + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Payment successful.");
            } else {
                System.out.println("Payment failed.");
            }
        } catch (SQLException se) {
            System.out.println("Payment failed.");
        }
        updateBalance(conn, id, amount);
    }

    private static int getLeaseId(Connection conn, int id) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select lease_id from lease_tenant where tenant_id = " + id;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant id.");
            }
            return result.getInt("lease_id");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    public static Object[] getCardInfo() {
        Scanner scanner = new Scanner(System.in);

        long cardNumber;
        while (true) {
            try {
                System.out.println("Enter a card number:");
                cardNumber = scanner.nextLong();
                // Check if the entered value is a 10-digit positive integer
                if (String.valueOf(cardNumber).length() == 10 && cardNumber > 0) {
                    break; // Exit the loop if a valid input is provided
                } else {
                    System.out.println("Invalid input. Please enter a 10-digit positive integer.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a 10-digit positive integer.");
                scanner.next(); // Consume the invalid input to avoid an infinite loop
            }
        }
        String expDate;
        while (true) {
            System.out.println("Enter the expiration date (mm/yy):");
            expDate = scanner.next();

            // Check if the entered date is after 12/23
            if (isValidDate(expDate)) {
                break; // Exit the loop if a valid input is provided
            } else {
                System.out.println("Invalid input. Please enter a date after today's date (mm/yy).");
            }
        }
        int cvv;
        while (true) {
            try {
                System.out.print("Please enter the CVV: ");
                cvv = scanner.nextInt();
                // Check if the entered value is a 3-digit integer
                if (cvv >= 100 && cvv <= 999) {
                    break; // Exit the loop if a valid input is provided
                } else {
                    System.out.println("Invalid input. Please enter a 3-digit integer.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a 3-digit integer.");
                scanner.next(); // Consume the invalid input to avoid an infinite loop
            }
        }
        // Close the scanner to prevent resource leak
        scanner.close();

        return new Object[] { cardNumber, expDate, cvv };
    }

    public static boolean isValidDate(String inputDate) {
        // Parse the input date
        inputDate = "31/" + inputDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        LocalDate dateToCompare = LocalDate.parse(inputDate, formatter); // Assuming day is always 01

        // Get today's date
        LocalDate today = LocalDate.now();

        // Compare the dates
        return dateToCompare.isAfter(today);
    }

    public static void updateBalance(Connection conn, int tenantId, double payment) {
        double currBalance = getBalance(conn, tenantId);
        try (Statement stmt = conn.createStatement()) {
            String updateRent = "update tenant set balance = " + (currBalance - payment) + " where tenant_id = "
                    + tenantId;
            int rowsUpdated = stmt.executeUpdate(updateRent);
            if (rowsUpdated > 0) {
                System.out.println("Rent successfully updated.");
                System.out.println("New tenant balance is: $" + getBalance(conn, tenantId));
            } else {
                System.out.println("Unable to update balance.");
            }

        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
    }

    public static double getBalance(Connection conn, int tenantId) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select balance from tenant where tenant_id = " + tenantId;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant id.");
            }
            return result.getDouble("balance");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    /*
     * param: Scanner
     * return: int
     * This method takes in a scanner and uses is to prompt the user
     * for a navigation choice until a valid one is provided.
     */
    public static int amenityMenu(Scanner scan) {
        while (true) {
            // Prints out the menu to choose an operation
            System.out.println("Please choose what operation you would like to do: ");
            System.out.println("(1) View current amenities");
            System.out.println("(2) Add new amentity subscription");
            System.out.println("(3) Cancel current subscription");
            System.out.println("(4) Exit");

            int response;
            // Checks if the response is valid
            if (scan.hasNextInt()) {
                response = scan.nextInt();
                if (response > 0 && response < 5) {
                    return response;
                }
            }
            System.out.println("Please enter an integer between 1 and 4.");
            scan.nextLine();
        }
    }

    /*
     * param: Connection, id
     * This method prints the amenities that a tenant is currently subscribed to
     */
    public static ArrayList<String> getAmenities(Connection conn, int id) {
        ArrayList<String> amenities = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            String idCheckQuery = "select public_amenity.name, public_amenity.price, public_amenity.hours from public_amenity join subscription on public_amenity.name=subscription.name where tenant_id="
                    + id;
            ResultSet result = stmt.executeQuery(idCheckQuery);
            if (!result.next()) {
                System.out.println("No current amenity subscriptions");
                return amenities;
            }
            System.out.println("Current Subscriptions");
            amenities.add(result.getString("name"));
            System.out.printf("%-20s %-10s %-15s%n", "Name:", "Price:", "Hours:");
            do {
                if (!result.getString("name").equals("")) {
                    System.out.printf("%-20s %-10.2f %-15s%n",
                            result.getString("name"),
                            result.getDouble("price"),
                            result.getString("hours"));
                }
            } while (result.next());
            System.out.println("-------------------");
            return amenities;
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return amenities;
    }

    /*
     * param: Connection, id
     * This method prints the amenities that a tenant is not currently subscribed to
     */
    public static ArrayList<String> getUnsubscribed(Connection conn, int id) {
        ArrayList<String> unsubs = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            String unsubQuery = "select pa.name, pa.price, pa.hours from public_amenity pa where name not in (select s.name from subscription s where s.tenant_id = "
                    + id + ")";
            ResultSet result = stmt.executeQuery(unsubQuery);
            if (!result.next()) {
                System.out.println("You are subscribed to all amenities.");
                return unsubs;
            }
            System.out.printf("%-20s %-10s %-15s%n", "Name:", "Price:", "Hours:");
            while (result.next()) {
                if (!result.getString("name").equals("")) {
                    unsubs.add(result.getString("name"));
                    System.out.printf("%-20s %-10d %-15s%n",
                            result.getString("name"),
                            result.getInt("price"),
                            result.getString("hours"));
                }
            }
            System.out.println("-------------------");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return unsubs;
    }

    public static void addSubscription(Connection conn, int id, String name) {
        double price = 0;
        try (Statement stmt = conn.createStatement()) {
            String newsubQuery = "select price from public_amenity where name='" + name + "'";
            ResultSet result = stmt.executeQuery(newsubQuery);
            if (!result.next()) {
                System.out.println("Invalid selection.");
            }
            price = result.getDouble("price");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        boolean rentUpdated = updateSubFee(conn, id, price);
        if (rentUpdated) {
            try (Statement stmt = conn.createStatement()) {
                String insert = "insert into subscription (tenant_id, name) values (" + id + ", '" + name
                        + "')";
                int rowsAffected = stmt.executeUpdate(insert);
                if (rowsAffected > 0) {
                    System.out.println("Subscription successful. The fee will be added onto your monthly rent.");
                } else {
                    System.out.println("Subscription failed.");
                }
            } catch (SQLException se) {
                System.out.println("Subscription failed.");
            }
        } else {
            System.out.println("Unable to process subscription");
        }
    }

    public static double getSubFee(Connection conn, int id) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select subscription_fee from tenant where tenant_id = " + id;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (result.next()) {
                return result.getDouble("subscription_fee");
            }
            System.out.println("Invalid tenant id.");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    public static boolean updateSubFee(Connection conn, int id, double price) {
        double currFee = getSubFee(conn, id);
        try (Statement s = conn.createStatement()) {
            String updateRent = "update tenant set subscription_fee = " + (currFee + price) + " where tenant_id = "
                    + id;
            int rowsUpdated = s.executeUpdate(updateRent);
            if (rowsUpdated > 0) {
                System.out.println("Rent successfully updated.");
                System.out.println("New subscription fee is: $" + getSubFee(conn, id));
                return true;
            } else {
                System.out.println("Unable to update rent.");
            }

        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return false;
    }

    public static void getTenantPayments(Connection conn, int tenantId) {
        try (Statement stmt = conn.createStatement()) {
            String paymentQuery = "select * from payment where tenant_id = " + tenantId;
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

    public static String getName(Connection conn, int tenantId) {
        try (Statement stmt = conn.createStatement()) {
            String nameQuery = "select name from tenant where tenant_id = " + tenantId;
            ResultSet result = stmt.executeQuery(nameQuery);
            if (!result.next()) {
                System.out.println("Tenant not found.");
            } else {
                return result.getString(tenantId);
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return "";
    }

    public static void cancelSubscription(Connection conn, int tenantId, String amen) {
        double price = 0;
        try (Statement stmt = conn.createStatement()) {
            String newsubQuery = "select price from public_amenity where name='" + amen + "'";
            ResultSet result = stmt.executeQuery(newsubQuery);
            if (!result.next()) {
                System.out.println("Invalid selection.");
            }
            price = result.getDouble("price");
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        boolean rentUpdated = updateSubFee(conn, tenantId, (price*-1));
        if (rentUpdated) {
            try (Statement stmt = conn.createStatement()) {
                String delete = "delete from subscription where tenant_id = " + tenantId + " and name='" + amen + "'";
                int rowsAffected = stmt.executeUpdate(delete);
                if (rowsAffected == 0) {
                    System.out.println("Subscription cancelation failed.");
                    return;
                } else {
                    System.out.println("Subscription cancelation successful");
                }
            } catch (SQLException se) {
                System.out.println("Subscription cancelation failed.");
            }
        }
    }
}