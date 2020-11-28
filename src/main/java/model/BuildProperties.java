package model;

import util.StreamUtil;

public class BuildProperties {
    private EntityType[] options;
    public EntityType[] getOptions() { return options; }
    public void setOptions(EntityType[] options) { this.options = options; }
    private Integer initHealth;
    public Integer getInitHealth() { return initHealth; }
    public void setInitHealth(Integer initHealth) { this.initHealth = initHealth; }
    public BuildProperties() {}
    public BuildProperties(EntityType[] options, Integer initHealth) {
        this.options = options;
        this.initHealth = initHealth;
    }
    public static BuildProperties readFrom(java.io.InputStream stream) throws java.io.IOException {
        BuildProperties result = new BuildProperties();
        result.options = new EntityType[StreamUtil.readInt(stream)];
        for (int i = 0; i < result.options.length; i++) {
            switch (StreamUtil.readInt(stream)) {
            case 0:
                result.options[i] = EntityType.WALL;
                break;
            case 1:
                result.options[i] = EntityType.HOUSE;
                break;
            case 2:
                result.options[i] = EntityType.BUILDER_BASE;
                break;
            case 3:
                result.options[i] = EntityType.BUILDER_UNIT;
                break;
            case 4:
                result.options[i] = EntityType.MELEE_BASE;
                break;
            case 5:
                result.options[i] = EntityType.MELEE_UNIT;
                break;
            case 6:
                result.options[i] = EntityType.RANGED_BASE;
                break;
            case 7:
                result.options[i] = EntityType.RANGED_UNIT;
                break;
            case 8:
                result.options[i] = EntityType.RESOURCE;
                break;
            case 9:
                result.options[i] = EntityType.TURRET;
                break;
            default:
                throw new java.io.IOException("Unexpected tag value");
            }
        }
        if (StreamUtil.readBoolean(stream)) {
            result.initHealth = StreamUtil.readInt(stream);
        } else {
            result.initHealth = null;
        }
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        StreamUtil.writeInt(stream, options.length);
        for (EntityType optionsElement : options) {
            StreamUtil.writeInt(stream, optionsElement.tag);
        }
        if (initHealth == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            StreamUtil.writeInt(stream, initHealth);
        }
    }
}
