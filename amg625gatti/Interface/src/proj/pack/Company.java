package proj.pack;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Company {
    public static int companyMenu(Scanner scan) {
        System.out.println("(1) View properties");
        System.out.println("(2) Add a new property");
        System.out.println("(3) View all property reports");
        System.out.println("(4) View company report");
        System.out.println("(5) Exit");
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
            }
            scan.next();
        }
    }

    public static ArrayList<Integer> getProperties(Connection conn) {
        ArrayList<Integer> leaseIds = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String propertyQuery = "select property_id, address from property";
            ResultSet result = stmt.executeQuery(propertyQuery);
            if (!result.next()) {
                System.out.println("No properties found.");
            } else {
                System.out.println("Current properties:");
                System.out.printf("%-5s %-20s%n", "Id:", "Address:");
                int propertyId = result.getInt("property_id");
                System.out.printf("%-5d %-20s%n", propertyId, result.getString("address"));
                leaseIds.add(propertyId);
                while (result.next()) {
                    propertyId = result.getInt("property_id");
                    System.out.printf("%-5d %-20s%n", propertyId, result.getString("address"));
                    leaseIds.add(propertyId);
                }
                System.out.println("------------------------------------");
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }

        return leaseIds;
    }

    public static void addProperty(Connection conn, Scanner scan) {
        String address = "";
        while (true) {
            System.out.println("Enter the street address of the new property [Number + Street Name] ('q' to exit):");
            try {
                address = scan.nextLine();
                if (address.equals("")) {
                    address = scan.nextLine();
                }
                if (address.equals("q")) {
                    return;
                } else if (address.equals("")) {
                    System.out.println("Invalid street address.");
                    continue;
                }
                String[] addressSegs = address.split(" ");
                int num = Integer.parseInt(addressSegs[0]);
                if (num > 0) {
                    if (num < 1000000) {
                        break;
                    }
                    System.out.println(
                            "Invalid street address. Building number cannot be more than 6 digits.");
                    continue;
                }
                System.out.println("Invalid street address. Building number must be greater than 0.");
                continue;
            } catch (InputMismatchException e) {
                System.out.println("Invalid street address");
            }
        }
        int numApts = 0;
        while (true) {
            System.out.println("Enter the number of apartments in this property (0 to exit):");
            try {
                numApts = scan.nextInt();
                if(numApts == 0){
                    break;
                } else if (numApts > 2) {
                    if (numApts < 21) {
                        break;
                    }
                    System.out.println("Invalid input. Properties have a maximum of 20 apartments.");
                    continue;
                }
                System.out.println("Invalid input. Properties have a minimum of 3 apartments.");
                continue;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a positive integer.");
            }
        }
        insertProperty(conn, address, numApts);
    }

    private static void insertProperty(Connection conn, String address, int numApts) {
        Random random = new Random();
        int petFriendly = random.nextInt(2);
        try (Statement stmt = conn.createStatement()) {
            String insert = "insert into property (address, pet_friendly) values ('" + address + "', '" + petFriendly
                    + "')";
            int rowsAffected = stmt.executeUpdate(insert);
            if (rowsAffected > 0) {
                System.out.println("Property successfully added.");
            } else {
                System.out.println("Property add failed.");
                return;
            }
        } catch (SQLException se) {
            System.out.println("Property add failed.");
            return;
        }
        int[] roomNums = new int[numApts];
        double[] area = new double[numApts];
        int[] beds = new int[numApts];
        double[] baths = new double[numApts];

        Random rand = new Random();
        Set<Integer> generatedRoomNumbers = new HashSet<>();
        for (int i = 0; i < numApts; i++) {
            int roomNumber;
            do {
                roomNumber = rand.nextInt(900) + 100;
            } while (!generatedRoomNumbers.add(roomNumber));

            roomNums[i] = roomNumber;

            beds[i] = rand.nextInt(4) + 1;
            switch (beds[i]) {
                case 1:
                    area[i] = Math.round((rand.nextDouble() * (550 - 450) + 450.0) * 10) / 10.0;
                    baths[i] = 1.0;
                    break;
                case 2:
                    area[i] = Math.round((rand.nextDouble() * (1500 - 800) + 800) * 10) / 10.0;
                    baths[i] = (rand.nextInt(3) * 0.5) + 1.0;
                    break;
                case 3:
                    area[i] = Math.round((rand.nextDouble() * (1800 - 1200) + 1200) * 10) / 10.0;
                    baths[i] = (rand.nextInt(4) * 0.5) + 1.0;
                    break;
                case 4:
                    area[i] = Math.round((rand.nextDouble() * (2000 - 1400) + 1400) * 10) / 10.0;
                    baths[i] = (rand.nextInt(7) * 0.5) + 1.0;
                    break;
            }
        }
        int propertyId = getPropertyIdByAddress(conn, address);
        ArrayList<String> privateAmenities = getPrivateAmenities(conn);
        generateApts(conn, propertyId, roomNums, area, beds, baths, privateAmenities);
        ArrayList<String> publicAmenities = getPublicAmenities(conn);
        generatePublicAmens(conn, propertyId, publicAmenities);
        printAptsInProperty(conn, propertyId);
    }

    public static int getPropertyIdByAddress(Connection conn, String address) {
        try (Statement stmt = conn.createStatement()) {
            String propertyQuery = "select property_id from property where address='" + address + "'";
            ResultSet result = stmt.executeQuery(propertyQuery);
            if (!result.next()) {
                System.out.println("Property not found.");
            } else {
                return result.getInt("property_id");
            }
        } catch (SQLException se) {
            System.out.println("Invalid address.");
        }
        return 0;
    }

    public static void generateApts(Connection conn, int propertyId, int[] roomNum, double[] area, int[] beds,
            double[] baths, ArrayList<String> privateAmenities) {
        for (int i = 0; i < roomNum.length; i++) {
            try (Statement stmt = conn.createStatement()) {
                String insert = "insert into apartment (property_id, room_number, area, bedrooms, bathrooms) values ('"
                        + propertyId + "', '" + roomNum[i] + "', '" + area[i] + "', '" + beds[i] + "', '" + baths[i]
                        + "')";
                int rowsAffected = stmt.executeUpdate(insert);
                if (rowsAffected == 0) {
                    System.out.println("Apartment add failed.");
                    return;
                }
            } catch (SQLException se) {
                System.out.println("Apartment add failed.");
                return;
            }
            Random random = new Random();
            int numAmens = 0;
            for (String amen : privateAmenities) {
                int randomNum = random.nextInt(10);
                if (randomNum < 7) {
                    numAmens++;
                    try (Statement stmt = conn.createStatement()) {
                        String insert = "insert into apartment_amenity (property_id, room_number, name) values ('"
                                + propertyId + "', '" + roomNum[i] + "', '" + amen + "')";
                        int rowsAffected = stmt.executeUpdate(insert);
                        if (rowsAffected == 0) {
                            System.out.println("Apartment add failed.");
                            return;
                        }
                    } catch (SQLException se) {
                        System.out.println("Apartment add failed.");
                        return;
                    }
                }
            }
            double rent = 500 * beds[i] + 50 * numAmens;
            try (Statement stmt = conn.createStatement()) {
                String insert = "insert into lease (property_id, room_number, rent, security_deposit, end_date) values ("
                        + propertyId + ", " + roomNum[i] + ", " + rent + ", " + rent + ", '')";
                int rowsAffected = stmt.executeUpdate(insert);
                if (rowsAffected == 0) {
                    System.out.println("Lease add failed.");
                    return;
                }
            } catch (SQLException se) {
                System.out.println("Lease add failed.");
                return;
            }
        }
    }

    public static void printAptsInProperty(Connection conn, int propertyId) {
        ArrayList<Integer> roomNum = new ArrayList<Integer>();
        ArrayList<Double> area = new ArrayList<Double>();
        ArrayList<Integer> beds = new ArrayList<Integer>();
        ArrayList<Double> baths = new ArrayList<Double>();
        try (Statement stmt = conn.createStatement()) {
            String propertyQuery = "select room_number, area, bedrooms, bathrooms from apartment where property_id="
                    + propertyId;
            ResultSet result = stmt.executeQuery(propertyQuery);
            if (!result.next()) {
                System.out.println("Property not found.");
            } else {
                roomNum.add(result.getInt("room_number"));
                area.add(result.getDouble("area"));
                beds.add(result.getInt("bedrooms"));
                baths.add(result.getDouble("bathrooms"));
                while (result.next()) {
                    roomNum.add(result.getInt("room_number"));
                    area.add(result.getDouble("area"));
                    beds.add(result.getInt("bedrooms"));
                    baths.add(result.getDouble("bathrooms"));
                }
            }
        } catch (SQLException se) {
            System.out.println("Invalid property id.");
        }
        System.out.printf("%-20s %-10s %-15s %-10s%n", "Apartment Number:", "Area:", "Bedrooms:", "Bathrooms:");

        for (int i = 0; i < roomNum.size(); i++) {
            System.out.printf("%-20d %-10.1f %-15d %-10.1f%n", roomNum.get(i), area.get(i), beds.get(i), baths.get(i));
        }
    }

    public static void generateAllPropertyReports(Connection conn) {
        ArrayList<Integer> propertyIds = getProperties(conn);
        for (int id : propertyIds) {
            PropertyManager.generatePropretyReport(conn, id);
        }
    }

    public static void generateCompanyReport(Connection conn) {
        System.out.println("------------------------------------");
        int[] totals = getAptsByProperty(conn);
        System.out.println("Total number or properties " + totals[0]);
        System.out.println("Total number or apartments: " + totals[1]);
        System.out.println("Total number of tenants: " + totals[2]);
        System.out.println("------------------------------------");
    }

    private static int[] getAptsByProperty(Connection conn) {
        int[] totals = new int[3];
        try (Statement stmt = conn.createStatement()) {
            String amenityQuery = "select property_id, count(room_number) as numApts from apartment group by property_id";
            ResultSet result = stmt.executeQuery(amenityQuery);
            if (!result.next()) {
                System.out.println("No properties found.");
            } else {
                System.out.println("Property id:    Number of apartments:    Tenants in Building:");
                do {
                    int propertyId = result.getInt("property_id");
                    totals[0] += 1;
                    int tensInBuilding = getTenantsInBuilding(conn, propertyId);
                    totals[2] += tensInBuilding;
                    int numApts = result.getInt("numApts");
                    totals[1] += numApts;
                    System.out.printf("%-15s %-25s %-20s%n", propertyId, numApts, tensInBuilding);
                } while (result.next());
                System.out.println();
                return totals;
            }
        } catch (SQLException se) {
            System.out.println("No properties found.");
        }
        return totals;
    }

    static int getTenantsInBuilding(Connection conn, int propertyId) {
        try (Statement stmt = conn.createStatement()) {
            String amenityQuery = "select count(tenant_id) as numTenants from resident where property_id=" + propertyId;
            ResultSet result = stmt.executeQuery(amenityQuery);
            if (!result.next()) {
                System.out.println("No properties found.");
            } else {
                return result.getInt("numTenants");
            }
        } catch (SQLException se) {
            System.out.println("No properties found.");
        }
        return 0;
    }

    private static ArrayList<String> getPrivateAmenities(Connection conn) {
        ArrayList<String> privateAmenities = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String amenityQuery = "select name from private_amenity";
            ResultSet result = stmt.executeQuery(amenityQuery);
            if (!result.next()) {
                System.out.println("No amenities found.");
            } else {
                String amenName = result.getString("name");
                privateAmenities.add(amenName);
                while (result.next()) {
                    amenName = result.getString("name");
                    privateAmenities.add(amenName);
                }
            }
        } catch (SQLException se) {
            System.out.println("No amenities found.");
        }

        return privateAmenities;
    }

    public static ArrayList<String> getPublicAmenities(Connection conn) {
        ArrayList<String> publicAmenities = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            String amenityQuery = "select name from public_amenity";
            ResultSet result = stmt.executeQuery(amenityQuery);
            if (!result.next()) {
                System.out.println("No amenities found.");
            } else {
                String name = result.getString("name");
                publicAmenities.add(name);
                while (result.next()) {
                    name = result.getString("name");
                    publicAmenities.add(name);
                }
            }
        } catch (SQLException se) {
            System.out.println("No amenities found.");
        }

        return publicAmenities;
    }

    public static void generatePublicAmens(Connection conn, int propertyId, ArrayList<String> publicAmenities) {
        Random random = new Random();
        for (String amen : publicAmenities) {
            int randomNum = random.nextInt(10);
            if (randomNum < 8) {
                try (Statement stmt = conn.createStatement()) {
                    String insert = "insert into building_amenity (property_id, name) values ('"
                            + propertyId + "', '" + amen + "')";
                    int rowsAffected = stmt.executeUpdate(insert);
                    if (rowsAffected == 0) {
                        System.out.println("Amenity add failed.");
                        return;
                    }
                } catch (SQLException se) {
                    System.out.println("Amenity add failed.");
                    return;
                }
            }
        }
        System.out.println();
    }

}
