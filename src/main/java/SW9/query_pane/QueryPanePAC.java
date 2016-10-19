package SW9.query_pane;

public class QueryPanePAC {

    private final QueryPanePresentation presentation;
    private final QueryPaneAbstraction abstraction;
    private QueryPaneController controller;

    QueryPanePAC(final QueryPanePresentation presentation) {
        this.presentation = presentation;
        this.abstraction = new QueryPaneAbstraction();
    }

    public QueryPanePAC(final QueryPaneAbstraction abstraction) {
        this.presentation = new QueryPanePresentation(this);
        this.abstraction = abstraction;
    }

    public QueryPanePresentation getPresentation() {
        return presentation;
    }

    public QueryPaneAbstraction getAbstraction() {
        return abstraction;
    }

    public QueryPaneController getController() {
        return controller;
    }

    public void setController(final QueryPaneController controller) {
        this.controller = controller;
    }

}
