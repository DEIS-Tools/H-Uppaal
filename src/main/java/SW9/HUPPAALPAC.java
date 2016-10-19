package SW9;

public class HUPPAALPAC {

    private final HUPPAALPresentation presentation;
    private final WindowAbstraction abstraction;
    private HUPPAALController controller;

    HUPPAALPAC(final HUPPAALPresentation presentation) {
        this.presentation = presentation;
        this.abstraction = new WindowAbstraction();
    }

    public HUPPAALPAC(final WindowAbstraction abstraction) {
        this.presentation = new HUPPAALPresentation(this);
        this.abstraction = abstraction;
    }

    public HUPPAALPresentation getPresentation() {
        return presentation;
    }

    public WindowAbstraction getAbstraction() {
        return abstraction;
    }

    public HUPPAALController getController() {
        return controller;
    }

    public void setController(final HUPPAALController controller) {
        this.controller = controller;
    }

}
