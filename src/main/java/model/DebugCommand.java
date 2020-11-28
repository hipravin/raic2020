package model;

import util.StreamUtil;

public abstract class DebugCommand {
    public abstract void writeTo(java.io.OutputStream stream) throws java.io.IOException;
    public static DebugCommand readFrom(java.io.InputStream stream) throws java.io.IOException {
        switch (StreamUtil.readInt(stream)) {
            case Add.TAG:
                return Add.readFrom(stream);
            case Clear.TAG:
                return Clear.readFrom(stream);
            default:
                throw new java.io.IOException("Unexpected tag value");
        }
    }

    public static class Add extends DebugCommand {
        public static final int TAG = 0;
        private DebugData data;
        public DebugData getData() { return data; }
        public void setData(DebugData data) { this.data = data; }
        public Add() {}
        public Add(DebugData data) {
            this.data = data;
        }
        public static Add readFrom(java.io.InputStream stream) throws java.io.IOException {
            Add result = new Add();
            result.data = DebugData.readFrom(stream);
            return result;
        }
        @Override
        public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
            StreamUtil.writeInt(stream, TAG);
            data.writeTo(stream);
        }
    }

    public static class Clear extends DebugCommand {
        public static final int TAG = 1;
        public Clear() {}
        public static Clear readFrom(java.io.InputStream stream) throws java.io.IOException {
            Clear result = new Clear();
            return result;
        }
        @Override
        public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
            StreamUtil.writeInt(stream, TAG);
        }
    }
}
