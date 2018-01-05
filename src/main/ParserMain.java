import ui.Viewer;

public class ParserMain {
    public static void main(String[] args) {
        Viewer viewer = new Viewer();
        viewer.updateViewer(args);
        viewer.applyViewer();
    }
}
