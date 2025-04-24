package com.example.projet_campus;

import com.example.projet_campus.classes.*;
import com.example.projet_campus.db.PersonDAO;
import com.example.projet_campus.db.BuildingDAO;
import com.example.projet_campus.db.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


import java.util.Random;

public class SimulationController {

    @FXML private Pane simulationPane;
    @FXML private ProgressBar satisfactionBar;
    @FXML private ProgressBar waterBar;
    @FXML private ProgressBar spaceBar;
    @FXML private ProgressBar wifiBar;
    @FXML private ProgressBar electricityBar;

    private User currentUser;
    private final Campus campus = new Campus();
    private int peopleCount = 0;
    private int buildingIdCounter = 1;
    private final Random rnd = new Random();
    // Resource limits
    private double maxWifi = 500, maxElectricity = 300, maxWater = 250, maxSpace = 1000;
    private int maxSatisfaction = 100;

    // Position marker for placing building icons
    private double lastX = 100, lastY = 100;

    /**
     * Called automatically after FXML is loaded.
     * Loads all people and buildings from the database and renders them.
     */
    private static class CampusEvent {
        String name;
        String description;
        Runnable effect;
        CampusEvent(String name, String description, Runnable effect) {
            this.name = name;
            this.description = description;
            this.effect = effect;
        }
    }
    private List<CampusEvent> events;
    @FXML
    public void initialize() {
        // 1) load existing data
        try {
            for (Person p : PersonDAO.getAll())         loadPerson(p);
            for (Building b : BuildingDAO.getAll())     loadBuilding(b);
            updateProgressBars();
        } catch (SQLException ex) {
            showAlert("DB Error", ex.getMessage());
        }

        // 2) define our random events in English
        events = List.of(
                new CampusEvent(
                        "Professor Strike",
                        "All professors are on strike—students lose satisfaction!",
                        () -> {
                            campus.getPeople().stream()
                                    .filter(p -> p instanceof Student)
                                    .map(p -> (Student)p)
                                    .forEach(s -> s.setSatisfaction(Math.max(0, s.getSatisfaction() - 20)));
                        }
                ),
                new CampusEvent(
                        "Wi‑Fi Outage",
                        "The Wi‑Fi is down—no one can access online resources!",
                        () -> campus.getResources().setWifiUsage(0)
                ),
                new CampusEvent(
                        "Infested Cafeteria",
                        "The cafeteria is infested—it's closed temporarily!",
                        () -> campus.getBuildings().stream()
                                .filter(b -> b.getType().equalsIgnoreCase("cafeteria"))
                                .collect(Collectors.toList())
                                .forEach(b -> campus.getResources().setWaterUsage(
                                        campus.getResources().getWaterUsage() - 10
                                ))
                ),
                new CampusEvent(
                        "Exams Approaching",
                        "Final exams are near—stress spikes and satisfaction drops!",
                        () -> campus.getPeople().stream()
                                .filter(p -> p instanceof Student)
                                .map(p -> (Student)p)
                                .forEach(s -> s.setSatisfaction(Math.max(0, s.getSatisfaction() - 15)))
                ),
                new CampusEvent(
                        "Open House Day",
                        "It's Open House—visitor numbers spike, space is tighter!",
                        () -> campus.getResources().setSpaceUsage(
                                campus.getResources().getSpaceUsage() + 50
                        )
                )
        );

        // 3) schedule one every 30 seconds
        Timeline ticker = new Timeline(new KeyFrame(Duration.seconds(30), e -> triggerRandomEvent()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
    }

    /**
     * Called by HomeController after successful login.
     * Shows or hides admin controls based on the user role.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // You can handle user role logic here later if needed
    }

    // ─── Add Handlers ─────────────────────────────────────────────────

    @FXML public void handleAddStudent() {
        if (!canAddPerson()) {
            showAlert("Resource Limit", "Cannot add student. Resources are full.");
            return;
        }
        String name = prompt("Add Student", "Enter student name:");
        if (name == null) return;
        String sector = prompt("Add Student", "Enter sector:");
        if (sector == null) return;
        String satStr = prompt("Add Student", "Enter satisfaction (0–100):");
        if (satStr == null) return;
        int satisfaction = Math.min(Integer.parseInt(satStr), maxSatisfaction);

        Student s = new Student(name, 20, 50, sector, satisfaction);
        campus.addPerson(s);
        try { PersonDAO.insert(s); }
        catch (SQLException ex) { showAlert("DB Error", "Save failed:\n" + ex.getMessage()); }

        consumePersonResources();
        addEntityImage("student.png", 100);
    }

    @FXML public void handleAddProfessor() {
        if (!canAddPerson()) {
            showAlert("Resource Limit", "Cannot add professor. Resources are full.");
            return;
        }
        String name = prompt("Add Professor", "Enter professor name:");
        if (name == null) return;
        String subject = prompt("Add Professor", "Enter subject:");
        if (subject == null) return;
        String availStr = prompt("Add Professor", "Is available? (yes/no):");
        boolean available = availStr != null && availStr.equalsIgnoreCase("yes");

        Professor p = new Professor(name, 45, 50, null, 0, subject, available);
        campus.addPerson(p);
        try { PersonDAO.insert(p); }
        catch (SQLException ex) { showAlert("DB Error", "Save failed:\n" + ex.getMessage()); }

        consumePersonResources();
        addEntityImage("professor.png", 150);
    }


    /** Manual button */
    @FXML
    public void handleTriggerEvent() {
        triggerRandomEvent();
    }

    /** picks one event at random, runs its effect, then shows an alert */
    private void triggerRandomEvent() {
        CampusEvent ev = events.get(rnd.nextInt(events.size()));
        ev.effect.run();
        updateProgressBars();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Random Campus Event");
        alert.setHeaderText(ev.name);
        alert.setContentText(ev.description);
        alert.showAndWait();
    }

    // ─── helper methods for loading and updating ────────────────────────

    private void loadPerson(Person p) {
        campus.addPerson(p);
        String img = (p instanceof Student) ? "student.png" : "professor.png";
        double y   = (p instanceof Student) ? 100 : 150;
        addEntityImage(img, y);
    }

    private void loadBuilding(Building b) {
        campus.addBuilding(b);
        double[] pos = getNextBuildingPosition();
        addBuildingImage(b.getType().toLowerCase() + ".png", pos[0], pos[1]);
    }




    private void addBuildingImage(String imgName, double x, double y) {
        var res = getClass().getResource("/images/" + imgName);
        if (res == null) return;
        ImageView iv = new ImageView(res.toExternalForm());
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        iv.setLayoutX(x);
        iv.setLayoutY(y);
        simulationPane.getChildren().add(iv);
    }




    // resource limits for progress bars

    @FXML public void handleAddLibrary()    { addTypedBuilding("library"); }
    @FXML public void handleAddClassroom()  { addTypedBuilding("classroom"); }
    @FXML public void handleAddCafeteria()  { addTypedBuilding("cafeteria"); }
    @FXML public void handleAddLaboratory() { addTypedBuilding("laboratory"); }

    public void handleExportPdf() {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.setLeading(18f);
                cs.newLineAtOffset(50, 700);

                cs.showText("Campus Report");
                cs.newLine();
                cs.newLine();

                // People
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.showText("People:");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 12);
                List<Person> people = campus.getPeople();
                if (people.isEmpty()) {
                    cs.showText("  <none>");
                    cs.newLine();
                } else {
                    for (Person p : people) {
                        String line = "  - " + p.getName();
                        if (p instanceof Student s) {
                            line += " (Student, " + s.getSector() + ", sat=" + s.getSatisfaction() + ")";
                        } else if (p instanceof Professor pr) {
                            line += " (Professor, subj=" + pr.getSubject()
                                    + (pr.isAvailability() ? ", avail" : ", busy") + ")";
                        }
                        cs.showText(line);
                        cs.newLine();
                    }
                }

                cs.newLine();
                // Buildings
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.showText("Buildings:");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 12);
                List<Building> buildings = campus.getBuildings();
                if (buildings.isEmpty()) {
                    cs.showText("  <none>");
                    cs.newLine();
                } else {
                    for (Building b : buildings) {
                        String line = String.format("  - %s (type=%s, cap=%d)",
                                b.getName(), b.getType(), b.getCapacity());
                        cs.showText(line);
                        cs.newLine();
                    }
                }

                cs.endText();
            }

            File out = new File("campus_report.pdf");
            doc.save(out);
            showAlert("Export Successful", "PDF saved to:\n" + out.getAbsolutePath());
        } catch (IOException e) {
            showAlert("PDF Error", "Failed to write PDF:\n" + e.getMessage());
        }
    }

    /**
     * Prompts for a custom building type.
     */
    @FXML public void handleAddCustomBuilding() {
        String type = prompt("Add Building", "Enter type (library, classroom, cafeteria, laboratory):");
        if (type != null) addTypedBuilding(type.toLowerCase());
    }

    /**
     * Creates a building of the given type, saves to DB, and renders it.
     */
    private void addTypedBuilding(String type) {
        if (!canAddBuilding()) {
            showAlert("Space Limit", "Cannot add building. Not enough space.");
            return;
        }
        String name = prompt("Add Building", "Enter building name:");
        if (name == null) return;
        int cap = 50 + (int)(Math.random()*100);
        Building b;
        switch(type) {
            case "library":    b = new Library(buildingIdCounter++, name, cap); break;
            case "classroom":  b = new Classroom(buildingIdCounter++, name, cap); break;
            case "cafeteria":  b = new Cafeteria(buildingIdCounter++, name, cap); break;
            case "laboratory": b = new Laboratory(buildingIdCounter++, name, cap); break;
            default:
                showAlert("Invalid Type", "Unsupported building type: " + type);
                return;
        }
        campus.addBuilding(b);
        try { BuildingDAO.insert(b); }
        catch (SQLException ex) { showAlert("DB Error", "Save failed:\n" + ex.getMessage()); }

        consumeBuildingResources();
        double[] pos = getNextBuildingPosition();
        addBuildingImage(type + ".png", pos[0], pos[1], b);
    }

    // ─── Show All Entities ────────────────────────────────────────────

    @FXML public void handleShowAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 People on Campus:\n");
        if (campus.getPeople().isEmpty()) sb.append("  <none>\n");
        for (Person p : campus.getPeople()) {
            sb.append("  - ").append(p.getName());
            if (p instanceof Student s) {
                sb.append(" (Student, sector=").append(s.getSector())
                        .append(", sat=").append(s.getSatisfaction()).append(")");
            } else if (p instanceof Professor pr) {
                sb.append(" (Professor, subj=").append(pr.getSubject())
                        .append(pr.isAvailability() ? ", avail)" : ", busy)");
            }
            sb.append("\n");
        }
        sb.append("\n🏢 Buildings on Campus:\n");
        if (campus.getBuildings().isEmpty()) sb.append("  <none>\n");
        for (Building b : campus.getBuildings()) {
            sb.append("  - ").append(b.getName())
                    .append(" (type=").append(b.getType())
                    .append(", cap=").append(b.getCapacity()).append(")\n");
        }

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("All Campus Entities");
        a.setHeaderText("People & Buildings");
        TextArea ta = new TextArea(sb.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setMaxHeight(Double.MAX_VALUE);
        a.getDialogPane().setContent(ta);
        a.showAndWait();
    }

    // ─── Edit Handlers (Admin) ────────────────────────────────────────

    @FXML public void handleEditPerson() {
        String name = prompt("Edit Person", "Enter person name to edit:");
        if (name == null) return;
        Optional<Person> opt = campus.getPeople().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
        if (opt.isEmpty()) { showAlert("Not Found", "No person named " + name); return; }
        Person p = opt.get();
        String newName = prompt("Edit Person", "New name (blank = keep):");
        if (newName != null && !newName.isBlank()) p.setName(newName);
        if (p instanceof Student s) {
            String sec = prompt("Edit Student", "Sector (current="+s.getSector()+"): ");
            if (sec != null && !sec.isBlank()) s.setSector(sec);
            String sat = prompt("Edit Student", "Satisfaction (0–100):");
            if (sat != null) s.setSatisfaction(Integer.parseInt(sat));
        } else if (p instanceof Professor pr) {
            String subj = prompt("Edit Professor", "Subject (current="+pr.getSubject()+"): ");
            if (subj != null && !subj.isBlank()) pr.setSubject(subj);
            String av = prompt("Edit Professor", "Available? yes/no:");
            pr.setAvailability(av != null && av.equalsIgnoreCase("yes"));
        }
        try { PersonDAO.update(p); } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        reloadAllEntities();
    }

    @FXML public void handleEditBuilding() {
        String name = prompt("Edit Building", "Enter building to edit:");
        if (name == null) return;
        Optional<Building> opt = campus.getBuildings().stream()
                .filter(b -> b.getName().equals(name))
                .findFirst();
        if (opt.isEmpty()) { showAlert("Not Found", "No building named " + name); return; }
        Building b = opt.get();
        String newName = prompt("Edit Building", "New name (blank = keep):");
        if (newName != null && !newName.isBlank()) b.setName(newName);
        String cap = prompt("Edit Building", "Capacity (current="+b.getCapacity()+"): ");
        if (cap != null) b.setCapacity(Integer.parseInt(cap));
        try { BuildingDAO.update(b); } catch (SQLException ex) { showAlert("DB Error", ex.getMessage()); }
        reloadAllEntities();
    }

    // ─── Delete Handlers ──────────────────────────────────────────────

    @FXML public void handleDeletePerson() {
        String name = prompt("Delete Person", "Enter person to delete:");
        if (name == null) return;
        campus.getPeople().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .ifPresentOrElse(p -> {
                    campus.getPeople().remove(p);
                    try { PersonDAO.deleteByName(name); } catch (SQLException ex) {
                        showAlert("DB Error", ex.getMessage()); }
                    reloadAllEntities();
                }, () -> showAlert("Not Found", "No person named " + name));
    }

    @FXML public void handleDeleteBuilding() {
        String name = prompt("Delete Building", "Enter building to delete:");
        if (name == null) return;
        campus.getBuildings().stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .ifPresentOrElse(b -> {
                    campus.getBuildings().remove(b);
                    try { BuildingDAO.deleteByName(name); } catch (SQLException ex) {
                        showAlert("DB Error", ex.getMessage()); }
                    reloadAllEntities();
                }, () -> showAlert("Not Found", "No building named " + name));
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private void reloadAllEntities() {
        simulationPane.getChildren().clear();
        peopleCount = 0;
        lastX = 100; lastY = 100;
        campus.clear();
        initialize();  // simply re-run loading
    }

    private void consumePersonResources() {
        Resources r = campus.getResources();
        r.setWaterUsage(r.getWaterUsage() + 5);
        r.setElectricityUsage(r.getElectricityUsage() + 3);
        r.setWifiUsage(r.getWifiUsage() + 4);
        updateProgressBars();
    }

    private void consumeBuildingResources() {
        Resources r = campus.getResources();
        r.setSpaceUsage(r.getSpaceUsage() + 50);
        updateProgressBars();
    }

    private boolean canAddPerson() {
        Resources r = campus.getResources();
        return r.getWaterUsage() < maxWater
                && r.getElectricityUsage() < maxElectricity
                && r.getWifiUsage() < maxWifi;
    }

    private boolean canAddBuilding() {
        return campus.getResources().getSpaceUsage() < maxSpace;
    }

    private double[] getNextBuildingPosition() {
        double x = lastX, y = lastY;
        lastX += 120;
        if (lastX > 800) {
            lastX = 100;
            lastY += 140;
        }
        return new double[]{ x, y };
    }

    private void addEntityImage(String imgName, double y) {
        var res = getClass().getResource("/images/" + imgName);
        if (res == null) return;
        ImageView iv = new ImageView(new Image(res.toExternalForm()));
        iv.setFitWidth(40); iv.setFitHeight(40);
        iv.setLayoutX(50 + peopleCount * 50);
        iv.setLayoutY(y);
        simulationPane.getChildren().add(iv);
        peopleCount++;
    }

    private void addBuildingImage(String imgName, double x, double y, Building b) {
        var res = getClass().getResource("/images/" + imgName);
        if (res == null) return;
        ImageView iv = new ImageView(new Image(res.toExternalForm()));
        iv.setFitWidth(100); iv.setFitHeight(100);
        iv.setLayoutX(x); iv.setLayoutY(y);
        simulationPane.getChildren().add(iv);
    }

    private void updateProgressBars() {
        satisfactionBar.setProgress(campus.getAverageSatisfaction() / maxSatisfaction);
        Resources r = campus.getResources();
        wifiBar.setProgress(Math.min(1.0, r.getWifiUsage() / maxWifi));
        electricityBar.setProgress(Math.min(1.0, r.getElectricityUsage() / maxElectricity));
        waterBar.setProgress(Math.min(1.0, r.getWaterUsage() / maxWater));
        spaceBar.setProgress(Math.min(1.0, r.getSpaceUsage() / maxSpace));
    }

    private String prompt(String title, String content) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle(title);
        dlg.setHeaderText(content);
        Optional<String> res = dlg.showAndWait();
        return res.filter(s -> !s.isBlank()).orElse(null);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
