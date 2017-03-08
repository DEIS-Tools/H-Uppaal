package SW9.utility.colors;

import javafx.util.Pair;

import java.util.HashMap;

public enum Color {

    RED(new Pair<>(Intensity.I50, new ColorParser("#ffebee", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#ffcdd2", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#ef9a9a", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#e57373", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#ef5350", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#f44336", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#e53935", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#d32f2f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#c62828", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#b71c1c", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ff8a80", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ff5252", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#ff1744", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#d50000", "rgba(255, 255, 255, 0.87)"))),

    PINK(new Pair<>(Intensity.I50, new ColorParser("#fce4ec", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#f8bbd0", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#f48fb1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#f06292", "rgba(255, 255, 255, 1);")),
            new Pair<>(Intensity.I400, new ColorParser("#ec407a", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#e91e63", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#d81b60", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#c2185b", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#ad1457", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#880e4f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ff80ab", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ff4081", "rgba(255, 255, 255, 1);")),
            new Pair<>(Intensity.A400, new ColorParser("#f50057", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#c51162", "rgba(255, 255, 255, 0.87)"))),

    PURPLE(new Pair<>(Intensity.I50, new ColorParser("#f3e5f5", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#e1bee7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#ce93d8", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#ba68c8", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I400, new ColorParser("#ab47bc", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I500, new ColorParser("#9c27b0", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#8e24aa", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#7b1fa2", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#6a1b9a", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#4a148c", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ea80fc", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#e040fb", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.A400, new ColorParser("#d500f9", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#aa00ff", "rgba(255, 255, 255, 0.87)"))),

    DEEP_PURPLE(new Pair<>(Intensity.I50, new ColorParser("#ede7f6", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#d1c4e9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#b39ddb", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#9575cd", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#7e57c2", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#673ab7", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#5e35b1", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#512da8", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#4527a0", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#311b92", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#b388ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#7c4dff", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.A400, new ColorParser("#651fff", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#6200ea", "rgba(255, 255, 255, 0.87)"))),

    INDIGO(new Pair<>(Intensity.I50, new ColorParser("#e8eaf6", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#c5cae9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#9fa8da", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#7986cb", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#5c6bc0", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#3f51b5", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#3949ab", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#303f9f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#283593", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#1a237e", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#8c9eff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#536dfe", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#3d5afe", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#304ffe", "rgba(255, 255, 255, 0.87)"))),

    BLUE(new Pair<>(Intensity.I50, new ColorParser("#e3f2fd", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#bbdefb", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#90caf9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#64b5f6", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#42a5f5", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#2196f3", "rgba(255, 255, 255, 1.0) ")),
            new Pair<>(Intensity.I600, new ColorParser("#1e88e5", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#1976d2", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#1565c0", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#0d47a1", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#82b1ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#448aff", "rgba(255, 255, 255, 1.0) ")),
            new Pair<>(Intensity.A400, new ColorParser("#2979ff", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#2962ff", "rgba(255, 255, 255, 0.87)"))),

    LIGHT_BLUE(new Pair<>(Intensity.I50, new ColorParser("#e1f5fe", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#b3e5fc", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#81d4fa", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#4fc3f7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#29b6f6", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#03a9f4", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#039be5", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I700, new ColorParser("#0288d1", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#0277bd", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#01579b", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#80d8ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#40c4ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#00b0ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#0091ea", "rgba(255, 255, 255, 1.0)"))),

    CYAN(new Pair<>(Intensity.I50, new ColorParser("#e0f7fa", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#b2ebf2", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#80deea", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#4dd0e1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#26c6da", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#00bcd4", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#00acc1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#0097a7", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#00838f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#006064", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#84ffff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#18ffff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#00e5ff", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#00b8d4", "rgba(0, 0, 0, 0.87)"))),

    TEAL(new Pair<>(Intensity.I50, new ColorParser("#e0f2f1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#b2dfdb", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#80cbc4", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#4db6ac", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#26a69a", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#009688", "rgba(255, 255, 255, 1.0) ")),
            new Pair<>(Intensity.I600, new ColorParser("#00897b", "rgba(255, 255, 255, 1.0) ")),
            new Pair<>(Intensity.I700, new ColorParser("#00796b", "rgba(255, 255, 255, 1.0) ")),
            new Pair<>(Intensity.I800, new ColorParser("#00695c", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#004d40", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#a7ffeb", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#64ffda", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#1de9b6", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#00bfa5", "rgba(0, 0, 0, 0.87)"))),

    GREEN(new Pair<>(Intensity.I50, new ColorParser("#e8f5e9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#c8e6c9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#a5d6a7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#81c784", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#66bb6a", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#4caf50", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#43a047", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I700, new ColorParser("#388e3c", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#2e7d32", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#1b5e20", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#b9f6ca", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#69f0ae", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#00e676", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#00c853", "rgba(255, 255, 255, 0.87)"))),

    LIGHT_GREEN(new Pair<>(Intensity.I50, new ColorParser("#f1f8e9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#dcedc8", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#c5e1a5", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#aed581", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#9ccc65", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#8bc34a", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#7cb342", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#689f38", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I800, new ColorParser("#558b2f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#33691e", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ccff90", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#b2ff59", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#76ff03", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#64dd17", "rgba(0, 0, 0, 0.87)"))),

    LIME(new Pair<>(Intensity.I50, new ColorParser("#f9fbe7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#f0f4c3", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#e6ee9c", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#dce775", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#d4e157", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#cddc39", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#c0ca33", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#afb42b", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#9e9d24", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#827717", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#f4ff81", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#eeff41", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#c6ff00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#aeea00", "rgba(0, 0, 0, 0.87)"))),

    YELLOW(new Pair<>(Intensity.I50, new ColorParser("#fffde7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#fff9c4", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#fff59d", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#fff176", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#ffee58", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#ffeb3b", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#fdd835", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#fbc02d", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#f9a825", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#f57f17", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ffff8d", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ffff00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#ffea00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#ffd600", "rgba(0, 0, 0, 0.87)"))),

    AMBER(new Pair<>(Intensity.I50, new ColorParser("#fff8e1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#ffecb3", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#ffe082", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#ffd54f", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#ffca28", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#ffc107", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#ffb300", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#ffa000", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#ff8f00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#ff6f00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ffe57f", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ffd740", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#ffc400", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#ffab00", "rgba(0, 0, 0, 0.87)"))),

    ORANGE(new Pair<>(Intensity.I50, new ColorParser("#fff3e0", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#ffe0b2", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#ffcc80", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#ffb74d", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#ffa726", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#ff9800", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#fb8c00", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#f57c00", "rgba(255, 255, 255, 1.0)")), // This should normally be black, but we did a riot
            new Pair<>(Intensity.I800, new ColorParser("#ef6c00", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I900, new ColorParser("#e65100", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ffd180", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ffab40", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#ff9100", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#ff6d00", "rgba(0, 0, 0, 0.87)"))),

    DEEP_ORANGE(new Pair<>(Intensity.I50, new ColorParser("#fbe9e7", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#ffccbc", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#ffab91", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#ff8a65", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#ff7043", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#ff5722", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I600, new ColorParser("#f4511e", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#e64a19", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#d84315", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#bf360c", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, new ColorParser("#ff9e80", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A200, new ColorParser("#ff6e40", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.A400, new ColorParser("#ff3d00", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A700, new ColorParser("#dd2c00", "rgba(255, 255, 255, 0.87)"))),

    BROWN(new Pair<>(Intensity.I50, new ColorParser("#efebe9", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#d7ccc8", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#bcaaa4", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#a1887f", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I400, new ColorParser("#8d6e63", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#795548", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#6d4c41", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#5d4037", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#4e342e", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#3e2723", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, null),
            new Pair<>(Intensity.A200, null),
            new Pair<>(Intensity.A400, null),
            new Pair<>(Intensity.A700, null)),

    GREY(new Pair<>(Intensity.I50, new ColorParser("#fafafa", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#f5f5f5", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#eeeeee", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#e0e0e0", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#bdbdbd", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I500, new ColorParser("#9e9e9e", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#757575", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#616161", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#424242", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#212121", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, null),
            new Pair<>(Intensity.A200, null),
            new Pair<>(Intensity.A400, null),
            new Pair<>(Intensity.A700, null)),

    GREY_BLUE(new Pair<>(Intensity.I50, new ColorParser("#eceff1", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I100, new ColorParser("#cfd8dc", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I200, new ColorParser("#b0bec5", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I300, new ColorParser("#90a4ae", "rgba(0, 0, 0, 0.87)")),
            new Pair<>(Intensity.I400, new ColorParser("#78909c", "rgba(255, 255, 255, 1.0)")),
            new Pair<>(Intensity.I500, new ColorParser("#607d8b", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I600, new ColorParser("#546e7a", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I700, new ColorParser("#455a64", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I800, new ColorParser("#37474f", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.I900, new ColorParser("#263238", "rgba(255, 255, 255, 0.87)")),
            new Pair<>(Intensity.A100, null),
            new Pair<>(Intensity.A200, null),
            new Pair<>(Intensity.A400, null),
            new Pair<>(Intensity.A700, null));

    private final HashMap<Intensity, ColorParser> colorMap = new HashMap<>();

    Color(Pair<Intensity, ColorParser>... colors) {
        // Make sure that the colors lists contains all intensities
        for (Intensity intensity : Intensity.values()) {
            boolean foundColor = false;

            for (Pair<Intensity, ColorParser> color : colors) {
                if (intensity.equals(color.getKey())) {
                    foundColor = true;
                    break;
                }
            }

            if (foundColor) {
                break;
            }

            // We found an intensity that is not present in the colors list, throw exception
            throw new IllegalArgumentException("Intensity " + intensity + " not found in constructor");
        }

        // Add the colors to the color map
        for (Pair<Intensity, ColorParser> color : colors) {
            colorMap.put(color.getKey(), color.getValue());
        }
    }

    public javafx.scene.paint.Color getColor(final Intensity intensity) {
        return colorMap.get(intensity).getColor();
    }

    public javafx.scene.paint.Color getColor(final Intensity intensity, final double opacity) {
        final javafx.scene.paint.Color color = colorMap.get(intensity).getColor();
        return new javafx.scene.paint.Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    public javafx.scene.paint.Color getTextColor(final Intensity intensity) {
        return colorMap.get(intensity).getTextColor();
    }

    public String getTextColorRgbaString(final Intensity intensity) {
        return colorMap.get(intensity).getTextColorRgbaString();
    }

    public java.awt.Color toAwtColor(final Intensity intensity) {
        return new java.awt.Color((int) (getColor(intensity).getRed() * 255),
                (int) (getColor(intensity).getGreen() * 255),
                (int) (getColor(intensity).getBlue() * 255));
    }

    public enum Intensity {
        I50,
        I100,
        I200,
        I300,
        I400,
        I500,
        I600,
        I700,
        I800,
        I900,
        A100,
        A200,
        A400,
        A700;

        public Intensity next() {
            return next(1);
        }

        public Intensity next(final int levels) {
            final Intensity[] values = values();

            // One of the first 10 elements
            if (this.ordinal() <= 9) {
                final int index = this.ordinal() + levels;

                if (index < 0) {
                    return values[0];
                } else if (index > 9) {
                    return values[9];
                } else {
                    return values[index];
                }
            }
            // One of the last 4 elements
            else {
                final int index = this.ordinal() + levels;

                if (index < 10) {
                    return values[10];
                } else if (index > 13) {
                    return values[13];
                } else {
                    return values[index];
                }
            }
        }
    }

    private static class ColorParser {

        private final String colorHex;
        private final String textColorRgbaString;
        private final javafx.scene.paint.Color color;
        private final javafx.scene.paint.Color textColor;

        public ColorParser(final String color, final String textColor) {
            colorHex = color;
            textColorRgbaString = textColor;
            this.color = javafx.scene.paint.Color.web(color);
            this.textColor = javafx.scene.paint.Color.web(textColor);
        }

        protected javafx.scene.paint.Color getColor() {
            return color;
        }

        protected javafx.scene.paint.Color getTextColor() {
            return textColor;
        }

        protected String getTextColorRgbaString() {return textColorRgbaString; }

    }

}
