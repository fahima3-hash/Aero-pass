import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

// ✅ ADD THESE IMPORT STATEMENTS
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.util.*;

public class AeropassBookingSystem extends Application {

    // MongoDB connection dewar part
    private boolean mongoDBConnected = false;
    // টেবিল ভিউ এবং ফ্লাইট ডেটা
    //mongoDB connection perfectly hoise kina oita track
    private TableView<Flight> routesTable;
    private ObservableList<Flight> allFlights;
    // UI er kaj
    private Label resultLabel;
    private ComboBox<String> fromComboBox;
    private ComboBox<String> toComboBox;
    private DatePicker datePicker;
    private TextField seatsField;
    private ComboBox<String> seatingPreferenceComboBox;
    // বুকিং সম্পর্কিত ভেরিয়েবল
    private Stage primaryStage;
    private Flight selectedFlight;
    private String selectedClass = "Economy Class";
    private List<String> selectedSeats = new ArrayList<>();
    private Set<String> bookedSeats = new HashSet<>();
    private Map<String, Button> seatButtons = new HashMap<>();

    // Discount-related variables
    private String appliedDiscountCode = "";
    private double discountPercentage = 0.0;
    private double discountAmount = 0.0;
    private Map<String, Discount> discountCodes = new HashMap<>();

    // Flight graph data structure
    private Map<String, List<FlightEdge>> flightGraph;

    // New variable to track backtracking-selected seats
    private Set<String> backtrackingSelectedSeats = new HashSet<>();

    // Auto-select state tracking
    private boolean autoSelectActive = false;
    private Label autoSelectStatus;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Aeropass Booking System");

        // ✅ ADD THIS LINE - Initialize MongoDB connection
        mongoDBConnected = MongoDBConnection.connect();

        initializeFlightGraph();
        initializeBookedSeats();
        initializeDiscountCodes();

        // Show welcome screen first
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        // Create a StackPane to layer content
        StackPane welcomeLayout = new StackPane();

        // ✅ FIRST LAYER: Airplane Background Image
        ImageView backgroundImage = new ImageView();
        try {
            // Online airplane image
            Image image = new Image("https://images.unsplash.com/photo-1436491865332-7a61a109cc05?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2074&q=80");
            backgroundImage.setImage(image);
        } catch (Exception e) {
            // Fallback if online image fails
            System.out.println("Online image failed, using solid color background");
            welcomeLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #1a73e8, #0056b3);");
        }

        backgroundImage.setFitWidth(800);
        backgroundImage.setFitHeight(600);
        backgroundImage.setPreserveRatio(false);

        // ✅ SECOND LAYER: Dark overlay for better text readability
        Rectangle overlay = new Rectangle(800, 600);
        overlay.setFill(Color.rgb(0, 0, 0, 0.3)); // Semi-transparent black overlay

        // ✅ THIRD LAYER: Content (Text and Button)
        VBox contentLayout = new VBox(30);
        contentLayout.setPadding(new Insets(50));
        contentLayout.setAlignment(Pos.CENTER);
        contentLayout.setStyle("-fx-background-color: transparent;");

        // Main title
        Label titleLabel = new Label("Welcome to Aeropass");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.BLACK);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 2);");

        // Subtitle
        Label subtitleLabel = new Label("A Smart Flight Booking System");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.BLACK);
        subtitleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 8, 0, 0, 1);");

        // Features list
        VBox featuresBox = new VBox(10);
        featuresBox.setAlignment(Pos.CENTER_LEFT);
        featuresBox.setMaxWidth(400);

        String[] features = {
                "✓ Find Optimal Flight Routes",
                "✓ Smart Seat Selection with Backtracking",
                "✓ Real-time Price Comparison",
                "✓ Dynamic Discount Management",
                "✓ Group Booking Support",
                "✓ User-friendly Interface",
                "✓ Secure Payment Options"
        };

        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            featureLabel.setTextFill(Color.BLACK);
            featureLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 1);");
            featuresBox.getChildren().add(featureLabel);
        }

        // Get Started Button
        Button getStartedButton = new Button("GET STARTED");
        getStartedButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        getStartedButton.setPrefSize(200, 50);
        getStartedButton.setOnAction(e -> showMainScreen());

        // Hover effect for button
        getStartedButton.setOnMouseEntered(e -> {
            getStartedButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 10, 0, 0, 3);");
        });

        getStartedButton.setOnMouseExited(e -> {
            getStartedButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");
        });

        // Add all components to content layout
        contentLayout.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                featuresBox,
                new Separator(),
                getStartedButton
        );

        // ADD ALL LAYERS TO STACKPANE (Background first, then overlay, then content)
        welcomeLayout.getChildren().addAll(backgroundImage, overlay, contentLayout);

        Scene welcomeScene = new Scene(welcomeLayout, 800, 600);
        primaryStage.setScene(welcomeScene);
        primaryStage.show();
    }

    private void initializeDiscountCodes() {
        discountCodes.put("WELCOME20", new Discount("WELCOME20", 20.0, "Welcome discount for new customers"));
        discountCodes.put("SUMMER15", new Discount("SUMMER15", 15.0, "Summer special discount"));
        discountCodes.put("FIRST10", new Discount("FIRST10", 10.0, "First booking discount"));
        discountCodes.put("STUDENT25", new Discount("STUDENT25", 25.0, "Student discount"));
        discountCodes.put("VIP30", new Discount("VIP30", 30.0, "VIP customer discount"));
        discountCodes.put("FAMILY20", new Discount("FAMILY20", 20.0, "Family booking discount"));
    }

    private void initializeBookedSeats() {
        bookedSeats.add("1A");
        bookedSeats.add("1C");
        bookedSeats.add("2B");
        bookedSeats.add("3D");
        bookedSeats.add("5F");
        bookedSeats.add("7A");
        bookedSeats.add("7C");
        bookedSeats.add("10B");
        bookedSeats.add("12E");
        bookedSeats.add("15D");
        bookedSeats.add("15F");
        bookedSeats.add("20A");
        bookedSeats.add("20C");
        bookedSeats.add("25B");
        bookedSeats.add("25E");
    }

    private void showMainScreen() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f5f7fa;");

        VBox headerSection = createHeaderSection();
        VBox searchSection = createSearchSection();
        VBox resultsSection = createResultsSection();
        VBox bookingSection = createBookingSection();

        mainLayout.getChildren().addAll(headerSection, searchSection, resultsSection, bookingSection);

        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeFlightGraph() {
        flightGraph = new HashMap<>();

        String[] cities = {"dhaka", "chittagong", "sylhet", "rajshahi", "khulna", "jessore", "barisal", "cox's bazar", "rangpur", "comilla"};

        for (String city : cities) {
            flightGraph.put(city, new ArrayList<>());
        }

        addFlight("dhaka", "chittagong", 4200, "1h 15m");
        addFlight("chittagong", "dhaka", 4200, "1h 15m");
        addFlight("dhaka", "sylhet", 3800, "1h 10m");
        addFlight("sylhet", "dhaka", 3800, "1h 10m");
        addFlight("dhaka", "rajshahi", 2500, "1h 05m");
        addFlight("rajshahi", "dhaka", 2500, "1h 05m");
        addFlight("dhaka", "jessore", 2800, "1h 00m");
        addFlight("jessore", "dhaka", 2800, "1h 00m");
        addFlight("dhaka", "barisal", 3200, "0h 55m");
        addFlight("barisal", "dhaka", 3200, "0h 55m");
        addFlight("dhaka", "khulna", 3500, "1h 20m");
        addFlight("khulna", "dhaka", 3500, "1h 20m");
        addFlight("dhaka", "rangpur", 4000, "1h 30m");
        addFlight("rangpur", "dhaka", 4000, "1h 30m");
        addFlight("dhaka", "comilla", 1800, "0h 45m");
        addFlight("comilla", "dhaka", 1800, "0h 45m");
        addFlight("chittagong", "cox's bazar", 1800, "0h 45m");
        addFlight("cox's bazar", "chittagong", 1800, "0h 45m");
        addFlight("sylhet", "chittagong", 2200, "1h 00m");
        addFlight("chittagong", "sylhet", 2200, "1h 00m");
        addFlight("jessore", "khulna", 1200, "0h 35m");
        addFlight("khulna", "jessore", 1200, "0h 35m");
        addFlight("barisal", "khulna", 2000, "0h 50m");
        addFlight("khulna", "barisal", 2000, "0h 50m");
        addFlight("rajshahi", "rangpur", 1800, "1h 00m");
        addFlight("rangpur", "rajshahi", 1800, "1h 00m");
        addFlight("comilla", "chittagong", 1500, "0h 40m");
        addFlight("chittagong", "comilla", 1500, "0h 40m");
    }

    private void addFlight(String from, String to, int price, String duration) {
        flightGraph.get(from).add(new FlightEdge(to, price, duration));
    }

    private VBox createHeaderSection() {
        VBox headerSection = new VBox(10);
        headerSection.setPadding(new Insets(15));
        headerSection.setStyle("-fx-background-color: #1a73e8; -fx-background-radius: 10;");

        Label titleLabel = new Label("Aeropass Booking System");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Search and Select Your Flight (Bellman-Ford Optimized)");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.WHITE);

        headerSection.getChildren().addAll(titleLabel, subtitleLabel);
        return headerSection;
    }

    private VBox createSearchSection() {
        VBox searchSection = new VBox(15);
        searchSection.setPadding(new Insets(20));
        searchSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(20);
        searchGrid.setVgap(10);

        Label fromLabel = new Label("From:");
        fromComboBox = new ComboBox<>();
        fromComboBox.getItems().addAll("dhaka", "chittagong", "sylhet", "rajshahi", "khulna", "jessore", "barisal", "cox's bazar", "rangpur", "comilla");
        fromComboBox.setValue("dhaka");

        Label toLabel = new Label("To:");
        toComboBox = new ComboBox<>();
        toComboBox.getItems().addAll("dhaka", "chittagong", "sylhet", "rajshahi", "khulna", "jessore", "barisal", "cox's bazar", "rangpur", "comilla");
        toComboBox.setValue("khulna");

        Label dateLabel = new Label("Date:");
        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.of(2025, 11, 26));

        searchGrid.add(fromLabel, 0, 0);
        searchGrid.add(fromComboBox, 0, 1);
        searchGrid.add(toLabel, 1, 0);
        searchGrid.add(toComboBox, 1, 1);
        searchGrid.add(dateLabel, 2, 0);
        searchGrid.add(datePicker, 2, 1);

        Button searchButton = new Button("Find Optimal Routes");
        searchButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton.setPrefWidth(200);
        searchButton.setOnAction(e -> findOptimalRoutes());

        HBox currentSelection = new HBox(10);
        currentSelection.setStyle("-fx-background-color: #e8f0fe; -fx-padding: 10; -fx-background-radius: 5;");
        currentSelection.setAlignment(Pos.CENTER);

        fromComboBox.setOnAction(e -> updateSelectionDisplay(currentSelection));
        toComboBox.setOnAction(e -> updateSelectionDisplay(currentSelection));
        datePicker.setOnAction(e -> updateSelectionDisplay(currentSelection));

        updateSelectionDisplay(currentSelection);

        searchSection.getChildren().addAll(searchGrid, searchButton, currentSelection);
        return searchSection;
    }

    private void updateSelectionDisplay(HBox currentSelection) {
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        String date = datePicker.getValue().toString();

        Label selectionLabel = new Label("| " + from + " | " + to + " | " + date + " |");
        currentSelection.getChildren().clear();
        currentSelection.getChildren().add(selectionLabel);
    }

    private VBox createResultsSection() {
        VBox resultsSection = new VBox(15);
        resultsSection.setPadding(new Insets(20));

        resultLabel = new Label("No routes found from dhaka to khulna");
        resultLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");

        Label tableTitle = new Label("All Available Routes (Cheapest route highlighted in green):");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        routesTable = new TableView<>();
        setupRoutesTable();

        resultsSection.getChildren().addAll(resultLabel, tableTitle, routesTable);
        return resultsSection;
    }

    private void setupRoutesTable() {
        TableColumn<Flight, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(new PropertyValueFactory<>("route"));
        routeCol.setPrefWidth(300);

        TableColumn<Flight, String> stopsCol = new TableColumn<>("Stops");
        stopsCol.setCellValueFactory(new PropertyValueFactory<>("stops"));
        stopsCol.setPrefWidth(100);

        TableColumn<Flight, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationCol.setPrefWidth(100);

        TableColumn<Flight, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<Flight, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        routesTable.getColumns().addAll(routeCol, stopsCol, durationCol, priceCol, typeCol);

        routesTable.setRowFactory(tv -> new TableRow<Flight>() {
            @Override
            protected void updateItem(Flight item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isCheapest()) {
                    setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2;");
                } else {
                    setStyle("");
                }
            }
        });

        allFlights = FXCollections.observableArrayList();
        routesTable.setItems(allFlights);
    }

    private VBox createBookingSection() {
        VBox bookingSection = new VBox(15);
        bookingSection.setPadding(new Insets(20));
        bookingSection.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        HBox bookingFields = new HBox(20);

        VBox seatsSection = new VBox(5);
        Label seatsLabel = new Label("Number of Seats:");
        seatsField = new TextField();
        seatsField.setPromptText("e.g., 5");
        seatsSection.getChildren().addAll(seatsLabel, seatsField);

        VBox seatingSection = new VBox(5);
        Label seatingLabel = new Label("Seating Preference:");
        seatingPreferenceComboBox = new ComboBox<>();
        seatingPreferenceComboBox.getItems().addAll("Row-wise", "Aisle", "Window", "Random");
        seatingPreferenceComboBox.setValue("Row-wise");
        seatingSection.getChildren().addAll(seatingLabel, seatingPreferenceComboBox);

        bookingFields.getChildren().addAll(seatsSection, seatingSection);

        Button continueButton = new Button("Select Route and Continue");
        continueButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        continueButton.setPrefWidth(250);
        continueButton.setOnAction(e -> selectRouteAndContinue());

        bookingSection.getChildren().addAll(bookingFields, continueButton);
        return bookingSection;
    }

    private void findOptimalRoutes() {
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();

        allFlights.clear();

        List<Flight> flights = findAllRoutes(from, to);

        if (flights.isEmpty()) {
            resultLabel.setText("No routes found from " + from + " to " + to);
            resultLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            resultLabel.setText("Found " + flights.size() + " routes from " + from + " to " + to);
            resultLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");

            if (!flights.isEmpty()) {
                Flight cheapest = flights.stream()
                        .min(Comparator.comparingInt(Flight::getPriceValue))
                        .orElse(flights.get(0));
                cheapest.setCheapest(true);
            }

            allFlights.addAll(flights);
        }
    }

    private List<Flight> findAllRoutes(String from, String to) {
        List<Flight> allRoutes = new ArrayList<>();

        List<Flight> directFlights = findDirectFlights(from, to);
        allRoutes.addAll(directFlights);

        List<Flight> oneStopFlights = findOneStopFlights(from, to);
        allRoutes.addAll(oneStopFlights);

        List<Flight> twoStopFlights = findTwoStopFlights(from, to);
        allRoutes.addAll(twoStopFlights);

        allRoutes.sort(Comparator.comparingInt(Flight::getPriceValue));

        return allRoutes;
    }

    private List<Flight> findDirectFlights(String from, String to) {
        List<Flight> directFlights = new ArrayList<>();

        if (flightGraph.containsKey(from)) {
            for (FlightEdge edge : flightGraph.get(from)) {
                if (edge.destination.equals(to)) {
                    directFlights.add(new Flight(
                            from + " → " + to,
                            "Non-stop",
                            edge.duration,
                            "৳ " + String.format("%,d", edge.price),
                            "Direct",
                            edge.price
                    ));
                }
            }
        }

        return directFlights;
    }

    private List<Flight> findOneStopFlights(String from, String to) {
        List<Flight> oneStopFlights = new ArrayList<>();

        if (!flightGraph.containsKey(from)) return oneStopFlights;

        for (FlightEdge firstLeg : flightGraph.get(from)) {
            String intermediate = firstLeg.destination;
            if (flightGraph.containsKey(intermediate)) {
                for (FlightEdge secondLeg : flightGraph.get(intermediate)) {
                    if (secondLeg.destination.equals(to)) {
                        int totalPrice = firstLeg.price + secondLeg.price;
                        String totalDuration = calculateTotalDuration(firstLeg.duration, secondLeg.duration);

                        oneStopFlights.add(new Flight(
                                from + " → " + intermediate + " → " + to,
                                "1 stop",
                                totalDuration,
                                "৳ " + String.format("%,d", totalPrice),
                                "Alternative",
                                totalPrice
                        ));
                    }
                }
            }
        }

        return oneStopFlights;
    }

    private List<Flight> findTwoStopFlights(String from, String to) {
        List<Flight> twoStopFlights = new ArrayList<>();

        if (!flightGraph.containsKey(from)) return twoStopFlights;

        for (FlightEdge firstLeg : flightGraph.get(from)) {
            String stop1 = firstLeg.destination;
            if (flightGraph.containsKey(stop1)) {
                for (FlightEdge secondLeg : flightGraph.get(stop1)) {
                    String stop2 = secondLeg.destination;
                    if (flightGraph.containsKey(stop2)) {
                        for (FlightEdge thirdLeg : flightGraph.get(stop2)) {
                            if (thirdLeg.destination.equals(to)) {
                                int totalPrice = firstLeg.price + secondLeg.price + thirdLeg.price;
                                String totalDuration = calculateTotalDuration(
                                        firstLeg.duration,
                                        secondLeg.duration,
                                        thirdLeg.duration
                                );

                                twoStopFlights.add(new Flight(
                                        from + " → " + stop1 + " → " + stop2 + " → " + to,
                                        "2 stops",
                                        totalDuration,
                                        "৳ " + String.format("%,d", totalPrice),
                                        "Alternative",
                                        totalPrice
                                ));
                            }
                        }
                    }
                }
            }
        }

        return twoStopFlights;
    }

    private String calculateTotalDuration(String... durations) {
        int totalMinutes = 0;
        for (String duration : durations) {
            totalMinutes += parseDurationToMinutes(duration);
        }

        totalMinutes += (durations.length - 1) * 60;

        return formatMinutesToDuration(totalMinutes);
    }

    private int parseDurationToMinutes(String duration) {
        try {
            String[] parts = duration.split("h|m");
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 60;
        }
    }

    private String formatMinutesToDuration(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    private void selectRouteAndContinue() {
        selectedFlight = routesTable.getSelectionModel().getSelectedItem();

        if (selectedFlight == null) {
            showAlert("Please select a flight route to continue.");
            return;
        }

        if (seatsField.getText().isEmpty()) {
            showAlert("Please enter the number of seats.");
            return;
        }

        showSeatSelectionScreen();
    }

    private void showSeatSelectionScreen() {
        // Clear previous backtracking selections when entering seat selection
        backtrackingSelectedSeats.clear();
        autoSelectActive = false;

        VBox seatSelectionLayout = new VBox(20);
        seatSelectionLayout.setPadding(new Insets(20));
        seatSelectionLayout.setStyle("-fx-background-color: #f5f7fa;");

        Label headerLabel = new Label("Select Your Seats - " + selectedFlight.getRoute());
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headerLabel.setTextFill(Color.DARKBLUE);

        Label classLabel = new Label("Select Class:");
        classLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        ComboBox<String> classComboBox = new ComboBox<>();
        classComboBox.getItems().addAll(
                "Economy Class (Base Price + 0%)",
                "Business Class (Base Price + 50%)",
                "First Class (Base Price + 100%)"
        );
        classComboBox.setValue("Economy Class (Base Price + 0%)");

        Label priceLabel = new Label();
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        updatePriceDisplay(priceLabel, "Economy Class (Base Price + 0%)");

        HBox smartSelectionBox = new HBox(15);
        smartSelectionBox.setAlignment(Pos.CENTER_LEFT);

        Label smartLabel = new Label("Smart Seat Selection:");
        smartLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Button autoSelectButton = new Button("Auto-Select Optimal Seats");
        autoSelectButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        autoSelectButton.setOnAction(e -> autoSelectOptimalSeats());

        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        clearSelectionButton.setOnAction(e -> clearAllSelections());

        // Auto-select status label
        autoSelectStatus = new Label("Auto-select: OFF");
        autoSelectStatus.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        autoSelectStatus.setTextFill(Color.RED);

        smartSelectionBox.getChildren().addAll(smartLabel, autoSelectButton, clearSelectionButton, autoSelectStatus);

        ScrollPane seatScrollPane = new ScrollPane();
        seatScrollPane.setPrefSize(800, 400);
        seatScrollPane.setStyle("-fx-background: white; -fx-border-color: #ccc;");
        seatScrollPane.setFitToWidth(true);

        VBox seatLayoutContainer = new VBox(15);
        seatScrollPane.setContent(seatLayoutContainer);

        showEconomyClassLayout(seatLayoutContainer);

        classComboBox.setOnAction(e -> {
            String selectedClass = classComboBox.getValue();
            this.selectedClass = selectedClass;
            seatLayoutContainer.getChildren().clear();
            updatePriceDisplay(priceLabel, selectedClass);

            if (selectedClass.contains("Economy")) {
                showEconomyClassLayout(seatLayoutContainer);
            } else if (selectedClass.contains("Business")) {
                showBusinessClassLayout(seatLayoutContainer);
            } else if (selectedClass.contains("First")) {
                showFirstClassLayout(seatLayoutContainer);
            }

            // Update seat availability based on auto-select state
            updateSeatAvailability();
        });

        Label selectedSeatsLabel = new Label("Selected Seats: None");
        selectedSeatsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        selectedSeatsLabel.setTextFill(Color.DARKBLUE);

        HBox legendBox = createLegend();

        Button confirmButton = new Button("Confirm Seat Selection");
        confirmButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        confirmButton.setPrefWidth(200);
        confirmButton.setOnAction(e -> {
            if (selectedSeats.isEmpty()) {
                showAlert("Please select at least one seat.");
                return;
            }
            showDiscountPortal();
        });

        Button backButton = new Button("Back to Flight Selection");
        backButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> {
            selectedSeats.clear();
            backtrackingSelectedSeats.clear();
            autoSelectActive = false;
            showMainScreen();
        });

        seatSelectionLayout.getChildren().addAll(
                headerLabel, classLabel, classComboBox, priceLabel,
                smartSelectionBox, seatScrollPane, legendBox,
                selectedSeatsLabel, confirmButton, backButton
        );

        Scene seatScene = new Scene(seatSelectionLayout, 1000, 800);
        primaryStage.setScene(seatScene);
    }

    private void clearAllSelections() {
        // Reset auto-select state
        autoSelectActive = false;
        autoSelectStatus.setText("Auto-select: OFF");
        autoSelectStatus.setTextFill(Color.RED);

        // Clear all selections and reset seat colors
        for (String seatId : selectedSeats) {
            Button btn = seatButtons.get(seatId);
            if (btn != null) {
                String className = selectedClass.contains("Business") ? "Business" :
                        selectedClass.contains("First") ? "First" : "Economy";
                resetSeatColor(btn, className);
                // Re-enable the button if it's not booked
                if (!bookedSeats.contains(seatId)) {
                    btn.setDisable(false);
                }
            }
        }

        // Clear backtracking selections and re-enable those seats
        for (String seatId : backtrackingSelectedSeats) {
            Button btn = seatButtons.get(seatId);
            if (btn != null) {
                String className = selectedClass.contains("Business") ? "Business" :
                        selectedClass.contains("First") ? "First" : "Economy";
                resetSeatColor(btn, className);
                // Re-enable the button if it's not booked
                if (!bookedSeats.contains(seatId)) {
                    btn.setDisable(false);
                }
            }
        }

        selectedSeats.clear();
        backtrackingSelectedSeats.clear();

        // Re-enable all non-booked seats
        for (String seatId : seatButtons.keySet()) {
            Button btn = seatButtons.get(seatId);
            if (btn != null && !bookedSeats.contains(seatId)) {
                btn.setDisable(false);
                resetSeatColor(btn, selectedClass.contains("Business") ? "Business" :
                        selectedClass.contains("First") ? "First" : "Economy");
            }
        }
    }

    private void showDiscountPortal() {
        //  Use ScrollPane as main container
        ScrollPane mainScrollPane = new ScrollPane();
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setStyle("-fx-background: #f5f7fa; -fx-border-color: transparent;");

        VBox discountLayout = new VBox(20);
        discountLayout.setPadding(new Insets(30));
        discountLayout.setStyle("-fx-background-color: #f5f7fa;");
        discountLayout.setAlignment(Pos.TOP_CENTER);

        // Set minimum height to ensure scroll works
        discountLayout.setMinHeight(Region.USE_PREF_SIZE);

        Label headerLabel = new Label("Discount Portal");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        headerLabel.setTextFill(Color.DARKBLUE);

        VBox flightInfoBox = new VBox(10);
        flightInfoBox.setPadding(new Insets(15));
        flightInfoBox.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 10;");
        flightInfoBox.setMaxWidth(700);

        Label flightInfoLabel = new Label("Flight Information");
        flightInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label routeLabel = new Label("Route: " + selectedFlight.getRoute());
        Label classLabel = new Label("Class: " + selectedClass.split(" ")[0] + " " + selectedClass.split(" ")[1]);
        Label seatsLabel = new Label("Selected Seats: " + String.join(", ", selectedSeats));

        int basePrice = selectedFlight.getPriceValue();
        int priceMultiplier = 1;
        if (selectedClass.contains("Business")) {
            priceMultiplier = 2;
        } else if (selectedClass.contains("First")) {
            priceMultiplier = 3;
        }
        int totalPrice = basePrice * priceMultiplier * selectedSeats.size();

        Label priceLabel = new Label("Total Price: ৳ " + String.format("%,d", totalPrice));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        flightInfoBox.getChildren().addAll(flightInfoLabel, routeLabel, classLabel, seatsLabel, priceLabel);

        VBox discountSection = new VBox(15);
        discountSection.setPadding(new Insets(20));
        discountSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ccc;");
        discountSection.setMaxWidth(700);

        Label discountTitle = new Label("Apply Discount Code");
        discountTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox discountInputBox = new HBox(10);
        discountInputBox.setAlignment(Pos.CENTER_LEFT);

        Label discountCodeLabel = new Label("Discount Code:");
        TextField discountField = new TextField();
        discountField.setPromptText("Enter discount code");
        discountField.setPrefWidth(150);

        Button applyDiscountButton = new Button("Apply Discount");
        applyDiscountButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");

        Button viewDiscountsButton = new Button("View All Discounts");
        viewDiscountsButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");

        discountInputBox.getChildren().addAll(discountCodeLabel, discountField, applyDiscountButton, viewDiscountsButton);

        VBox priceInfoBox = new VBox(10);
        Label discountStatusLabel = new Label("No discount applied");
        discountStatusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        Label finalPriceLabel = new Label("Final Price: ৳ " + String.format("%,d", totalPrice));
        finalPriceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        finalPriceLabel.setTextFill(Color.DARKRED);

        priceInfoBox.getChildren().addAll(discountStatusLabel, finalPriceLabel);

        applyDiscountButton.setOnAction(e -> {
            String code = discountField.getText();
            if (code == null || code.trim().isEmpty()) {
                discountStatusLabel.setText("Please enter a discount code");
                discountStatusLabel.setTextFill(Color.RED);
                return;
            }

            String upperCode = code.toUpperCase();
            Discount discount = discountCodes.get(upperCode);

            if (discount != null) {
                appliedDiscountCode = upperCode;
                discountPercentage = discount.getPercentage();
                discountAmount = (totalPrice * discountPercentage) / 100;
                int finalPrice = (int) (totalPrice - discountAmount);

                discountStatusLabel.setText("Discount applied: " + discountPercentage + "% off - " + discount.getDescription());
                discountStatusLabel.setTextFill(Color.GREEN);
                finalPriceLabel.setText("Final Price: ৳ " + String.format("%,d", finalPrice) +
                        " (Saved: ৳ " + String.format("%,d", (int)discountAmount) + ")");
                finalPriceLabel.setTextFill(Color.DARKGREEN);
            } else {
                appliedDiscountCode = "";
                discountPercentage = 0.0;
                discountAmount = 0.0;
                discountStatusLabel.setText("Invalid discount code");
                discountStatusLabel.setTextFill(Color.RED);
                finalPriceLabel.setText("Final Price: ৳ " + String.format("%,d", totalPrice));
                finalPriceLabel.setTextFill(Color.DARKRED);
            }
        });

        viewDiscountsButton.setOnAction(e -> showAllDiscounts());

        //  Payment Section with Helpline and Bank Details
        VBox paymentSection = new VBox(15);
        paymentSection.setPadding(new Insets(20));
        paymentSection.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 10; -fx-border-color: #ff9800;");
        paymentSection.setMaxWidth(700);

        Label paymentTitle = new Label("📞 Payment Information");
        paymentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        paymentTitle.setTextFill(Color.DARKORANGE);

        // Helpline Information
        VBox helplineBox = new VBox(10);
        helplineBox.setPadding(new Insets(10));
        helplineBox.setStyle("-fx-background-color: #ffecb3; -fx-background-radius: 5;");

        Label helplineLabel = new Label("🆘 24/7 Helpline Numbers");
        helplineLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label helpline1 = new Label("📱 Mobile: +880 1711-123456 (WhatsApp Available)");
        Label helpline2 = new Label("☎️  Landline: +880 2-9876543");
        Label helpline3 = new Label("📧 Email: support@aeropass.com");
        Label helpline4 = new Label("🕒 Service Hours: 24/7");

        helpline1.setStyle("-fx-font-size: 12;");
        helpline2.setStyle("-fx-font-size: 12;");
        helpline3.setStyle("-fx-font-size: 12;");
        helpline4.setStyle("-fx-font-size: 12;");

        helplineBox.getChildren().addAll(helplineLabel, helpline1, helpline2, helpline3, helpline4);

        // Bank Account Details
        VBox bankBox = new VBox(10);
        bankBox.setPadding(new Insets(10));
        bankBox.setStyle("-fx-background-color: #c8e6c9; -fx-background-radius: 5;");

        Label bankLabel = new Label("🏦 Bank Account Details for Payment");
        bankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        GridPane bankGrid = new GridPane();
        bankGrid.setHgap(15);
        bankGrid.setVgap(8);
        bankGrid.setPadding(new Insets(5));

        bankGrid.add(new Label("Bank Name:"), 0, 0);
        bankGrid.add(new Label("Aeropass Bank Ltd."), 1, 0);

        bankGrid.add(new Label("Account Name:"), 0, 1);
        bankGrid.add(new Label("Aeropass Airlines"), 1, 1);

        bankGrid.add(new Label("Account Number:"), 0, 2);
        bankGrid.add(new Label("1234567890123"), 1, 2);

        bankGrid.add(new Label("Routing Number:"), 0, 3);
        bankGrid.add(new Label("AEROPASSBDDH"), 1, 3);

        bankGrid.add(new Label("Branch:"), 0, 4);
        bankGrid.add(new Label("Dhaka Main Branch"), 1, 4);

        Label paymentNote = new Label("💡 After payment, please send the transaction ID to our WhatsApp number or email with your booking details.");
        paymentNote.setWrapText(true);
        paymentNote.setStyle("-fx-font-size: 11; -fx-text-fill: #d32f2f;");

        bankBox.getChildren().addAll(bankLabel, bankGrid, paymentNote);

        // Mobile Banking
        VBox mobileBankingBox = new VBox(10);
        mobileBankingBox.setPadding(new Insets(10));
        mobileBankingBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 5;");

        Label mobileBankingLabel = new Label("📱 Mobile Banking Options");
        mobileBankingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox mobileBankingGrid = new HBox(20);
        VBox bKashBox = new VBox(5);
        bKashBox.setStyle("-fx-border-color: #e91e63; -fx-border-radius: 5; -fx-padding: 10;");
        Label bKashLabel = new Label("bKash");
        bKashLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e63;");
        Label bKashNumber = new Label("Merchant: 01711-000123");
        Label bKashType = new Label("Type: Personal");

        VBox nagadBox = new VBox(5);
        nagadBox.setStyle("-fx-border-color: #f57c00; -fx-border-radius: 5; -fx-padding: 10;");
        Label nagadLabel = new Label("Nagad");
        nagadLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f57c00;");
        Label nagadNumber = new Label("Merchant: 01711-000456");
        Label nagadType = new Label("Type: Merchant");

        VBox rocketBox = new VBox(5);
        rocketBox.setStyle("-fx-border-color: #388e3c; -fx-border-radius: 5; -fx-padding: 10;");
        Label rocketLabel = new Label("Rocket");
        rocketLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #388e3c;");
        Label rocketNumber = new Label("Account: 01711-000789");
        Label rocketType = new Label("Type: Personal");

        bKashBox.getChildren().addAll(bKashLabel, bKashNumber, bKashType);
        nagadBox.getChildren().addAll(nagadLabel, nagadNumber, nagadType);
        rocketBox.getChildren().addAll(rocketLabel, rocketNumber, rocketType);

        mobileBankingGrid.getChildren().addAll(bKashBox, nagadBox, rocketBox);
        mobileBankingBox.getChildren().addAll(mobileBankingLabel, mobileBankingGrid);

        paymentSection.getChildren().addAll(
                paymentTitle,
                helplineBox,
                bankBox,
                mobileBankingBox
        );

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button backButton = new Button("Back to Seat Selection");
        backButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> showSeatSelectionScreen());

        Button continueButton = new Button("Continue to Passenger Details");
        continueButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        continueButton.setOnAction(e -> showPersonalDetailsScreen());

        buttonBox.getChildren().addAll(backButton, continueButton);

        discountSection.getChildren().addAll(discountTitle, discountInputBox, priceInfoBox);

        discountLayout.getChildren().addAll(
                headerLabel,
                flightInfoBox,
                discountSection,
                paymentSection,
                buttonBox
        );

        mainScrollPane.setContent(discountLayout);

        Scene discountScene = new Scene(mainScrollPane, 900, 650); // Scene height কমিয়ে রাখা
        primaryStage.setScene(discountScene);
    }

    private void showAllDiscounts() {
        Stage discountStage = new Stage();
        discountStage.setTitle("Available Discount Codes");

        VBox discountLayout = new VBox(15);
        discountLayout.setPadding(new Insets(20));
        discountLayout.setStyle("-fx-background-color: #f5f7fa;");

        Label titleLabel = new Label("Available Discount Codes");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.DARKBLUE);

        TableView<Discount> discountTable = new TableView<>();

        TableColumn<Discount, String> codeCol = new TableColumn<>("Discount Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(150);

        TableColumn<Discount, Double> percentageCol = new TableColumn<>("Discount %");
        percentageCol.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        percentageCol.setPrefWidth(100);

        TableColumn<Discount, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionCol.setPrefWidth(300);

        discountTable.getColumns().addAll(codeCol, percentageCol, descriptionCol);

        ObservableList<Discount> discountList = FXCollections.observableArrayList(discountCodes.values());
        discountTable.setItems(discountList);

        Label infoLabel = new Label("Tip: Enter any of these codes in the discount portal to get special offers!");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        infoLabel.setTextFill(Color.DARKGREEN);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        closeButton.setOnAction(e -> discountStage.close());

        discountLayout.getChildren().addAll(titleLabel, discountTable, infoLabel, closeButton);

        Scene discountScene = new Scene(discountLayout, 600, 400);
        discountStage.setScene(discountScene);
        discountStage.show();
    }

    private void showPersonalDetailsScreen() {
        VBox personalDetailsLayout = new VBox(20);
        personalDetailsLayout.setPadding(new Insets(30));
        personalDetailsLayout.setStyle("-fx-background-color: #f5f7fa;");
        personalDetailsLayout.setAlignment(Pos.TOP_CENTER);

        Label headerLabel = new Label("Passenger Details");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        headerLabel.setTextFill(Color.DARKBLUE);

        VBox flightInfoBox = new VBox(10);
        flightInfoBox.setPadding(new Insets(15));
        flightInfoBox.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 10;");

        Label flightInfoLabel = new Label("Flight Information");
        flightInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label routeLabel = new Label("Route: " + selectedFlight.getRoute());
        Label classLabel = new Label("Class: " + selectedClass.split(" ")[0] + " " + selectedClass.split(" ")[1]);
        Label seatsLabel = new Label("Selected Seats: " + String.join(", ", selectedSeats));

        Label discountInfoLabel = new Label();
        if (!appliedDiscountCode.isEmpty()) {
            discountInfoLabel.setText("Discount Applied: " + appliedDiscountCode + " (" + discountPercentage + "% off)");
            discountInfoLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        } else {
            discountInfoLabel.setText("No discount applied");
            discountInfoLabel.setStyle("-fx-text-fill: #757575;");
        }

        flightInfoBox.getChildren().addAll(flightInfoLabel, routeLabel, classLabel, seatsLabel, discountInfoLabel);

        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ccc;");
        formContainer.setMaxWidth(600);

        Label formTitle = new Label("Passenger Information");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        int numPassengers = selectedSeats.size();
        ScrollPane formScrollPane = new ScrollPane();
        formScrollPane.setPrefSize(800, 400);
        formScrollPane.setStyle("-fx-background: transparent;");
        formScrollPane.setFitToWidth(true);

        VBox passengersForm = new VBox(20);

        for (int i = 1; i <= numPassengers; i++) {
            VBox passengerForm = createPassengerForm(i, selectedSeats.get(i - 1));
            passengersForm.getChildren().add(passengerForm);

            if (i < numPassengers) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(10, 0, 10, 0));
                passengersForm.getChildren().add(separator);
            }
        }

        formScrollPane.setContent(passengersForm);

        VBox contactForm = new VBox(10);
        contactForm.setPadding(new Insets(15));
        contactForm.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label contactTitle = new Label("Contact Information");
        contactTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(35);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");
        phoneField.setPrefHeight(35);

        contactForm.getChildren().addAll(contactTitle, emailField, phoneField);

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button backButton = new Button("Back to Discount Portal");
        backButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> showDiscountPortal());

        Button confirmBookingButton = new Button("Confirm Booking");
        confirmBookingButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        confirmBookingButton.setOnAction(e -> processBooking(emailField.getText(), phoneField.getText()));

        buttonBox.getChildren().addAll(backButton, confirmBookingButton);

        personalDetailsLayout.getChildren().addAll(
                headerLabel, flightInfoBox, formTitle, formScrollPane,
                contactForm, buttonBox
        );

        Scene personalScene = new Scene(personalDetailsLayout, 900, 700);
        primaryStage.setScene(personalScene);
    }

    private VBox createPassengerForm(int passengerNumber, String seatNumber) {
        VBox passengerForm = new VBox(10);
        passengerForm.setPadding(new Insets(15));
        passengerForm.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label passengerLabel = new Label("Passenger " + passengerNumber + " (Seat: " + seatNumber + ")");
        passengerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setPrefHeight(35);

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setPrefHeight(35);

        Label dobLabel = new Label("Date of Birth:");
        DatePicker dobPicker = new DatePicker();
        dobPicker.setPrefHeight(35);

        Label genderLabel = new Label("Gender:");
        ComboBox<String> genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        genderComboBox.setValue("Male");
        genderComboBox.setPrefHeight(35);

        Label idLabel = new Label("Passport/NID Number:");
        TextField idField = new TextField();
        idField.setPromptText("Passport or NID Number");
        idField.setPrefHeight(35);

        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);
        formGrid.add(lastNameLabel, 0, 1);
        formGrid.add(lastNameField, 1, 1);
        formGrid.add(dobLabel, 0, 2);
        formGrid.add(dobPicker, 1, 2);
        formGrid.add(genderLabel, 0, 3);
        formGrid.add(genderComboBox, 1, 3);
        formGrid.add(idLabel, 0, 4);
        formGrid.add(idField, 1, 4);

        passengerForm.getChildren().addAll(passengerLabel, formGrid);
        return passengerForm;
    }

    private void processBooking(String email, String phone) {
        if (email.isEmpty() || phone.isEmpty()) {
            showAlert("Please fill in all contact information.");
            return;
        }

        int basePrice = selectedFlight.getPriceValue();
        int priceMultiplier = 1;

        if (selectedClass.contains("Business")) {
            priceMultiplier = 2;
        } else if (selectedClass.contains("First")) {
            priceMultiplier = 3;
        }

        int totalPrice = basePrice * priceMultiplier * selectedSeats.size();
        int originalPrice = totalPrice;

        if (!appliedDiscountCode.isEmpty()) {
            discountAmount = (totalPrice * discountPercentage) / 100;
            totalPrice = (int) (totalPrice - discountAmount);
        }

        //  Save to MongoDB
        if (mongoDBConnected) {
            MongoDBConnection.saveBooking(
                    selectedFlight.getRoute(),
                    selectedClass,
                    selectedSeats,
                    email,
                    phone,
                    totalPrice,
                    appliedDiscountCode
            );
        }

        String message = String.format(
                "Booking Confirmed!\n\n" +
                        "Flight Details:\n" +
                        "• Route: %s\n" +
                        "• Class: %s\n" +
                        "• Seats: %s\n" +
                        "• Price per seat: ৳ %s\n" +
                        "• Total Price: ৳ %s\n",
                selectedFlight.getRoute(),
                selectedClass.split(" ")[0] + " " + selectedClass.split(" ")[1],
                String.join(", ", selectedSeats),
                String.format("%,d", basePrice * priceMultiplier),
                String.format("%,d", originalPrice)
        );

        if (!appliedDiscountCode.isEmpty()) {
            message += String.format(
                    "• Discount Applied: %s (%s%%)\n" +
                            "• Discount Amount: -৳ %s\n" +
                            "• Final Price: ৳ %s\n\n",
                    appliedDiscountCode,
                    discountPercentage,
                    String.format("%,d", (int)discountAmount),
                    String.format("%,d", totalPrice)
            );
        } else {
            message += "\n";
        }

        message += String.format(
                "Contact Information:\n" +
                        "• Email: %s\n" +
                        "• Phone: %s\n\n" +
                        "📞 Payment Instructions:\n" +
                        "• Send payment to any of our bank accounts or mobile banking numbers\n" +
                        "• Helpline: +880 1711-123456 for payment assistance\n" +
                        "• WhatsApp: +880 1711-123456 for quick support\n" +
                        "• Email your transaction ID to: payment@aeropass.com\n\n" +
                        "Your tickets will be sent to your email after payment confirmation.\n" +
                        "Thank you for choosing Aeropass! Have a wonderful journey!",
                email,
                phone
        );

        showAlert("Booking Successful!", message);

        bookedSeats.addAll(selectedSeats);
        selectedSeats.clear();
        backtrackingSelectedSeats.clear();
        autoSelectActive = false;
        appliedDiscountCode = "";
        discountPercentage = 0.0;
        discountAmount = 0.0;
        showMainScreen();
    }

    private HBox createLegend() {
        HBox legendBox = new HBox(20);
        legendBox.setAlignment(Pos.CENTER);

        HBox availableBox = new HBox(5);
        Region availableColor = new Region();
        availableColor.setPrefSize(20, 20);
        availableColor.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9;");
        Label availableLabel = new Label("Available");
        availableBox.getChildren().addAll(availableColor, availableLabel);

        HBox bookedBox = new HBox(5);
        Region bookedColor = new Region();
        bookedColor.setPrefSize(20, 20);
        bookedColor.setStyle("-fx-background-color: #ffcdd2; -fx-border-color: #f44336;");
        Label bookedLabel = new Label("Booked");
        bookedBox.getChildren().addAll(bookedColor, bookedLabel);

        HBox selectedBox = new HBox(5);
        Region selectedColor = new Region();
        selectedColor.setPrefSize(20, 20);
        selectedColor.setStyle("-fx-background-color: #4caf50; -fx-border-color: #388e3c;");
        Label selectedLabel = new Label("Selected");
        selectedBox.getChildren().addAll(selectedColor, selectedLabel);

        HBox backtrackingBox = new HBox(5);
        Region backtrackingColor = new Region();
        backtrackingColor.setPrefSize(20, 20);
        backtrackingColor.setStyle("-fx-background-color: #9c27b0; -fx-border-color: #7b1fa2;");
        Label backtrackingLabel = new Label("Auto-Selected");
        backtrackingBox.getChildren().addAll(backtrackingColor, backtrackingLabel);

        HBox disabledBox = new HBox(5);
        Region disabledColor = new Region();
        disabledColor.setPrefSize(20, 20);
        disabledColor.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #bdbdbd;");
        Label disabledLabel = new Label("Disabled (Auto-select)");
        disabledBox.getChildren().addAll(disabledColor, disabledLabel);

        legendBox.getChildren().addAll(availableBox, bookedBox, selectedBox, backtrackingBox, disabledBox);
        return legendBox;
    }

    private void updatePriceDisplay(Label priceLabel, String selectedClass) {
        int basePrice = selectedFlight.getPriceValue();
        int finalPrice = basePrice;
        String classType = "";

        if (selectedClass.contains("Business")) {
            finalPrice = (int) (basePrice * 1.5);
            classType = "Business Class";
        } else if (selectedClass.contains("First")) {
            finalPrice = basePrice * 2;
            classType = "First Class";
        } else {
            classType = "Economy Class";
        }

        try {
            int numSeats = Integer.parseInt(seatsField.getText());
            int totalPrice = finalPrice * numSeats;

            if (!appliedDiscountCode.isEmpty()) {
                double discountedPrice = totalPrice - (totalPrice * discountPercentage / 100);
                priceLabel.setText("Class: " + classType + " | Price per seat: ৳ " +
                        String.format("%,d", finalPrice) + " | Total: ৳ " +
                        String.format("%,d", totalPrice) + " | After " + discountPercentage + "% discount: ৳ " +
                        String.format("%,d", (int)discountedPrice));
            } else {
                priceLabel.setText("Class: " + classType + " | Price per seat: ৳ " +
                        String.format("%,d", finalPrice) + " | Total for " +
                        seatsField.getText() + " seats: ৳ " +
                        String.format("%,d", totalPrice));
            }
        } catch (NumberFormatException e) {
            priceLabel.setText("Class: " + classType + " | Price per seat: ৳ " +
                    String.format("%,d", finalPrice) + " | Enter number of seats");
        }
        priceLabel.setTextFill(Color.RED);
    }

    private void showEconomyClassLayout(VBox container) {
        Label title = new Label("Economy Class - 90 Seats (3-3-3 Configuration)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox economyLayout = createSeatLayout(30, 3, 3, "Economy");
        container.getChildren().addAll(title, economyLayout);
    }

    private void showBusinessClassLayout(VBox container) {
        Label title = new Label("Business Class - 12 Seats (2-2 Configuration)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox businessLayout = createSeatLayout(6, 2, 2, "Business");
        container.getChildren().addAll(title, businessLayout);
    }

    private void showFirstClassLayout(VBox container) {
        Label title = new Label("First Class - 6 Seats (1-1 Configuration)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        VBox firstClassLayout = createSeatLayout(3, 1, 2, "First");
        container.getChildren().addAll(title, firstClassLayout);
    }

    private VBox createSeatLayout(int rows, int seatsPerSection, int sections, String className) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10;");

        for (int row = 1; row <= rows; row++) {
            HBox rowLayout = new HBox(15);
            rowLayout.setAlignment(Pos.CENTER);

            Label rowLabel = new Label("Row " + row);
            rowLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            rowLabel.setMinWidth(50);

            for (int section = 0; section < sections; section++) {
                HBox sectionLayout = new HBox(5);

                for (int seatNum = 1; seatNum <= seatsPerSection; seatNum++) {
                    char seatLetter = (char) ('A' + (section * seatsPerSection) + (seatNum - 1));
                    String seatId = row + String.valueOf(seatLetter);

                    Button seatButton = createSeatButton(seatId, className);
                    sectionLayout.getChildren().add(seatButton);
                }

                if (section < sections - 1) {
                    Region gap = new Region();
                    gap.setMinWidth(20);
                    rowLayout.getChildren().addAll(sectionLayout, gap);
                } else {
                    rowLayout.getChildren().add(sectionLayout);
                }
            }

            HBox completeRow = new HBox(10);
            completeRow.setAlignment(Pos.CENTER_LEFT);
            completeRow.getChildren().addAll(rowLabel, rowLayout);

            layout.getChildren().add(completeRow);
        }

        Label cockpitLabel = new Label("↑ Cockpit");
        cockpitLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        cockpitLabel.setTextFill(Color.GRAY);
        layout.getChildren().add(0, cockpitLabel);

        return layout;
    }

    private Button createSeatButton(String seatId, String className) {
        Button seatButton = new Button(seatId);
        seatButton.setPrefSize(40, 40);

        if (bookedSeats.contains(seatId)) {
            seatButton.setStyle("-fx-background-color: #ffcdd2; -fx-border-color: #f44336; -fx-border-radius: 5;");
            seatButton.setDisable(true);
        } else if (backtrackingSelectedSeats.contains(seatId)) {
            // Purple color for backtracking-selected seats
            seatButton.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-border-color: #7b1fa2; -fx-border-radius: 5;");
            seatButton.setDisable(true);
        } else if (selectedSeats.contains(seatId)) {
            seatButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-border-color: #388e3c; -fx-border-radius: 5;");
        } else if (autoSelectActive) {
            // Gray out and disable all other seats when auto-select is active
            seatButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e; -fx-border-color: #bdbdbd; -fx-border-radius: 5;");
            seatButton.setDisable(true);
        } else {
            switch (className) {
                case "Business":
                    seatButton.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #4caf50; -fx-border-radius: 5;");
                    break;
                case "First":
                    seatButton.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-radius: 5;");
                    break;
                default:
                    seatButton.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-radius: 5;");
                    break;
            }
        }

        seatButton.setFont(Font.font("Arial", FontWeight.NORMAL, 10));

        seatButton.setOnAction(e -> {
            if (autoSelectActive) {
                showAlert("Auto-select is active. Please use 'Clear Selection' to enable manual seat selection.");
                return;
            }

            if (bookedSeats.contains(seatId)) {
                showAlert("Seat " + seatId + " is already booked!");
                return;
            }

            if (backtrackingSelectedSeats.contains(seatId)) {
                showAlert("Seat " + seatId + " is auto-selected and cannot be manually selected or deselected.");
                return;
            }

            if (selectedSeats.contains(seatId)) {
                resetSeatColor(seatButton, className);
                selectedSeats.remove(seatId);
            } else {
                // Check if we're trying to select adjacent seats to backtracking seats
                if (isAdjacentToBacktrackingSeats(seatId)) {
                    showAlert("Cannot select seats adjacent to auto-selected seats. Please use auto-select for optimal seating.");
                    return;
                }
                seatButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-border-color: #388e3c; -fx-border-radius: 5;");
                selectedSeats.add(seatId);
            }
        });

        seatButtons.put(seatId, seatButton);
        return seatButton;
    }

    private boolean isAdjacentToBacktrackingSeats(String seatId) {
        int row = getRowNumber(seatId);
        char col = seatId.charAt(seatId.length() - 1);

        // Check left seat
        if (col > 'A') {
            String leftSeat = row + String.valueOf((char)(col - 1));
            if (backtrackingSelectedSeats.contains(leftSeat)) {
                return true;
            }
        }

        // Check right seat
        if (col < 'F') {
            String rightSeat = row + String.valueOf((char)(col + 1));
            if (backtrackingSelectedSeats.contains(rightSeat)) {
                return true;
            }
        }

        return false;
    }

    private void resetSeatColor(Button seatButton, String className) {
        if (backtrackingSelectedSeats.contains(seatButton.getText())) {
            seatButton.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-border-color: #7b1fa2; -fx-border-radius: 5;");
        } else if (autoSelectActive) {
            seatButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e; -fx-border-color: #bdbdbd; -fx-border-radius: 5;");
        } else {
            switch (className) {
                case "Economy":
                    seatButton.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-radius: 5;");
                    break;
                case "Business":
                    seatButton.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #4caf50; -fx-border-radius: 5;");
                    break;
                case "First":
                    seatButton.setStyle("-fx-background-color: #fff3e0; -fx-border-color: #ff9800; -fx-border-radius: 5;");
                    break;
            }
        }
    }

    private void autoSelectOptimalSeats() {
        try {
            int requestedSeats = Integer.parseInt(seatsField.getText());
            String preference = seatingPreferenceComboBox.getValue();

            List<String> optimalSeats = findOptimalSeats(requestedSeats, preference);

            if (optimalSeats.isEmpty()) {
                showAlert("Not enough consecutive seats available for " + requestedSeats + " seats with " + preference + " preference.");
                return;
            }

            // Set auto-select active
            autoSelectActive = true;
            autoSelectStatus.setText("Auto-select: ACTIVE");
            autoSelectStatus.setTextFill(Color.GREEN);

            // Clear previous selections
            selectedSeats.forEach(seatId -> {
                Button btn = seatButtons.get(seatId);
                if (btn != null && !backtrackingSelectedSeats.contains(seatId)) {
                    resetSeatColor(btn, selectedClass.contains("Business") ? "Business" :
                            selectedClass.contains("First") ? "First" : "Economy");
                }
            });
            selectedSeats.clear();

            // Add to backtracking selected seats and mark them
            backtrackingSelectedSeats.addAll(optimalSeats);
            selectedSeats.addAll(optimalSeats);

            // Update seat colors and disable all other seats
            for (String seatId : seatButtons.keySet()) {
                Button btn = seatButtons.get(seatId);
                if (btn != null) {
                    if (optimalSeats.contains(seatId)) {
                        // Mark auto-selected seats
                        btn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white; -fx-border-color: #7b1fa2; -fx-border-radius: 5;");
                        btn.setDisable(true);
                    } else if (!bookedSeats.contains(seatId)) {
                        // Disable all other available seats when auto-select is active
                        btn.setDisable(true);
                        // Visual indication that seat is unavailable due to auto-select
                        btn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e; -fx-border-color: #bdbdbd; -fx-border-radius: 5;");
                    }
                }
            }

            String explanation = generateBacktrackingExplanation(optimalSeats, preference);
            showAlert("Auto-Selection Complete",
                    "Optimal seats selected: " + String.join(", ", optimalSeats) +
                            "\n\n" + explanation +
                            "\n\nNote: Auto-selected seats are fixed. Use 'Clear Selection' to enable manual selection.");

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number of seats.");
        }
    }

    private void updateSeatAvailability() {
        for (String seatId : seatButtons.keySet()) {
            Button btn = seatButtons.get(seatId);
            if (btn != null) {
                if (bookedSeats.contains(seatId)) {
                    btn.setDisable(true);
                } else if (autoSelectActive && !selectedSeats.contains(seatId)) {
                    btn.setDisable(true);
                    btn.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #9e9e9e; -fx-border-color: #bdbdbd; -fx-border-radius: 5;");
                } else if (!autoSelectActive && !selectedSeats.contains(seatId) && !backtrackingSelectedSeats.contains(seatId)) {
                    btn.setDisable(false);
                    resetSeatColor(btn, selectedClass.contains("Business") ? "Business" :
                            selectedClass.contains("First") ? "First" : "Economy");
                }
            }
        }
    }

    private List<String> findOptimalSeats(int requestedSeats, String preference) {
        List<String> allAvailableSeats = getAllAvailableSeats();
        List<String> bestSolution = new ArrayList<>();

        switch (preference) {
            case "Row-wise":
                bestSolution = backtrackRowWise(allAvailableSeats, requestedSeats, new ArrayList<>(), 0);
                break;
            case "Aisle":
                bestSolution = backtrackAisleSeats(allAvailableSeats, requestedSeats, new ArrayList<>(), 0);
                break;
            case "Window":
                bestSolution = backtrackWindowSeats(allAvailableSeats, requestedSeats, new ArrayList<>(), 0);
                break;
            case "Random":
                bestSolution = findRandomSeats(requestedSeats, allAvailableSeats);
                break;
        }

        return bestSolution;
    }

    private List<String> backtrackWindowSeats(List<String> availableSeats, int remaining, List<String> current, int start) {
        if (remaining == 0) {
            return new ArrayList<>(current);
        }

        List<String> bestSolution = new ArrayList<>();

        for (int i = start; i < availableSeats.size(); i++) {
            String seat = availableSeats.get(i);
            char column = seat.charAt(seat.length() - 1);

            boolean isWindowSeat = (column == 'A' || column == 'F');

            if (isWindowSeat && !current.contains(seat)) {
                current.add(seat);
                List<String> solution = backtrackWindowSeats(availableSeats, remaining - 1, current, i + 1);

                if (solution.size() == current.size() - 1 + remaining) {
                    int solutionScore = calculateWindowSeatScore(solution);

                    if (bestSolution.isEmpty() || solutionScore > calculateWindowSeatScore(bestSolution)) {
                        bestSolution = new ArrayList<>(solution);
                    }
                }
                current.remove(current.size() - 1);
            }
        }

        if (bestSolution.isEmpty() && remaining > 0) {
            bestSolution = findBestWindowSeatCombination(availableSeats, remaining);
        }

        return bestSolution;
    }

    private List<String> findBestWindowSeatCombination(List<String> availableSeats, int requestedSeats) {
        List<String> windowSeats = new ArrayList<>();

        for (String seat : availableSeats) {
            char column = seat.charAt(seat.length() - 1);
            if (column == 'A' || column == 'F') {
                windowSeats.add(seat);
            }
        }

        windowSeats.sort(Comparator.comparingInt(this::getRowNumber));

        Map<Integer, List<String>> windowSeatsByRow = new HashMap<>();
        for (String seat : windowSeats) {
            int row = getRowNumber(seat);
            windowSeatsByRow.computeIfAbsent(row, k -> new ArrayList<>()).add(seat);
        }

        for (int row = 1; row <= 30; row++) {
            List<String> rowWindowSeats = windowSeatsByRow.getOrDefault(row, new ArrayList<>());
            if (rowWindowSeats.size() >= requestedSeats) {
                return rowWindowSeats.subList(0, requestedSeats);
            }
        }

        List<String> bestCombination = new ArrayList<>();
        Set<Integer> usedRows = new HashSet<>();

        for (String seat : windowSeats) {
            if (bestCombination.size() < requestedSeats) {
                int row = getRowNumber(seat);
                if (!usedRows.contains(row) || usedRows.size() < 2) {
                    bestCombination.add(seat);
                    usedRows.add(row);
                }
            } else {
                break;
            }
        }

        return bestCombination.size() == requestedSeats ? bestCombination : new ArrayList<>();
    }

    private int calculateWindowSeatScore(List<String> seats) {
        int score = 0;
        Set<Integer> rows = new HashSet<>();
        for (String seat : seats) {
            rows.add(getRowNumber(seat));
            char column = seat.charAt(seat.length() - 1);
            if (column == 'A' || column == 'F') {
                score += 10;
            }
        }

        if (rows.size() == 1) {
            score += 20;
        } else if (rows.size() == 2) {
            score += 10;
        }

        if (areSeatsConsecutiveRows(seats)) {
            score += 15;
        }

        return score;
    }

    private List<String> backtrackAisleSeats(List<String> availableSeats, int remaining, List<String> current, int start) {
        if (remaining == 0) {
            return new ArrayList<>(current);
        }

        List<String> bestSolution = new ArrayList<>();

        for (int i = start; i < availableSeats.size(); i++) {
            String seat = availableSeats.get(i);
            char column = seat.charAt(seat.length() - 1);

            boolean isAisleSeat = (column == 'C' || column == 'D');

            if (isAisleSeat && !current.contains(seat)) {
                current.add(seat);
                List<String> solution = backtrackAisleSeats(availableSeats, remaining - 1, current, i + 1);

                if (solution.size() == current.size() - 1 + remaining) {
                    int solutionScore = calculateAisleSeatScore(solution);

                    if (bestSolution.isEmpty() || solutionScore > calculateAisleSeatScore(bestSolution)) {
                        bestSolution = new ArrayList<>(solution);
                    }
                }
                current.remove(current.size() - 1);
            }
        }

        if (bestSolution.isEmpty() && remaining > 0) {
            bestSolution = backtrackRowWise(availableSeats, remaining, current, start);
        }

        return bestSolution;
    }

    private List<String> backtrackRowWise(List<String> availableSeats, int remaining, List<String> current, int start) {
        if (remaining == 0) {
            return new ArrayList<>(current);
        }

        List<String> bestSolution = new ArrayList<>();
        int bestScore = -1;

        for (int i = start; i < availableSeats.size(); i++) {
            String seat = availableSeats.get(i);

            if (!current.contains(seat)) {
                current.add(seat);

                boolean validAddition = true;
                if (current.size() > 1) {
                    int currentRow = getRowNumber(seat);
                    int firstRow = getRowNumber(current.get(0));
                    if (Math.abs(currentRow - firstRow) > 2) {
                        validAddition = false;
                    }
                }

                if (validAddition) {
                    List<String> solution = backtrackRowWise(availableSeats, remaining - 1, current, i + 1);

                    if (solution.size() == current.size() - 1 + remaining) {
                        int currentScore = calculateRowWiseScore(solution);

                        if (bestSolution.isEmpty() || currentScore > bestScore) {
                            bestSolution = new ArrayList<>(solution);
                            bestScore = currentScore;
                        }
                    }
                }
                current.remove(current.size() - 1);
            }
        }

        return bestSolution;
    }

    private int calculateAisleSeatScore(List<String> seats) {
        int score = 0;
        for (String seat : seats) {
            char column = seat.charAt(seat.length() - 1);
            if (column == 'C' || column == 'D') {
                score += 10;
            } else if (column == 'B' || column == 'E') {
                score += 5;
            } else {
                score += 1;
            }
        }

        if (areSeatsConsecutive(seats)) {
            score += 20;
        }

        return score;
    }

    private int calculateRowWiseScore(List<String> seats) {
        int score = 0;
        Set<Integer> rows = new HashSet<>();
        for (String seat : seats) {
            rows.add(getRowNumber(seat));
        }

        score += (10 - rows.size()) * 5;

        if (areSeatsConsecutive(seats)) {
            score += 15;
        }

        return score;
    }

    private boolean areSeatsConsecutive(List<String> seats) {
        if (seats.size() <= 1) return true;

        List<String> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort(Comparator.comparingInt(this::getSeatNumber));

        for (int i = 1; i < sortedSeats.size(); i++) {
            int current = getSeatNumber(sortedSeats.get(i));
            int previous = getSeatNumber(sortedSeats.get(i - 1));
            if (current != previous + 1) {
                return false;
            }
        }
        return true;
    }

    private boolean areSeatsConsecutiveRows(List<String> seats) {
        if (seats.size() <= 1) return true;

        List<Integer> rows = new ArrayList<>();
        for (String seat : seats) {
            rows.add(getRowNumber(seat));
        }
        Collections.sort(rows);

        for (int i = 1; i < rows.size(); i++) {
            if (rows.get(i) != rows.get(i - 1) + 1) {
                return false;
            }
        }
        return true;
    }

    private String generateBacktrackingExplanation(List<String> optimalSeats, String preference) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Backtracking Analysis:\n");
        explanation.append("─────────────────────\n");

        int preferredSeats = 0;
        int totalSeats = optimalSeats.size();

        for (String seat : optimalSeats) {
            char column = seat.charAt(seat.length() - 1);

            if (preference.equals("Window") && (column == 'A' || column == 'F')) {
                preferredSeats++;
            } else if (preference.equals("Aisle") && (column == 'C' || column == 'D')) {
                preferredSeats++;
            }
        }

        explanation.append("• Preference: ").append(preference).append("\n");
        explanation.append("• Total seats found: ").append(totalSeats).append("\n");
        explanation.append("• ").append(preference).append(" seats: ").append(preferredSeats).append("/").append(totalSeats).append("\n");

        if (areSeatsConsecutive(optimalSeats)) {
            explanation.append("• All seats are consecutive ✓\n");
        } else {
            explanation.append("• Seats are in different rows (best available)\n");
        }

        explanation.append("• Selected seats analysis:\n");
        for (String seat : optimalSeats) {
            char column = seat.charAt(seat.length() - 1);
            String seatType = getSeatType(column);
            explanation.append("  - ").append(seat).append(" (").append(seatType).append(")\n");
        }

        return explanation.toString();
    }

    private String getSeatType(char column) {
        switch (column) {
            case 'A':
            case 'F':
                return "Window";
            case 'C':
            case 'D':
                return "Aisle";
            case 'B':
            case 'E':
                return "Middle";
            default:
                return "Standard";
        }
    }

    private List<String> findRandomSeats(int requestedSeats, List<String> availableSeats) {
        if (availableSeats.size() >= requestedSeats) {
            List<String> shuffled = new ArrayList<>(availableSeats);
            Collections.shuffle(shuffled);
            return new ArrayList<>(shuffled.subList(0, requestedSeats));
        }
        return new ArrayList<>();
    }

    private List<String> getAllAvailableSeats() {
        List<String> available = new ArrayList<>();
        for (String seatId : seatButtons.keySet()) {
            if (!bookedSeats.contains(seatId) && !selectedSeats.contains(seatId) && !backtrackingSelectedSeats.contains(seatId)) {
                available.add(seatId);
            }
        }
        available.sort(Comparator.comparingInt(this::getSeatNumber));
        return available;
    }

    private int getRowNumber(String seat) {
        return Integer.parseInt(seat.replaceAll("[A-Z]", ""));
    }

    private int getSeatNumber(String seat) {
        int row = getRowNumber(seat);
        char col = seat.charAt(seat.length() - 1);
        return row * 10 + (col - 'A');
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Required");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Flight {
        private String route;
        private String stops;
        private String duration;
        private String price;
        private String type;
        private int priceValue;
        private boolean isCheapest;

        public Flight(String route, String stops, String duration, String price, String type, int priceValue) {
            this.route = route;
            this.stops = stops;
            this.duration = duration;
            this.price = price;
            this.type = type;
            this.priceValue = priceValue;
            this.isCheapest = false;
        }

        public String getRoute() { return route; }
        public String getStops() { return stops; }
        public String getDuration() { return duration; }
        public String getPrice() { return price; }
        public String getType() { return type; }
        public int getPriceValue() { return priceValue; }
        public boolean isCheapest() { return isCheapest; }
        public void setCheapest(boolean cheapest) { isCheapest = cheapest; }
    }

    private static class FlightEdge {
        String destination;
        int price;
        String duration;

        FlightEdge(String destination, int price, String duration) {
            this.destination = destination;
            this.price = price;
            this.duration = duration;
        }
    }

    public static class Discount {
        private String code;
        private double percentage;
        private String description;

        public Discount(String code, double percentage, String description) {
            this.code = code;
            this.percentage = percentage;
            this.description = description;
        }

        public String getCode() { return code; }
        public double getPercentage() { return percentage; }
        public String getDescription() { return description; }

    }
    //  Close MongoDB connection
    @Override
    public void stop() {
        MongoDBConnection.close();
    }
}