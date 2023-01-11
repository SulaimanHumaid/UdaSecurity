module securityService {
    requires imageService;
    requires miglayout; //
    requires com.google.common; //
    requires com.google.gson; //
    requires java.desktop;
    requires java.prefs;
    opens com.udacity.catpoint.data to com.google.gson;

}