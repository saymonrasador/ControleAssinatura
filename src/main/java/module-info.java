module com.subtrack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.subtrack to javafx.fxml;
    opens com.subtrack.controller to javafx.fxml;
    opens com.subtrack.domain to javafx.base, javafx.fxml;

    exports com.subtrack;
    exports com.subtrack.domain;
}
