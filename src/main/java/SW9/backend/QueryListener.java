package SW9.backend;

import SW9.model_canvas.locations.Location;
import com.uppaal.engine.QueryFeedback;
import com.uppaal.engine.QueryVerificationResult;
import com.uppaal.model.system.SystemLocation;
import com.uppaal.model.system.symbolic.SymbolicTransition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QueryListener implements QueryFeedback {

    private final HUPPAALDocument huppaalDocumentLDocument;
    private final Consumer<Trace> traceCallback;

    public QueryListener(final HUPPAALDocument huppaalDocumentLDocument, final Consumer<Trace> traceCallback) {
        this.huppaalDocumentLDocument = huppaalDocumentLDocument;
        this.traceCallback = traceCallback;
    }

    @Override
    public void setProgressAvail(boolean b) {

    }

    @Override
    public void setProgress(int i, long l, long l1, long l2, long l3, long l4, long l5, long l6, long l7, long l8) {

    }

    @Override
    public void setSystemInfo(long l, long l1, long l2) {

    }

    @Override
    public void setLength(int i) {

    }

    @Override
    public void setCurrent(int i) {

    }

    @Override
    public void setTrace(char c, String s, ArrayList<SymbolicTransition> arrayList, int i, QueryVerificationResult queryVerificationResult) {


        final List<Location> locationList = new ArrayList<>();

        for(final SymbolicTransition symbolicTransition : arrayList) {
            for (final SystemLocation systemLocation : symbolicTransition.getTarget().getLocations()) {
                locationList.add(getHUPPAALDocument().getLocation(systemLocation.getLocation()));
            }
        }

        // TODO map the trace
        traceCallback.accept(new Trace(locationList ,null, c));
    }

    @Override
    public void setFeedback(String s) {

    }

    @Override
    public void appendText(String s) {

    }

    @Override
    public void setResultText(String s) {

    }

    public HUPPAALDocument getHUPPAALDocument() {
        return huppaalDocumentLDocument;
    }
}
