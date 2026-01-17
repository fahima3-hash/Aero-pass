package com.aeropass.utils;

import java.util.*;

public class SeatSelectionUtils {

    // Available seat types
    public static final String AISLE = "AISLE";
    public static final String WINDOW = "WINDOW";
    public static final String MIDDLE = "MIDDLE";

    /**
     * Main method to select seats based on preference
     */
    public static List<String> selectSeats(List<String> availableSeats, int numSeats, String seatType) {
        List<String> selected = new ArrayList<>();

        switch (seatType.toUpperCase()) {
            case AISLE:
                selected = selectAisleSeats(availableSeats, numSeats);
                break;
            case WINDOW:
                selected = selectWindowSeats(availableSeats, numSeats);
                break;
            case MIDDLE:
                selected = selectMiddleSeats(availableSeats, numSeats);
                break;
            default:
                selected = selectAnySeats(availableSeats, numSeats);
        }

        return selected;
    }

    /**
     * Backtracking algorithm for aisle seats
     */
    public static List<String> backtrackAisleSeats(List<String> allSeats, int numSeats,
                                                   ArrayList<Object> selectedSeats, int currentIndex) {
        List<String> result = new ArrayList<>();

        if (selectedSeats.size() >= numSeats) {
            // Convert Object list to String list
            for (Object obj : selectedSeats) {
                if (obj instanceof String) {
                    result.add((String) obj);
                }
            }
            return result;
        }

        for (int i = currentIndex; i < allSeats.size(); i++) {
            String seat = allSeats.get(i);

            if (isAisleSeat(seat) && !selectedSeats.contains(seat)) {
                selectedSeats.add(seat);

                List<String> tempResult = backtrackAisleSeats(allSeats, numSeats, selectedSeats, i + 1);

                if (tempResult.size() >= numSeats) {
                    return tempResult;
                }

                selectedSeats.remove(selectedSeats.size() - 1);
            }
        }

        return result;
    }

    /**
     * Select aisle seats (simplified version)
     */
    public static List<String> selectAisleSeats(List<String> availableSeats, int numSeats) {
        List<String> aisleSeats = new ArrayList<>();

        for (String seat : availableSeats) {
            if (isAisleSeat(seat) && aisleSeats.size() < numSeats) {
                aisleSeats.add(seat);
            }
        }

        return aisleSeats;
    }

    /**
     * Select window seats
     */
    public static List<String> selectWindowSeats(List<String> availableSeats, int numSeats) {
        List<String> windowSeats = new ArrayList<>();

        for (String seat : availableSeats) {
            if (isWindowSeat(seat) && windowSeats.size() < numSeats) {
                windowSeats.add(seat);
            }
        }

        return windowSeats;
    }

    /**
     * Select middle seats
     */
    public static List<String> selectMiddleSeats(List<String> availableSeats, int numSeats) {
        List<String> middleSeats = new ArrayList<>();

        for (String seat : availableSeats) {
            if (isMiddleSeat(seat) && middleSeats.size() < numSeats) {
                middleSeats.add(seat);
            }
        }

        return middleSeats;
    }

    /**
     * Select any available seats
     */
    public static List<String> selectAnySeats(List<String> availableSeats, int numSeats) {
        return availableSeats.subList(0, Math.min(numSeats, availableSeats.size()));
    }

    /**
     * Check if seat is aisle seat
     * In 3-3 configuration: A, C, D, F are aisle seats
     */
    private static boolean isAisleSeat(String seat) {
        if (seat == null || seat.isEmpty()) return false;

        char seatLetter = seat.charAt(seat.length() - 1); // Get last character
        return seatLetter == 'A' || seatLetter == 'C' || seatLetter == 'D' || seatLetter == 'F';
    }

    /**
     * Check if seat is window seat
     * In 3-3 configuration: A and F are window seats
     */
    private static boolean isWindowSeat(String seat) {
        if (seat == null || seat.isEmpty()) return false;

        char seatLetter = seat.charAt(seat.length() - 1);
        return seatLetter == 'A' || seatLetter == 'F';
    }

    /**
     * Check if seat is middle seat
     * In 3-3 configuration: B and E are middle seats
     */
    private static boolean isMiddleSeat(String seat) {
        if (seat == null || seat.isEmpty()) return false;

        char seatLetter = seat.charAt(seat.length() - 1);
        return seatLetter == 'B' || seatLetter == 'E';
    }

    /**
     * Generate all seats for a flight
     */
    public static List<String> generateSeats(int rows, String[] columns) {
        List<String> allSeats = new ArrayList<>();

        for (int row = 1; row <= rows; row++) {
            for (String col : columns) {
                allSeats.add(row + col);
            }
        }

        return allSeats;
    }

    /**
     * Mark seats as booked
     */
    public static List<String> markSeatsBooked(List<String> allSeats, List<String> bookedSeats) {
        List<String> availableSeats = new ArrayList<>(allSeats);
        availableSeats.removeAll(bookedSeats);
        return availableSeats;
    }

    /**
     * Find seats together (group booking)
     */
    public static List<String> findSeatsTogether(List<String> availableSeats, int groupSize) {
        List<String> groupSeats = new ArrayList<>();

        // Simple algorithm: find consecutive seats in same row
        Map<Integer, List<String>> seatsByRow = new HashMap<>();

        for (String seat : availableSeats) {
            int row = extractRowNumber(seat);
            seatsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(seat);
        }

        for (List<String> rowSeats : seatsByRow.values()) {
            if (rowSeats.size() >= groupSize) {
                // Sort seats by column
                rowSeats.sort(Comparator.comparing(s -> s.charAt(s.length() - 1)));

                // Check for consecutive seats
                for (int i = 0; i <= rowSeats.size() - groupSize; i++) {
                    boolean consecutive = true;
                    for (int j = 0; j < groupSize - 1; j++) {
                        if (!areSeatsConsecutive(rowSeats.get(i + j), rowSeats.get(i + j + 1))) {
                            consecutive = false;
                            break;
                        }
                    }

                    if (consecutive) {
                        return rowSeats.subList(i, i + groupSize);
                    }
                }
            }
        }

        return groupSeats;
    }

    /**
     * Extract row number from seat (e.g., "12A" → 12)
     */
    private static int extractRowNumber(String seat) {
        try {
            return Integer.parseInt(seat.replaceAll("[A-Z]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if two seats are consecutive
     */
    private static boolean areSeatsConsecutive(String seat1, String seat2) {
        if (extractRowNumber(seat1) != extractRowNumber(seat2)) {
            return false;
        }

        char col1 = seat1.charAt(seat1.length() - 1);
        char col2 = seat2.charAt(seat2.length() - 1);

        return Math.abs(col1 - col2) == 1;
    }

    /**
     * Test method
     */
    public static void main(String[] args) {
        // Generate seats for a flight (30 rows, ABC DEF configuration)
        String[] columns = {"A", "B", "C", "D", "E", "F"};
        List<String> allSeats = generateSeats(30, columns);

        System.out.println("Total seats: " + allSeats.size());
        System.out.println("First 10 seats: " + allSeats.subList(0, 10));

        // Test seat selection
        List<String> availableSeats = markSeatsBooked(allSeats,
                Arrays.asList("1A", "1B", "1C", "2A", "2F"));

        System.out.println("\nAvailable seats: " + availableSeats.size());

        // Test aisle seat selection
        List<String> aisleSeats = selectAisleSeats(availableSeats, 3);
        System.out.println("Aisle seats selected: " + aisleSeats);

        // Test window seat selection
        List<String> windowSeats = selectWindowSeats(availableSeats, 2);
        System.out.println("Window seats selected: " + windowSeats);

        // Test backtracking
        ArrayList<Object> selected = new ArrayList<>();
        List<String> backtrackResult = backtrackAisleSeats(availableSeats, 4, selected, 0);
        System.out.println("Backtracking result: " + backtrackResult);

        // Test group booking
        List<String> groupSeats = findSeatsTogether(availableSeats, 3);
        System.out.println("Group seats (3 together): " + groupSeats);
    }
}