package dk.cs.aau.huppaal.abstractions;

import dk.cs.aau.huppaal.code_analysis.Nearable;
import dk.cs.aau.huppaal.utility.helpers.LocationAware;
import dk.cs.aau.huppaal.utility.serialize.Serializable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Jork implements Serializable, Nearable, LocationAware {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static AtomicInteger idGenerator = new AtomicInteger();

    // Modeling properties
    private final StringProperty id = new SimpleStringProperty("");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(Type.JOIN);

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);

    public Jork(final Type type) {
        setType(type);

        if (type.equals(Type.JOIN)) {
            setId("J" + idGenerator.incrementAndGet());
        } else {
            setId("F" + idGenerator.incrementAndGet());
        }
    }

    public Jork(final JsonObject jsonObject) {
        deserialize(jsonObject);
    }

    public String getId() {
        return id.get();
    }

    public void setId(final String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
    }

    public double getX() {
        return x.get();
    }

    public void setX(final double x) {
        this.x.set(x);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public void setY(final double y) {
        this.y.set(y);
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public Type getType() {
        return type.get();
    }

    public void setType(final Type type) {
        this.type.set(type);
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(ID, getId());
        result.add(TYPE, new Gson().toJsonTree(getType(), Type.class));

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setId(json.getAsJsonPrimitive(ID).getAsString());
        setType(new Gson().fromJson(json.getAsJsonPrimitive(TYPE), Type.class));
    }

    @Override
    public String generateNearString() {
        // TODO: Jorks should know their parent component
        // TODO: id Should be a UUID
        return "[%s](jork:%s/%s)".formatted(generateNearStringOld(),
                "ParentPlaceholder", "IdPlaceholder");
    }

    private String generateNearStringOld() {
        if (getType().equals(Type.FORK))
            return "Fork " + getId();
        return "Join " + getId();
    }

    public enum Type {
        JOIN,
        FORK
    }
}
