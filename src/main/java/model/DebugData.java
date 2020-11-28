package model;

import util.StreamUtil;

public abstract class DebugData {
    public abstract void writeTo(java.io.OutputStream stream) throws java.io.IOException;
    public static DebugData readFrom(java.io.InputStream stream) throws java.io.IOException {
        switch (StreamUtil.readInt(stream)) {
            case Log.TAG:
                return Log.readFrom(stream);
            case Primitives.TAG:
                return Primitives.readFrom(stream);
            case PlacedText.TAG:
                return PlacedText.readFrom(stream);
            default:
                throw new java.io.IOException("Unexpected tag value");
        }
    }

    public static class Log extends DebugData {
        public static final int TAG = 0;
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Log() {}
        public Log(String text) {
            this.text = text;
        }
        public static Log readFrom(java.io.InputStream stream) throws java.io.IOException {
            Log result = new Log();
            result.text = StreamUtil.readString(stream);
            return result;
        }
        @Override
        public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
            StreamUtil.writeInt(stream, TAG);
            StreamUtil.writeString(stream, text);
        }
    }

    public static class Primitives extends DebugData {
        public static final int TAG = 1;
        private ColoredVertex[] vertices;
        public ColoredVertex[] getVertices() { return vertices; }
        public void setVertices(ColoredVertex[] vertices) { this.vertices = vertices; }
        private PrimitiveType primitiveType;
        public PrimitiveType getPrimitiveType() { return primitiveType; }
        public void setPrimitiveType(PrimitiveType primitiveType) { this.primitiveType = primitiveType; }
        public Primitives() {}
        public Primitives(ColoredVertex[] vertices, PrimitiveType primitiveType) {
            this.vertices = vertices;
            this.primitiveType = primitiveType;
        }
        public static Primitives readFrom(java.io.InputStream stream) throws java.io.IOException {
            Primitives result = new Primitives();
            result.vertices = new ColoredVertex[StreamUtil.readInt(stream)];
            for (int i = 0; i < result.vertices.length; i++) {
                result.vertices[i] = ColoredVertex.readFrom(stream);
            }
            switch (StreamUtil.readInt(stream)) {
            case 0:
                result.primitiveType = PrimitiveType.LINES;
                break;
            case 1:
                result.primitiveType = PrimitiveType.TRIANGLES;
                break;
            default:
                throw new java.io.IOException("Unexpected tag value");
            }
            return result;
        }
        @Override
        public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
            StreamUtil.writeInt(stream, TAG);
            StreamUtil.writeInt(stream, vertices.length);
            for (ColoredVertex verticesElement : vertices) {
                verticesElement.writeTo(stream);
            }
            StreamUtil.writeInt(stream, primitiveType.tag);
        }
    }

    public static class PlacedText extends DebugData {
        public static final int TAG = 2;
        private ColoredVertex vertex;
        public ColoredVertex getVertex() { return vertex; }
        public void setVertex(ColoredVertex vertex) { this.vertex = vertex; }
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        private float alignment;
        public float getAlignment() { return alignment; }
        public void setAlignment(float alignment) { this.alignment = alignment; }
        private float size;
        public float getSize() { return size; }
        public void setSize(float size) { this.size = size; }
        public PlacedText() {}
        public PlacedText(ColoredVertex vertex, String text, float alignment, float size) {
            this.vertex = vertex;
            this.text = text;
            this.alignment = alignment;
            this.size = size;
        }
        public static PlacedText readFrom(java.io.InputStream stream) throws java.io.IOException {
            PlacedText result = new PlacedText();
            result.vertex = ColoredVertex.readFrom(stream);
            result.text = StreamUtil.readString(stream);
            result.alignment = StreamUtil.readFloat(stream);
            result.size = StreamUtil.readFloat(stream);
            return result;
        }
        @Override
        public void writeTo(java.io.OutputStream stream) throws java.io.IOException {
            StreamUtil.writeInt(stream, TAG);
            vertex.writeTo(stream);
            StreamUtil.writeString(stream, text);
            StreamUtil.writeFloat(stream, alignment);
            StreamUtil.writeFloat(stream, size);
        }
    }
}
