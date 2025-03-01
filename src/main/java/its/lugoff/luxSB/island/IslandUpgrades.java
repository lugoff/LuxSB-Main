package its.lugoff.luxSB.island;

public class IslandUpgrades {
    private int sizeLevel;
    private int generatorLevel;
    private boolean oreSpawningEnabled;

    public IslandUpgrades() {
        this.sizeLevel = 1;
        this.generatorLevel = 1;
        this.oreSpawningEnabled = true;
    }

    public int getSizeLevel() {
        return sizeLevel;
    }

    public void setSizeLevel(int sizeLevel) {
        this.sizeLevel = Math.min(Math.max(sizeLevel, 1), 3);
    }

    public int getMaxSize() {
        return switch (sizeLevel) {
            case 2 -> 50;  // 100x100 (radius 50)
            case 3 -> 75;  // 150x150 (radius 75)
            default -> 25; // 50x50 (radius 25)
        };
    }

    public boolean isMaxSize() {
        return sizeLevel >= 3;
    }

    public int getGeneratorLevel() {
        return generatorLevel;
    }

    public void setGeneratorLevel(int generatorLevel) {
        this.generatorLevel = Math.min(Math.max(generatorLevel, 1), 3);
    }

    public double getGeneratorRate() {
        return switch (generatorLevel) {
            case 2 -> 0.2;  // 20% chance per break
            case 3 -> 0.3;  // 30% chance per break
            default -> 0.1; // 10% chance per break
        };
    }

    public boolean isMaxGenerator() {
        return generatorLevel >= 3;
    }

    public boolean isOreSpawningEnabled() {
        return oreSpawningEnabled;
    }

    public void setOreSpawningEnabled(boolean enabled) {
        this.oreSpawningEnabled = enabled;
    }
}