package proj.pack;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PropertyManager {
    /*
     * param: Scanner
     * return: int
     * This method takes in a scanner paramether and uses it to prompt the user to
     * navigate to an operation on the property manager interface
     */
    public static int propertyMenu(Scanner scan) {
        while (true) {
            // Prints out the menu to choose an operation
            System.out.println("Please choose what operation you would like to do: ");
            System.out.println("(1) Record visit data");
            System.out.println("(2) Record lease data");
            System.out.println("(3) Record move-out");
            System.out.println("(4) Add person or pet to lease");
            System.out.println("(5) Charge rent");
            System.out.println("(6) Generate property report");
            System.out.println("(7) Get apartment data");
            System.out.println("(8) Exit");

            try {
                int response = scan.nextInt();
                if (response > 0 && response < 9) {
                    return response;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid response");
                scan.next();
            }
            System.out.println("Please enter an integer between 1 and 8.");
        }
    }

    /*
     * param: Scanner, Connection
     * return: int
     * This method takes in a scanner and a database connection paramether and them
     * to prompt the user for a property id until a valid one is provided.
     */
    public static int getPropertyId(Scanner scan, Connection conn) {
        Company.getProperties(conn);
        while (true) {
            System.out.println("Please enter your unique property id or '0' to exit: ");
            try {
                int response = scan.nextInt();
                if (response == 0) {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                // Performs a query to check if the id matches a property in the db
                try (Statement stmt = conn.createStatement()) {
                    String idCheckQuery = "select count(*) as property from property where property_id = " + response;
                    ResultSet result = stmt.executeQuery(idCheckQuery);
                    if (result.next()) {
                        if (result.getInt("property") == 1) {
                            System.out.println("Property found.");
                            return response;
                        }
                    }
                } catch (SQLException se) {
                    System.out.println("Invalid property id.");
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
            }
            scan.nextLine();
        }
        return 0;
    }

    public static void recordVisit(Connection conn, Scanner scan, int propId) {
        getProspTenant(conn);
        int tenantId = 0;
        int roomNumber = 0;
        String visitDate = "";
        while (true) {
            System.out
                    .println("Enter a tenant id to record a new visit or select 1 to create a new tenant (0 to exit)");
            try {
                int input = scan.nextInt();
                if (input == 0) {
                    return;
                } else if (input == 1) {
                    tenantId = createNewTenant(conn, scan);
                    if (tenantId == 0) {
                        return;
                    }
                    break;
                } else {
                    String validTenant = getProspTenantName(conn, input);
                    if (validTenant.length() > 0) {
                        System.out.println(validTenant + " selected.");
                        tenantId = input;
                        break;
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                scan.next();
            }
        }
        getRoomNumbers(conn, propId);
        while (true) {
            try {
                System.out.print("Please enter the room number (0 to exit): ");
                roomNumber = scan.nextInt();
                if (roomNumber == 0) {
                    break;
                }
                // Check if the entered value is a 3-digit integer
                if (roomNumber >= 100 && roomNumber <= 999) {
                    boolean validRoom = checkRoomNumber(conn, propId, roomNumber);
                    if (validRoom) {
                        break;
                    }
                } else {
                    System.out.println("Invalid input. Please enter a 3-digit integer.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a 3-digit integer.");
                scan.next(); // Consume the invalid input to avoid an infinite loop
            }
        }
        if (roomNumber == 0) {
            return;
        }
        while (true) {
            try {
                System.out.print("Please enter the date of the visit (mm/dd/yyyy) (0 to exit): ");
                visitDate = scan.nextLine();
                if (visitDate.equals("")) {
                    visitDate = scan.nextLine();
                }
                if (isValidDate(visitDate)) {
                    break;
                }
                System.out.println(
                        "Invalid date. Please enter a date in the format (mm/dd/yyyy) that has already passed.");
            } catch (InputMismatchException e) {
                System.out.println(
                        "Invalid date. Please enter a date in the format (mm/dd/yyyy) that has already passed.");
                scan.next(); // Consume the invalid input to avoid an infinite loop
            }
        }
        if (visitDate.equals("")) {
            return;
        }
        insertTour(conn, tenantId, propId, roomNumber, visitDate);
    }

    public static boolean getProspTenant(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String prospTenantQuery = "select tenant_id, name from prospective_tenant where leasing=0";
            ResultSet result = stmt.executeQuery(prospTenantQuery);
            if (!result.next()) {
                System.out.println("No current prospective tenants.");
            } else {
                System.out.println("Tenant Id:  Name:");
                do {
                    int id = result.getInt("tenant_id");
                    String name = result.getString("name");
                    System.out.printf("%-11s %-20s%n", id, name);
                } while (result.next());
                System.out.println("-------------------");
                return true;
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return false;
    }

    public static String getProspTenantName(Connection conn, int id) {
        String name = "";
        try (Statement stmt = conn.createStatement()) {
            String prospTenantQuery = "select name from prospective_tenant where leasing=0 and tenant_id = " + id;
            ResultSet result = stmt.executeQuery(prospTenantQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant id.");
            } else {
                name = result.getString("name");
                if (name.length() > 0) {
                    return name;
                }
                System.out.println("Invalid tenant id.");
            }
        } catch (SQLException se) {
            System.out.println("Invalid tenant id.");
        }
        return name;
    }

    public static int getNewTenantId(Connection conn, String name) {
        try (Statement stmt = conn.createStatement()) {
            String prospTenantQuery = "select tenant_id from tenant where name = '" + name + "'";
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

    public static int createNewTenant(Connection conn, Scanner scan) {
        String name = "";
        while (true) {
            System.out.println("Enter the new tenant's full name ('q' to exit):");
            try {
                name = scan.nextLine();
                if (name.equals("")) {
                    name = scan.nextLine();
                }
                if (name.equals("q")) {
                    return 0;
                } else if (!name.equals("")) {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid name");
            }
        }
        double income = 0;
        while (true) {
            System.out.println("Enter the new tenant's annual income (0 to exit):");
            try {
                income = scan.nextDouble();
                if (income == 0) {
                    return 0;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid income. Please enter a numeric value");
                scan.next();
            }
        }
        int creditScore = 0;
        while (true) {
            System.out.println("Enter the new tenant's credit score (0 to exit):");
            try {
                creditScore = scan.nextInt();
                if (creditScore == 0) {
                    return 0;
                }
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid credit score");
                scan.next();
            }
        }
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into prospective_tenant (name, income, credit_score) values ('" + name + "', '"
                    + income
                    + "', '" + creditScore + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Tenant successfully added.");
            } else {
                System.out.println("Tenant add failed.");
            }
        } catch (SQLException se) {
            System.out.println("Tenant add failed.");
        }
        return getNewTenantId(conn, name);
    }

    public static void getRoomNumbers(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String roomQuery = "select room_number from apartment where property_id = " + propertyId;
            ResultSet result = stmt.executeQuery(roomQuery);
            if (!result.next()) {
                System.out.println("No room data available.");
            } else {
                System.out.println("Room numbers:");
                System.out.println(result.getInt("room_number"));
                while (result.next()) {
                    System.out.println(result.getInt("room_number"));
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
    }

    public static boolean checkRoomNumber(Connection conn, int propertyId, int roomNumber) {
        try (Statement stmt = conn.createStatement()) {
            String roomQuery = "select count(*) from apartment where property_id = " + propertyId
                    + " and room_number = " + roomNumber;
            ResultSet result = stmt.executeQuery(roomQuery);
            if (!result.next()) {
                System.out.println("Invalid room number.");
            } else {
                int num = result.getInt("count(*)");
                if (num > 0) {
                    return true;
                }
                System.out.println("Invalid room number.");
            }
        } catch (SQLException se) {
            System.out.println("Invalid room number.");
        }
        return false;
    }

    public static boolean isValidDate(String inputDate) {
        try {
            // Parse the input date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate dateToCompare = LocalDate.parse(inputDate, formatter);

            // Get today's date
            LocalDate today = LocalDate.now();

            // Compare the dates
            if (dateToCompare.isBefore(today)) {
                return true;
            } else if (dateToCompare.isEqual(today)) {
                return true;
            } else {
                return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static void insertTour(Connection conn, int tenantId, int propId, int roomNumber, String visitDate) {
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into tour (tenant_id, property_id, room_number, tour_date) values (" + tenantId
                    + ", "
                    + propId
                    + ", " + roomNumber + ", '" + visitDate + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Visit successfully added.");
            } else {
                System.out.println("Visit add failed.");
            }
        } catch (SQLException se) {
            System.out.println("Visit add failed.");
        }
        try (Statement s = conn.createStatement()) {
            String updateVisit = "update prospective_tenant set visited=1 where tenant_id = " + tenantId;
            int rowsUpdated = s.executeUpdate(updateVisit);
            if (rowsUpdated > 0) {
                System.out.println("Tenant eligable for rental");
            } else {
                System.out.println("Unable to update tenant tour status.");
            }

        } catch (SQLException se) {
            System.out.println("Unable to update tenant tour status.");
        }
    }

    public static void recordLease(Connection conn, Scanner scan, int propId) {
        ArrayList<Integer> availableRooms = getEmptyApts(conn, propId);
        if (availableRooms.size() > 0) {
            int roomNumber = 0;
            while (true) {
                System.out
                        .println("Enter a room number to assign a new lease to it (0 to exit)");
                try {
                    int input = scan.nextInt();
                    if (input == 0) {
                        break;
                    } else if (availableRooms.contains(input)) {
                        roomNumber = input;
                        double rent = 0;
                        while (true) {
                            System.out.println("Enter the monthly rent: (0 to exit)");
                            try {
                                rent = scan.nextDouble();
                                if (rent == 0) {
                                    break;
                                } else if (rent < 500) {
                                    System.out.println("Error. The minimum monthly rent is $500.");
                                } else if (rent > 2250) {
                                    System.out.println("Error. The maximum monthly rent is $2250.");
                                } else {
                                    break;
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter a numeric value");
                            }
                        }
                        double securityDeposit = rent;
                        String endDate = "";
                        while (true) {
                            System.out.println("Enter the duration of the lease in months (0 to exit):");
                            try {
                                int duration = scan.nextInt();
                                if (duration == 0) {
                                    break;
                                }
                                if (duration < 1) {
                                    System.out.println("Error. Lease must be at least 1 month");
                                } else {
                                    LocalDate startDate = LocalDate.now();
                                    LocalDate end = startDate.plusMonths(duration);
                                    endDate = end.toString();
                                    break;
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter a positive integer value");
                            }
                        }
                        insertLease(conn, propId, roomNumber, rent, securityDeposit, endDate);
                        break;
                    } else {
                        System.out.println("Invalid input.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input.");
                    scan.next();
                }
            }
        }
    }

    public static ArrayList<Integer> getAvailableRooms(Connection conn, int propertyId) {
        ArrayList<Integer> roomNums = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String availableRoomQuery = "select lease.lease_id, lease.room_number, lease.end_date from lease join apartment on lease.property_id = apartment.property_id and lease.room_number = apartment.room_number where lease.property_id = "
                    + propertyId;
            ResultSet result = stmt.executeQuery(availableRoomQuery);
            boolean roomPrinted = false;
            if (!result.next()) {
                System.out.println("Invalid property id.");
            } else {
                System.out.println("Available apartments");
                System.out.println("Apartment number:   Available rooms:");
                do {
                    String date = result.getString("end_date");
                    if (checkValidLease(conn, date)) {
                        int leaseId = result.getInt("lease_id");
                        roomPrinted = true;
                        int roomNumber = result.getInt("room_number");
                        int availability = checkLeaseOccupancy(conn, leaseId);

                        // Print in two columns
                        System.out.printf("%-20s %-15s%n", "Apartment " + roomNumber, availability);

                        roomNums.add(roomNumber);
                    }
                } while (result.next());
                if (!roomPrinted) {
                    System.out.println("All apartments are at capacity!");
                } else {
                    System.out.println("------------");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }

        return roomNums;
    }

    public static ArrayList<Integer> getEmptyApts(Connection conn, int propertyId) {
        ArrayList<Integer> roomNums = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String availableRoomQuery = "select lease.room_number, lease.end_date from lease join apartment on lease.property_id = apartment.property_id and lease.room_number = apartment.room_number where lease.property_id = "
                    + propertyId;
            ResultSet result = stmt.executeQuery(availableRoomQuery);
            boolean roomPrinted = false;
            if (!result.next()) {
                System.out.println("Invalid property id.");
            } else {
                System.out.println("Available apartments:");
                String date = result.getString("end_date");
                do {
                    if (!checkValidLease(conn, date)) {
                        roomPrinted = true;
                        int roomNumber = result.getInt("room_number");
                        roomNums.add(roomNumber);
                    }
                } while (result.next());

                if (!roomPrinted) {
                    System.out.println("All apartments are at capacity!");
                } else {
                    System.out.println("------------");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }

        return roomNums;
    }

    public static boolean checkValidLease(Connection conn, String endDate) {

        try {
            // Parse the input date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate dateToCompare = LocalDate.parse(endDate, formatter);

            // Get today's date
            LocalDate today = LocalDate.now();

            // Compare the dates
            if (dateToCompare.isAfter(today)) {
                return true;
            }
        } catch (DateTimeParseException e) {
            return false;
        } catch (NullPointerException e){
            return true;
        }
        return false;
    }

    public static void insertLease(Connection conn, int propertyId, int roomNumber, double rent,
            double securityDeposit, String endDate) {
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into lease (property_id, room_number, rent, security_deposit, end_date) values ('"
                    + propertyId + "', '" + roomNumber + "', '" + rent + "', '" + securityDeposit + "', '" + endDate
                    + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Lease successfully added.");
                System.out.println("Property id: " + propertyId);
                System.out.println("Room number: " + roomNumber);
                System.out.println("Rent: " + rent);
                System.out.println("Security Deposit: " + securityDeposit);
                System.out.println("End date: " + endDate);
            } else {
                System.out.println("Lease insert failed.");
            }
        } catch (SQLException se) {
            System.out.println("Lease insert failed.");
        }
    }

    public static void recordMoveOut(Connection conn, Scanner scan, int propertyId) {
        ArrayList<Integer> tenants = getTenants(conn, propertyId);
        int tenantId = 0;
        int roomNumber = 0;
        while (true) {
            System.out.println("Enter the id of the tenant that is moving out (0 to exit):");
            try {
                tenantId = scan.nextInt();
                if (tenantId == 0) {
                    return;
                }
                if (tenants.contains(tenantId)) {
                    Iterator<Integer> iterator = tenants.iterator();
                    while (iterator.hasNext()) {
                        Integer id = iterator.next();
                        if (id == tenantId) {
                            roomNumber = iterator.next();
                        }
                    }
                    roomNumber = 1;
                    break;
                }
                System.out.println("Invalid tenant id.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
            }
        }
        String tenantName = Tenant.getName(conn, tenantId);
        while (true) {
            System.out.println("Are you sure you want to move out " + tenantName + "? (y/n)");
            try {
                String ans = scan.next();
                if (ans.equals("n")) {
                    return;
                } else if (ans.equals("y")) {
                    boolean success = removeTenant(conn, tenantId, propertyId, roomNumber);
                    if (success) {
                        break;
                    }
                } else {
                    System.out.println("Invalid input.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    public static ArrayList<Integer> getTenants(Connection conn, int propertyId) {
        ArrayList<Integer> tenants = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String tenantQuery = "select tenant.tenant_id, tenant.name, resident.room_number from tenant join resident on tenant.tenant_id = resident.tenant_id and resident.property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(tenantQuery);

            if (!result.next()) {
                System.out.println("No current tenants.");
            } else {
                System.out.println("Current tenants:");
                do {
                    int id = result.getInt("tenant_id");
                    String name = result.getString("name");
                    int roomNum = result.getInt("room_number");

                    tenants.add(id);
                    tenants.add(roomNum);

                    System.out.printf("%-5s %-20s %-10s%n", id, name, roomNum);
                } while (result.next());

                if (tenants.isEmpty()) {
                    System.out.println("No current tenants");
                } else {
                    System.out.println("-------------------");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
        return tenants;
    }

    public static boolean removeTenant(Connection conn, int tenantId, int propertyId, int roomNumber) {
        try (Statement stmt = conn.createStatement()) {
            String delete = "delete from lease_tenant where tenant_id = " + tenantId;
            int rowsAffected = stmt.executeUpdate(delete);
            if (rowsAffected == 0) {
                System.out.println("Failed to remove tenant");
                return false;
            }
        } catch (SQLException se) {
            System.out.println("Tenant move-out failed.");
        }
        try (Statement stmt = conn.createStatement()) {
            String delete = "delete from pet where tenant_id = " + tenantId;
            stmt.executeUpdate(delete);
        } catch (SQLException se) {
            System.out.println("Tenant move-out failed.");
        }
        try (Statement stmt = conn.createStatement()) {
            String delete = "delete from subscription where tenant_id = " + tenantId;
            stmt.executeUpdate(delete);
        } catch (SQLException se) {
            System.out.println("Tenant move-out failed.");
        }
        try (Statement stmt = conn.createStatement()) {
            String delete = "delete from tenant where tenant_id = " + tenantId;
            int rowsAffected = stmt.executeUpdate(delete);
            if (rowsAffected == 0) {
                System.out.println("Failed to remove tenant");
                return false;
            }
        } catch (SQLException se) {
            System.out.println("Tenant move-out failed.");
        }
        return true;
    }

    public static void addPerson(Connection conn, Scanner scan, int propertyId) {
        int roomNum = getVaildApt(conn, scan, propertyId);
        if (roomNum == 0) {
            return;
        }
        int leaseId = getLeaseId(conn, propertyId, roomNum);
        int tenantId = 0;
        String tenantName = "";
        boolean tenants = getProspTenant(conn);
        if (!tenants) {
            return;
        }
        while (true) {
            System.out
                    .println("Enter a tenant id to add to a lease (0 to exit)");
            try {
                int input = scan.nextInt();
                if (input == 0) {
                    break;
                } else {
                    tenantName = getEligibleProspTenantName(conn, input);
                    if (tenantName.length() > 0) {
                        boolean hasVisited = getVisit(conn, input);
                        if (hasVisited) {
                            tenantId = input;
                            System.out.println("Tenant eligible.");
                            break;
                        } else {
                            System.out.println("Tenant is ineligible. Must complete a tour before signing a lease.");
                        }
                    } else {
                        System.out.println("Invalid tenant id.");
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                scan.next();
            }
        }
        insertPerson(conn, leaseId, tenantId, tenantName, propertyId, roomNum);
    }

    private static int getLeaseId(Connection conn, int propertyId, int roomNum) {
        try (Statement stmt = conn.createStatement()) {
            String idQuery = "select lease_id from lease where property_id=" + propertyId + " and room_number="
                    + roomNum;
            ResultSet result = stmt.executeQuery(idQuery);
            if (!result.next()) {
                System.out.println("Invalid apartment");
            } else {
                return result.getInt("lease_id");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    private static int getVaildApt(Connection conn, Scanner scan, int propertyId) {
        ArrayList<Integer> apartments = getAvailableRooms(conn, propertyId);
        while (true) {
            System.out.println("Select an apartment number to add the tenant to (0 to exit)");
            try {
                int aptNum = scan.nextInt();
                if (apartments.contains(aptNum)) {
                    return aptNum;
                } else if (aptNum == 0) {
                    return 0;
                } else {
                    System.out.println("Invalid apartment number");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
            }
        }
    }

    private static int checkLeaseOccupancy(Connection conn, int leaseId) {
        try (Statement stmt = conn.createStatement()) {
            String availabilityQuery = "with occupancy as (select bedrooms from apartment join lease on lease.property_id = apartment.property_id and lease.room_number = apartment.room_number where lease_id ="
                    + leaseId
                    + "), occupants as (select count(tenant_id) as occupants from lease_tenant where lease_tenant.lease_id="
                    + leaseId + ") select (bedrooms-occupants) as availability from occupancy, occupants";
            ResultSet result = stmt.executeQuery(availabilityQuery);
            if (!result.next()) {
                return 0;
            } else {
                int availability = result.getInt("availability");
                if (availability > 0) {
                    return availability;
                } else {
                    return 0;
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    public static String getEligibleProspTenantName(Connection conn, int id) {
        String name = "";
        try (Statement stmt = conn.createStatement()) {
            String prospTenantQuery = "select name from prospective_tenant where tenant_id = " + id;
            ResultSet result = stmt.executeQuery(prospTenantQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant id.");
            } else {
                name = result.getString("name");
                if (name.length() > 0) {
                    return name;
                }
                System.out.println("Invalid tenant id.");
            }
        } catch (SQLException se) {
            System.out.println("Invalid tenant id.");
        }
        return name;
    }

    public static boolean getVisit(Connection conn, int tenantId) {
        try (Statement stmt = conn.createStatement()) {
            String visitQuery = "select visited from prospective_tenant where tenant_id=" + tenantId;
            ResultSet result = stmt.executeQuery(visitQuery);
            if (!result.next()) {
                System.out.println("Invalid tenant id.");
            } else {
                return result.getBoolean("visited");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return false;
    }

    public static void addPet(Connection conn, Scanner scan, int propertyId) {
        int tenantId = getTenantId(conn, scan, propertyId);
        if (tenantId == 0) {
            return;
        }
        String name = "";
        String species = "";
        double weight = 0;
        while (true) {
            System.out.println("Enter the name of the pet ('q' to exit):");
            try {
                name = scan.nextLine();
                if (name.equals("")) {
                    name = scan.nextLine();
                }
                if (name.equals("q")) {
                    return;
                } else if (!name.equals("")) {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
            }
        }
        while (true) {
            System.out.println("Enter the species of the pet ('q' to exit):");
            try {
                species = scan.nextLine();
                if (species.equals("")) {
                    species = scan.nextLine();
                }
                if (species.equals("q")) {
                    return;
                } else if (!species.equals("")) {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
            }
        }
        while (true) {
            System.out.println("Enter the weight of the pet (0 to exit):");
            try {
                weight = scan.nextDouble();
                if (weight == 0) {
                    return;
                } else if (weight <= 0) {
                    System.out.println("Per must have positive weight.");
                } else if (weight > 120) {
                    System.out.println("Pet weight limit of 120lbs exceeded.");
                } else {
                    break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input.");
                scan.next();
            }
        }
        insertPet(conn, tenantId, name, species, weight);
    }

    private static int getTenantId(Connection conn, Scanner scan, int propertyId) {
        int tenantId = 0;
        ArrayList<Integer> tenants = getTenants(conn, propertyId);
        while (true) {
            System.out.println("Enter the id of the pet owner (0 to exit):");
            try {
                tenantId = scan.nextInt();
                if (tenants.contains(tenantId)) {
                    return tenantId;
                } else if (tenantId == 0) {
                    return 0;
                } else {
                    System.out.println("Invalid tenant id");
                    scan.next();
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
                scan.next();
            }
        }

    }

    public static int getValidLeaseId(Connection conn, Scanner scan, int propertyId) {
        int leaseId = 0;
        ArrayList<Integer> leaseIds = getLeaseIds(conn, propertyId);
        while (true) {
            System.out.println("Enter the id of the lease that is being added to (0 to exit):");
            try {
                leaseId = scan.nextInt();
                if (leaseId == 0) {
                    return 0;
                } else if (leaseIds.contains(leaseId)) {
                    return leaseId;
                } else {
                    System.out.println("Invalid lease id");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input");
            }
        }
    }

    private static ArrayList<Integer> getLeaseIds(Connection conn, int propertyId) {
        ArrayList<Integer> leaseIds = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String leaseQuery = "select lease_id, end_date from lease where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(leaseQuery);
            if (!result.next()) {
                System.out.println("Invalid property id.");
            } else {
                boolean leasePrinted = false;
                String date = result.getString("end_date");
                if (checkValidLease(conn, date)) {
                    System.out.println("Current leases:");
                    int leaseId = result.getInt("lease_id");
                    System.out.println(leaseId);
                    leaseIds.add(leaseId);
                    leasePrinted = true;
                }

                while (result.next()) {
                    date = result.getString("end_date");
                    if (checkValidLease(conn, date)) {
                        if (!leasePrinted) {
                            System.out.println("Current leases:");
                            leasePrinted = true;
                        }
                        int leaseId = result.getInt("lease_id");
                        System.out.println(leaseId);
                        leaseIds.add(leaseId);
                    }
                }

                if (!leasePrinted) {
                    System.out.println("No available leases");
                } else {
                    System.out.println("-------------------");
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }

        return leaseIds;
    }

    private static void insertPerson(Connection conn, int leaseId, int tenantId, String name, int propertyId,
            int roomNumber) {
        int occupancy = getOccupancy(conn, leaseId);
        double balance = 0;
        if (occupancy == 0) {
            balance = getRent(conn, leaseId);
        }
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into tenant (name, balance) values ('" + name + "', '"
                    + balance + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected == 0) {
                System.out.println("Tenant add failed.");
                return;
            } 
        } catch (SQLException se) {
            System.out.println("Tenant add failed.");
            return;
        }
        int tenId = getNewTenantId(conn, name);
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into lease_tenant (lease_id, tenant_id) values (" + leaseId + ", "
                    + tenId + ")";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected == 0) {
                System.out.println("Tenant add failed.");
                return;
            } 
        } catch (SQLException se) {
            System.out.println("Tenant add failed.");
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            String updateLeasing = "update prospective_tenant set leasing=1 where tenant_id = " + tenantId;
            int rowsUpdated = stmt.executeUpdate(updateLeasing);
            if (rowsUpdated == 0) {
                System.out.println("Unable to update prospective tenant leasing status.");
            } 
        } catch (SQLException se) {
            System.out.println("Unable to update prospective tenant leasing status.");
        }
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into resident (property_id, room_number, tenant_id) values (" + propertyId + ", "
                    + roomNumber + ", " + tenId + ")";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected == 0) {
                System.out.println("Tenant add failed.");
                return;
            } 
        } catch (SQLException se) {
            System.out.println("Tenant add failed.");
            return;
        }
        if (occupancy == 0) {
            LocalDate today = LocalDate.now();
            LocalDate sixMonthsLater = today.plusMonths(6);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String endDate = sixMonthsLater.format(formatter);
            try (Statement stmt = conn.createStatement()) {
                String insert = "update lease set end_date='" + endDate + "' where property_id=" + propertyId
                        + " and room_number=" + roomNumber;
                int rowsAffected = stmt.executeUpdate(insert);
                if (rowsAffected == 0) {
                    System.out.println("Tenant add failed.");
                    return;
                } 
            } catch (SQLException se) {
                System.out.println("Tenant add failed.");
                return;
            }
        }
    }

    public static int getOccupancy(Connection conn, int leaseId) {
        int occupancy = 0;
        try (Statement stmt = conn.createStatement()) {
            String occupancyQuery = "select count(tenant_id) as occupancy from lease_tenant where lease_id=" + leaseId;
            ResultSet result = stmt.executeQuery(occupancyQuery);
            if (!result.next()) {
                System.out.println("Invalid lease id");
            } else {
                occupancy = result.getInt("occupancy");
                return occupancy;
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return occupancy;
    }

    public static double getRent(Connection conn, int leaseId) {
        double rent = 0;
        try (Statement stmt = conn.createStatement()) {
            String occupancyQuery = "select rent from lease where lease_id=" + leaseId;
            ResultSet result = stmt.executeQuery(occupancyQuery);
            if (!result.next()) {
                System.out.println("Invalid lease id");
            } else {
                return result.getDouble("rent");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return rent;
    }

    private static void insertPet(Connection conn, int tenantId, String name, String species, double weight) {
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into pet (tenant_id, name, species, weight) values ('" + tenantId + "', '"
                    + name
                    + "', '" + species + "', '" + weight + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Pet successfully added.");
            } else {
                System.out.println("Pet add failed.");
            }
        } catch (SQLException se) {
            System.out.println("Pet add failed.");
        }
    }

    public static void chargeRent(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select lease.lease_id, tenant_id, rent from lease join lease_tenant on lease.lease_id=lease_tenant.lease_id where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid lease id");
            } else {
                do {
                    int leaseId = result.getInt("lease_id");
                    int tenants = getTenantsOnLease(conn, leaseId);
                    int id = result.getInt("tenant_id");
                    double rent = result.getDouble("rent");
                    double subscription = getSubscription(conn, id);
                    insertRent(conn, id, (rent/tenants), subscription);
                } while (result.next());
                System.out.println("Total rent due: " + getTotalRentDue(conn, propertyId));
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
    }

    public static int getTenantsOnLease(Connection conn, int leaseId){
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select count(tenant_id) from lease_tenant where lease_id=" + leaseId;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid lease id");
            } else {
                return result.getInt("count(tenant_id)");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    private static double getSubscription(Connection conn, int id) {
        double subscription = 0;
        try (Statement stmt = conn.createStatement()) {
            String subscripitionQuery = "select subscription_fee from tenant where tenant_id=" + id;
            ResultSet result = stmt.executeQuery(subscripitionQuery);
            if (!result.next()) {
                System.out.println("Invalid lease id");
            } else {
                return result.getDouble("subscription_fee");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return subscription;
    }

    private static void insertRent(Connection conn, int tenantId, double rent, double subscription) {
        try (Statement stmt = conn.createStatement()) {
            String updateRent = "update tenant set balance = " + (rent + subscription) + " where tenant_id = "
                    + tenantId;
            int rowsUpdated = stmt.executeUpdate(updateRent);
            if (rowsUpdated == 0) {
                System.out.println("Unable to update balance.");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
    }

    public static int getTotalRentDue(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select sum(balance) from tenant join resident on tenant.tenant_id = resident.tenant_id where property_id ="
                    + propertyId;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid property id");
            } else {
                return result.getInt("sum(balance)");
            }
        } catch (SQLException se) {
            System.out.println("Invalid query.");
        }
        return 0;
    }

    public static void generatePropretyReport(Connection conn, int propertyId) {
        System.out.println("------------------------------------");
        System.out.println("Property id: " + propertyId);
        System.out.println("Address: " + getAddressById(conn, propertyId));
        System.out.println();
        try (Statement stmt = conn.createStatement()) {
            String aptQuery = "select * from apartment where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(aptQuery);
            if (!result.next()) {
                System.out.println("No apartments found");
                System.out.println();
            } else {
                System.out.printf("%-15s %-10s %-12s %-13s %-9s%n", "Room number:", "Area:", "Bedrooms:", "Bathrooms:",
                        "Occupants");
                while (result.next()) {
                    int roomNum = result.getInt("room_number");
                    double area = result.getDouble("area");
                    int bedrooms = result.getInt("bedrooms");
                    double bathrooms = result.getDouble("bathrooms");
                    int occupants = getAptOccupants(conn, propertyId, roomNum);
                    System.out.printf("%-15d %-10.1f %-12d %-13.1f %-9d%n", roomNum, area, bedrooms, bathrooms,
                            occupants);
                }
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
        getAvailableRooms(conn, propertyId);
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
                System.out.printf("%-10s %-20s %-15s %-10s%n", "Tenant id", "Name", "Species", "Weight");
                while (result.next()) {
                    int tenantId = result.getInt("tenant_id");
                    String name = result.getString("name");
                    String species = result.getString("species");
                    double weight = result.getDouble("weight");
                    System.out.printf("%-10d %-20s %-15s %-10.2f%n", tenantId, name, species, weight);
                }
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
        System.out.println("------------------------------------");
    }

    public static int getAptOccupants(Connection conn, int propertyId, int roomNum) {
        try (Statement stmt = conn.createStatement()) {
            String occupantQuery = "select count(tenant_id) as occupants from lease_tenant join lease on lease.lease_id = lease_tenant.lease_id where property_id="
                    + propertyId + " and room_number=" + roomNum;
            ResultSet result = stmt.executeQuery(occupantQuery);
            if (!result.next()) {
                System.out.println("Property not found.");
            } else {
                return result.getInt("occupants");
            }
        } catch (SQLException se) {
            System.out.println("Invalid room.");
        }
        return 0;
    }

    public static String getAddressById(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String propertyQuery = "select address from property where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(propertyQuery);
            if (!result.next()) {
                System.out.println("Property not found.");
            } else {
                return result.getString("address");
            }
        } catch (SQLException se) {
            System.out.println("Invalid address.");
        }
        return "";
    }

    public static boolean isPetFriendly(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String petQuery = "select pet_friendly from property where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(petQuery);
            if (!result.next()) {
                System.out.println("Property not found.");
            } else {
                int resp = result.getInt("pet_friendly");
                if (resp == 1) {
                    return true;
                }
                return false;
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
        return false;
    }

    public static ArrayList<Integer> getApartments(Connection conn, int propertyId) {
        ArrayList<Integer> roomNums = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            String availableRoomQuery = "select room_number from apartment where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(availableRoomQuery);
            if (!result.next()) {
                System.out.println("Invalid property id.");
            } else {
                System.out.println("Apartments:");
                do {
                    int roomNumber = result.getInt("room_number");
                    System.out.println(roomNumber);
                    roomNums.add(roomNumber);
                } while (result.next());
                System.out.println("------------");
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }

        return roomNums;
    }

    public static void getAptData(Connection conn, int propertyId, int roomNumber) {
        System.out.println();
        System.out.println("------------------------------------");
        System.out.println("Room number: " + roomNumber);
        System.out.println();
        System.out.println("Apartment specs");
        try (Statement stmt = conn.createStatement()) {
            String availableRoomQuery = "select * from apartment where property_id=" + propertyId + " and room_number=" + roomNumber;
            ResultSet result = stmt.executeQuery(availableRoomQuery);
            if (!result.next()) {
                System.out.println("Invalid apartment.");
            } else {
                System.out.println("Area: " + result.getDouble("area"));
                System.out.println("Bedrooms: " + result.getInt("bedrooms"));
                System.out.println("Bathrooms: " + result.getDouble("bathrooms"));
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
        try (Statement stmt = conn.createStatement()) {
            String rentQuery = "select rent from lease where property_id=" + propertyId + " and room_number=" + roomNumber;
            ResultSet result = stmt.executeQuery(rentQuery);
            if (!result.next()) {
                System.out.println("Invalid lease.");
            } else {
                double rent = result.getDouble("rent");
                System.out.println("Monthly Rent: $" + rent);
                System.out.println();
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
        try (Statement stmt = conn.createStatement()) {
            String amenQuery = "select name from apartment_amenity where property_id=" + propertyId + " and room_number=" + roomNumber;
            ResultSet result = stmt.executeQuery(amenQuery);
            if (!result.next()) {
                System.out.println("Invalid apartment.");
            } else {
                System.out.println("Included amenities: ");
                do{
                    String amen = result.getString("name");
                    System.out.println(amen);
                } while (result.next());
            }
        } catch (SQLException se) {
            System.out.println("Invalid apartment.");
        }
        System.out.println();
        ArrayList<Integer> ids = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            String amenQuery = "select tenant.tenant_id, tenant.name, tenant.balance from tenant join resident on tenant.tenant_id=resident.tenant_id where property_id=" + propertyId + " and room_number=" + roomNumber;
            ResultSet result = stmt.executeQuery(amenQuery);
            if (!result.next()) {
                System.out.println("No current tenants.");
            } else {
                System.out.println("Tenants");
                System.out.printf("%-10s %-20s %-10s%n", "Tenant id:", "Name:", "Balance:");
                do {
                    int id = result.getInt("tenant_id");
                    ids.add(id);
                    String name = result.getString("name");
                    double balance = result.getDouble("balance");

                    System.out.printf("%-10d %-20s %-10.2f%n", id, name, balance);
                } while (result.next());
            }
        } catch (SQLException se) {
            System.out.println("Invalid apartment");
        }
        System.out.println();
        boolean petPrinted = false;
        for(int id : ids){
            try (Statement stmt = conn.createStatement()) {
                String amenQuery = "select * from pet where tenant_id=" + id;
                ResultSet result = stmt.executeQuery(amenQuery);
                if (!result.next()) {
                } else {
                    if (!petPrinted) {
                        System.out.println("Pets");
                        System.out.printf("%-20s %-20s %-10s%n", "Name:", "Species:", "Weight:");
                        petPrinted = true; 
                    }   
                    do {
                        String name = result.getString("name");
                        String species = result.getString("species");
                        double weight = result.getDouble("weight");
                    
                        System.out.printf("%-20s %-20s %-10.2f%n", name, species, weight);
                    } while (result.next());
                }
            } catch (SQLException se) {
                System.out.println("Invalid apartment.");
            }
        }
        System.out.println("------------------------------------");
    }
}
