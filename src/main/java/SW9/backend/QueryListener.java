package SW9.backend;

import com.uppaal.engine.QueryFeedback;
import com.uppaal.engine.QueryVerificationResult;
import com.uppaal.model.system.symbolic.SymbolicTransition;

import java.util.ArrayList;

public class QueryListener implements QueryFeedback {

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

    }

    @Override
    public void appendText(final String s) {

    }

    @Override
    public void setResultText(final String s) {

    }
}
