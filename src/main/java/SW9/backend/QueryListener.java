package SW9.backend;

import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.controllers.HUPPAALController;
import com.uppaal.engine.QueryFeedback;
import com.uppaal.engine.QueryVerificationResult;
import com.uppaal.model.system.symbolic.SymbolicTransition;
import javafx.application.Platform;

import java.util.ArrayList;

public class QueryListener implements QueryFeedback {

    private Query query;

    public QueryListener() {
        this(new Query("Unknown", "Unknown", QueryState.UNKNOWN));
    }

    public QueryListener(final Query query) {
        this.query = query;
    }

    @Override
    public void setProgressAvail(final boolean b) {

    }

    @Override
    public void setProgress(final int i, final long l, final long l1, final long l2, final long l3, final long l4, final long l5, final long l6, final long l7, final long l8) {

    }

    @Override
    public void setSystemInfo(final long l, final long l1, final long l2) {

    }

    @Override
    public void setLength(final int i) {

    }

    @Override
    public void setCurrent(final int i) {

    }

    @Override
    public void setTrace(final char c, final String s, final ArrayList<SymbolicTransition> arrayList, final int i, final QueryVerificationResult queryVerificationResult) {
    }

    @Override
    public void setFeedback(final String s) {
        if (s.contains("inf") || s.contains("sup")) {
            Platform.runLater(() -> {
                HUPPAALController.openQueryDialog(query, s.split("\n")[1]);
            });
        }
    }

    @Override
    public void appendText(final String s) {
    }

    @Override
    public void setResultText(final String s) {
    }
}
