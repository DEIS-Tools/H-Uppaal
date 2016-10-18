package SW9.model_canvas;

import com.google.gson.JsonObject;

public abstract class JsonParent extends Parent {

    public JsonParent() {

    }

    public JsonParent(final JsonObject jsonObject) {
        fromJsonObject();
    }

    protected abstract void fromJsonObject();

    public abstract JsonObject toJsonObject();
}
