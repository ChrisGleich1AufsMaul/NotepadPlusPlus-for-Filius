package filius.software.clientserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import filius.rahmenprogramm.I18n;
// import filius.software.transportschicht.TCPSocket;

public class NotepadPlusPlus extends ClientAnwendung implements I18n {

    private static final Logger logger = LoggerFactory.getLogger(NotepadPlusPlus.class);

    public static void main(String[] args) {
        // Startet die GUI-Anwendung
        javax.swing.SwingUtilities.invokeLater(() -> {
            //new GUIApplicationNotepadPlusPlusWindow();
        });
    }

    // Hier koennten Netzwerkoperationen hinzugefuegt werden, falls erforderlich
}
