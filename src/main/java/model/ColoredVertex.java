package model;

import util.StreamUtil;

public class ColoredVertex {
    private Vec2Float worldPos;
    public Vec2Float getWorldPos() { return worldPos; }
    public void setWorldPos(Vec2Float worldPos) { this.worldPos = worldPos; }
    private Vec2Float screenOffset;
    public Vec2Float getScreenOffset() { return screenOffset; }
    public void setScreenOffset(Vec2Float screenOffset) { this.screenOffset = screenOffset; }
    private Color color;
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public ColoredVertex() {}
    public ColoredVertex(Vec2Float worldPos, Vec2Float screenOffset, Color color) {
        this.worldPos = worldPos;
        this.screenOffset = screenOffset;
        this.color = color;
    }
    public static ColoredVertex readFrom(java.io.InputStream stream) throws java.io.IOException {
        ColoredVertex result = new ColoredVertex();
        if (StreamUtil.readBoolean(stream)) {
            result.worldPos = Vec2Float.readFrom(stream);
        } else {
            result.worldPos = null;
        }
        result.screenOffset = Vec2Float.readFrom(stream);
        result.color = Color.readFrom(stream);
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        if (worldPos == null) {
            StreamUtil.writeBoolean(stream, false);
        } else {
            StreamUtil.writeBoolean(stream, true);
            worldPos.writeTo(stream);
        }
        screenOffset.writeTo(stream);
        color.writeTo(stream);
    }
}
