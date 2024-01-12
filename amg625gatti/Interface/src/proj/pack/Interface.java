package proj.pack;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Interface {
    static final String DB_URL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";

    public static void main(String[] args) {
        // Calls connectionSetup() to establish DB connection
        Connection conn = null;
        Scanner scan = new Scanner(System.in);
        do {
            conn = connectionSetup(scan);
        } while (conn == null);
        System.out.println("Connection established.");
        boolean prompt = true;
        while (prompt) {
            // Prompts the user to choose which interface they want to use
            int interfaceChoice = interfaceMenu(scan);
            switch (interfaceChoice) {
                case 1:
                    System.out.println("You selected the tenant interface.");
                    if(!printTenants(conn)){
                        break;
                    }
                    // Prompts the user to enter their tenant id
                    int tenantId = Tenant.getTenantId(scan, conn);
                    boolean loop=true;
                    while (loop) {
                        int tenantMenuChoice = Tenant.tenantMenu(scan);
                        switch (tenantMenuChoice) {
                            case 1:
                                Tenant.getTenantBalance(scan, conn, tenantId);
                                break;
                            case 2:
                                double balance = Tenant.getTenantBalance(scan, conn, tenantId);
                                if (balance == 0) {
                                    System.out.println("No payments due.");
                                    break;
                                }
                                while (true) {
                                    System.out.println("Would you like to make a payment? (y/n)");
                                    String response = scan.next();
                                    if (response.equals("y")) {
                                        Tenant.makePayment(scan, conn, tenantId, balance);
                                        break;
                                    } else if (response.equals("n")) {
                                        break;
                                    } else {
                                        System.out.println("Invalid Response.");
                                    }

                                }
                                break;
                            case 3:
                                boolean cont = true;
                                while (cont) {
                                    int amenityMenuChoice = Tenant.amenityMenu(scan);
                                    switch (amenityMenuChoice) {
                                        case 1:
                                            Tenant.getAmenities(conn, tenantId);
                                            break;
                                        case 2:
                                            while (true) {
                                                ArrayList<String> unsubs = Tenant.getUnsubscribed(conn, tenantId);
                                                if (unsubs.size() > 0) {
                                                    System.out.println(
                                                            "Enter the name of the amenity you would like to add ('q' to exit): ");
                                                    try {
                                                        String amen = scan.next();
                                                        if (amen.equals("q")) {
                                                            break;
                                                        } else if (!unsubs.contains(amen)) {
                                                            System.out.println("Invalid name.");
                                                        } else {
                                                            Tenant.addSubscription(conn, tenantId, amen);
                                                        }
                                                    } catch (InputMismatchException e) {
                                                        System.out.println(
                                                                "Invalid input.");
                                                    }
                                                }
                                            }
                                            break;
                                        case 3:
                                            ArrayList<String> amens = Tenant.getAmenities(conn, tenantId);
                                            while (true) {
                                                System.out.println(
                                                        "Enter the name of the amenity subscription you would like to cancel: ('q' to exit)");
                                                try {
                                                    String amen = scan.next();
                                                    if (amen.equals("q")) {
                                                        break;
                                                    } else if (!amens.contains(amen)) {
                                                        System.out.println("Invalid name.");
                                                    } else {
                                                        Tenant.cancelSubscription(conn, tenantId, amen);
                                                    }
                                                } catch (InputMismatchException e) {
                                                    System.out.println(
                                                            "Invalid input.");
                                                }
                                            }
                                            break;
                                        case 4:
                                            cont = false;
                                            break;
                                    }
                                }
                                break;
                            case 4:
                                Tenant.getTenantPayments(conn, tenantId);
                                break;
                            case 5:
                                loop=false;
                                break;
                        }
                    }
                    break;
                case 2:
                    System.out.println("You selected the property manager interface.");
                    // Prompts the user to enter their property id
                    int propertyId = PropertyManager.getPropertyId(scan, conn);
                    boolean cont = true;
                    while (cont) {
                        int propertyMenuChoice = PropertyManager.propertyMenu(scan);
                        switch (propertyMenuChoice) {
                            case 1:
                                PropertyManager.recordVisit(conn, scan, propertyId);
                                while (true) {
                                    System.out.println("Would you like to record another visit? (y/n)");
                                    try {
                                        String ans = scan.next();
                                        if (ans.equals("y")) {
                                            PropertyManager.recordVisit(conn, scan, propertyId);
                                        } else if (ans.equals("n")) {
                                            break;
                                        } else {
                                            System.out.println("Invalid response.");
                                        }
                                    } catch (InputMismatchException e) {
                                        System.out.println("Invalid response.");
                                    }
                                }
                                break;
                            case 2:
                                PropertyManager.recordLease(conn, scan, propertyId);
                                while (true) {
                                    System.out.println("Would you like to record another lease? (y/n)");
                                    try {
                                        String ans = scan.next();
                                        if (ans.equals("")) {
                                            ans = scan.next();
                                        }
                                        if (ans.equals("y")) {
                                            PropertyManager.recordLease(conn, scan, propertyId);
                                        } else if (ans.equals("n")) {
                                            break;
                                        } else {
                                            System.out.println("Invalid response.");
                                        }
                                    } catch (InputMismatchException e) {
                                        System.out.println("Invalid response.");
                                    }
                                }
                                break;
                            case 3:
                                PropertyManager.recordMoveOut(conn, scan, propertyId);
                                while (true) {
                                    System.out.println("Would you like to record a move-out? (y/n)");
                                    try {
                                        String ans = scan.next();
                                        if (ans.equals("y")) {
                                            PropertyManager.recordMoveOut(conn, scan, propertyId);
                                        } else if (ans.equals("n")) {
                                            break;
                                        } else {
                                            System.out.println("Invalid response.");
                                        }
                                    } catch (InputMismatchException e) {
                                        System.out.println("Invalid response.");
                                    }
                                }
                                break;
                            case 4:
                                boolean run = true;
                                while (run) {
                                    System.out.println("Which would you like to add:");
                                    System.out.println("(1) Person");
                                    System.out.println("(2) Pet");
                                    System.out.println("(3) Exit");
                                    while (true) {
                                        try {
                                            int ans = scan.nextInt();
                                            if (ans == 1) {
                                                PropertyManager.addPerson(conn, scan, propertyId);
                                                break;
                                            } else if (ans == 2) {
                                                if (PropertyManager.isPetFriendly(conn, propertyId)) {
                                                    PropertyManager.addPet(conn, scan, propertyId);
                                                    break;
                                                } else {
                                                    System.out.println("This building does not allow pets.");
                                                    break;
                                                }
                                            } else if (ans == 3) {
                                                run = false;
                                                break;
                                            } else {
                                                System.out.println("Invalid response");
                                                scan.next();
                                            }
                                        } catch (InputMismatchException e) {
                                            System.out.println("Invalid response");
                                            scan.next();
                                        }
                                    }
                                }
                                break;
                            case 5:
                                System.out.println("Are you sure you want to charge rent? (y/n)");
                                try {
                                    String choice = scan.next();
                                    if (choice.equals("y")) {
                                        PropertyManager.chargeRent(conn, propertyId);
                                    } else if (choice.equals("n")) {
                                        break;
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Invalid response");
                                }
                                break;
                            case 6:
                                PropertyManager.generatePropretyReport(conn, propertyId);
                                break;
                            case 7:
                                ArrayList<Integer> apts = PropertyManager.getApartments(conn, propertyId);
                                while(true){
                                    System.out.println("Enter the room number of the apartment you would like to view: (0 to exit)");
                                    try{
                                        int resp = scan.nextInt();
                                        if(resp==0){
                                            break;
                                        }else if(apts.contains(resp)){
                                            PropertyManager.getAptData(conn, propertyId, resp);
                                            break;
                                        }
                                    } catch (InputMismatchException e){
                                        System.out.println("Invalid room number");
                                    }
                                }
                                break;
                            case 8:
                                cont=false;
                                break;
                        }
                    }
                    break;
                case 3:
                    System.out.println("You selected the company manager interface.");
                    boolean run = true;
                    while (run) {
                        int choice = Company.companyMenu(scan);
                        if (choice == 1) {
                            ArrayList<Integer> propertyIds = Company.getProperties(conn);
                            if(propertyIds.size()==0){
                                continue;
                            }
                            while (true) {
                                System.out.println("Enter a property id to generate a report (0 to exit):");
                                try {
                                    int propId = scan.nextInt();
                                    if (propId == 0) {
                                        break;
                                    }
                                    if (propertyIds.contains(propId)) {
                                        PropertyManager.generatePropretyReport(conn, propId);
                                        break;
                                    } else {
                                        System.out.println("Invalid property id");
                                    }
                                } catch (InputMismatchException e) {
                                    System.out.println("Invalid response");
                                }
                            }
                        } else if (choice == 2) {
                            Company.addProperty(conn, scan);
                            while (true) {
                                System.out.println("Would you like to add another property? (y/n)");
                                if (scan.hasNext()) {
                                    String response = scan.next();
                                    if (response.equals("n")) {
                                        break;
                                    } else if (response.equals("y")) {
                                        Company.addProperty(conn, scan);
                                    } else {
                                        System.out.println("Please respond with either 'y' or 'n'.");
                                    }
                                }
                            }
                        } else if (choice == 3) {
                            Company.generateAllPropertyReports(conn);
                        } else if (choice == 4) {
                            Company.generateCompanyReport(conn);
                        } else if (choice == 5) {
                            run = false;
                            break;
                        }
                    }
                    break;
                case 4:
                    System.out.println("You selected the financial manager interface.");
                    boolean go = true;
                    while (go) {
                        int financialMenuChoice = Financial.financialMenu(scan);
                        switch (financialMenuChoice) {
                            case 1:
                                Financial.viewTenantsWithPayments(conn);
                                break;
                            case 2:
                                ArrayList<Integer> propIds = Company.getProperties(conn);
                                while (true) {
                                    System.out.println(
                                            "Enter the id of the property you would like to view (0 to exit):");
                                    try {
                                        int propId = scan.nextInt();
                                        if (propId == 0) {
                                            break;
                                        }
                                        if (propIds.contains(propId)) {
                                            Financial.generatePropretyReport(conn, propId);
                                            break;
                                        } else {
                                            System.out.println("Invalid property id");
                                        }
                                    } catch (InputMismatchException e) {
                                        System.out.println("Invalid response");
                                    }
                                }
                                break;
                            case 3:
                                Financial.generateCompanyFinancials(conn);
                                break;
                            case 4:
                                Financial.getPaymentHistory(conn);
                                break;
                            case 5:
                                go=false;
                                break;
                        }  
                    }
                    break;
                case 5:
                    System.out.println("Goodbye!");
                    prompt = false;
                    break;
            }
        }

        // Closes the DB connection if it has been established
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Closes the scanner
        scan.close();
    }

    private static boolean printTenants(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String idCheckQuery = "select tenant_id, name from tenant";
            ResultSet result = stmt.executeQuery(idCheckQuery);
            if(!result.next()){
                System.out.println("No current tenants");
                System.exit(99);
            }
            System.out.println("Tenant Id:  Name:");
            do {
                int id = result.getInt("tenant_id");
                String name = result.getString("name");
                System.out.printf("%-12s %-20s%n", id, name);
            } while (result.next());
            return true;
        } catch (SQLException se) {
            System.out.println("Invalid tenant id.");
        }
        return false;
    }

    /*
     * param: Scanner
     * return: Connection
     * This method takes in a scanner paramether and uses it to take user input to
     * establish a DB connection
     */
    public static Connection connectionSetup(Scanner scan) {
        try {
            System.out.print("Enter Oracle user id: ");
            String user = scan.nextLine();
            System.out.print("Enter Oracle user password: ");
            String pass = scan.nextLine();
            return DriverManager.getConnection(DB_URL, user, pass);

        } catch (SQLException se) {
            System.out.println("[Error]: Connect error. Re-enter login data:");
        }
        return null;
    }

    /*
     * param: Scanner
     * return: int
     * This method takes in a scanner paramether and uses it to prompt the user to
     * navigate to an interface
     */
    public static int interfaceMenu(Scanner scan) {
        while (true) {
            // Prints out the menu to choose an interface
            System.out.println("Please choose which interface you would like to use: ");
            System.out.println("(1) Tenant");
            System.out.println("(2) Property Manager");
            System.out.println("(3) Company Manager");
            System.out.println("(4) Financial Manager");
            System.out.println("(5) Exit");

            int response;
            // Checks if the response is valid
            if (scan.hasNextInt()) {
                response = scan.nextInt();
                if (response > 0 && response < 6) {
                    return response;
                }
            }
            System.out.println("Please enter an integer between 1 and 5.");
            scan.nextLine();
        }
    }

    public static int getProspTenantId(Connection conn, String name) {
        try (Statement stmt = conn.createStatement()) {
            String prospTenantQuery = "select tenant_id from prospective_tenant where name = '" + name + "'";
            ResultSet result = stmt.executeQuery(prospTenantQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant name!.");
            } else {
                return result.getInt("tenant_id");
            }
        } catch (SQLException se) {
            System.out.println("Invalid tenant name.");
        }
        return 0;
    }

}