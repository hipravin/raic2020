package model;

import util.StreamUtil;

public class DebugState {
    private Vec2Int windowSize;
    public Vec2Int getWindowSize() { return windowSize; }
    public void setWindowSize(Vec2Int windowSize) { this.windowSize = windowSize; }
    private Vec2Float mousePosWindow;
    public Vec2Float getMousePosWindow() { return mousePosWindow; }
    public void setMousePosWindow(Vec2Float mousePosWindow) { this.mousePosWindow = mousePosWindow; }
    private Vec2Float mousePosWorld;
    public Vec2Float getMousePosWorld() { return mousePosWorld; }
    public void setMousePosWorld(Vec2Float mousePosWorld) { this.mousePosWorld = mousePosWorld; }
    private String[] pressedKeys;
    public String[] getPressedKeys() { return pressedKeys; }
    public void setPressedKeys(String[] pressedKeys) { this.pressedKeys = pressedKeys; }
    private Camera camera;
    public Camera getCamera() { return camera; }
    public void setCamera(Camera camera) { this.camera = camera; }
    private int playerIndex;
    public int getPlayerIndex() { return playerIndex; }
    public void setPlayerIndex(int playerIndex) { this.playerIndex = playerIndex; }
    public DebugState() {}
    public DebugState(Vec2Int windowSize, Vec2Float mousePosWindow, Vec2Float mousePosWorld, String[] pressedKeys, Camera camera, int playerIndex) {
        this.windowSize = windowSize;
        this.mousePosWindow = mousePosWindow;
        this.mousePosWorld = mousePosWorld;
        this.pressedKeys = pressedKeys;
        this.camera = camera;
        this.playerIndex = playerIndex;
    }
    public static DebugState readFrom(java.io.InputStream stream) throws java.io.IOException {
        DebugState result = new DebugState();
        result.windowSize = Vec2Int.readFrom(stream);
        result.mousePosWindow = Vec2Float.readFrom(stream);
        result.mousePosWorld = Vec2Float.readFrom(stream);
        result.pressedKeys = new String[StreamUtil.readInt(stream)];
        for (int i = 0; i < result.pressedKeys.length; i++) {
            result.pressedKeys[i] = StreamUtil.readString(stream);
        }
        result.camera = Camera.readFrom(stream);
        result.playerIndex = StreamUtil.readInt(stream);
        return result;
    }
    public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
        windowSize.writeTo(stream);
        mousePosWindow.writeTo(stream);
        mousePosWorld.writeTo(stream);
        StreamUtil.writeInt(stream, pressedKeys.length);
        for (String pressedKeysElement : pressedKeys) {
            StreamUtil.writeString(stream, pressedKeysElement);
        }
        camera.writeTo(stream);
        StreamUtil.writeInt(stream, playerIndex);
    }
}
